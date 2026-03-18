package com.fatigue.expert.ui

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf

/**
 * Bilingual string resources matching the thesis terminology.
 * "PIELĀGOJAMS EKSPERTU SISTĒMU KOMPLEKSS CILVĒKA NOGURUMA NOVĒRTĒŠANAI"
 *
 * Recommendation classes (Thesis Appendix 4):
 *   R=1: Var turpināt darbu
 *   R=2: Neliels ierobežojums jeb pastaigu pauze
 *   R=3: Būtisks ierobežojums jeb atpūta, snauda vai pusdienu pārtraukums
 *   R=4: Jābeidz maiņa, jāizsauc dublieris
 *
 * Alert types (Thesis §2.2):
 *   T=0: Nav trauksmes
 *   T=1: Zummers
 *   T=2: Zvans
 *   T=3: Sirēna un gaisma
 *
 * 3 Scenarios (Thesis Table 5.3):
 *   1. Pirms reisa aptauja (Pre-trip survey)
 *   2. Monitorings ar rekomendācijām (Monitoring with recommendations)
 *   3. Monitorings nestandarta situācijām ar apsteidzošo trauksmi
 */

enum class Language { LV, EN }

val currentLanguage = mutableStateOf(Language.LV)

data class BiString(val lv: String, val en: String) {
    fun get(): String = when (currentLanguage.value) {
        Language.LV -> lv
        Language.EN -> en
    }
}

object S {
    // ── App ──
    val appTitle = BiString(
        "Ekspertu panelis",
        "Expert Panel"
    )
    val appSubtitle = BiString(
        "[Demo] Modulāra ekspertu datorsistēma cilvēka noguruma un miegainības novērtēšanai",
        "[Demo] Modular expert computer system for human fatigue and drowsiness assessment"
    )
    val appDescription = BiString(
        "Nestriktās loģikas ekspertu sistēma mentālā noguruma un miegainības novērtēšanai. " +
        "Izmanto Mamdani tipa secinājumu ar .fis modeļiem no Matlab Fuzzy Logic Toolbox.",
        "Fuzzy logic expert system for mental fatigue and drowsiness assessment. " +
        "Uses Mamdani inference with .fis models from Matlab Fuzzy Logic Toolbox."
    )

    // ── Language ──
    val language = BiString("Valoda", "Language")
    val latvian = BiString("Latviešu", "Latvian")
    val english = BiString("Angļu", "English")

    // ── Navigation ──
    val scenarios = BiString("Lietojuma scenāriji", "Usage Scenarios")
    val scenariosSubtitle = BiString(
        "Daudzpakāpju lēmumu pieņemšanas plūsmas",
        "Multi-stage decision-making pipelines"
    )
    val individualModules = BiString("Individuālie moduļi", "Individual Modules")
    val individualModulesSubtitle = BiString(
        "Pārbaudīt katru nestriktās loģikas moduli atsevišķi",
        "Test each fuzzy logic module independently"
    )
    val back = BiString("Atpakaļ", "Back")
    val evaluate = BiString("Novērtēt", "Evaluate")
    val result = BiString("Rezultāts", "Result")
    val results = BiString("Rezultāti", "Results")
    val resultsPipeline = BiString("Rezultātu plūsma", "Results Pipeline")
    val notEvaluated = BiString("Nav novērtēts", "Not evaluated")
    val inputVariables = BiString("Ieejas mainīgie", "Input Variables")
    val quickSetAll = BiString("Ātri iestatīt visus", "Quick Set All")

    // ── Levels ──
    val low = BiString("Zems", "Low")
    val medium = BiString("Vidējs", "Medium")
    val high = BiString("Augsts", "High")
    val levelShortLow = BiString("Z (Zems)", "L (Low)")
    val levelShortMed = BiString("V (Vidējs)", "M (Med)")
    val levelShortHigh = BiString("A (Augsts)", "H (High)")

