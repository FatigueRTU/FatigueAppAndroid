package com.fatigue.expert.engine

import android.content.Context
import kotlin.math.max
import kotlin.math.min

/**
 * Fuzzy inference engine that evaluates FIS models.
 * Supports Mamdani (centroid, MOM defuzz) and Sugeno (wtaver, wtsum) inference.
 */
class FuzzyEngine(private val context: Context) {

    private val cache = mutableMapOf<String, FuzzySystem>()

    /** Load a .fis file from assets/fuzzy_models/ */
    fun loadSystem(fisFileName: String): FuzzySystem {
        return cache.getOrPut(fisFileName) {
            context.assets.open("fuzzy_models/$fisFileName").use { stream ->
                FisParser.parse(stream)
            }
        }
    }

    /** Evaluate a fuzzy system with given input values. Returns map of output name → value */
    fun evaluate(system: FuzzySystem, inputValues: Map<String, Double>): Map<String, Double> {
        // Map input values to ordered list matching system.inputs
        val inputs = system.inputs.map { variable ->
            inputValues[variable.name] ?: 0.0
        }

        return when (system.type) {
            SystemType.MAMDANI -> evaluateMamdani(system, inputs)
            SystemType.SUGENO -> evaluateSugeno(system, inputs)
        }
    }

    /** Evaluate with positional inputs (ordered list) */
    fun evaluate(system: FuzzySystem, inputValues: List<Double>): Map<String, Double> {
        return when (system.type) {
            SystemType.MAMDANI -> evaluateMamdani(system, inputValues)
            SystemType.SUGENO -> evaluateSugeno(system, inputValues)
        }
    }

    // ── Mamdani Inference ──

    private fun evaluateMamdani(system: FuzzySystem, inputs: List<Double>): Map<String, Double> {
        val resolution = 201 // Number of points for defuzzification
        val results = mutableMapOf<String, Double>()

        for (outIdx in system.outputs.indices) {
            val output = system.outputs[outIdx]
            val step = (output.max - output.min) / (resolution - 1)

            // Build aggregated output membership for this output variable
            val aggregated = DoubleArray(resolution) { 0.0 }

            for (rule in system.rules) {
                val consequentMfIdx = rule.consequents.getOrElse(outIdx) { 0 }
                if (consequentMfIdx == 0) continue // don't care for this output

                // Compute rule firing strength
                val firingStrength = computeFiringStrength(system, rule, inputs)
                if (firingStrength <= 0.0) continue

                val weightedStrength = firingStrength * rule.weight

                // Get consequent MF
                val mf = output.membershipFunctions.getOrNull(consequentMfIdx - 1) ?: continue

                // Implication + Aggregation
                for (i in 0 until resolution) {
                    val x = output.min + i * step
                    val mfValue = mf.evaluate(x)

                    // Implication (clip or scale)
                    val implicated = when (system.impMethod) {
                        ImpMethod.MIN -> min(weightedStrength, mfValue)
                        ImpMethod.PROD -> weightedStrength * mfValue
                    }

                    // Aggregation
                    aggregated[i] = when (system.aggMethod) {
                        AggMethod.MAX -> max(aggregated[i], implicated)
                        AggMethod.PROBOR -> aggregated[i] + implicated - aggregated[i] * implicated
                        AggMethod.SUM -> aggregated[i] + implicated
                    }
                }
            }

            // Defuzzification
            results[output.name] = defuzzify(system.defuzzMethod, aggregated, output.min, step)
        }

        return results
    }

    // ── Sugeno Inference ──

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

