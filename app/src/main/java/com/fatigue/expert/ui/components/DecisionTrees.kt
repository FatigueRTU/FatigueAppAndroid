package com.fatigue.expert.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fatigue.expert.ui.BiString
import com.fatigue.expert.ui.theme.*

/**
 * Expandable decision tree card showing the FIS logic flow as a formatted diagram.
 */
@Composable
fun DecisionTreeDropdown(
    title: String,
    diagram: String,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = if (expanded) 2.dp else 0.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.AccountTree, contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                Icon(
                    if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null, tint = MaterialTheme.colorScheme.primary
                )
            }
            AnimatedVisibility(visible = expanded) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(start = 12.dp, end = 12.dp, bottom = 12.dp)
                ) {
                    Text(
                        diagram,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        lineHeight = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Button that opens a dialog showing the module's decision rule matrix.
 */
@Composable
fun DecisionTreeButton(
    moduleName: String,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }

    val (title, diagram) = getModuleDecisionTree(moduleName)

    OutlinedButton(
        onClick = { showDialog = true },
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Icon(Icons.Default.AccountTree, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(BiString("Lēmumu koks", "Decision Tree").get(), style = MaterialTheme.typography.labelSmall)
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium) },
            text = {
                Box(
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .fillMaxWidth()
                ) {
                    Text(
                        diagram,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        lineHeight = 14.sp
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(BiString("Aizvērt", "Close").get())
                }
            }
        )
    }
}

// ── Scenario flow diagrams ──

val scenario1Diagram = """
┌─────────────────────────┐
│  LP1: SUBJECTIVE DATA   │
│  13 inputs (survey+tests)│
│  subjektiva_dala.fis     │
│  200 rules, AND=prod     │
└───────────┬─────────────┘
            ▼
┌─────────────────────────┐
│  LP2: MENTAL FATIGUE    │
│  nogurums.fis           │
│  Subj + Obj(=0) → Fatigue│
│  9 rules, AND=min       │
└───────────┬─────────────┘
            ▼
┌─────────────────────────┐
│  LP3: RECOMMENDATION    │
│  rekomendacijas.fis     │
│  Fatigue + Alert(=0)    │
│  12 rules → Action      │
└─────────────────────────┘
""".trimIndent()

val scenario2Diagram = """
┌──────────────────┐  ┌──────────────────┐
│ LP1: SUBJECTIVE  │  │ LP1: OBJECTIVE   │
│ 13 inputs        │  │ 6 EEG/eye inputs │
│ 200 rules        │  │ 729 rules        │
└────────┬─────────┘  └────────┬─────────┘
         │                     │
         └──────┬──────────────┘
                ▼
┌──────────────────────────────┐
│  LP2: MENTAL FATIGUE         │
│  Subj + Obj → Fatigue        │
│  9 rules (max severity)      │
└──────────────┬───────────────┘
               │
┌──────────────┴───────────────┐
│                              │
│  ┌────────────────────────┐  │
│  │ LP1: EXTRAORDINARY     │  │
│  │ 4 checkboxes → 0/1     │  │
│  │ neordinaras_pazimes.fis│  │
│  │ 16 rules               │  │
│  └───────────┬────────────┘  │
│              │               │
│  ┌───────────┴────────────┐  │
│  │ LP1: EEG BYPASS        │  │
│  │ α_A + α_B → Z/V/A     │  │
│  │ eeg_apsteigsana.fis    │  │
│  │ 4 rules                │  │
│  └───────────┬────────────┘  │
│              ▼               │
│  ┌────────────────────────┐  │
│  │ LP2: ALERT TYPE        │  │
│  │ EEG + Eye + Extra      │  │
│  │ trauksme.fis           │  │
│  │ 18 rules → N/B/Z/S    │  │
│  └───────────┬────────────┘  │
└──────────────┬───────────────┘
               ▼
┌──────────────────────────────┐
│  LP3: RECOMMENDATION         │
│  Fatigue + Alert → Action    │
│  rekomendacijas.fis          │
│  12 rules                    │
└──────────────────────────────┘
""".trimIndent()

val scenario3Diagram = """
┌──────────────────────────┐
│  LP1: OBJECTIVE DATA     │
│  6 EEG/eye inputs        │
│  objective_full.fis      │
│  729 rules               │
└───────────┬──────────────┘
            │
┌───────────┴──────────────┐
│                          │
│  ┌────────────────────┐  │
│  │ LP1: EXTRAORDINARY │  │
│  │ 4 checkboxes → 0/1 │  │
│  │ 16 rules           │  │
│  └─────────┬──────────┘  │
│            │             │
│  ┌─────────┴──────────┐  │
│  │ LP1: MONITOR EEG   │  │
│  │ α duration + β     │  │
│  │ monitoringa_eeg.fis│  │
│  │ 6 rules → Z/V/A   │  │
│  └─────────┬──────────┘  │
│            ▼             │
│  ┌────────────────────┐  │
│  │ LP2: ALERT TYPE    │  │
│  │ EEG + Eye + Extra  │  │
│  │ 18 rules → N/B/Z/S│  │
│  └─────────┬──────────┘  │
│            │             │
│  ┌─────────┴──────────┐  │
│  │ LP2: FATIGUE       │  │
│  │ existing level     │  │
│  │ (from pre-trip)    │  │
│  └─────────┬──────────┘  │
└────────────┬─────────────┘
             ▼
┌──────────────────────────┐
│  LP3: RECOMMENDATION     │
│  Fatigue + Alert → Action│
│  12 rules                │
└──────────────────────────┘
""".trimIndent()

// ── Module decision trees ──