    // ── 3 Scenarios (Thesis Table 5.3) ──
    val scenario1Title = BiString(
        "1. Pirms reisa aptauja",
        "1. Pre-trip Survey"
    )
    val scenario1Desc = BiString(
        "Subjektīvie parametri (13 ieejas) → Noguruma lēmums → Rekomendācija. " +
        "Pirms darba izpildes, balstoties uz anketas aizpildīšanu.",
        "Subjective parameters (13 inputs) → Fatigue decision → Recommendation. " +
        "Before work execution, based on questionnaire completion."
    )
    val scenario1Flow = BiString(
        "LP1 Subjektīvā → LP2 Nogurums → LP3 Rekomendācija",
        "LP1 Subjective → LP2 Fatigue → LP3 Recommendation"
    )
    val scenario1Info = BiString(
        "Pirms reisa scenārijs tiek realizēts, kad persona ierodas darba vietā pirms darba izpildes. " +
        "Personai tiek sniegta rekomendācija, balstoties uz noguruma vērtējumu pēc pirms reisa anketas aizpildīšanas. " +
        "Trauksmes vērtējums šajā brīdī saglabājas viszemākais, jo trauksmes situācija nevar rasties.",
        "Pre-trip scenario is executed when a person arrives at the workplace before work. " +
        "A recommendation is provided based on fatigue assessment from the pre-trip questionnaire. " +
        "Alert level remains at minimum since no alert situation can arise at this stage."
    )

    val scenario2Title = BiString(
        "2. Monitorings ar rekomendācijām",
        "2. Monitoring with Recommendations"
    )
    val scenario2Desc = BiString(
        "Subjektīvie + Objektīvie parametri → Nogurums + Trauksme → Rekomendācija. " +
        "Kad cilvēkam pieslēgti sensori un persona veic darbu.",
        "Subjective + Objective parameters → Fatigue + Alert → Recommendation. " +
        "When sensors are connected and person is performing work."
    )
    val scenario2Flow = BiString(
        "LP1 Subjektīvā + Objektīvā → LP2 Nogurums + Trauksme → LP3 Rekomendācija",
        "LP1 Subjective + Objective → LP2 Fatigue + Alert → LP3 Recommendation"
    )
    val scenario2Info = BiString(
        "Monitoringa scenārijs tiek realizēts, kad cilvēkam pieslēgti sensori un persona veic darbu. " +
        "Rekomendācija tiek izstrādāta, balstoties uz noguruma un trauksmes vērtējumiem. " +
        "Trauksme nostrādā kā papildus indikators, lai rekomendāciju laikā izceltu riska gadījumus.",
        "Monitoring scenario is executed when sensors are connected and the person is performing work. " +
        "Recommendation is produced based on fatigue and alert assessments. " +
        "Alert acts as an additional indicator to highlight risk cases during recommendations."
    )

    val scenario3Title = BiString(
        "3. Apsteidzošā trauksme",
        "3. Preemptive Alert Monitoring"
    )
    val scenario3Desc = BiString(
        "Objektīvie parametri → Trauksme + Nogurums → Rekomendācija. " +
        "Nestandarta miegainības situāciju noteikšana ar apsteidzošo trauksmi.",
        "Objective parameters → Alert + Fatigue → Recommendation. " +
        "Non-standard drowsiness detection with preemptive alert."
    )
    val scenario3Flow = BiString(
        "LP1 Objektīvā → LP2 Trauksme + Nogurums → LP3 Rekomendācija",
        "LP1 Objective → LP2 Alert + Fatigue → LP3 Recommendation"
    )
    val scenario3Info = BiString(
        "Apsteidzošās trauksmes scenārijs notiek, kad cilvēkam pieslēgti sensori un tiek konstatēta miegainība pirms aizmigšanas. " +
        "Apsteidzošā trauksme tiek realizēta, izmantojot sensoru un algoritmu sniegtos objektīvos datus, lai novērstu aizmigšanu.",
        "Preemptive alert scenario occurs when sensors are connected and drowsiness is detected before falling asleep. " +
        "The preemptive alert is triggered using objective data from sensors and algorithms to prevent the person from falling asleep."
    )

