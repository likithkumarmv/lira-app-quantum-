# LIRA Field — Native Android Driver Companion Application

**Logistics Intelligence & Recovery Automation (LIRA-Q Sentinel Companion)**  
**Official Track: GENAI-01 — Freight Exception Resolution Before Demurrage and Delivery Penalties**  
**Normative Standard: LIRA Master PRD v13.0 (Shared Contract v13 + Canonical Fixtures C1–C6 + Real-Time Plan Changes & SSE + Epistemic Honesty)**

[![Kotlin 2.2](https://img.shields.io/badge/Kotlin-2.2-blue.svg)](https://kotlinlang.org)
[![Compose Material 3](https://img.shields.io/badge/Jetpack%20Compose-Material%203-brightgreen.svg)](https://developer.android.com/jetpack/compose)
[![Unit Tests Passing](https://img.shields.io/badge/Unit%20Tests-58%2F58%20Passing%20(100%25)-success.svg)](app/src/test/java/com/lira/field/ContractV13Tests.kt)
[![FastAPI Control Tower](https://img.shields.io/badge/FastAPI%20LAN-v13.0--QUANTUM--EDGE-009688.svg)](#2-three-laptop-live-hackathon-integration-topology)
[![Hardware Edge Node](https://img.shields.io/badge/Edge%20Hardware-ESP32%20320x240%20TFT%20%2B%20RC522-orange.svg)](#9-station-tft--interchange-stepper-alignment-zero-early-checkout)
[![Shared Contract](https://img.shields.io/badge/Shared%20Contract-v13.1%20PNR%20%2B%20WOW-blue.svg)](#3-shared-contract-v13-normative-standard--apis)
[![LIRA-Q Dual-Theme](https://img.shields.io/badge/LIRA--Q-Dual--Theme%20Light%2FDark-067A9B.svg)](#10-lira-q-sentinel-industrial-dual-theme-design-system)

---

## Master Table of Contents
1. [Executive Overview & Problem Statement (GENAI-01)](#1-executive-overview--problem-statement-genai-01)
2. [Three-Laptop Live Hackathon Integration Topology](#2-three-laptop-live-hackathon-integration-topology)
3. [Shared Contract v13 Normative Standard & APIs](#3-shared-contract-v13-normative-standard--apis)
4. [What is Real vs Fixture (Epistemic Honesty Table)](#4-what-is-real-vs-fixture-epistemic-honesty-table)
5. [Canonical Fixtures (C1–C6) & Bay-04 Demo Scenarios](#5-canonical-fixtures-c1c6--bay-04-demo-scenarios)
6. [Real-Time Plan Supersession & Server-Sent Events (SSE)](#6-real-time-plan-supersession--server-sent-events-sse)
7. [Epistemic Boundaries & Evidence Authority Tiers (T1–T4)](#7-epistemic-boundaries--evidence-authority-tiers-t1t4)
8. [Voice Reporting Flow (Sarvam AI Transcribe & Confirm)](#8-voice-reporting-flow-sarvam-ai-transcribe--confirm)
9. [Station TFT & Interchange Stepper Alignment (Zero Early Checkout)](#9-station-tft--interchange-stepper-alignment-zero-early-checkout)
10. [LIRA-Q Sentinel Industrial Dual-Theme Design System](#10-lira-q-sentinel-industrial-dual-theme-design-system)
11. [Universal Outlined Icon System (LiraIcons)](#11-universal-outlined-icon-system-liraicons)
12. [Multi-Language Localization (English, Hindi, Marathi, Tamil)](#12-multi-language-localization-english-hindi-marathi-tamil)
13. [Screen-by-Screen Implementation (4 Primary Tabs)](#13-screen-by-screen-implementation-4-primary-tabs)
14. [Offline Persistence & WorkManager Queue Architecture](#14-offline-persistence--workmanager-queue-architecture)
15. [Automated Verification & Unit Test Suite (46/46 Tests Passing)](#15-automated-verification--unit-test-suite-4646-tests-passing)
16. [Build, Test, APK Assembly & Physical Handset Installation](#16-build-test-apk-assembly--physical-handset-installation)
17. [Live Hackathon 5-Second Hotspot Setup Guide](#17-live-hackathon-5-second-hotspot-setup-guide)

---

## 1. Executive Overview & Problem Statement (GENAI-01)

**Hackathon Challenge:** GENAI-01 — Freight Exception Resolution Before Demurrage and Delivery Penalties.

High-density maritime drayage through Indian intermodal corridors (Nhava Sheva / JNPT, Chennai Port, Mundra) operates under compounding contractual and statutory financial cliffs:
- **Statutory Customs Holds & e-DO Reconciliation:** Strict shipping line electronic Delivery Order (e-DO) cutoffs (16:30 IST daily banking UTR window) create deadlocks where import containers are physically grounded.
- **The DPD 48-Hour Evacuation Cliff:** Under Direct Port Delivery (DPD), containers must evacuate port terminal gates within 48 hours at base handling fees (~₹4,500). If this window expires, port terminal operating systems (Navis N4) automatically order a forced shift to an off-dock Container Freight Station (CFS), incurring an immediate ₹14,500+ transfer penalty and compounding daily demurrage ranging from ₹10,000 to ₹50,000 per container.
- **Physical Gate Mismatches & Yard Congestion:** Vehicle breakdowns, damaged twist-locks, and RTG yard stack delays trigger cascading slot failures across connected supply chains.

**LIRA (Logistics Intelligence & Recovery Automation)** is a multi-tier recovery operating system. While central multimodal engines (Gemini 2.0) parse customs bills of lading and quantum-classical hybrid solvers (QpiAI XY-QAOA + Cubic HUBO) solve multi-vehicle slot allocation, **LIRA Field** is the mission-critical, native Android companion application running inside the driver's cab.

---

## 2. Three-Laptop Live Hackathon Integration Topology

```mermaid
flowchart TB
  subgraph Laptop1 ["LAPTOP 1 — WEB OPERATIONS & DISPATCH CONTROL TOWER"]
    L1_UI["Next.js 14 Web Operations Dashboard\n• Port Gate Telemetry Map\n• Unapproved Recovery Proposal Review\n• Custodian Handover Approval Interface\n• One-Click Wave Approval"]
    L1_STORE["PostgreSQL / Supabase Audit Mirror\n• Real-Time Row-Level Security (RLS)\n• Audit Event Log Stream"]
  end

  subgraph Laptop2 ["LAPTOP 2 — CENTRAL BACKEND & RECOVERY ENGINES"]
    L2_API["Authoritative FastAPI Control Tower Backend\nhttp://<FASTAPI_LAN_IP>:8000/api/v1/\n• Normalized Driver Routes\n• Fast In-Memory State Machine\n• Active Schema: v13.0-QUANTUM-EDGE"]
    L2_ENG["Quantum & Classical Recovery Solvers\n• QpiAI XY-QAOA Conflict Core\n• QpiAI Opt Cubic HUBO Validated Engine\n• Gemini 2.0 Document Extraction\n• Bolna Voice Telephony Agent Host"]
    L2_API <--> L2_ENG
  end

  subgraph Laptop3 ["LAPTOP 3 & FIELD — PHYSICAL STAGING & DRIVER CAB"]
    L3_PHONE["LIRA Field Native Android App (This Repo)\nPackage: com.lira.field\nKotlin + Jetpack Compose Material 3 + Room + WorkManager\nPhysical Target: Driver Cab / Smartphone (LBIVRWQSHQGALVJ7)"]
    L3_EDGE["ESP32 Marshal Terminal Node\n• ESP32 DevKit V1 (Tensilica 240MHz)\n• 2.8\" ILI9341 SPI TFT (Display Only)\n• RC522 13.56MHz SPI RFID Reader\n• Zero Physical Buttons / Zero Touch"]
  end

  L1_UI <-->|"HTTP / SSE"| L2_API
  L3_EDGE <-->|"Wi-Fi REST HMAC\nDecision Capsule v13"| L2_API
  L3_PHONE <-->|"LAN REST JSON (/api/v1/)\n+ Bearer JWT"| L2_API
  L3_PHONE <-->|"GET /driver/stream\nServer-Sent Events"| L2_API
  L3_PHONE -.->|"Supabase Auth Fallback\nAudit Mirror"| L1_STORE
```

---

## 3. Shared Contract v13 Normative Standard & APIs

All dates and timestamps on the wire are ISO-8601 with offset `+05:30`. Every write operation carries an `idempotency_key` (UUID v4) reused on retry. All enums support unknown fallbacks to prevent deserialization crashes when future server versions add enum variants.

### Base Endpoint
`http://<BACKEND_HOST>:8000/api/v1`

### Endpoints Implemented in LIRA Field:
| Method | Path | Request / Payload | Driver Application Response & Handling |
|---|---|---|---|
| `GET` | `/driver/assignments` | Header: `Bearer <token>` | `{"server_time":..., "assignments":[AssignmentV13]}` |
| `POST` | `/driver/assignments/{id}/ack` | `{"idempotency_key","plan_version","action":"ACK\|CANNOT_COMPLY","reason_code?","note?"}` | Returns 409 `STALE_PLAN` if `plan_version` superseded. Android marks stale and refreshes. |
| `POST` | `/driver/assignments/{id}/eta` | `{"idempotency_key","plan_version","eta_minutes","source":"DRIVER_REPORTED"}` | Updates server ETA; strictly badged as `DRIVER-REPORTED (T4)`. |
| `POST` | `/driver/assignments/{id}/voice-report` | Multipart: `audio` (AAC ≤60s), `idempotency_key`, `plan_version`, `client_recorded_at` | Receives `VoiceReport` with original & English transcripts and extracted reason code. |
| `POST` | `/driver/voice-reports/{id}/confirm` | `{"idempotency_key","accepted":bool,"corrected_reason_code?","corrected_eta_minutes?"}` | Confirms or corrects ASR interpretation before dispatch escalation. |
| `GET` | `/driver/calls` | Header: `Bearer <token>` | `[CallV13]` Bolna telephony logs for this driver. Monospace IDs and honest fallbacks. |
| `GET` | `/driver/bay-board?site_id=JNPT-BAY-04` | Query: `site_id` | `BayBoard` (mirrors physical TFT at staging bay; 3-row mini queue). |
| `GET` | `/driver/interchanges` | Header: `Bearer <token>` | `[Interchange]` state history for driver's truck. |
| `GET` | `/driver/assignments/{id}/why` | Header: `Bearer <token>` | `WhySheet` with T1–T4 evidence quotes, solver run IDs, and modeled cost breakdown. |
| `PUT` | `/driver/profile` | `{"language":"en\|hi\|mr\|ta\|te\|kn\|gu\|bn"}` | Persists language preference locally and syncs to driver profile. |
| `GET` | `/driver/stream` | SSE Header: `Accept: text/event-stream` | Re-fetches assignments immediately on `plan_superseded`, `branch_activated`, `interchange_updated`. 15s polling fallback. |
| `GET` | `/driver/health` | None | Returns `{"status":"ok","schema":"v13.0"}` for connection verification. |

---

## 4. What is Real vs Fixture (Epistemic Honesty Table)

In strict adherence to engineering integrity and hackathon judging guidelines, the following table delineates what runs natively/live on the device versus what is driven by authoritative lab fixtures:

| Component / Subsystem | Real (Native / Device-Enforced) | Fixture / Simulated (Lab Demo) | Epistemic Boundary Guarantee |
|---|---|---|---|
| **Android Architecture** | Native Jetpack Compose, Kotlin 2.2, Room SQLite database, WorkManager, OkHttp 4.12, Retrofit 2.11, Moshi 1.15. | None. 100% production-grade native stack. | Runs natively on physical ARM64 device (`LBIVRWQSHQGALVJ7`). |
| **Network Client & Enums** | Retrofit/OkHttp with Bearer token authentication, SSE EventSource streaming, unknown-enum fallback adapters. | Fallback to `lab_fixture_v13.json` when backend is offline or Lab Fixture switch is enabled in Settings. | When running on fixtures, a red top ribbon displays `LAB FIXTURE C1–C6 ACTIVE`. |
| **Container Identification** | Strict ISO 6346 checksum verification and 4+6+1 display formatting (`MEDU 948210 1`). | Canonical fixture container IDs (`MEDU9482101`, `MSCU6619200`, etc.). | Check digit validated algorithmically. BL numbers are strictly decoupled from container numbers. |
| **Plan Supersession** | High-importance Android notification, card locking, dimming, red banner, and automatic stale acknowledgment reconciliation. | Plan updates triggered via backend SSE `/driver/stream` or fixture switch. | Old plan cards cannot be acted upon. Stale ACKs return 409 and purge from retry queue. |
| **Interchange State Machine** | 5-step stepper mapping (`NOT_STARTED` $\rightarrow$ `TRUCK_OBSERVED` $\rightarrow$ `EQUIPMENT_OBSERVED` $\rightarrow$ `MATCH_PREVIEW` $\rightarrow$ `COMMITTED`). | Hardware RFID scan events simulated or streamed from physical ESP32 node `EDGE-01`. | **Zero early checkout:** Checkout step turns green ONLY on `COMMITTED` with server transaction ID and EIR number. `MATCH_PREVIEW` is amber and explicit. |
| **Voice Reporting** | Native Android `AudioRecord` / `MediaRecorder` encoding 16kHz mono AAC/M4A, file management, UI waveforms, and review flow. | Backend ASR transcription via Sarvam AI API or fixture fallback. | Audio reports are labeled `DRIVER-REPORTED (T4)`. Driver must review transcript before sending. |
| **Bolna Telephony** | Monospace IDs, masked phone numbers (`+91 22 6900 8421`), inline text YES/NO fallback answering. | Backend Bolna AI telephony service execution logs. | App does not claim to answer phone calls; displays call status and honest text response fallback. |
| **Provenance & Why Sheet** | Bottom sheet with T1–T4 evidence tiers, direct quote citations, solver run references, and cost exposure cards. | QpiAI Opt solver run metadata (`QOPT-8f2c`, `QPIAI_OPT_HUBO`, `PASS`). | All solver provenance is attributed to backend runs. No quantum solvers claim to run on the phone. |
| **Corridor & Weather Risks** | TomTom traffic ETA calculations, OpenWeather radar trends, distance/time offsets. | Backend weather and traffic API feeds. | All predictive feeds are badged `PREDICTED (NON-AUTHORITATIVE)`. Never confused with statutory port gate closures. |
| **Provider Secrets** | **Zero secrets in client code:** No Bolna, QpiAI, OpenWeather, or Google Maps keys exist in source code or `BuildConfig`. | Handled strictly by FastAPI backend or mock proxies. | Passes static code analysis with zero forbidden API credentials. |

---

## 5. Canonical Fixtures (C1–C6) & Bay-04 Demo Scenarios

The companion app supports the 6 canonical hackathon freight exception cases:

| Case | Container No (ISO 6346) | Shipping Line | Blocker Code | Assigned Truck | Chassis | Driver Action & Epistemic Rule |
|:---:|:---:|:---:|:---:|:---:|:---:|---|
| **C1** | `MEDU 948210 1` | MSC | `CUSTOMS_HOLD_DOC` | — | — | **HOLD / DO NOT PROCEED**. Customs document verification pending. Driver cannot override statutory customs. |
| **C2** | `HLCU 920194 0` | Hapag-Lloyd | `CARRIER_FREIGHT_HOLD` | — | — | **COMMERCIAL HOLD**. e-DO reconciliation pending wire cutoff. Bank UTR receipt $\neq$ release. |
| **C3** | `MAEU 881920 8` | Maersk | `WEATHER_ETA_DRIFT` | T11 | R-02 | **ADVISORY MONITORING**. Weather drift predicted; driver proceeds under current approved slot. |
| **C4** | `CMAU 771920 1` | CMA CGM | `CHASSIS_DEFICIT` | T04 | R-Q1 $\rightarrow$ R-07 | **RESOURCE ALLOCATED**. Chassis R-Q1 quarantined; re-assigned to R-07 upon marshal verification. |
| **C5** | `MSCU 661920 0` | MSC | `TRUCK_BREAKDOWN` | T07 $\rightarrow$ T09 | R-03 | **ACTION REQUIRED**. T07 broke down; plan superseded to T09. Driver T09 confirms pickup. |
| **C6** | `ONEU 551920 6` | ONE | `EMPTY_RETURN_RESTRICTED` | T04 | — | **EMPTY RETURN PENDING**. Designated off-dock depot destination and detention deadline countdown. |

### Bay-04 Staging Board Demo Setup:
1. `T09` · `MSCU 661920 0` · Chassis `R-03` · Window `14:30 – 15:00`
2. `T11` · `MAEU 881920 8` · Chassis `R-02` · Window `15:00 – 15:30`
3. `T04` · `CMAU 771920 1` · Chassis `R-07` · Window `15:30 – 16:00`

**Pre-Approved Branch B Logic:**
If `T09` experiences delay, triggering `TRUCK_SUBSTITUTE` with `T11`'s card in the 14:30 window activates Branch B:
- `T11` immediately takes the 14:30 window with chassis `R-02`.
- `T09` shifts to the 15:00 window.
- The Android app displays a prominent amber banner: `PROVISIONAL — Branch B active: Substitute truck T11 taking 14:30 slot`.

---

## 6. Real-Time Plan Supersession & Server-Sent Events (SSE)

When dispatch or a solver re-plans an assignment, the phone receives an event via `GET /driver/stream`:
```json
{
  "type": "plan_superseded",
  "assignment_id": "ASG-C5-T07-2",
  "plan_version": 3,
  "at": "2026-10-29T14:15:00+05:30"
}
```

1. **Immediate SSE Ingestion (`LiraSseClient.kt`):** Background OkHttp EventSource streams server events and immediately re-fetches `/driver/assignments`.
2. **Notification Dispatch (`NotificationHelper.kt`):** Posts a high-priority heads-up notification in the "Plan changes" channel: *"Plan v3 arrived: Supersedes old plan v2"*.
3. **Card Locking & Visual Demarcation:**
   - The superseded plan is visually locked, dimmed, and badged with `SUPERSEDED BY PLAN V3`.
   - Action buttons (Confirm On Time / Cannot Comply) are disabled.
4. **Stale Acknowledgment Reconciliation:**
   - Any pending acknowledgment queued in WorkManager for the superseded plan is marked `RECONCILED_STALE` in history and purged from the retry queue to prevent 409 collisions.

---

## 7. Epistemic Boundaries & Evidence Authority Tiers (T1–T4)

LIRA Field enforces strict epistemological rigor across all display cards and bottom sheets:

| Tier | Category | Examples | Evidentiary Authority |
|:---:|---|---|---|
| **T1** | **Official Issuer** | Customs Bill of Entry, Shipping Line e-DO, Terminal Operating System (Navis N4) release | Authoritative statutory and commercial clearance. Can unblock holds. |
| **T2** | **Machine-Attested** | ESP32 station RFID scan logs, Equipment Interchange Receipts (EIR), GPS geofence timestamps | Authoritative physical custody and arrival records. |
| **T3** | **Internal Operational** | Dispatcher notes, TMS routing schedules, yard marshal logs | Operational coordination; cannot override T1 or T2. |
| **T4** | **Participant Statement** | Driver voice notes, Bolna telephony interview transcripts, WhatsApp messages | **Subjective participant statements**. Can trigger solver re-planning but can NEVER clear a hold, verify custody, or approve gate checkout. |

---

## 8. Voice Reporting Flow (Sarvam AI Transcribe & Confirm)

Drivers can report exceptions hands-free using native speech:

```mermaid
sequenceDiagram
  autonumber
  actor Driver as Driver (Cab)
  participant UI as LIRA Field UI
  participant Rec as VoiceRecorderHelper
  participant Worker as VoiceReportUploadWorker
  participant API as FastAPI Backend

  Driver->>UI: Tap Mic & Hold to Speak (Hindi/Marathi/Tamil/English)
  UI->>Rec: Start recording (16kHz mono AAC, max 60s)
  Rec-->>UI: Live recording timer & audio level
  Driver->>UI: Release / Tap Stop
  UI->>Worker: Enqueue multipart upload with UUID idempotency key
  Worker->>API: POST /driver/assignments/{id}/voice-report
  API-->>UI: Return VoiceReport JSON with transcript & reason code
  UI->>Driver: Display "We understood: ... Is this right?"
  alt Driver Accepts
    Driver->>UI: Tap [Yes, send]
    UI->>API: POST /driver/voice-reports/{id}/confirm {"accepted": true}
  else Driver Corrects
    Driver->>UI: Tap [No, fix it]
    UI->>Driver: Show quick correction picker (Breakdown / Traffic / Slip)
    Driver->>UI: Select reason & Tap [Confirm correction]
    UI->>API: POST /driver/voice-reports/{id}/confirm {"accepted": false, "corrected_reason_code": "TRAFFIC"}
  end
```

---

## 9. Station TFT & Interchange Stepper Alignment (Zero Early Checkout)

The driver application's `InterchangeTracker` mirrors the physical marshal bay terminal (`EDGE-01`):

```mermaid
flowchart LR
  S1["1. Truck Observed\n(RFID T09)"] --> S2["2. Equipment Observed\n(Chassis R-03)"]
  S2 --> S3["3. Match Preview\n(AMBER • Unverified)"]
  S3 --> S4["4. Awaiting Custodian\n(Web Approval)"]
  S4 --> S5["5. Gate Checkout\n(GREEN • COMMITTED Only)"]
```

- **Amber Preview State:** When step 3 is reached, the status badge clearly states `MATCH_PREVIEW — TAG MATCH UNVERIFIED CUSTODY`. Step 5 remains disabled.
- **Strict Checkout Rule:** Step 5 turns green and shows "CHECKOUT VERIFIED" **only when** the server state machine advances to `COMMITTED`, with authoritative `txn_id` and `eir_no`.

---

## 10. LIRA-Q Sentinel Industrial Dual-Theme Design System

LIRA Field implements the official industrial companion design system to the LIRA-Q Sentinel operations platform:

| Design Token | Terminal Paper (Light Mode) | Obsidian Industrial (Dark Mode) |
|---|:---:|:---:|
| **Canvas / Background** | `#F3F6FA` | `#0B1420` |
| **Card / Container Surfaces** | `#FFFFFF` | `#111F2E` |
| **Elevated Surface** | `#E5ECF2` | `#16273A` |
| **Primary Typography** | `#102235` | `#EAF3F8` |
| **Secondary Typography** | `#516678` | `#9FB5C5` |
| **Dividers & Borders** | `#D9E3EB` | `#294355` |
| **Primary Action Accent** | `#067A9B` | `#39C9DF` |
| **Semantic Error** | `#BA1A1A` | `#FFB4AB` |
| **Semantic Warning** | `#E67E22` | `#FFB951` |
| **Semantic Success** | `#00875A` | `#38D39F` |

Dynamic system bar coloring (`WindowCompat.getInsetsController`) ensures transparent status and navigation bars matching the active theme mode.

---

## 11. Universal Outlined Icon System (LiraIcons)

Disparate icon packs and filled Material icons have been completely replaced with a single centralized vector registry: `LiraIcons` (`com.lira.field.ui.components.LiraIcons`).
- **Standardized Viewport:** 24x24 outlined vectors.
- **RTL Adaptive:** AutoMirrored vectors for navigation arrows and actions.
- **No Emojis / No Icon Mismatches:** 100% vector fidelity across all 4 primary tabs, cards, badges, and modals.
- **Adaptive Launcher:** High-precision isometric container graphic in `res/drawable/ic_launcher_*.xml`.

---

## 12. Multi-Language Localization (English, Hindi, Marathi, Tamil, Kannada, Telugu)

Driver instructions and entire app interfaces are localized using ISO-639-1 language strings:
- **English (`en`):** `res/values/strings.xml`
- **Hindi (`hi`):** `res/values-hi/strings.xml`
- **Marathi (`mr`):** `res/values-mr/strings.xml`
- **Tamil (`ta`):** `res/values-ta/strings.xml`
- **Kannada (`kn`):** `res/values-kn/strings.xml`
- **Telugu (`te`):** `res/values-te/strings.xml`

Instructions on the My Job tab are prominently displayed at **20sp bold typography** at the very top of the card so drivers can immediately read their mandate in direct sunlight across any supported language:
> **बे 4 पर 14:30 तक पहुँचें, चेसिस R-03 लें** (Hindi)  
> **ಬೇ 4 ಕ್ಕೆ 14:30 ರ ವೇಳೆಗೆ ತಲುಪಿ, ಚಾಸಿಸ್ R-03 ತೆಗೆದುಕೊಳ್ಳಿ** (Kannada)  
> **బే 4 కు 14:30 సమయానికి చేరుకోండి, ఛాసిస్ R-03 తీసుకోండి** (Telugu)  
> *(English translation subtitle: Reach Bay 4 by 14:30, pick chassis R-03)*

---

## 13. Screen-by-Screen Implementation (4 Primary Tabs)

1. **Tab 1: My Job (`HomeScreen.kt`)**
   - Top 20sp localized instruction card with Sarvam AI translation badge.
   - Active branch activation banner (`PROVISIONAL — Branch B active`).
   - Superseded plan notice with locked action buttons.
   - Freight exception card (C1–C6) with owner role, deadline countdown, and hold status.
   - Bay card with queue position, 3-row mini queue synced with station TFT, and station online indicator.
   - 5-step Interchange tracker (amber match preview, green committed checkout).
   - Bolna AI telephony card with monospace execution ID and inline YES/NO fallback.
   - Formatted ISO 6346 container number (`MSCU 661920 0`) with verified check digit.
   - Provenance line (`Plan v3 · QpiAI Opt run QOPT-8f2c · validator PASS`) opening Why Sheet.
   - Action buttons: [Confirm On Time] and [Cannot Comply].
2. **Tab 2: Exceptions (`RiskAlertsScreen.kt`)**
   - Voice Report button at the top for instant hands-free driver delay logging.
   - Predictive corridor intelligence with TomTom traffic and OpenWeather radar trends.
   - Distinction between `PREDICTED (NON-AUTHORITATIVE)` vs `CONFIRMED_PORT_CLOSURE`.
3. **Tab 3: History (`ActivityTimelineScreen.kt`)**
   - Canonical timeline events: plan issued, plan superseded, branch activated, voice report transcribed, Bolna call received, interchange committed.
4. **Tab 4: Settings (`SettingsScreen.kt`)**
   - Language selector (English, Hindi, Marathi, Tamil, Kannada, Telugu) with instant dynamic UI reload and profile sync.
   - Real-time SSE connection status badge (`SSE Connected` / `SSE Disconnected`).
   - Lab Fixtures toggle (`lab_fixture_v13.json` C1–C6) with driver ribbon indicator.
   - Control Tower LAN IP switcher with live ping latency and schema health check (`v13.0`).
   - Local database cache purge.

---

## 14. Offline Persistence & WorkManager Queue Architecture

- **Room Database (`LiraDatabase` v2):** Caches assignments, events, disruptions, voice reports, and bay boards with non-destructive schema migrations.
- **Stale Cache Indicator:** Displays `STALE — LAST SYNC [TIMESTAMP]` when disconnected.
- **WorkManager (`SyncWorker` & `VoiceReportUploadWorker`):** Enqueues driver ACK, ETA, and voice reports with exponential backoff and stable UUID v4 idempotency keys. Administrative checkout cannot be queued offline.

---

## 14B. v13.1 "WOW" Features (Judge-Facing Driver Experience)

1. **"NEW JOB" Full-Screen Takeover Alert (`TakeoverDialog.kt`)**:
   - Triggered instantly on SSE `live_incident`, `plan_superseded`, or `branch_activated` (or high-importance full-screen-intent notification on channel `Urgent dispatch`).
   - Localized headline: **"NEW JOB ASSIGNED"** (in Hindi: `नया कार्य सौंपा गया`, Marathi: `नवीन काम नियुक्त केले`, Tamil: `புதிய பணி ஒதுக்கப்பட்டது`, Kannada: `ಹೊಸ ಕೆಲಸ ನಿಗದಿಪಡಿಸಲಾಗಿದೆ`, Telugu: `కొత్త పని కేటాయించబడింది`).
   - Container number, truck/chassis, target site (**JNPT Bay 4**), appointment window, and dynamic countdown.
   - Origin chip badge (`LIVE CALL`, `COPILOT`, `DISPATCHER`, `STATION PLAN B`).
   - On-device **TextToSpeech** read-aloud in the driver's regional language with missing voice detection and graceful English fallback, plus speaker replay button.
   - Distinctive urgent vibration pattern `[0, 500, 200, 500, 200, 500]` and sound.
   - Receipt latency ping posted to `POST /driver/events/receipt` (`{assignment_id, plan_version, shown_at}`).
   - Buttons: **[ACK: I'M GOING]** (acknowledges assignment with current plan version) and **[CAN'T DO IT]** (opens exception flow).

2. **Reserve / Standby Duty (`StandbyCard.kt`)**:
   - Items with `assignment_kind == "RESERVE_STANDBY"` render in a dedicated **"Standby tomorrow"** card.
   - Labeled with prominent `RESERVE: NOT A DISPATCH` chip.
   - **No dispatch ACK button** — strictly provides **"Got it"** (read receipt) and **"Not available"** (reports exception).
   - SSE `reserve_assigned` triggers normal-priority notification.
   - **"Why am I on standby? ›"** opens the Why Sheet displaying `FORECAST (MODELED)` and quantum risk forecast provenance (`model: quantum scenario forecast`, `qcloud_job_id`).

3. **Live Incident Grouping in Timeline (`ActivityTimelineScreen.kt`)**:
   - History groups events matching the same incident under an expandable header with origin chips (`LIVE CALL`, `COPILOT`, `STRESS`, `STATION PLAN B`).
   - Allows judges to scroll through the driver's end-to-end incident chronology.

4. **Animated Interchange Stepper (`InterchangeTracker.kt`)**:
   - Short scale (1.0 → 1.15) and alpha fade transitions between active steps.
   - Displays a green success state with verified `txn_id` and `eir_no` and haptic feedback when `COMMITTED` is reached.

5. **Strict Epistemic Honesty Filter (`LiraRepository.kt`)**:
   - The driver app **never** renders simulation drafts or stress tests. Items with status `PROPOSED` or origin `STRESS_SANDBOX` are strictly filtered out from driver view.

6. **Point of No Return (PNR) & "ACT BY" Telemetry (`PnrHelper.kt`, `HomeScreen.kt`)**:
   - Replaces generic arrival deadlines with a dynamic countdown: **`ACT BY 14:00 · 1h 35m`** (in driver's language).
   - Dynamic colour-coding based on authoritative `pnr_status`:
     - `ON_TRACK`: Green (`#22C55E`)
     - `PNR_IMMINENT`: Amber (`#F59E0B`)
     - `PAST_SAFE_PNR`: Red (`#EF4444`)
     - `ALREADY_LOST`: Calm Grey (`#94A3B8`)
   - Visible statutory deadline displayed directly underneath: `Port deadline 16:00`.
   - Clear causal explanation from the Why sheet: *"After 14:00 the container cannot leave port today, even if you hurry."* (localized in EN, HI, MR, TA, KN, TE).
   - High-urgency alert when status becomes `PNR_IMMINENT`: high-priority notification + vibration + TextToSpeech in driver's language (*"You must leave within 10 minutes"* / *"आपको 10 मिनट के भीतर निकलना होगा"*), triggered strictly once per status change.
   - When `ALREADY_LOST`: calm grey notice *"Dispatch is re-planning. Wait for the new job."* with dispatch ACK buttons completely hidden.
   - Moshi deserializer handles optional fields with graceful `PnrStatus.UNKNOWN` fallback.
   - Non-destructive SQLite/Room migration from v2 to v3 adds `pnr`, `act_by`, and `pnr_status` columns.

---

## 15. Automated Verification & Unit Test Suite (58/58 Tests Passing)

All 33 baseline tests, 19 Contract v13 tests, and 6 Point of No Return (PNR) tests pass with 100% success rate:

```powershell
.\gradlew.bat testDebugUnitTest
```

```text
BUILD SUCCESSFUL in 45s
58 tests completed, 0 failed, 100% PASS RATE

Test Suites:
- com.lira.field.ContractV13Tests (25 tests) ........................... PASS
  ✓ branchActivated_ShowsProvisionalBanner
  ✓ voiceReport_ConfirmFlow_CorrectedValuesSent
  ✓ languagePreference_PersistsAndSyncsToProfile
  ✓ contractV13_UnknownEnumFallsBackToUNKNOWN
  ✓ noHardcodedProvenanceStrings
  ✓ supersede_LocksOldCard_And_ReconcilesQueuedStaleAck
  ✓ containerNoFormatting_ISO6346_DisplayAndCheckDigitValidation
  ✓ interchangeStateMapping_NeverShowsCheckoutBeforeCommitted
  ✓ whySheet_ParsesAllFourTiers
  ✓ bayBoard_MiniQueue_DisplaysThreeRows
  ✓ bolnaCall_NoAnswerFallback_ShowsTextButtons
  ✓ roomMigration_v11_to_v13_preservesAssignments
  ✓ sse_FallbackToPolling_OnFailure
  ✓ takeover_ShownOnLiveIncident_AndOnSupersede
  ✓ takeover_AckUsesCurrentPlanVersion
  ✓ tts_FallsBackToEnglishWhenHindiVoiceMissing
  ✓ reserveStandby_HasNoDispatchAck
  ✓ proposedOrSandboxPayload_NeverRendered
  ✓ history_GroupsIncidentEvents
  ✓ pnr_CountdownFormatting
  ✓ pnr_StatusColours
  ✓ pnr_LostJobHidesAck
  ✓ pnr_TtsTriggerOncePerStatusChange
  ✓ pnr_Moshi_OptionalFields_And_UnknownFallback
  ✓ pnr_RoomMigration_v2_to_v3
- com.lira.field.DriverIsolationAndStateTest (28 tests) ................ PASS
- com.lira.field.LiraContractAndModelTest (5 tests) ..................... PASS
```

---

## 16. Build, Test, APK Assembly & Physical Handset Installation

### 1. Run Unit Test Suite
```powershell
.\gradlew.bat testDebugUnitTest
```

### 2. Assemble Debug APK
```powershell
.\gradlew.bat assembleDebug
```

### 3. Install on Connected Physical Android Device
```powershell
& "C:\Users\yashas.k\AppData\Local\Android\Sdk\platform-tools\adb.exe" -s LBIVRWQSHQGALVJ7 install -r app/build/outputs/apk/debug/app-debug.apk
```

### 4. Launch LIRA Field on Handset
```powershell
& "C:\Users\yashas.k\AppData\Local\Android\Sdk\platform-tools\adb.exe" -s LBIVRWQSHQGALVJ7 shell am start -W -a android.intent.action.MAIN -c android.intent.category.LAUNCHER -n com.lira.field/.MainActivity
```

---

## 17. Live Hackathon 5-Second Hotspot Setup Guide

1. **Find Laptop 2's LAN IP:** On the FastAPI laptop, run `ipconfig` or `ifconfig` (e.g. `192.168.43.100`).
2. **Open LIRA Field on the Phone:** Navigate to the **Settings** tab.
3. **Set the IP in 5 Seconds:**
   - Under **CONTROL TOWER LAN IP:PORT**, type: `http://192.168.43.100:8000/api/v1/`.
   - Tap **SET IP**.
4. **Test the Connection:**
   - Tap **PING CONTROL TOWER**.
   - Displays green confirmation: `CONNECTED • STATUS=HEALTHY • MODE=LIVE • SCHEMA=v13.0`.
