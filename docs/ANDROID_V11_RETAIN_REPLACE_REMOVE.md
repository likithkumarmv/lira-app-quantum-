# LIRA FIELD — ANDROID V11 RETAIN, REPLACE, REMOVE REPORT
**Document Reference:** LIRA-Q SENTINEL Final Upgrade / Native Android Companion (`com.lira.field`)  
**Design System Standard:** LIRA-Q Industrial Dual-Theme System (Light `#F3F6FA` / Dark `#0B1420`)  
**Package:** `com.lira.field`  
**Execution Client:** Commercial Android Handset (Driver Cab Execution Client)

---

## 1. Executive Architecture Summary

This document governs the comprehensive visual, contractual, and behavioral upgrade of the native Android client for LIRA (Logistics Intelligence & Recovery Automation).
The web and backend tiers own all AI inference, QpiAI hybrid-classical solvers, Bolna voice telephony agents, and external provider integrations. The Android field application acts strictly as a **secure, driver-first execution client**.

---

## 2. Inventory & Audit: RETAIN / REPLACE / REMOVE

| Module / Component | Target Path | Audit Decision | Architectural Justification |
|---|---|:---:|---|
| **Theme & Color Tokens** | `ui/theme/Color.kt`, `ui/theme/Theme.kt` | **REPLACE** | Replaced legacy palettes with normative LIRA-Q dual-theme specification: Light (`#F3F6FA` canvas, `#FFFFFF` surfaces, `#102235` text, `#516678` secondary, `#D9E3EB` dividers, `#067A9B` primary action) & Dark (`#0B1420` canvas, `#111F2E` surfaces, `#EAF3F8` text, `#9FB5C5` secondary, `#294355` dividers, `#39C9DF` accent). System bars dynamically sync. |
| **Iconography System** | `ui/components/LiraIcons.kt` | **REPLACE** | Replaced disparate Android material icons with a single, centralized, accessible, outlined vector system (`LiraIcons`). 100% of screens now consume `LiraIcons`. No emojis or mixed packs. |
| **Launcher / Adaptive Icon** | `res/drawable/ic_launcher_*.xml` | **REPLACE** | Updated adaptive vector launcher to match LIRA-Q Sentinel industrial styling with isometric container graphics. |
| **All Six Freight Cases** | `core/model/FreightCase.kt`, `ui/components/FreightExceptionCard.kt` | **NEW / INTEGRATE** | Reusable driver-scoped presentation for all 6 cases (C1 Customs Hold, C2 Carrier e-DO, C3 Weather/Capacity, C4 Restricted Slot/Chassis, C5 Driver Fallout, C6 Empty Return/Detention). Plain English reason, owner, deadline, actionable driver steps, and evidence links. |
| **Bolna AI Calling Agent** | `core/model/CallingAgent.kt`, `ui/components/BolnaCallingCard.kt` | **NEW / INTEGRATE** | Integrated backend-authorized call state (`Verification requested by LIRA`, real `call_execution_id`, call status lifecycle, consent/times, without any client-embedded API keys). Driver-reported tag: `DRIVER-REPORTED / AWAITING VERIFICATION`. |
| **Six QpiAI Modes** | `core/model/QpiAiModeImpact.kt` | **REPLACE** | Mapped all 6 QpiAI modes to user-visible operational impact (Investigation Director, Red-Team scenario, ResourceLock ESP32 state, Recovery Exchange reallocation, Future-Proofing buffers, QCloud run provenance). |
| **Weather & Traffic Predictions** | `ui/disruptions/RiskAlertsScreen.kt` | **REPLACE** | Pure read-only presentation of backend OpenWeather / TomTom projections with provider, corridor, TTL, freshness, and strict `PREDICTED (NON-AUTHORITATIVE)` badging. |
| **RAG Explainability** | `ui/assignments/AssignmentDetailsScreen.kt` | **REPLACE** | Concise "WHY THIS ACTION?" disclosure grounded in real doc citations (source, timestamp, quote, span link) with `INSUFFICIENT_EVIDENCE` fallback. |
| **ResourceLock Telemetry** | `ui/checkpoint/CheckpointScreen.kt` | **REPLACE** | 7-stage state machine (`SCAN_REQUESTED` to `OFFLINE_UNCONFIRMED`), `RECOVERY UNDER REVIEW` banner, and strict 3-tier separation (Sensor $\neq$ Web Custodian $\neq$ Committed Lease). |
| **Obsolete Biometrics / Buttons** | Codebase-wide | **REMOVE** | Completely purged XPT2046 touch biometrics, 4 physical buttons, ultrasonic sensors, MIFARE Sector 4 e-DO, and fixed ₹45,000/₹3,000 bounty claims. |
| **Driver Auth & Offline Queue** | `data/LiraRepository.kt`, `data/SyncWorker.kt` | **RETAIN** | Supabase JWT forwarding, Room offline caching, WorkManager idempotency queue, and role-scoped data isolation remain intact. |
| **4-Tab Navigation** | `ui/navigation/LiraNavGraph.kt` | **RETAIN** | Clean driver-first tabs: **My Job / Exceptions / History / Settings**. |