    // ── Pipeline timing ──
    val pipelineTime = BiString("Kopējais plūsmas laiks", "Total pipeline time")

    // ── Module Names ──
    val moduleSubjective = BiString("Subjektīvā komponente", "Subjective Component")
    val moduleSubjectiveDesc = BiString(
        "subjektiva_dala.fis · 13 ieejas · 200 likumi",
        "subjektiva_dala.fis · 13 inputs · 200 rules"
    )
    val moduleSubjectiveInfo = BiString(
        "Apvieno anamnēzes datus, pirms maiņas aptaujas rezultātus un kognitīvo testu novērtējumus vienotā subjektīvajā miegainības pakāpē.",
        "Combines anamnesis data, pre-shift survey results and cognitive test scores into a unified subjective drowsiness level."
    )
    val moduleObjective = BiString("Objektīvā komponente", "Objective Component")
    val moduleObjectiveDesc = BiString(
        "objective_full.fis · 6 ieejas · 729 likumi",
        "objective_full.fis · 6 inputs · 729 rules"
    )
    val moduleObjectiveInfo = BiString(
        "Novērtē objektīvo miegainību pēc acu mirkšķināšanas biežuma, EEG alfa ritma un četriem EEG spektrālo joslu indeksiem (J1–J4).",
        "Evaluates objective drowsiness from blink frequency, EEG alpha rhythm and four EEG spectral band indices (J1–J4)."
    )
    val moduleFatigue = BiString("Mentālais nogurums", "Mental Fatigue")
    val moduleFatigueDesc = BiString(
        "nogurums.fis · 2 ieejas · 9 likumi",
        "nogurums.fis · 2 inputs · 9 rules"
    )
    val moduleFatigueInfo = BiString(
        "Apvieno subjektīvo un objektīvo komponenti kopējā noguruma pakāpē, izmantojot pesimistisko apvienošanu (maksimālā smaguma princips).",
        "Combines subjective and objective components into an overall fatigue level using pessimistic fusion (maximum severity principle)."
    )
    val moduleAlert = BiString("Trauksmes modulis", "Alert Module")
    val moduleAlertDesc = BiString(
        "trauksme.fis · 3 ieejas · 18 likumi",
        "trauksme.fis · 3 inputs · 18 rules"
    )
    val moduleAlertInfo = BiString(
        "Nosaka apsteidzošās trauksmes veidu (zummers, zvans, sirēna) pēc EEG novērtējuma, acu novērtējuma un neordinārām pazīmēm. Imitē signālu nosūtīšanu valkājamiem aktuatoriem.",
        "Determines preemptive alert type (buzzer, bell, siren) from EEG assessment, eye assessment and extraordinary signs. Imitates sending signals to wearable actuators."
    )
    val moduleRecommendation = BiString("Rekomendācijas modulis", "Recommendation Module")
    val moduleRecommendationDesc = BiString(
        "rekomendacijas.fis · 2 ieejas · 12 likumi",
        "rekomendacijas.fis · 2 inputs · 12 rules"
    )
    val moduleRecommendationInfo = BiString(
        "Izstrādā rekomendāciju noguruma mazināšanai, apvienojot noguruma pakāpi un trauksmes veidu lietojuma sfērai atbilstošā darbībā.",
        "Produces a fatigue mitigation recommendation by combining fatigue level and alert type into a domain-appropriate action."
    )

