# Thesis Document Review — Suggested Changes

**Document:** Promocijas darba kopsavilkums, Eriņš, 2026
**Title:** PIELĀGOJAMS EKSPERTU SISTĒMU KOMPLEKSS CILVĒKA NOGURUMA NOVĒRTĒŠANAI
**Reviewed against:** Source code in `expert_system/`, `.fis` models, Node-RED flow, test data

---

## A. Discrepancies Between Document and Code

### 1. FIS Output Variable Naming Mismatch (§4, Appendix 5)

The thesis describes module outputs as "nogurums", "subjektīvā komponente", "objektīvā komponente". However, the actual `.fis` files use different output variable names. The Node.js API layer silently renames them in the JSON response:

| FIS File | FIS Output Variable | JS API Response Key | Thesis Term |
|---|---|---|---|
| `nogurums.fis` | `objektiva_miegainiba` | `nogurums` | Noguruma pakāpe |
| `subjektiva_dala.fis` | `subjektiva_miegainiba` | `subjektiva_komponente` | Subjektīvā komponente |
| `objective_full.fis` | `objektiva_miegainiba` | `objektiva_komponente` | Objektīvā komponente |
| `trauksme.fis` | `trauksmes_veids` | `trauksmes_veids` | ✅ Matches |
| `rekomendacijas.fis` | `rekomendacija` | `rekomendacija` | ✅ Matches |

**Suggestion:** Add a mapping table in §4 (system realization) documenting the FIS-internal names vs. API-level names, or rename the FIS output variables to match the thesis terminology.

---

### 2. Rule Count Clarification (§2, Table 2.1)

The thesis states "76 produkciju likumi" for the knowledge base. The actual `.fis` files contain significantly more rules due to fuzzy expansion of the expert-defined logic:

| Module | FIS File | Rules in .fis |
|---|---|---|
| LP1 Subjektīvā | `subjektiva_dala.fis` | 200 |
| LP1 Objektīvā | `objective_full.fis` | 729 |
| LP2 Nogurums | `nogurums.fis` | 9 |
| LP2 Trauksme | `trauksme.fis` | 18 |
| LP3 Rekomendācija | `rekomendacijas.fis` | 12 |
| LP1 Objektīvā (daļēja) | `objective_part.fis` | 8 |
| LP1 1. līmenis | `objective_part_1st_level.fis` | 9 |
| LP1 2. līmenis | `objective_part_2nd_level.fis` | 7 |
| LP1 3. līmenis | `objective_part_3rd_level.fis` | 7 |
| **Kopā** | | **999** |

**Suggestion:** Clarify that the 76 refers to the expert-defined production rules before fuzzy combinatorial expansion. Add a note that the realized FIS models contain 999 total fuzzy rules after expansion.

---

### 3. Test Vector Count Reconciliation (§5, Table 5.3)

The thesis reports 1086 automated tests across 3 scenarios (279 + 479 + 328). The test data files in the repository contain:

| Test Data File | Vectors | Module |
|---|---|---|
| `subjective_test_data.txt` | 200 | LP1 Subjektīvā |
| `objective_test_data.txt` | 729 | LP1 Objektīvā |
| `nogurums_test_data.txt` | 9 | LP2 Nogurums |
| `trauksme_test_data.txt` | 18 | LP2 Trauksme |
| `rekomendacijas_test_data.txt` | 12 | LP3 Rekomendācija |
| **Kopā** | **968** | Individual modules |

The 1086 scenario tests likely include chained multi-module combinations not present as individual module test files.

**Suggestion:** Add a note explaining the relationship: 968 individual module tests + additional chained scenario combinations = 1086 total integration tests.

---

### 4. Recommendation Labels Mismatch (§2.2, Appendix 4)

The thesis defines 4 recommendation classes (page 25-26):
1. Var turpināt darbu
2. Neliels ierobežojums jeb pastaigu pauze
3. Būtisks ierobežojums jeb atpūta, snauda vai pusdienu pārtraukums
4. Jābeidz maiņa, jāizsauc dublieris

