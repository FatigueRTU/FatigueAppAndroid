package com.fatigue.expert.engine

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for the FIS parser itself — verifies that all 11 .fis files
 * from the expert system parse correctly and have the expected structure.
 */
class FisParserTest {

    private val engine = TestFuzzyEngine()

    // ── Parse validation for all 11 FIS files ──

    @Test
    fun `nogurums_fis parses correctly`() {
        val sys = engine.loadSystem("nogurums.fis")
        assertEquals(SystemType.MAMDANI, sys.type)
        assertEquals(2, sys.inputs.size)
        assertEquals(1, sys.outputs.size)
        assertEquals(9, sys.rules.size)
        assertEquals(AndMethod.MIN, sys.andMethod)
        assertEquals(DefuzzMethod.CENTROID, sys.defuzzMethod)
        // Each input has 3 MFs (low/med/high)
        sys.inputs.forEach { assertEquals(3, it.membershipFunctions.size) }
        assertEquals(3, sys.outputs[0].membershipFunctions.size)
    }

    @Test
    fun `subjektiva_dala_fis parses correctly`() {
        val sys = engine.loadSystem("subjektiva_dala.fis")
        assertEquals(SystemType.MAMDANI, sys.type)
        assertEquals(13, sys.inputs.size)
        assertEquals(1, sys.outputs.size)
        assertEquals(200, sys.rules.size)
        assertEquals(AndMethod.PROD, sys.andMethod)
        assertEquals(AggMethod.PROBOR, sys.aggMethod)
        assertEquals(DefuzzMethod.MOM, sys.defuzzMethod)
    }

    @Test
    fun `objective_full_fis parses correctly`() {
        val sys = engine.loadSystem("objective_full.fis")
        assertEquals(SystemType.MAMDANI, sys.type)
        assertEquals(6, sys.inputs.size)
        assertEquals(1, sys.outputs.size)
        assertEquals(729, sys.rules.size)
        assertEquals(AndMethod.PROD, sys.andMethod)
        assertEquals(DefuzzMethod.CENTROID, sys.defuzzMethod)
    }

    @Test
    fun `trauksme_fis parses correctly`() {
        val sys = engine.loadSystem("trauksme.fis")
        assertEquals(SystemType.MAMDANI, sys.type)
        assertEquals(3, sys.inputs.size)
        assertEquals(1, sys.outputs.size)
        assertEquals(18, sys.rules.size)
        assertEquals(AndMethod.MIN, sys.andMethod)
        // Output has 4 MFs (none/buzzer/bell/siren)
        assertEquals(4, sys.outputs[0].membershipFunctions.size)
    }

    @Test
    fun `rekomendacijas_fis parses correctly`() {
        val sys = engine.loadSystem("rekomendacijas.fis")
        assertEquals(SystemType.MAMDANI, sys.type)
        assertEquals(2, sys.inputs.size)
        assertEquals(1, sys.outputs.size)
        assertEquals(12, sys.rules.size)
        // Input 2 (trauksmes_veids) has 4 MFs, output has 4 MFs
        assertEquals(3, sys.inputs[0].membershipFunctions.size)
        assertEquals(4, sys.inputs[1].membershipFunctions.size)
        assertEquals(4, sys.outputs[0].membershipFunctions.size)
    }

    @Test
    fun `drowsiness_fis parses correctly`() {
        val sys = engine.loadSystem("drowsiness.fis")
        assertEquals(SystemType.MAMDANI, sys.type)
        assertEquals(9, sys.inputs.size)
        assertEquals(2, sys.outputs.size)
        assertEquals(20, sys.rules.size)
        // Output names from the FIS file
        assertEquals("miegainibas_limenis", sys.outputs[0].name)
        assertEquals("lemuma_ticamiba", sys.outputs[1].name)
    }

    @Test
    fun `nogurums_fis output is objektiva_miegainiba`() {
        // The JS API renames this to "nogurums" but the FIS output is "objektiva_miegainiba"
        val sys = engine.loadSystem("nogurums.fis")
        assertEquals("objektiva_miegainiba", sys.outputs[0].name)
    }

    @Test
    fun `subjektiva_dala_fis output is subjektiva_miegainiba`() {
        // The JS API renames this to "subjektiva_komponente"
        val sys = engine.loadSystem("subjektiva_dala.fis")
        assertEquals("subjektiva_miegainiba", sys.outputs[0].name)
    }

    @Test
    fun `objective_full_fis output is objektiva_miegainiba`() {
        // The JS API renames this to "objektiva_komponente"
        val sys = engine.loadSystem("objective_full.fis")
        assertEquals("objektiva_miegainiba", sys.outputs[0].name)
    }

    @Test
    fun `objective_part_fis parses correctly`() {
        val sys = engine.loadSystem("objective_part.fis")
        assertEquals(SystemType.MAMDANI, sys.type)
        assertEquals(3, sys.inputs.size)
        assertEquals(1, sys.outputs.size)
        assertEquals(8, sys.rules.size)
        assertEquals(AggMethod.PROBOR, sys.aggMethod)
        assertEquals(DefuzzMethod.MOM, sys.defuzzMethod)
    }