                when (system.defuzzMethod) {
                    DefuzzMethod.WTAVER -> {
                        weightedSum += weightedStrength * outputValue
                        totalStrength += weightedStrength
                    }
                    DefuzzMethod.WTSUM -> {
                        weightedSum += weightedStrength * outputValue
                        totalStrength = 1.0 // not normalized
                    }
                    else -> {
                        weightedSum += weightedStrength * outputValue
                        totalStrength += weightedStrength
                    }
                }
            }

            results[output.name] = if (totalStrength > 0) weightedSum / totalStrength else 0.0
        }

        return results
    }

    // ── Rule Firing Strength ──

    private fun computeFiringStrength(system: FuzzySystem, rule: FuzzyRule, inputs: List<Double>): Double {
        var strength = if (rule.useAnd) 1.0 else 0.0

        for (i in rule.antecedents.indices) {
            val mfIdx = rule.antecedents[i]
            if (mfIdx == 0) continue // don't care

            val inputVar = system.inputs.getOrNull(i) ?: continue
            val inputVal = inputs.getOrElse(i) { 0.0 }

            val mfIndex = kotlin.math.abs(mfIdx) - 1
            val mf = inputVar.membershipFunctions.getOrNull(mfIndex) ?: continue
            var membership = mf.evaluate(inputVal)

            // Negative index means NOT
            if (mfIdx < 0) membership = 1.0 - membership

            strength = if (rule.useAnd) {
                when (system.andMethod) {
                    AndMethod.MIN -> min(strength, membership)
                    AndMethod.PROD -> strength * membership
                }
            } else {
                when (system.orMethod) {
                    OrMethod.MAX -> max(strength, membership)
                    OrMethod.PROBOR -> strength + membership - strength * membership
                }
            }
        }

        return strength
    }

    // ── Defuzzification ──

    private fun defuzzify(method: DefuzzMethod, aggregated: DoubleArray, minVal: Double, step: Double): Double {
        return when (method) {
            DefuzzMethod.CENTROID -> defuzzCentroid(aggregated, minVal, step)
            DefuzzMethod.MOM -> defuzzMOM(aggregated, minVal, step)
            DefuzzMethod.BISECTOR -> defuzzBisector(aggregated, minVal, step)
            DefuzzMethod.LOM -> defuzzLOM(aggregated, minVal, step)
            DefuzzMethod.SOM -> defuzzSOM(aggregated, minVal, step)
            else -> defuzzCentroid(aggregated, minVal, step)
        }
    }

    private fun defuzzCentroid(agg: DoubleArray, minVal: Double, step: Double): Double {
        var num = 0.0
        var den = 0.0
        for (i in agg.indices) {
            val x = minVal + i * step
            num += x * agg[i]
            den += agg[i]
        }
        return if (den > 0) num / den else (minVal + (agg.size - 1) * step) / 2.0
    }

    private fun defuzzMOM(agg: DoubleArray, minVal: Double, step: Double): Double {
        val maxVal = agg.maxOrNull() ?: 0.0
        if (maxVal <= 0.0) return minVal
        var sum = 0.0
        var count = 0
        for (i in agg.indices) {
            if (agg[i] >= maxVal - 1e-10) {
                sum += minVal + i * step
                count++
            }
        }
        return if (count > 0) sum / count else minVal
    }

    private fun defuzzBisector(agg: DoubleArray, minVal: Double, step: Double): Double {
        val total = agg.sum()
        if (total <= 0) return minVal
        var running = 0.0
        for (i in agg.indices) {
            running += agg[i]
            if (running >= total / 2.0) return minVal + i * step
        }
        return minVal + (agg.size - 1) * step
    }

    private fun defuzzSOM(agg: DoubleArray, minVal: Double, step: Double): Double {
        val maxVal = agg.maxOrNull() ?: 0.0
        if (maxVal <= 0.0) return minVal
        for (i in agg.indices) {
            if (agg[i] >= maxVal - 1e-10) return minVal + i * step
        }
        return minVal
    }

    private fun defuzzLOM(agg: DoubleArray, minVal: Double, step: Double): Double {
        val maxVal = agg.maxOrNull() ?: 0.0
        if (maxVal <= 0.0) return minVal
        for (i in agg.indices.reversed()) {
            if (agg[i] >= maxVal - 1e-10) return minVal + i * step
        }
        return minVal
    }
}
