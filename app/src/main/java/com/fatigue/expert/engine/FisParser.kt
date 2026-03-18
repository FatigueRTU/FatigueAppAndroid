package com.fatigue.expert.engine

import java.io.InputStream

/**
 * Parses Matlab .fis (Fuzzy Inference System) files into FuzzySystem objects.
 * Supports Mamdani and Sugeno system types with trimf, trapmf, gaussmf, and constant MFs.
 *
 * Handles two FIS format variants:
 *   Standard:  MF1='zems':'trimf',[0 0 0]
 *   Spaced:    MF1 = 'Low' : 'trapmf', [-1 0 90 110]
 */
object FisParser {

    fun parse(inputStream: InputStream): FuzzySystem {
        val lines = inputStream.bufferedReader().readLines().map { it.trim() }

        var systemName = ""
        var systemType = SystemType.MAMDANI
        var andMethod = AndMethod.MIN
        var orMethod = OrMethod.MAX
        var impMethod = ImpMethod.MIN
        var aggMethod = AggMethod.MAX
        var defuzzMethod = DefuzzMethod.CENTROID

        val inputs = mutableListOf<FuzzyVariable>()
        val outputs = mutableListOf<FuzzyVariable>()
        val rules = mutableListOf<FuzzyRule>()

        var currentSection = ""
        var currentVarName = ""
        var currentVarRange = 0.0 to 1.0
        var currentMFs = mutableListOf<MembershipFunction>()
        var numInputs = 0

        for (line in lines) {
            if (line.isEmpty() || line.startsWith("%") || line.startsWith("#")) continue

            // Section headers: [System], [Input1], [Output1], [Rules]
            if (line.startsWith("[") && line.endsWith("]")) {
                // Save previous variable if any
                if (currentSection.startsWith("[Input") || currentSection.startsWith("[Output")) {
                    val variable = FuzzyVariable(currentVarName, currentVarRange.first, currentVarRange.second, currentMFs.toList())
                    if (currentSection.startsWith("[Input")) inputs.add(variable)
                    else outputs.add(variable)
                    currentMFs = mutableListOf()
                }
                currentSection = line
                continue
            }

            // ── Rules section: lines have NO '=' sign ──
            if (currentSection == "[Rules]") {
                // Rule lines look like: "1 1, 1 (1) : 1" or "1 2 3, 1 2 (0.5) : 1"
                if (line.contains(",") || line.contains("(")) {
                    try {
                        rules.add(parseRule(line))
                    } catch (_: Exception) {
                        // Skip malformed rule lines
                    }
                }
                continue
            }

            // ── Key=Value lines ──
            val eqIdx = line.indexOf('=')
            if (eqIdx < 0) continue
            val key = line.substring(0, eqIdx).trim()
            val value = line.substring(eqIdx + 1).trim()

            when (currentSection) {
                "[System]" -> when (key) {
                    "Name" -> systemName = value.removeSurrounding("'")
                    "Type" -> systemType = if (value.removeSurrounding("'").lowercase() == "sugeno") SystemType.SUGENO else SystemType.MAMDANI
                    "NumInputs" -> numInputs = value.toInt()
                    "AndMethod" -> andMethod = when (value.removeSurrounding("'").lowercase()) {
                        "prod" -> AndMethod.PROD
                        else -> AndMethod.MIN
                    }
                    "OrMethod" -> orMethod = when (value.removeSurrounding("'").lowercase()) {
                        "probor" -> OrMethod.PROBOR
                        else -> OrMethod.MAX
                    }
                    "ImpMethod" -> impMethod = when (value.removeSurrounding("'").lowercase()) {
                        "prod" -> ImpMethod.PROD
                        else -> ImpMethod.MIN
                    }
                    "AggMethod" -> aggMethod = when (value.removeSurrounding("'").lowercase()) {
                        "probor" -> AggMethod.PROBOR
                        "sum" -> AggMethod.SUM
                        else -> AggMethod.MAX
                    }
                    "DefuzzMethod" -> defuzzMethod = when (value.removeSurrounding("'").lowercase()) {
                        "mom" -> DefuzzMethod.MOM
                        "bisector" -> DefuzzMethod.BISECTOR
                        "lom" -> DefuzzMethod.LOM
                        "som" -> DefuzzMethod.SOM
                        "wtaver" -> DefuzzMethod.WTAVER
                        "wtsum" -> DefuzzMethod.WTSUM
                        else -> DefuzzMethod.CENTROID
                    }
                }
                else -> {
                    if (currentSection.startsWith("[Input") || currentSection.startsWith("[Output")) {
                        when (key) {
                            "Name" -> currentVarName = value.removeSurrounding("'")
                            "Range" -> {
                                val nums = value.removePrefix("[").removeSuffix("]").trim().split("\\s+".toRegex())
                                currentVarRange = nums[0].toDouble() to nums[1].toDouble()
                            }
                            else -> if (key.startsWith("MF")) {
                                try {
                                    currentMFs.add(parseMF(value))
                                } catch (_: Exception) {
                                    // Skip malformed MF lines
                                }
                            }
                        }
                    }
                }
            }
        }

        // Save last variable
        if (currentSection.startsWith("[Input") || currentSection.startsWith("[Output")) {
            val variable = FuzzyVariable(currentVarName, currentVarRange.first, currentVarRange.second, currentMFs.toList())
            if (currentSection.startsWith("[Input")) inputs.add(variable)
            else outputs.add(variable)
        }

        return FuzzySystem(
            name = systemName,
            type = systemType,
            andMethod = andMethod,
            orMethod = orMethod,
            impMethod = impMethod,
            aggMethod = aggMethod,
            defuzzMethod = defuzzMethod,
            inputs = inputs,
            outputs = outputs,
            rules = rules
        )
    }

