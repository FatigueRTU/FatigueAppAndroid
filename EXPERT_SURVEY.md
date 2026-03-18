# Ekspertu novērtējuma aptauja / Expert Evaluation Survey

**Sistēma / System:** Modulāra datorsistēma cilvēka noguruma un miegainības novērtēšanai / Modular computer system for human fatigue and drowsiness assessment

**Demonstrācijas lietotne / Demo app:** Android lietotne ar pirms reisa aptauju, kognitīvajiem testiem un monitoringa simulāciju / Android app with pre-trip survey, cognitive tests and monitoring simulation

**Mērķis / Purpose:** Iegūt ekspertu viedokli par sistēmas lēmumu pareizību, praktisko lietojamību un lietotnes darbību / Obtain expert opinion on system decision correctness, practical applicability and app performance

---

## Instrukcija / Instructions

**LV:** Lūdzu, izmantojiet Android lietotni, lai iepazītos ar sistēmu. Lietotnē ir pieejami divi galvenie scenāriji:

1. **Aptauja pirms reisa** — aizpildiet aptauju (9 jautājumi) un veiciet 4 kognitīvos testus. Sistēma automātiski novērtēs noguruma pakāpi un sniegs rekomendāciju.
2. **Monitoringa aktivitāte** — simulējiet sensoru datus (EEG, acu parametri) un novērojiet, kā sistēma reaģē ar trauksmi un rekomendācijām.

Pēc iepazīšanās ar lietotni, lūdzu, atbildiet uz aptaujas jautājumiem.

**EN:** Please use the Android app to familiarize yourself with the system. The app has two main scenarios:

1. **Pre-trip survey** — complete a survey (9 questions) and 4 cognitive tests. The system automatically evaluates fatigue level and provides a recommendation.
2. **Monitoring activity** — simulate sensor data (EEG, eye parameters) and observe how the system responds with alerts and recommendations.

After exploring the app, please answer the survey questions.

**Vērtējuma skala / Rating scale:**
1 = Pilnīgi nepiekrītu / Strongly disagree
2 = Nepiekrītu / Disagree
3 = Neitrāli / Neutral
4 = Piekrītu / Agree
5 = Pilnīgi piekrītu / Strongly agree

---

## A sadaļa / Section A. Aptaujas un testu novērtējums / Survey and Tests Evaluation

*Lūdzu, veiciet pirms reisa aptauju un kognitīvos testus lietotnē. / Please complete the pre-trip survey and cognitive tests in the app.*

### A1. Aptaujas jautājumu saprotamība / Survey question clarity
Aptaujas jautājumi ir saprotami un atbilžu varianti ir skaidri formulēti.
The survey questions are understandable and the answer options are clearly formulated.

Vērtējums / Rating: [ 1 ] [ 2 ] [ 3 ] [ 4 ] [ 5 ]

### A2. Aptaujas parametru atbilstība / Survey parameter relevance
Aptaujā iekļautie parametri (nakts darbs, stress, asinsspiediens, medikamenti, miega kvalitāte u.c.) ir klīniski nozīmīgi noguruma novērtēšanā.
The parameters included in the survey (night work, stress, blood pressure, medications, sleep quality, etc.) are clinically relevant for fatigue assessment.

Vērtējums / Rating: [ 1 ] [ 2 ] [ 3 ] [ 4 ] [ 5 ]

Komentāri / Comments: _______________________________________________

### A3. Kognitīvo testu piemērotība / Cognitive test suitability
Kognitīvie testi (reakcijas ātrums, aritmētika, atmiņa, modrība) ir piemēroti noguruma izpausmju novērtēšanai.
The cognitive tests (reaction speed, arithmetic, memory, vigilance) are suitable for evaluating fatigue manifestations.

Vērtējums / Rating: [ 1 ] [ 2 ] [ 3 ] [ 4 ] [ 5 ]

### A4. Rezultātu saprotamība / Result clarity
Sistēmas sniegtie rezultāti (noguruma pakāpe, rekomendācija) ir saprotami un interpretējami bez papildu skaidrojumiem.
The results provided by the system (fatigue level, recommendation) are understandable and interpretable without additional explanation.

Vērtējums / Rating: [ 1 ] [ 2 ] [ 3 ] [ 4 ] [ 5 ]

---

