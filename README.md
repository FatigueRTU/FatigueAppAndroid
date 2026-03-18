# Fatigue Expert System — Android App

A Kotlin/Jetpack Compose Android application that implements the fuzzy logic expert system
for mental fatigue and drowsiness assessment. Converts the original Node.js/jFuzzyLite
service into a standalone mobile app with a pure Kotlin fuzzy inference engine.

## Architecture

```
┌─────────────────────────────────────────────────────┐
│  UI Layer (Jetpack Compose + Material 3)            │
│  ├── HomeScreen (scenarios + modules)               │
│  ├── ModuleScreen (individual FIS testing)          │
│  ├── FullAssessmentScreen (Scenario 1)              │
│  ├── MonitoringScreen (Scenario 2)                  │
│  └── QuickCheckScreen (Scenario 3)                  │
├─────────────────────────────────────────────────────┤
│  ViewModel (FatigueViewModel)                       │
│  State management for all inputs/outputs            │
├─────────────────────────────────────────────────────┤
│  Engine Layer (Pure Kotlin)                         │
│  ├── FisParser — Parses Matlab .fis files           │
│  ├── FuzzyEngine — Mamdani + Sugeno inference       │
│  ├── FuzzyTypes — MFs, rules, system definitions    │
│  └── FatigueScenarios — Chained FIS pipelines       │
├─────────────────────────────────────────────────────┤
│  Assets: 11 .fis fuzzy model files from Matlab      │
└─────────────────────────────────────────────────────┘
```

## 3 Chained Scenarios (from Node-RED flow)

### Scenario 1: Full Assessment
Complete pre-shift + monitoring assessment:
```
Subjective(13 inputs) ──┐
                        ├──→ Fatigue ──┐
Objective(6 inputs) ────┤              ├──→ Recommendation
                        └──→ Alert ────┘
```
**Node-RED equivalent:** PIRMS_REISA → MONITORINGS → LĒMUMS → TRAUKSME → REKOMENDĀCIJA

### Scenario 2: Monitoring
Continuous monitoring with existing fatigue level:
```
Objective(6 inputs) ──→ Alert(EEG+Eye+Extraordinary) ──→ Recommendation
                                                         ↑
                                              Current fatigue level
```
**Node-RED equivalent:** MONITORINGS → TRAUKSME → REKOMENDĀCIJA

### Scenario 3: Quick Check
Direct component values for rapid assessment:
```
Fatigue(subj+obj) ──┐
                    ├──→ Recommendation
Alert(3 inputs) ────┘
```
**Node-RED equivalent:** NOGURUMS + TRAUKSME → REKOMENDĀCIJA

## FIS Models Used

| Model | File | Inputs | Rules | Type |
|-------|------|--------|-------|------|
| Subjective | subjektiva_dala.fis | 13 | 200 | Mamdani |
| Objective (Full) | objective_full.fis | 6 | 729 | Mamdani |
| Fatigue | nogurums.fis | 2 | 9 | Mamdani |
| Alert | trauksme.fis | 3 | 18 | Mamdani |
| Recommendation | rekomendacijas.fis | 2 | 12 | Mamdani |

## Fuzzy Engine

The app includes a **pure Kotlin fuzzy inference engine** (no JNI/native dependencies) that:

- Parses standard Matlab `.fis` file format
- Supports **Mamdani** inference (centroid, MOM, bisector, SOM, LOM defuzzification)
- Supports **Sugeno** inference (weighted average, weighted sum)
- Handles `trimf`, `trapmf`, `gaussmf`, and `constant` membership functions
- Implements `min`/`prod` AND methods, `max`/`probor` OR methods
- Handles degenerate MFs like `trimf [0,0,0]` (singleton points)
- Rule wildcards (index 0 = don't care) and negation (negative index)

## Building

Open in Android Studio (Hedgehog or newer) and build:

```bash
./gradlew assembleDebug
```

Requires:
- Android SDK 34
- JDK 17
- Kotlin 1.9.20

## Output Classification (matching Node-RED thresholds)

| Module | Low | Medium | High | Critical |
|--------|-----|--------|------|----------|
| Fatigue | < 0.7 | 0.7–1.5 | > 1.5 | — |
| Alert | < 0.6 (None) | 0.6–1.5 (Buzzer) | 1.5–2.5 (Bell) | > 2.5 (Siren) |
| Recommendation | < 0.6 (Continue) | 0.6–1.5 (Pause) | 1.5–2.5 (Lunch) | > 2.5 (Stop) |