    /**
     * Parse a membership function definition.
     * Handles both formats:
     *   'zems':'trimf',[0 0 0]
     *   'Low' : 'trapmf', [-1 0 90 110]
     *   'constant', [7.5]     (Sugeno)
     */
    private fun parseMF(value: String): MembershipFunction {
        // Strategy: find the MF name (first quoted string), type (second quoted string),
        // and parameters (content inside square brackets)
        val quotePattern = Regex("'([^']*)'")
        val quotes = quotePattern.findAll(value).map { it.groupValues[1] }.toList()

        val name: String
        val typeName: String

        if (quotes.size >= 2) {
            name = quotes[0]
            typeName = quotes[1]
        } else if (quotes.size == 1) {
            // Sugeno constant: might just have the type
            name = quotes[0]
            typeName = quotes[0]
        } else {
            throw IllegalArgumentException("Cannot parse MF: $value")
        }

        // Extract parameters from square brackets
        val bracketMatch = Regex("\\[([^\\]]*)]").find(value)
        val params = if (bracketMatch != null) {
            bracketMatch.groupValues[1].trim().split("\\s+".toRegex())
                .filter { it.isNotEmpty() }
                .map { it.toDouble() }
                .toDoubleArray()
        } else {
            doubleArrayOf()
        }

        return when (typeName.lowercase()) {
            "trimf" -> TriMF(name, params)
            "trapmf" -> TrapMF(name, params)
            "gaussmf" -> GaussMF(name, params)
            "constant" -> ConstantMF(name, params)
            else -> TriMF(name, params) // fallback
        }
    }

    /**
     * Parse a fuzzy rule line.
     * Format: "1 2, 3 (1) : 1" or "1 2 3, 1 2 (0.5) : 1"
     *         antecedents, consequents (weight) : connector
     * Where connector: 1=AND, 2=OR
     * Antecedent/consequent index 0 means "don't care"
     */
    private fun parseRule(line: String): FuzzyRule {
        val cleanLine = line.trim()

        // Split by comma to separate antecedents from consequents+metadata
        val commaSplit = cleanLine.split(",")
        val antecedentStr = commaSplit[0].trim()
        val restStr = commaSplit.getOrElse(1) { "" }.trim()

        // Parse antecedent indices
        val antecedents = antecedentStr.split("\\s+".toRegex())
            .filter { it.isNotEmpty() }
            .map { it.toInt() }

        // Parse weight from parentheses: (1) or (0.5)
        val weightMatch = Regex("\\(([^)]+)\\)").find(restStr)
        val weight = weightMatch?.groupValues?.get(1)?.toDoubleOrNull() ?: 1.0

        // Parse connector after colon: 1=AND, 2=OR
        val connectorMatch = Regex(":\\s*(\\d)").find(restStr)
        val connector = connectorMatch?.groupValues?.get(1)?.toIntOrNull() ?: 1

        // Everything before the weight parenthesis is consequent indices
        val beforeWeight = restStr.substringBefore("(").trim()
        val consequents = if (beforeWeight.isEmpty()) listOf(0)
        else beforeWeight.split("\\s+".toRegex())
            .filter { it.isNotEmpty() }
            .map { it.toInt() }

        return FuzzyRule(
            antecedents = antecedents,
            consequents = consequents,
            weight = weight,
            useAnd = connector == 1
        )
    }
}