## B sadaļa / Section B. Lēmumu pareizība / Decision Correctness

*Lūdzu, lietotnē izmēģiniet dažādas ieejas kombinācijas un novērtējiet, vai sistēmas lēmumi ir loģiski. / Please try different input combinations in the app and evaluate whether the system decisions are logical.*

### B1. Noguruma novērtējuma pareizība / Fatigue assessment correctness
Sistēma pareizi nosaka noguruma pakāpi (zems / vidējs / augsts) — augstāks nogurums tiek konstatēts, ja ir vairāk riska faktoru.
The system correctly determines the fatigue level (low / medium / high) — higher fatigue is detected when more risk factors are present.

Vērtējums / Rating: [ 1 ] [ 2 ] [ 3 ] [ 4 ] [ 5 ]

### B2. Rekomendāciju atbilstība / Recommendation appropriateness
Sistēmas rekomendācijas (turpināt darbu / pauze / atpūta / beigt maiņu) atbilst konstatētajam noguruma līmenim.
The system recommendations (continue work / pause / rest / end shift) correspond to the detected fatigue level.

Vērtējums / Rating: [ 1 ] [ 2 ] [ 3 ] [ 4 ] [ 5 ]

### B3. Trauksmes eskalācija / Alert escalation
Monitoringa laikā trauksmes līmenis (nav / zummers / zvans / sirēna) pieaug atbilstoši situācijas nopietnībai.
During monitoring, the alert level (none / buzzer / bell / siren) increases appropriately with situation severity.

Vērtējums / Rating: [ 1 ] [ 2 ] [ 3 ] [ 4 ] [ 5 ]

### B4. Drošības pieeja / Safety approach
Sistēma drīzāk pārnovērtē nogurumu nekā to nenovērtē — tas ir pieņemami drošības kritiskās jomās (transports, operatoru darbs).
The system tends to overestimate fatigue rather than underestimate it — this is acceptable in safety-critical domains (transport, operator work).

Vērtējums / Rating: [ 1 ] [ 2 ] [ 3 ] [ 4 ] [ 5 ]

Komentāri / Comments: _______________________________________________

---

## C sadaļa / Section C. Sistēmas uzbūve un pieeja / System Design and Approach

### C1. Divu avotu kombinēšana / Two-source combination
Sistēma apvieno subjektīvos datus (aptauja, testi) un objektīvos datus (EEG sensori) kopējā novērtējumā — šī pieeja ir pamatota.
The system combines subjective data (survey, tests) and objective data (EEG sensors) into an overall assessment — this approach is justified.

Vērtējums / Rating: [ 1 ] [ 2 ] [ 3 ] [ 4 ] [ 5 ]

### C2. Trīs soļu process / Three-step process
Trīs soļu process (1. datu ievākšana → 2. noguruma un trauksmes novērtēšana → 3. rekomendācija) ir loģisks un saprotams.
The three-step process (1. data collection → 2. fatigue and alert assessment → 3. recommendation) is logical and understandable.

Vērtējums / Rating: [ 1 ] [ 2 ] [ 3 ] [ 4 ] [ 5 ]

### C3. Novērtējuma skalu piemērotība / Assessment scale suitability
Trīs pakāpju skala nogurumam (zems / vidējs / augsts) un četru pakāpju skala rekomendācijām ir pietiekama un praktiski lietojama.
The three-level scale for fatigue (low / medium / high) and four-level scale for recommendations is sufficient and practically applicable.

Vērtējums / Rating: [ 1 ] [ 2 ] [ 3 ] [ 4 ] [ 5 ]

### C4. Apsteidzošā trauksme / Preemptive alert
Apsteidzošās trauksmes ideja (brīdināt pirms aizmigšanas, nevis pēc) ir praktiski noderīga un klīniski pamatota.
The preemptive alert concept (warning before falling asleep, not after) is practically useful and clinically justified.

Vērtējums / Rating: [ 1 ] [ 2 ] [ 3 ] [ 4 ] [ 5 ]

Komentāri / Comments: _______________________________________________

---

## D sadaļa / Section D. Lietotnes lietojamība un veiktspēja / App Usability and Performance

