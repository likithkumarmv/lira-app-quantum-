# LIRA FIELD — ARCHITECTURAL AUDIT: KEEP / CHANGE / REMOVE REPORT
**Document Reference:** LIRA Final Master PRD v9.0 + Shared Integration Contract Addendum  
**Package:** `com.lira.field`  
**Target Hardware:** ESP32 DevKit V1 + ILI9341 320x240 SPI TFT (DISPLAY ONLY) + RC522 RFID  

---

## 1. Executive Summary

This report maps the incremental upgrade from LIRA Field v7.0/v9.0 into the **authoritative driver execution operating system**. All changes strictly preserve existing robust modules (Jetpack Compose, Room, WorkManager, auth isolation, and test suite) while purging obsolete hardware and sensor claims, integrating server-side weather/traffic predictions, adding explainable RAG citations, incorporating driver-facing QpiAI Investigation Director tasks, and enforcing honest ResourceLock state distinctions.

---

## 2. File-by-File Mapping: KEEP / CHANGE / REMOVE

| Real File Path | Module / Feature | Audit Verdict | Architectural Justification |
|---|---|:---:|---|
| `app/build.gradle.kts` | Build system & dependencies | **KEEP** | Standard dependencies (Compose, Material 3, Retrofit, Moshi, Room, WorkManager) are stable and healthy. |
| `core/network/AuthInterceptor.kt` | Header & JWT injection | **KEEP** | Preserved clean `Authorization: Bearer <token>` and `Idempotency-Key` headers for FastAPI `/api/v1/driver/*`. |
| `core/network/ApiService.kt` | Retrofit endpoint contracts | **CHANGE** | Add typed endpoints for driver disruption projections, RAG explainability details, and investigation tasks. |
| `core/session/SessionRepository.kt` | Session & LAN IP persistence | **KEEP** | Dynamic hotspot IP switching and secure token storage working as designed. |
| `core/database/LiraDatabase.kt` | Room SQLite persistence | **KEEP** | Offline table storage for assignments and pending WorkManager queue is proven and resilient. |
| `data/SyncWorker.kt` | Background sync worker | **KEEP** | Idempotency-based retry engine for offline driver actions remains intact. |
| `core/model/Checkpoint.kt` | Checkpoint & ResourceLock DTOs | **CHANGE / REMOVE** | **REMOVE:** Live use of `XPT2046_Z_PRESSURE`, 48 touch strokes, MIFARE Sector 4 EEPROM as customs e-DO, silicon anti-clone ratchets, 1420µs on-chip QAOA computing.<br>**CHANGE:** Add 7 authoritative ResourceLock lifecycle states (`SCAN_REQUESTED`, `TAG_OBSERVED`, `AWAITING_CUSTODIAN_CONFIRMATION`, `CHECKOUT_ACCEPTED`, `REJECTED`, `QUARANTINED`, `OFFLINE_UNCONFIRMED`). |
| `ui/checkpoint/CheckpointScreen.kt` | Hardware status screen | **CHANGE** | Rename screen presentation to **ResourceLock Status**. Replace 4 Hardware WOW cards with an honest ResourceLock panel distinguishing RFID scan (T3) vs Web custodian confirmation (T4) vs Statutory customs release (T1). |
| `core/model/Assignment.kt` | Assignment & Financial DTOs | **CHANGE / REMOVE** | **REMOVE:** Fixed ungrounded ₹45,000 / ₹3,000 bounty auto-guarantee and automatic 46 CFR fee immunity.<br>**CHANGE:** Add `RagExplainabilityDisclosure` (Why This Action, exact quote, span reference), `QpiAiInvestigationTask`, `RobustRecoverySummary`, and labeled monetary exposure (`MODELED ESTIMATE` vs `ACTUAL INVOICE` vs `UNKNOWN COST`). |
| `ui/assignments/AssignmentDetailsScreen.kt` | Assignment details UI | **CHANGE** | Add concise **"WHY THIS ACTION?"** RAG disclosure card with trusted evidence title, timestamp, and exact quote. Add optional `PLAN RISK` robust recovery insight. |
| `core/model/Disruption.kt` | Disruption & Risk models | **CHANGE** | Add `provider`, `data_source_mode` (`LIVE_EXTERNAL` vs `LAB_FIXTURE`), corridor, TTL, freshness seconds, predicted delay, and revised ETA. |
| `ui/disruptions/RiskAlertsScreen.kt` | Exceptions & Risk Radar | **CHANGE** | Replace demo banners with driver-scoped weather + traffic risk projection. Clearly label forecasts as **PREDICTED** (non-authoritative). Never equate weather/traffic with official port closure. |
| `ui/home/HomeScreen.kt` | My Job Command Center | **CHANGE** | Add QpiAI Investigation Director driver-scoped task card (`VERIFY_DRIVER_AVAILABILITY` / `CONFIRM_ETA`) with one-tap action. Keep Wave 2 lockout banner. |
| `ui/navigation/LiraNavGraph.kt` | Compose bottom navigation | **KEEP** | 4 primary tabs aligned with v9 baseline: **My Job / Exceptions / History / Settings**. |
| `src/test/.../DriverIsolationAndStateTest.kt` | Automated verification | **CHANGE** | Update tests to assert ResourceLock states, RAG evidence citations, weather prediction boundaries, and zero provider secrets. |

---

## 3. Explicit Prohibitions & Architectural Guardrails Enforced

1. **Zero Provider Secrets in APK:** No OpenWeather, Google Maps, Gemini, QpiAI, Bolna, or Sarvam keys embedded in Android sources or `BuildConfig`.
2. **Zero Fictitious Hardware Telemetry:** ESP32 is strictly **ILI9341 320x240 TFT (Display-Only) + RC522 RFID**. No touchscreen biometrics, no buttons, no ultrasonic sensors.
3. **No Direct Supabase Operational Mutations:** All operational writes flow via FastAPI `/api/v1/driver/*` with Bearer JWT and UUIDv4 idempotency key.
4. **No Standalone Chatbot in Driver Cab:** Drivers need fast, unambiguous 3-second comprehension; RAG is delivered as concise source-grounded disclosures, not an interactive chatbot.