The Node-RED implementation (`Rekomendācija vārdiem` function node) uses simplified labels:
1. "Rekomendācija turpināt darbu"
2. "Rekomendācija paņemt pauzi"
3. "Pusdienu pārtraukums"
4. "Rekomendācija pārtraukt aktivitāti"

**Suggestion:** Align the Node-RED function node strings with the thesis-defined recommendation classes, or note the simplification as an implementation choice.

---

### 5. `nogurums.fis` Input Order vs. Test Data Column Order

The FIS file defines: `Input1=objektiva_komponente`, `Input2=subjektiva_komponente`.
The test data file `nogurums_test_data.txt` columns follow this same order (col0=objektiva, col1=subjektiva).
However, `test_fatigue.js` maps them as: `"subjektiva_komponente": arr[0]-1, "objektiva_komponente": arr[1]-1`.

This works because the JS sends named parameters (not positional), but the column naming in the test data is potentially confusing.

**Suggestion:** Add column headers or comments to the test data files documenting the column order.

---

## B. Potential Technical Issues

### 6. `drowsiness.fis` Membership Function Parameters (Appendix 5)

Two output membership functions appear to have malformed parameters:

- **Output1 MF3** `augsta`: `trapmf [7 8 1 1]` — on a range of 0–10, the last two values (1, 1) should likely be `[7 8 10 10]`
- **Output2 MF3** `ticams`: `trimf [6 1 1]` — on a range of 0–1, this creates a degenerate shape; likely should be `[0.6 1 1]`

**Suggestion:** Verify these parameters against the original Matlab Fuzzy Logic Toolbox model. If they are errors, correct them; if intentional, add a comment explaining the rationale.

---

### 7. `heart_disease_risk.fis` — Orphaned Model

This Sugeno-type model (LDL + HDL → Heart Disease Risk) exists in `fuzzy_models/` but is not referenced by any module in `server.js`, any Node-RED flow, or any test file.

**Suggestion:** Either remove it from the repository or document it as a reference/demo model used during development.

---

### 8. Objective Module Extreme Conservatism (§5)

`objective_full.fis` maps 666 out of 729 input combinations (91.4%) to "augsts" (high). Only the single combination where all 6 inputs are "zems" yields "zems". This means the objective component almost always reports high drowsiness.

**Suggestion:** Discuss this design choice in the experimental results section — it is appropriate for safety-critical applications (transport, operators) but may cause alert fatigue in less critical contexts. Consider whether the threshold distribution should be application-domain-dependent.

---

### 9. Degenerate Membership Functions (§3.1)

All normalized models (range 0–2) use `trimf [0,0,0]` as the "zems" membership function. This is technically a singleton point (membership=1 only at x=0, 0 everywhere else), not a triangle. Combined with `trimf [0,1,1]` and `trimf [1,2,2]`, this creates a nearly crisp partition with minimal fuzzy overlap.

**Suggestion:** Note in §3.1 that the three-grade linguistic scale (Z/V/A) with these specific MF shapes produces quasi-crisp behavior for integer inputs (0, 1, 2), with fuzzy interpolation only occurring for non-integer intermediate values. This is a deliberate design choice matching the discrete nature of the expert-defined gradations.

---

## C. Suggested Additions

### 10. Mobile Platform Prototype (§4.4)

The thesis mentions "Saskarne viedtālrunī sistēmas lietotājiem rekomendāciju saņemšanai" as a target interface. The Android app developed alongside this review demonstrates:
- Pure Kotlin fuzzy inference engine (no server dependency)
- All 5 FIS modules running natively on mobile
- 3 chained scenarios matching the Node-RED flow
- 56 unit tests with 968 test vectors passing

**Suggestion:** Reference the Android prototype as a realized implementation of the mobile interface described in §4.4, demonstrating that the fuzzy inference can run entirely on-device without network connectivity.

### 11. Automated Test Infrastructure (§5.2)

The existing test infrastructure (`test_*.js` files) requires a running server and uses HTTP integration tests. The Android app includes pure unit tests that validate the fuzzy engine against the same test data without any server dependency.

**Suggestion:** Note in §5.2 that the test data files can be used for both integration testing (via HTTP API) and unit testing (direct engine invocation), enabling validation of alternative implementations.