    @Test
    fun `objective_part_1st_level_fis parses correctly`() {
        val sys = engine.loadSystem("objective_part_1st_level.fis")
        assertEquals(SystemType.MAMDANI, sys.type)
        assertEquals(3, sys.inputs.size)
        assertEquals(1, sys.outputs.size)
        assertEquals(9, sys.rules.size)
        // Real-world ranges
        assertEquals(0.0, sys.inputs[0].min, 0.001)  // blink freq
        assertEquals(100.0, sys.inputs[0].max, 0.001)
    }

    @Test
    fun `objective_part_2nd_level_fis parses correctly`() {
        val sys = engine.loadSystem("objective_part_2nd_level.fis")
        assertEquals(SystemType.MAMDANI, sys.type)
        assertEquals(2, sys.inputs.size)
        assertEquals(1, sys.outputs.size)
        assertEquals(7, sys.rules.size)
    }

    @Test
    fun `objective_part_3rd_level_fis parses correctly`() {
        val sys = engine.loadSystem("objective_part_3rd_level.fis")
        assertEquals(SystemType.MAMDANI, sys.type)
        assertEquals(2, sys.inputs.size)
        assertEquals(1, sys.outputs.size)
        assertEquals(7, sys.rules.size)
    }

    // ── Membership function parsing tests ──

    @Test
    fun `trimf degenerate singleton evaluates correctly`() {
        // trimf [0,0,0] — used extensively in the normalized models
        val mf = TriMF("test", doubleArrayOf(0.0, 0.0, 0.0))
        assertEquals(1.0, mf.evaluate(0.0), 0.001)
        assertEquals(0.0, mf.evaluate(0.5), 0.001)
        assertEquals(0.0, mf.evaluate(1.0), 0.001)
    }

    @Test
    fun `trimf ramp up evaluates correctly`() {
        // trimf [0,1,1] — ramp from 0 to 1
        val mf = TriMF("test", doubleArrayOf(0.0, 1.0, 1.0))
        assertEquals(0.0, mf.evaluate(0.0), 0.001)
        assertEquals(0.5, mf.evaluate(0.5), 0.001)
        assertEquals(1.0, mf.evaluate(1.0), 0.001)
        assertEquals(0.0, mf.evaluate(1.5), 0.001)
    }

    @Test
    fun `trimf ramp down evaluates correctly`() {
        // trimf [1,2,2] — ramp from 1 to 2
        val mf = TriMF("test", doubleArrayOf(1.0, 2.0, 2.0))
        assertEquals(0.0, mf.evaluate(0.5), 0.001)
        assertEquals(0.0, mf.evaluate(1.0), 0.001)
        assertEquals(0.5, mf.evaluate(1.5), 0.001)
        assertEquals(1.0, mf.evaluate(2.0), 0.001)
    }

    @Test
    fun `trapmf evaluates correctly`() {
        // trapmf [0, 100, 330, 330] from drowsiness.fis
        val mf = TrapMF("test", doubleArrayOf(0.0, 100.0, 330.0, 330.0))
        assertEquals(0.0, mf.evaluate(0.0), 0.001)
        assertEquals(0.5, mf.evaluate(50.0), 0.001)
        assertEquals(1.0, mf.evaluate(200.0), 0.001)
        assertEquals(1.0, mf.evaluate(330.0), 0.001)
    }

    @Test
    fun `gaussmf evaluates correctly`() {
        // gaussmf [13, 60] from drowsiness.fis (pulse zone_1)
        val mf = GaussMF("test", doubleArrayOf(13.0, 60.0))
        assertEquals(1.0, mf.evaluate(60.0), 0.001)
        // At 1 sigma away, should be ~0.606
        assertEquals(0.606, mf.evaluate(73.0), 0.01)
        // Far away should be near 0
        assertTrue(mf.evaluate(200.0) < 0.01)
    }

    @Test
    fun `constant MF returns fixed value`() {
        val mf = ConstantMF("test", doubleArrayOf(7.5))
        assertEquals(7.5, mf.value, 0.001)
        assertEquals(7.5, mf.evaluate(0.0), 0.001)
        assertEquals(7.5, mf.evaluate(999.0), 0.001)
    }

    // ── Rule parsing tests ──

    @Test
    fun `rules parse with correct antecedent count`() {
        val sys = engine.loadSystem("nogurums.fis")
        // 2-input system: each rule should have 2 antecedents
        sys.rules.forEach { rule ->
            assertEquals(2, rule.antecedents.size)
        }
    }

    @Test
    fun `rules with wildcards parse correctly`() {
        val sys = engine.loadSystem("drowsiness.fis")
        // drowsiness.fis uses 0 as don't-care in many rules
        val rulesWithWildcards = sys.rules.filter { rule ->
            rule.antecedents.any { it == 0 }
        }
        assertTrue("Expected rules with wildcards", rulesWithWildcards.isNotEmpty())
    }

    @Test
    fun `multi-output rules parse correctly`() {
        val sys = engine.loadSystem("drowsiness.fis")
        // drowsiness.fis has 2 outputs
        sys.rules.forEach { rule ->
            assertEquals(2, rule.consequents.size)
        }
    }
}