*Šī sadaļa novērtē Android lietotnes darbību. Sistēma darbojas pilnībā lokāli ierīcē — visi aprēķini tiek veikti tieši planšetdatorā/telefonā bez interneta savienojuma vai mākoņpakalpojumiem. / This section evaluates the Android app operation. The system runs entirely locally on the device — all calculations are performed directly on the tablet/phone without internet connection or cloud services.*

### D1. Lietotnes saprotamība / App comprehensibility
Lietotnes saskarni ir viegli saprast un izmantot bez papildu apmācības.
The app interface is easy to understand and use without additional training.

Vērtējums / Rating: [ 1 ] [ 2 ] [ 3 ] [ 4 ] [ 5 ]

### D2. Navigācija un plūsma / Navigation and flow
Lietotnes navigācija (aptauja → testi → rezultāts → monitorings) ir loģiska un intuitīva.
The app navigation (survey → tests → result → monitoring) is logical and intuitive.

Vērtējums / Rating: [ 1 ] [ 2 ] [ 3 ] [ 4 ] [ 5 ]

### D3. Sistēmas ātrums / System speed
Sistēma reaģē ātri — rezultāti parādās praktiski uzreiz pēc datu ievades (bez ielādes laika vai gaidīšanas).
The system responds quickly — results appear practically instantly after data input (no loading time or waiting).

Vērtējums / Rating: [ 1 ] [ 2 ] [ 3 ] [ 4 ] [ 5 ]

### D4. Lokālas darbības priekšrocība / Local operation advantage
Sistēmas spēja darboties pilnībā lokāli (bez interneta) ir priekšrocība salīdzinājumā ar mākoņa risinājumiem, jo nodrošina datu privātumu un darbību bez tīkla.
The system's ability to operate entirely locally (without internet) is an advantage compared to cloud solutions, as it ensures data privacy and offline operation.

Vērtējums / Rating: [ 1 ] [ 2 ] [ 3 ] [ 4 ] [ 5 ]

### D5. Rezultātu attēlojums / Result presentation
Rezultātu attēlojums (krāsu kodējums, rekomendāciju teksti, moduļu vērtības) ir informatīvs un pārskatāms.
The result presentation (color coding, recommendation texts, module values) is informative and clear.

Vērtējums / Rating: [ 1 ] [ 2 ] [ 3 ] [ 4 ] [ 5 ]

Komentāri / Comments: _______________________________________________

---

## E sadaļa / Section E. Praktiskā pielietojamība / Practical Applicability

### E1. Pielāgojamība nozarēm / Adaptability to industries
Sistēma ir pielāgojama dažādām nozarēm, mainot aptaujas jautājumus un rekomendāciju saturu.
The system is adaptable to different industries by modifying survey questions and recommendation content.

Vērtējums / Rating: [ 1 ] [ 2 ] [ 3 ] [ 4 ] [ 5 ]

Kurām nozarēm sistēma būtu vispiemērotākā? / Which industries would the system be most suitable for?

[ ] Transports (autovadītāji, mašīnisti) / Transport (drivers, train operators)
[ ] Operatoru darbs (rūpniecība, enerģētika) / Operator work (industry, energy)
[ ] Aviācija (piloti, dispečeri) / Aviation (pilots, dispatchers)
[ ] Veselības aprūpe (miega medicīna) / Healthcare (sleep medicine)
[ ] Sports un rehabilitācija / Sports and rehabilitation
[ ] Militārā joma / Military
[ ] Cits / Other: _______________________________________________

### E2. Sistēmas pilnīgums / System completeness
Kopumā sistēma aptver galvenās funkcijas, kas nepieciešamas noguruma novērtēšanai un brīdināšanai.
Overall, the system covers the main functions needed for fatigue assessment and alerting.

Vērtējums / Rating: [ 1 ] [ 2 ] [ 3 ] [ 4 ] [ 5 ]

### E3. Lietotnes noderīgums / App usefulness
Android lietotne ir noderīgs rīks sistēmas demonstrēšanai un novērtēšanai.
The Android app is a useful tool for demonstrating and evaluating the system.

Vērtējums / Rating: [ 1 ] [ 2 ] [ 3 ] [ 4 ] [ 5 ]

---

## F sadaļa / Section F. Brīvie jautājumi / Open Questions

### F1. Stiprās puses / Strengths
Kādas ir sistēmas galvenās stiprās puses? / What are the main strengths of the system?

_______________________________________________
_______________________________________________

