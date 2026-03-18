package com.fatigue.expert.engine

import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min

// ── System Configuration Enums ──

enum class SystemType { MAMDANI, SUGENO }
enum class AndMethod { MIN, PROD }
enum class OrMethod { MAX, PROBOR }
enum class ImpMethod { MIN, PROD }
enum class AggMethod { MAX, PROBOR, SUM }
enum class DefuzzMethod { CENTROID, MOM, BISECTOR, LOM, SOM, WTAVER, WTSUM }

// ── Membership Functions ──

sealed class MembershipFunction(val name: String) {
    abstract fun evaluate(x: Double): Double
}

/** Triangular: trimf [a, b, c] */
class TriMF(name: String, private val params: DoubleArray) : MembershipFunction(name) {
    override fun evaluate(x: Double): Double {
        val a = params[0]; val b = params[1]; val c = params[2]
        // Handle degenerate cases like [0,0,0] or [1,2,2]
        if (a == b && b == c) return if (x == a) 1.0 else 0.0
        if (a == b) return if (x <= b) { if (x == b) 1.0 else 0.0 } else max(0.0, (c - x) / (c - b))
        if (b == c) return if (x >= b) { if (x == b) 1.0 else 0.0 } else max(0.0, (x - a) / (b - a))
        return when {
            x <= a -> 0.0
            x <= b -> (x - a) / (b - a)
            x <= c -> (c - x) / (c - b)
            else -> 0.0
        }
    }
}

/** Trapezoidal: trapmf [a, b, c, d] */
class TrapMF(name: String, private val params: DoubleArray) : MembershipFunction(name) {
    override fun evaluate(x: Double): Double {
        val a = params[0]; val b = params[1]; val c = params[2]; val d = params[3]
        return when {
            x <= a -> 0.0
            x <= b -> if (b == a) 1.0 else (x - a) / (b - a)
            x <= c -> 1.0
            x <= d -> if (d == c) 1.0 else (d - x) / (d - c)
            else -> 0.0
        }
    }
}

/** Gaussian: gaussmf [sigma, center] */
class GaussMF(name: String, private val params: DoubleArray) : MembershipFunction(name) {
    override fun evaluate(x: Double): Double {
        val sigma = params[0]; val c = params[1]
        if (sigma == 0.0) return if (x == c) 1.0 else 0.0
        return exp(-0.5 * ((x - c) / sigma) * ((x - c) / sigma))
    }
}

/** Constant (Sugeno): constant [value] */
class ConstantMF(name: String, private val params: DoubleArray) : MembershipFunction(name) {
    val value: Double get() = if (params.isNotEmpty()) params[0] else 0.0
    override fun evaluate(x: Double): Double = value
}

// ── Fuzzy Variable ──

data class FuzzyVariable(
    val name: String,
    val min: Double,
    val max: Double,
    val membershipFunctions: List<MembershipFunction>
)

// ── Fuzzy Rule ──

data class FuzzyRule(
    val antecedents: List<Int>,    // MF index per input (1-based, 0 = don't care)
    val consequents: List<Int>,    // MF index per output (1-based)
    val weight: Double = 1.0,
    val useAnd: Boolean = true     // true=AND, false=OR
)

// ── Fuzzy System ──

data class FuzzySystem(
    val name: String,
    val type: SystemType,
    val andMethod: AndMethod,
    val orMethod: OrMethod,
    val impMethod: ImpMethod,
    val aggMethod: AggMethod,
    val defuzzMethod: DefuzzMethod,
    val inputs: List<FuzzyVariable>,
    val outputs: List<FuzzyVariable>,
    val rules: List<FuzzyRule>
)
