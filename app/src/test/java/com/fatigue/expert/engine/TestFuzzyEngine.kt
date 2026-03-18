package com.fatigue.expert.engine

import java.io.InputStream

/**
 * Test-only fuzzy engine that loads .fis files from classpath resources
 * instead of Android assets. Mirrors the production FuzzyEngine API.
 */
class TestFuzzyEngine {

    private val cache = mutableMapOf<String, FuzzySystem>()

    fun loadSystem(fisFileName: String): FuzzySystem {
        return cache.getOrPut(fisFileName) {
            val stream: InputStream = javaClass.classLoader!!
                .getResourceAsStream("fuzzy_models/$fisFileName")
                ?: throw IllegalArgumentException("FIS file not found: fuzzy_models/$fisFileName")
            stream.use { FisParser.parse(it) }
        }
    }

    fun evaluate(system: FuzzySystem, inputValues: Map<String, Double>): Map<String, Double> {
        val inputs = system.inputs.map { variable ->
            inputValues[variable.name] ?: 0.0
        }
        return when (system.type) {
            SystemType.MAMDANI -> evaluateMamdani(system, inputs)
            SystemType.SUGENO -> evaluateSugeno(system, inputs)
        }
    }

    // ── Mamdani ──

    private fun evaluateMamdani(system: FuzzySystem, inputs: List<Double>): Map<String, Double> {
        val resolution = 201
        val results = mutableMapOf<String, Double>()

        for (outIdx in system.outputs.indices) {
            val output = system.outputs[outIdx]
            val step = (output.max - output.min) / (resolution - 1)
            val aggregated = DoubleArray(resolution) { 0.0 }

            for (rule in system.rules) {
                val consequentMfIdx = rule.consequents.getOrElse(outIdx) { 0 }
                if (consequentMfIdx == 0) continue

                val firingStrength = computeFiringStrength(system, rule, inputs)
                if (firingStrength <= 0.0) continue
                val weightedStrength = firingStrength * rule.weight
                val mf = output.membershipFunctions.getOrNull(consequentMfIdx - 1) ?: continue

                for (i in 0 until resolution) {
                    val x = output.min + i * step
                    val mfValue = mf.evaluate(x)
                    val implicated = when (system.impMethod) {
                        ImpMethod.MIN -> kotlin.math.min(weightedStrength, mfValue)
                        ImpMethod.PROD -> weightedStrength * mfValue
                    }
                    aggregated[i] = when (system.aggMethod) {
                        AggMethod.MAX -> kotlin.math.max(aggregated[i], implicated)
                        AggMethod.PROBOR -> aggregated[i] + implicated - aggregated[i] * implicated
                        AggMethod.SUM -> aggregated[i] + implicated
                    }
                }
            }

            results[output.name] = defuzzify(system.defuzzMethod, aggregated, output.min, step)
        }
        return results
    }

    // ── Sugeno ──

    private fun evaluateSugeno(system: FuzzySystem, inputs: List<Double>): Map<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (outIdx in system.outputs.indices) {
            val output = system.outputs[outIdx]
            var weightedSum = 0.0
            var totalStrength = 0.0

            for (rule in system.rules) {
                val consequentMfIdx = rule.consequents.getOrElse(outIdx) { 0 }
                if (consequentMfIdx == 0) continue
                val firingStrength = computeFiringStrength(system, rule, inputs)
                if (firingStrength <= 0.0) continue
                val weightedStrength = firingStrength * rule.weight
                val mf = output.membershipFunctions.getOrNull(consequentMfIdx - 1) ?: continue
                val outputValue = when (mf) {
                    is ConstantMF -> mf.value
                    else -> mf.evaluate(inputs.firstOrNull() ?: 0.0)
                }
                weightedSum += weightedStrength * outputValue
                totalStrength += weightedStrength
            }
            results[output.name] = if (totalStrength > 0) weightedSum / totalStrength else 0.0
        }
        return results
    }

    // ── Firing Strength ──

    private fun computeFiringStrength(system: FuzzySystem, rule: FuzzyRule, inputs: List<Double>): Double {
        var strength = if (rule.useAnd) 1.0 else 0.0

        for (i in rule.antecedents.indices) {
            val mfIdx = rule.antecedents[i]
            if (mfIdx == 0) continue
            val inputVar = system.inputs.getOrNull(i) ?: continue
            val inputVal = inputs.getOrElse(i) { 0.0 }
            val mfIndex = kotlin.math.abs(mfIdx) - 1
            val mf = inputVar.membershipFunctions.getOrNull(mfIndex) ?: continue
            var membership = mf.evaluate(inputVal)
            if (mfIdx < 0) membership = 1.0 - membership

            strength = if (rule.useAnd) {
                when (system.andMethod) {
                    AndMethod.MIN -> kotlin.math.min(strength, membership)
                    AndMethod.PROD -> strength * membership
                }
            } else {
                when (system.orMethod) {
                    OrMethod.MAX -> kotlin.math.max(strength, membership)
                    OrMethod.PROBOR -> strength + membership - strength * membership
                }
            }
        }
        return strength
    }

    // ── Defuzzification ──

    private fun defuzzify(method: DefuzzMethod, agg: DoubleArray, minVal: Double, step: Double): Double {
        return when (method) {
            DefuzzMethod.CENTROID -> {
                var num = 0.0; var den = 0.0
                for (i in agg.indices) { val x = minVal + i * step; num += x * agg[i]; den += agg[i] }
                if (den > 0) num / den else (minVal + (agg.size - 1) * step) / 2.0
            }
            DefuzzMethod.MOM -> {
                val maxVal = agg.maxOrNull() ?: 0.0
                if (maxVal <= 0.0) return minVal
                var sum = 0.0; var count = 0
                for (i in agg.indices) { if (agg[i] >= maxVal - 1e-10) { sum += minVal + i * step; count++ } }
                if (count > 0) sum / count else minVal
            }
            DefuzzMethod.BISECTOR -> {
                val total = agg.sum(); if (total <= 0) return minVal
                var running = 0.0
                for (i in agg.indices) { running += agg[i]; if (running >= total / 2.0) return minVal + i * step }
                minVal + (agg.size - 1) * step
            }
            DefuzzMethod.SOM -> {
                val maxVal = agg.maxOrNull() ?: 0.0; if (maxVal <= 0.0) return minVal
                for (i in agg.indices) { if (agg[i] >= maxVal - 1e-10) return minVal + i * step }
                minVal
            }
            DefuzzMethod.LOM -> {
                val maxVal = agg.maxOrNull() ?: 0.0; if (maxVal <= 0.0) return minVal
                for (i in agg.indices.reversed()) { if (agg[i] >= maxVal - 1e-10) return minVal + i * step }
                minVal
            }
            else -> {
                var num = 0.0; var den = 0.0
                for (i in agg.indices) { val x = minVal + i * step; num += x * agg[i]; den += agg[i] }
                if (den > 0) num / den else (minVal + (agg.size - 1) * step) / 2.0
            }
        }
    }
}