    // ── LP1 Non-standard situation detection (Thesis Table 5.2, module 6) ──
    val moduleNonstandard = BiString("Nestandarta situācijas", "Non-standard Situations")
    val moduleNonstandardDesc = BiString(
        "3 apakšmoduļi · 8 ieejas · 26 likumi",
        "3 sub-modules · 8 inputs · 26 rules"
    )
    val moduleNonstandardInfo = BiString(
        "Apvieno trīs apakšmoduļus nestandarta miegainības situāciju noteikšanai: neordinārās pazīmes (mikromiegs, narkolepsija u.c.), " +
        "EEG apsteidzošā trauksme (alfa A/B posmu analīze) un monitoringa EEG novērtējums (alfa ilgums un beta aktivitāte).",
        "Combines three sub-modules for non-standard drowsiness situation detection: extraordinary signs (microsleep, narcolepsy, etc.), " +
        "EEG bypass alert (alpha A/B phase analysis) and monitoring EEG assessment (alpha duration and beta activity)."
    )
    val moduleExtraordinary = BiString("Neordinārās pazīmes", "Extraordinary Signs")
    val moduleExtraordinaryDesc = BiString(
        "neordinaras_pazimes.fis · 4 ieejas · 16 likumi",
        "neordinaras_pazimes.fis · 4 inputs · 16 rules"
    )
    val moduleExtraordinaryInfo = BiString(
        "Apkopo četras neordinārās pazīmes (mikromiegs, narkolepsija, miegainība bez noguruma, bezmiegs) vienā binārā novērtējumā: konstatētas vai nav konstatētas.",
        "Aggregates four extraordinary signs (microsleep, narcolepsy, sleepiness without fatigue, insomnia) into a single binary assessment: detected or not detected."
    )
    val moduleEegBypass = BiString("EEG apsteidzošā trauksme", "EEG Bypass Alert")
    val moduleEegBypassDesc = BiString(
        "eeg_apsteigsana.fis · 2 ieejas · 4 likumi",
        "eeg_apsteigsana.fis · 2 inputs · 4 rules"
    )
    val moduleEegBypassInfo = BiString(
        "Novērtē apsteidzošo trauksmi pirms maiņas pēc EEG alfa A un B posmu klātbūtnes: abi → augsts, viens → vidējs, neviens → zems.",
        "Assesses pre-shift bypass alert from EEG alpha A and B phase presence: both → high, one → medium, neither → low."
    )
    val moduleMonitoringEeg = BiString("Monitoringa EEG", "Monitoring EEG")
    val moduleMonitoringEegDesc = BiString(
        "monitoringa_eeg.fis · 2 ieejas · 6 likumi",
        "monitoringa_eeg.fis · 2 inputs · 6 rules"
    )
    val moduleMonitoringEegInfo = BiString(
        "Novērtē EEG stāvokli monitoringa laikā pēc alfa ritma ilguma un beta aktivitātes klātbūtnes.",
        "Assesses EEG state during monitoring from alpha rhythm duration and beta activity presence."
    )

    // ── LP1 input labels ──
    val inputMicrosleep = BiString("Mikromiegs", "Microsleep")
    val inputNarcolepsy = BiString("Narkolepsija", "Narcolepsy")
    val inputSleepWithoutFatigue = BiString("Miegainība bez noguruma", "Sleepiness without fatigue")
    val inputInsomnia = BiString("Bezmiegs", "Insomnia")
    val inputAlphaAPhase = BiString("EEG alfa A posms", "EEG alpha A phase")
    val inputAlphaBPhase = BiString("EEG alfa B posms", "EEG alpha B phase")
    val inputAlphaDuration = BiString("EEG alfa ilgums (s)", "EEG alpha duration (s)")
    val inputBetaPresence = BiString("EEG beta klātbūtne", "EEG beta presence")

    // ── Alert Types (Thesis §2.2) ──
    val alertNone = BiString("Nav trauksmes", "No alert")
    val alertBuzzer = BiString("Zummers", "Buzzer")
    val alertBell = BiString("Zvans", "Bell")
    val alertSiren = BiString("Sirēna un gaisma", "Siren and light")