### F2. Vājās puses / Weaknesses
Kādas ir galvenās vājās puses vai ierobežojumi? / What are the main weaknesses or limitations?

_______________________________________________
_______________________________________________

### F3. Ieteikumi / Suggestions
Kādus uzlabojumus jūs ieteiktu? / What improvements would you suggest?

_______________________________________________
_______________________________________________

### F4. Trūkstošā funkcionalitāte / Missing functionality
Vai sistēmai trūkst kādas būtiskas funkcijas? / Is the system missing any essential functionality?

_______________________________________________
_______________________________________________

---

## Eksperta informācija / Expert Information

Vārds, uzvārds / Name: _______________________________________________
Iestāde / Institution: _______________________________________________
Amats / Position: _______________________________________________
Specialitāte / Specialty: _______________________________________________
Pieredze jomā (gadi) / Experience (years): _______________________________________________
Datums / Date: _______________________________________________
Paraksts / Signature: _______________________________________________

---

## Aptaujas kopsavilkums / Survey Summary

| Sadaļa / Section | Jautājumi / Questions | Fokuss / Focus |
|---|---|---|
| A. Aptauja un testi / Survey & Tests | A1–A4 | Aptaujas un testu kvalitāte / Survey and test quality |
| B. Lēmumu pareizība / Decision correctness | B1–B4 | Vai sistēma pieņem pareizus lēmumus / Whether the system makes correct decisions |
| C. Sistēmas uzbūve / System design | C1–C4 | Arhitektūras un pieejas pamatotība / Architecture and approach justification |
| D. Lietojamība un veiktspēja / Usability & performance | D1–D5 | Lietotnes darbība un ātrums / App operation and speed |
| E. Praktiskā pielietojamība / Practical applicability | E1–E3 | Nozaru piemērotība un pilnīgums / Industry suitability and completeness |
| F. Brīvie jautājumi / Open questions | F1–F4 | Kvalitatīvā atgriezeniskā saite / Qualitative feedback |

**Kopā / Total:** 20 kvantitatīvie jautājumi (Likerta skala 1–5) + 4 brīvā teksta jautājumi / 20 quantitative questions (Likert scale 1–5) + 4 open-text questions

---

## Aptaujas metodoloģiskais pamatojums / Survey Methodology

**LV:**
Šī aptauja izstrādāta, lai papildinātu promocijas darba ekspertu novērtējumu ar detalizētāku kvantitatīvo un kvalitatīvo atgriezenisko saiti. Iepriekšējais novērtējums fiksēja tikai bināru "Atbilst / Neatbilst" rezultātu. Šī aptauja to papildina ar:

- **20 jautājumiem** ar 5 ballu Likerta skalu, sadalītiem 5 tematiskās sadaļās;
- **4 brīvā teksta jautājumiem** detalizētai atgriezeniskajai saitei;
- **Lietotnes novērtējumu** — eksperti tiek aicināti izmantot lietotni pirms aptaujas aizpildīšanas;
- **Veiktspējas novērtējumu** — atsevišķa sadaļa par lietotnes ātrumu un lokālās darbības priekšrocībām.

**Mērķa respondenti:** 3–5 eksperti no medicīnas, medicīnas tehnoloģiju, darba drošības vai transporta drošības jomām.

**Rezultātu analīze:**
- Likerta vērtējumi: vidējais un standartnovirze pa jautājumiem un sadaļām
- Vērtētāju saskaņotība: Kendalla W koeficients
- Brīvie komentāri: tematiskā apkopošana uzlabojumu prioritāšu noteikšanai

**EN:**
This survey was developed to supplement the doctoral thesis expert evaluation with more detailed quantitative and qualitative feedback. The previous evaluation recorded only a binary "Compliant / Non-compliant" result. This survey extends it with:

- **20 questions** on a 5-point Likert scale, organized into 5 thematic sections;
- **4 open-text questions** for detailed feedback;
- **App evaluation** — experts are asked to use the app before completing the survey;
- **Performance evaluation** — a dedicated section on app speed and local operation advantages.

**Target respondents:** 3–5 experts from medicine, medical technology, occupational safety or transport safety fields.

**Results analysis:**
- Likert ratings: mean and standard deviation per question and section
- Rater agreement: Kendall's W coefficient
- Open comments: thematic grouping for improvement prioritization