private fun getModuleDecisionTree(moduleName: String): Pair<String, String> = when (moduleName) {
    "fatigue" -> "nogurums.fis — 3×3" to """
         Objective Component
        ┌────────┼────────┐
       Low     Medium    High
        │        │        │
   Subj: L M H  L M H   L M H
        │ │ │   │ │ │   │ │ │
        ▼ ▼ ▼   ▼ ▼ ▼   ▼ ▼ ▼
        Z V A   V V A   A A A

Z=Zems  V=Vidējs  A=Augsts
Rule: output = max(subj, obj)
""".trimIndent()

    "alert" -> "trauksme.fis — 18 rules" to """
EEG\Eye │  Low      │ Medium    │ High
────────┼───────────┼───────────┼──────
Low     │ N/Buzz    │ Buzz/Bell │ SIR
Medium  │ Buzz/Bell │ Bell/SIR  │ SIR
High    │ SIR/SIR   │ SIR/SIR  │ SIR

(left=no extra / right=with extra)

N=Nav  Buzz=Zummers  Bell=Zvans
SIR=Sirēna un gaisma

Rule: ANY high → Siren always
""".trimIndent()

    "recommendation" -> "rekomendacijas.fis — 3×4" to """
Fatigue\Alert│ None │ Buzz │ Bell │Siren
─────────────┼──────┼──────┼──────┼─────
Low          │  ✅  │  ☕  │  🛑  │ 🛑
Medium       │  🍽️  │  🍽️  │  🛑  │ 🛑
High         │  🍽️  │  🛑  │  🛑  │ 🛑

✅ = Turpināt darbu (Continue)
☕ = Pastaigu pauze (Walking pause)
🍽️ = Atpūta/pusdienas (Rest/lunch)
🛑 = Beigt maiņu (End shift)
""".trimIndent()

    "subjective" -> "subjektiva_dala.fis — 200 rules" to """
OVERRIDE RULES (highest priority):
  Survey(KSS) = HIGH → always HIGH
  Unexpected sleep = HIGH → always HIGH

STANDARD EVALUATION:
  Medical profile (inputs 1-8):
    Low risk / Moderate / High risk

  Cognitive tests (inputs 10-13):
    All low → LOW
    1-2 medium → MEDIUM
    Any high or 3+ medium → HIGH
    
  High risk profile → always HIGH

13 inputs → 200 rules → Z/V/A output
AND=prod, Agg=probor, Defuzz=MOM
""".trimIndent()

    "objective" -> "objective_full.fis — 729 rules" to """
6 inputs: Blink, EEG α, J1-J4
729 rules = complete 3⁶ coverage

ANY input = HIGH?
├── YES ──────────────────► HIGH
│
Count of MEDIUM inputs?
├── 3 or more ────────────► HIGH
├── 1 or 2 ───────────────► MEDIUM
└── 0 (all Low) ──────────► LOW

Distribution:
  LOW:    1 rule  ( 0.1%)
  MEDIUM: 62 rules ( 8.5%)
  HIGH:  666 rules (91.4%)

⚠️ 91.4% → HIGH (conservative safety)
AND=prod, Defuzz=centroid
""".trimIndent()

    "nonstandard" -> BiString("Nestandarta situāciju modulis", "Non-standard Situations Module").get() to """
3 sub-modules (26 rules total):

① EXTRAORDINARY SIGNS (16 rules)
   4 boolean inputs → OR → 0/1
   ANY sign detected → output = 1

② EEG BYPASS ALERT (4 rules)
   α_A + α_B phases → Z/V/A
   Neither=0, One=1, Both=2

③ MONITORING EEG (6 rules)
   α duration + β presence
   α>3s → HIGH (danger)
   α≤2s + no β → MEDIUM
   otherwise → LOW

Outputs feed into Alert module:
  ① → Neordinaras_pazimes (0/1)
  ②③ → EEG_vertejums (0/1/2)
""".trimIndent()

    "extraordinary" -> "neordinaras_pazimes.fis — 16 rules" to """
4 inputs (boolean checkboxes):
  mikromiegs, narkolepsija,
  miegainiba_bez_noguruma, bezmiegs

Logic: OR aggregation
┌─────────────────────────┐
│ ALL inputs = 0 (Ne)?    │
│   YES → Nav konstatētas │
│   NO  → Ir konstatētas  │
└─────────────────────────┘

ANY checkbox = 1 → output = 1
All checkboxes = 0 → output = 0

16 rules = complete 2⁴ coverage
AND=min, Defuzz=centroid
""".trimIndent()

    "eeg_bypass" -> "eeg_apsteigsana.fis — 4 rules" to """
2 inputs (boolean):
  EEG_alfa_A_posms, EEG_alfa_B_posms

A\B  │  Ne (0)  │  Jā (1)
─────┼──────────┼──────────
Ne   │  Zems    │  Vidējs
Jā   │  Vidējs  │  Augsts

Neither → 0 (Zems)
One     → 1 (Vidējs)
Both    → 2 (Augsts)

4 rules = complete 2² coverage
AND=min, Defuzz=centroid
""".trimIndent()

    "monitoring_eeg" -> "monitoringa_eeg.fis — 6 rules" to """
2 inputs:
  EEG_alfa_ilgums (0-5s)
  EEG_beta_klatbutne (0/1)

Duration\Beta │  Ne (0)  │  Jā (1)
──────────────┼──────────┼──────────
Short (≤2s)   │  Vidējs  │  Zems
Medium (2-3s) │  Zems    │  Zems
Long (>3s)    │  Augsts  │  Augsts

Key insight:
  α > 3s → always HIGH (danger)
  α ≤ 2s + no β → MEDIUM (warning)
  otherwise → LOW (safe)

6 rules = complete 3×2 coverage
AND=min, Defuzz=centroid
""".trimIndent()

    else -> moduleName to "No decision tree available"
}