    // ── Recommendation Classes (Thesis Appendix 4) ──
    val recoContWork = BiString(
        "Var turpināt darbu",
        "Can continue work"
    )
    val recoPause = BiString(
        "Neliels ierobežojums — pastaigu pauze",
        "Minor restriction — walking pause"
    )
    val recoBreak = BiString(
        "Būtisks ierobežojums — atpūta, snauda vai pusdienu pārtraukums",
        "Significant restriction — rest, nap or lunch break"
    )
    val recoStop = BiString(
        "Jābeidz maiņa, jāizsauc dublieris",
        "Must end shift, call replacement"
    )

    // ── Fatigue Levels ──
    val fatigueLow = BiString("Zems nogurums", "Low fatigue")
    val fatigueMedium = BiString("Vidējs nogurums", "Medium fatigue")
    val fatigueHigh = BiString("Augsts nogurums", "High fatigue")

    // ── Input Group Headers ──
    val groupAnamnesis = BiString("Anamnēzes dati (Grupa 1)", "Anamnesis Data (Group 1)")
    val groupSurvey = BiString("Pirms maiņas aptauja (Grupa 2)", "Pre-shift Survey (Group 2)")
    val groupTests = BiString("Testu aktivitātes (Grupa 3)", "Test Activities (Group 3)")
    val groupEEG = BiString("EEG un acu parametri", "EEG and Eye Parameters")
    val groupAlert = BiString("Trauksmes parametri", "Alert Parameters")
    val groupFatigue = BiString("Noguruma komponentes", "Fatigue Components")

    // ── Subjective Input Labels ──
    val inputNightWork = BiString("Nakts darbs", "Night work")
    val inputStress = BiString("Stress", "Stress")
    val inputBloodPressure = BiString("Paaugstināts asinsspiediens", "High blood pressure")
    val inputBrainDisorders = BiString("Smadzeņu darbības traucējumi", "Brain function disorders")
    val inputStimulants = BiString("Lieto uzmundrinošus dzērienus", "Uses stimulant drinks")
    val inputSedatives = BiString("Lieto nomierinošus līdzekļus", "Uses sedatives")
    val inputApnea = BiString("Apnoja", "Sleep apnea")
    val inputUnexpectedSleep = BiString("Negaidīta aizmigsana", "Unexpected falling asleep")
    val inputSurvey = BiString("Aptauja (KSS)", "Survey (KSS)")
    val inputReactionTest = BiString("Reakcijas tests", "Reaction test")
    val inputSectorMemory = BiString("Sektoru atmiņas tests", "Sector memory test")
    val inputSequenceMemory = BiString("Secības atmiņas tests", "Sequence memory test")
    val inputMathTest = BiString("Matemātiskais laika tests", "Math time test")

    // ── Objective Input Labels (Thesis Table 5.1) ──
    val inputBlinkFreq = BiString(
        "Acu mirkšķināšanas biežums",
        "Blink frequency"
    )
    val inputBlinkFreqDesc = BiString(
        "Mirkšķināšanas biežuma izmaiņa",
        "Blink rate change"
    )
    val inputEEGAlpha = BiString("EEG alfa ritms", "EEG alpha rhythm")
    val inputEEGAlphaDesc = BiString(
        "Alfa joslas izteiktības ilgums",
        "Alpha band prominence duration"
    )
    val inputEEGJ1 = BiString("J1 — Iesaiste uzdevumā", "J1 — Task engagement")
    val inputEEGJ1Desc = BiString(
        "rel.β / (rel.α + rel.θ)",
        "rel.β / (rel.α + rel.θ)"
    )
    val inputEEGJ2 = BiString("J2 — Uzmanība", "J2 — Attention")
    val inputEEGJ2Desc = BiString(
        "rel.θ / rel.α",
        "rel.θ / rel.α"
    )
    val inputEEGJ3 = BiString("J3 — Stress", "J3 — Stress")
    val inputEEGJ3Desc = BiString(
        "rel.θ / rel.β",
        "rel.θ / rel.β"
    )
    val inputEEGJ4 = BiString("J4 — Modrība", "J4 — Alertness")
    val inputEEGJ4Desc = BiString(
        "rel.α / rel.β",
        "rel.α / rel.β"
    )