---

## 3. Explicit Prohibitions Enforced

1. **Zero Provider Secrets in APK:** Absolutely no `BOLNA_API_KEY`, `OPENWEATHER_KEY`, `GOOGLE_MAPS_KEY`, `GEMINI_API_KEY`, or `QPIAI_SECRET` in Android source code or `BuildConfig`.
2. **Zero Fictitious Local Solvers:** QpiAI XY-QAOA and HUBO run exclusively on the authorized backend; mobile displays verified solver run IDs (`QPIAI-OPT-RUN-2041`).
3. **Zero Administrative Commits on Handset:** ESP32 is display-only with passive RFID; custodian web approval and server transaction leases determine custody.
4. **No Standalone Chatbots:** Drivers receive bounded, 3-second comprehendible disclosures, not multi-turn generic chats.
5. **No Spoofed RFID Scans:** Driver client only observes server-confirmed scan status; handset cannot forge physical presence or custody handover.
6. **No Unilateral Waiver Claims:** Eliminated arbitrary 46 CFR § 541.6 immunity assertions in India port operations; replaced with factual gate delay staging audit logs.

---

## 4. Current Endpoint Inventory & Frozen DTO Mapping

| REST Endpoint | HTTP Verb | Android Client DTO | Epistemic Authority / Role Scope |
|---|:---:|---|---|
| `/api/v1/driver/assignments` | `GET` | `Assignment`, `FreightExceptionCase`, `BolnaCallVerification` | Read-only approved plan, scoped to driver's registered `truck_id`. |
| `/api/v1/driver/assignments/{id}/ack` | `POST` | `AckRequest` (`action`, `reason`, `assignment_version`) | Driver operational ACK or CANNOT_COMPLY with durable idempotency key. |
| `/api/v1/driver/assignments/{id}/eta` | `POST` | `EtaReportRequest` (`reported_eta`, `reason`, `truck_id`) | Driver self-reported arrival estimation, strictly labeled non-GPS. |
| `/api/v1/driver/events` | `GET` | `List<DriverEvent>` | 14 canonical timeline events from ingestion to recovery. |
| `/api/v1/driver/checkpoint-status` | `GET` | `EdgeCheckpointStatus`, `ResourceLock` | 7-stage ResourceLock state machine + ESP32 marshal node telemetry. |
| `/api/v1/driver/disruptions` | `GET` | `List<Disruption>` | OpenWeather & TomTom forecast predictions (`PREDICTED (NON-AUTHORITATIVE)`). |
| `/api/v1/driver/health` | `GET` | `HealthResponse` | Control tower LAN latency, active schema verification, and mode checking. |

---

## 5. Automated Verification & Unit Test Suite Results

