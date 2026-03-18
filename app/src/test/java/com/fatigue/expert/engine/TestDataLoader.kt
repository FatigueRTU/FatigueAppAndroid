package com.fatigue.expert.engine

/**
 * Loads test vectors from the original Node.js test data files.
 *
 * File format: space-separated integers, 1-based indexing.
 * - Columns 0..N-1 are input values (1=Low, 2=Med, 3=High)
 * - Last column is the expected output (also 1-based)
 *
 * The JS tests subtract 1 from every value before sending to the fuzzy engine,
 * converting to 0-based (0=Low, 1=Med, 2=High). We do the same here.
 */
data class TestVector(
    val inputs: List<Double>,   // 0-based values sent to the fuzzy engine
    val expected: Int            // 0-based expected rounded output
)

object TestDataLoader {

    fun load(resourcePath: String, numInputs: Int): List<TestVector> {
        val stream = javaClass.classLoader!!.getResourceAsStream(resourcePath)
            ?: throw IllegalArgumentException("Test data not found: $resourcePath")

        return stream.bufferedReader().readLines()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .map { line ->
                val parts = line.split("\\s+".toRegex()).map { it.toInt() }
                require(parts.size >= numInputs + 1) {
                    "Expected ${numInputs + 1} columns, got ${parts.size} in: $line"
                }
                TestVector(
                    inputs = parts.take(numInputs).map { (it - 1).toDouble() },
                    expected = parts[numInputs] - 1
                )
            }
    }
}