    // ── Simulation ──
    val simStart = BiString("▶ Simulācija", "▶ Simulate")
    val simStop = BiString("⏹ Apturēt", "⏹ Stop")
    val simRunning = BiString("Simulācija aktīva…", "Simulation running…")
    val simTip = BiString(
        "💡 Simulācija secīgi palielina katru slīdni par 0.1 ik 100ms un novērtē moduli pēc katras pilnas secības. Sasniedzot maksimumu, vērtības tiek atiestatītas.",
        "💡 Simulation sequentially increments each slider by 0.1 every 100ms and evaluates the module after each full sequence. Values reset when all reach maximum."
    )

    // ── Tips ──
    val mainPageTip = BiString(
        "💡 Šis panelis paredzēts ekspertiem lēmumu pieņemšanas plūsmu pārskatīšanai un individuālo moduļu novērtēšanai. " +
        "Izvēlieties lietojuma scenāriju, lai pārbaudītu saistītu moduļu plūsmu, vai atsevišķu moduli, lai testētu tā ieejas un izejas neatkarīgi.",
        "💡 This panel is intended for experts to review the decision-making flow and evaluate individual modules. " +
        "Select a usage scenario to test a chained module pipeline, or an individual module to test its inputs and outputs independently."
    )

    // ── Alert Input Labels ──
    val inputEEGAssessment = BiString("EEG novērtējums", "EEG assessment")
    val inputEyeAssessment = BiString("Acu novērtējums", "Eye assessment")
    val inputExtraordinary = BiString("Neordināras pazīmes", "Extraordinary signs")

    // ── Other Labels ──
    val inputSubjComponent = BiString("Subjektīvā komponente", "Subjective component")
    val inputObjComponent = BiString("Objektīvā komponente", "Objective component")
    val inputDrowsinessLevel = BiString("Miegainības līmenis", "Drowsiness level")
    val inputAlertType = BiString("Trauksmes veids", "Alert type")
    val currentFatigueLevel = BiString("Pašreizējais noguruma līmenis", "Current fatigue level")
    val currentFatigueLevelDesc = BiString(
        "Iestatīt no iepriekšējā pilnā novērtējuma vai manuāli",
        "Set from previous full assessment or manually"
    )

    // ── Scenario Step Labels ──
    val stepSubjective = BiString("Subjektīvie dati", "Subjective Data")
    val stepObjective = BiString("Objektīvie dati", "Objective Data")
    val stepFatigue = BiString("Nogurums", "Fatigue")
    val stepAlert = BiString("Trauksme", "Alert")
    val stepRecommendation = BiString("Rekomendācija", "Recommendation")

    // ── Scenario-specific ──
    val preTrip = BiString("Pirms reisa", "Pre-trip")
    val monitoring = BiString("Monitorings", "Monitoring")
    val quickMode = BiString("Ātrā pārbaude", "Quick check")
    val quickModeDesc = BiString(
        "Ievadiet iepriekš aprēķinātās komponentu vērtības tieši. " +
        "Izmantojiet, ja jums jau ir subjektīvie/objektīvie rezultāti no iepriekšējā novērtējuma.",
        "Enter pre-computed component values directly. " +
        "Use when you already have subjective/objective scores from a previous assessment."
    )

    val runFullAssessment = BiString("Veikt pilnu novērtējumu", "Run Full Assessment")
    val runMonitoring = BiString("Veikt monitoringu", "Run Monitoring")
    val runQuickCheck = BiString("Veikt ātro pārbaudi", "Run Quick Check")
}