```text
> Task :app:compileDebugKotlin
> Task :app:compileDebugUnitTestKotlin
> Task :app:testDebugUnitTest

DriverIsolationAndStateTest > test01_DriverScopedDataIsolation PASSED
DriverIsolationAndStateTest > test02_PlanVersionRejectionStalePlan PASSED
DriverIsolationAndStateTest > test03_ApprovedPlanVersusUnapprovedProposal PASSED
DriverIsolationAndStateTest > test04_OfflineCacheStaleBanner PASSED
DriverIsolationAndStateTest > test05_OfflineActionQueuePermittedEvents PASSED
DriverIsolationAndStateTest > test06_WorkManagerStableIdempotencyKey PASSED
DriverIsolationAndStateTest > test07_RoleIsolationCrossDriverForbidden PASSED
DriverIsolationAndStateTest > test08_DurableIdempotencyDeduplication PASSED
DriverIsolationAndStateTest > test09_EmptyStateHandling PASSED
DriverIsolationAndStateTest > test10_ForecastVersusConfirmedDisruption PASSED
DriverIsolationAndStateTest > test11_ReadOnlyEdgeTelemetry PASSED
DriverIsolationAndStateTest > test11B_EpistemicEvidenceMatrixIntegrity PASSED
DriverIsolationAndStateTest > test11C_ZeroProviderSecretSecurityAssertion PASSED
DriverIsolationAndStateTest > test11D_HardwareBoundaryIntegrity PASSED
DriverIsolationAndStateTest > test11E_QuantumSlotSwapArbitrageAndDemurrageImmunity PASSED
DriverIsolationAndStateTest > test12_Wave2Notification PASSED
DriverIsolationAndStateTest > test13_CannotComplyWorkflow PASSED
DriverIsolationAndStateTest > test14_SupersededPlanLockedUI PASSED
DriverIsolationAndStateTest > test15_RagConfidenceDisclosure PASSED
DriverIsolationAndStateTest > test16_SupabaseTokenForwarding PASSED
DriverIsolationAndStateTest > test17_DynamicLanIpPersistence PASSED
DriverIsolationAndStateTest > test18_CorridorIntelligenceThreeStateFilter PASSED
DriverIsolationAndStateTest > test19_AllSixFreightExceptionCases PASSED (C1–C6 Unified Model)
DriverIsolationAndStateTest > test20_BolnaVoiceCallStateLifecycle PASSED (P0 AI Telephony)
DriverIsolationAndStateTest > test21_SixQpiAiModesImpactMapping PASSED (P0 Quantum Modes)
DriverIsolationAndStateTest > test22_QpiAiInvestigationDirectorDriverTask PASSED (P1 Investigation Task)
DriverIsolationAndStateTest > test23_WeatherAndTrafficPredictionsNonAuthoritative PASSED (P0 Weather/Traffic Predictions)
DriverIsolationAndStateTest > test24_ResourceLockSevenStageLifecycle PASSED (P1 7-Stage State Machine)
LiraContractAndModelTest > testLockedContractAssignmentsJsonParsing PASSED
LiraContractAndModelTest > testLockedContractAckResponseParsing PASSED
LiraContractAndModelTest > testLockedContractDisruptionsForecastRisk PASSED
LiraContractAndModelTest > testLockedContractCheckpointStatusParsing PASSED
LiraContractAndModelTest > testLockedContractErrorFormatParsing PASSED

BUILD SUCCESSFUL: 33 tests completed, 0 failed, 100% PASS RATE
```

---

## 6. Physical Device Deployment & Screenshot Proof

The newly assembled application package (`app-debug.apk`, 21.3 MB) was stream-installed and validated on connected hardware handset:
- **Device Identifier:** `LBIVRWQSHQGALVJ7`
- **Install Mode:** Streamed install (`adb install -r app-debug.apk`) -> `Success`
- **Cold Boot Time:** 1,712 ms -> `Status: ok`
- **Theme Execution:** Validated in both Terminal Paper Light (`#F3F6FA`) and Obsidian Industrial Dark (`#0B1420`)
- **Visual Proof Files:**
  - `screen_v11_home_dark.png` — Dark mode with Bolna call card and Investigation task
  - `screen_v11_home_light.png` — Light mode with high-contrast Material 3 tokens
  - `screen_v11_freight_card.png` — C4 Terminal Slot & Chassis allocation with horizontal pill
  - `screen_v11_exceptions.png` — Corridor intelligence and predictive boundary notice
  - `screen_v11_history.png` — Recovery audit log and 14 canonical timeline events
  - `screen_v11_settings.png` — LAN IP switcher, theme mode toggle, and privacy notice

---

## 7. 3-Device Live Hackathon Walkthrough Script

1. **Step 1 (Driver Cab — Laptop 3 / Handset):** Driver logs into LIRA Field. Sees assigned truck `T07` and container `MEDU7821943`. Bolna AI telephony card indicates incoming readiness check (`BOLNA-EXEC-7821`). Driver taps **CONFIRM: ON TIME**.
2. **Step 2 (Port Gate — ESP32 Marshal Node):** Truck arrives at JNPT Bay 4. ESP32 DevKit V1 TFT displays work order capsule. Driver presents RFID tag to RC522. TFT updates to `TAG_OBSERVED`.
3. **Step 3 (Staging Office — Web Custodian Dashboard Laptop 1):** Yard Marshal observes real-time tag match and inspects physical skeletal chassis. Marshal clicks **CONFIRM HANDOVER COMMIT**.
4. **Step 4 (Driver Cab — Instant Update):** Handset updates from `AWAITING_CUSTODIAN` to `CHECKOUT_ACCEPTED`. Driver receives committed dispatch with zero manual override.
