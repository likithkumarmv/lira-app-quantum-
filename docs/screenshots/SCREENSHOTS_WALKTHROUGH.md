# LIRA Field — Screen Architecture & Visual Layouts

This document illustrates the visual hierarchy and screen designs implemented in **LIRA Field** (`com.lira.field`).

---

## Screen 1: Sign-In / Demo Role
- **Package:** `com.lira.field.ui.login`
- **Route:** `Screen.Login.route` (`"login"`)
- **Key Components:**
  - Hero Logo with Truck Icon in high-visibility cyan.
  - Notice Banner explaining track GENAI-01 demonstration role authentication.
  - Interactive profile cards:
    - `DRIVER_T07`: Primary demo driver (Plan R2 -> No-Show -> Wave 2 Plan R3).
    - `DRIVER_T04`: Conflict driver (Delayed 45 min).
    - `DRIVER_T09`: Reserve fleet backup driver.
  - Fast toggle for **Live Backend** vs **Offline Mock Fixtures**.
  - High-contrast 54dp button: `ENTER LIRA FIELD AS DRIVER_T07`.

---

## Screen 2: My Today (Home)
- **Package:** `com.lira.field.ui.home`
- **Route:** `Screen.Home.route` (`"home"`)
- **Key Components:**
  - **Demo Scenario Stage Banner:** Fast switcher for Stages 1 to 4 with interactive chips and reset button.
  - **Driver Identity Bar:** Displaying driver name, truck ID (`T07`), and quick manual refresh.
  - **Physical Check Failed WOW Card:** If edge sensor detects a no-show, immediately displays:
    ```text
    PHYSICAL CHECK FAILED
    Checkpoint event: NO_SHOW
    Previous plan is no longer active.
    New recovery is being calculated...
    ```
  - **Hero Container Card:**
    - Container ID: `C102` (32sp Bold)
    - Truck ID: `T07`
    - Confirmed Slot: `S1600 (16:00 - 16:30)`
    - Checkpoint: `LIRA-EDGE-01`
    - Avoidable Demurrage Cost: `₹12,000 INR` (Saved from penalties)
    - Reason for Change: `T04 delayed 45 min`
    - Badges: `APPROVED`, `ACK: PENDING`
    - Large Action Buttons: Big Cyan `ACKNOWLEDGE` (with double-tap locking) and Outlined Red `CANNOT COMPLY` (triggers reason picker modal).
  - **Quick Action Grid:** One-tap navigation to Update ETA, Checkpoint status, and Risk Alerts.

---

## Screen 3: Assignment Details
- **Package:** `com.lira.field.ui.assignments`
- **Route:** `Screen.AssignmentDetails.route` (`"assignment_details/{id}"`)
- **Key Components:**
  - Side-by-side comparison:
    - **Original Plan:** Truck `T04 @ 16:00` (Status: `CONFLICT`)
    - **Active Recovery:** Truck `T07 @ S1600` (Status: `APPROVED`)
  - Grounded Evidence Verification:
    - `EV-DOC-102`: Shipping Document Verification
    - `EV-CALL-04`: Driver Voice Agent Fact Check
  - Operational Constraints:
    - Customs Release: `CONFIRMED`
    - Dispatcher Approval: `CONFIRMED BY MANAGER`
    - Penalty Deadline: `2026-10-28T18:30:00+05:30`

---

## Screen 4: Update ETA
- **Package:** `com.lira.field.ui.eta`
- **Route:** `Screen.UpdateEta.route` (`"update_eta"`)
- **Key Components:**
  - Driver Self-Report Disclaimer: explicitly marked as `DRIVER_SELF_REPORT` for dynamic rescheduling.
  - Offset Quick-Select Chips: `+15 min`, `+30 min`, `+45 min`, `+60 min`.
  - Target ISO ETA display: `2026-10-28T16:45:00+05:30`.
  - Delay Reason Radio Group:
    - `TRAFFIC`: Heavy Traffic / Highway Slowdown
    - `WEATHER`: Heavy Rain / Visibility Loss
    - `BREAKDOWN`: Mechanical Issue / Tire Problem
    - `OTHER`: Port Queue / Inspection Hold
  - Large Submit Button calling `POST /api/v1/driver/eta`.

---

## Screen 5: Risk Alerts & Disruptions
- **Package:** `com.lira.field.ui.disruptions`
- **Route:** `Screen.RiskAlerts.route` (`"risk_alerts"`)
- **Key Components:**
  - Risk Intelligence Legend: Explains that `FORECAST RISK` indicates potential weather risk without gate closure.
  - Distinct Visual Cards:
    - **FORECAST RISK:** Amber card with alert badge (`HEAVY_RAIN`, Severity: `HIGH`, affected truck: `T07`).
    - **CONFIRMED DISRUPTION:** Red bordered card (`PORT_MAINTENANCE`, Severity: `MEDIUM`, affected trucks: `T07`, `T04`).

---

## Screen 6: ESP32 Checkpoint Status
- **Package:** `com.lira.field.ui.checkpoint`
- **Route:** `Screen.Checkpoint.route` (`"checkpoint/{id}"`)
- **Key Components:**
  - Hardware Boundary Card: Explains telemetry is read-only from the project owner's ESP32 + TFT edge checkpoint.
  - Physical Staging Evidence Card:
    - Displays `PHYSICAL STAGING NOT YET EVIDENCED` until hardware records sensor confirmation.
    - Transitions to `PHYSICAL STAGING EVIDENCED` once RFID tag and optical presence are validated.
  - Telemetry Metrics:
    - Bay Occupancy Sensor: `VEHICLE PRESENT IN BAY` / `BAY VACANT`
    - RFID Gate Scanner: `NOT_SCANNED` / `SCANNED_MATCH` / `SCANNED_MISMATCH`
    - Last Edge Event Timestamp.

---

## Screen 7: Activity Timeline
- **Package:** `com.lira.field.ui.activity`
- **Route:** `Screen.Activity.route` (`"activity"`)
- **Key Components:**
  - Chronological timeline cards with sequence numbers (`Seq #120`, `Seq #121`, `Seq #122`).
  - Distinct icons and colors per event type:
    - `PLAN_APPROVED` (Cyan)
    - `DRIVER_ACKNOWLEDGED` (Green)
    - `ETA_UPDATED` (Cyan)
    - `PHYSICAL_MISMATCH` (Red)
    - `RECOVERY_STARTED` (Amber)
    - `PLAN_REPLACED` (Amber)
    - `STAGING_EVIDENCED` (Green)
  - Key-value payload inspector displaying previous vs new plan IDs and blocker reasons.

---

## Screen 8: Offline & Settings
- **Package:** `com.lira.field.ui.settings`
- **Route:** `Screen.Settings.route` (`"settings"`)
- **Key Components:**
  - **Backend Server Configuration:** In-app editable base URL (e.g. `http://192.168.1.50:8000/api/v1/`).
  - **In-App Health Test:** Pings `GET /api/v1/health` and displays status, mode, and round-trip latency in milliseconds.
  - **Offline Demo Fixtures Switch:** Toggle between self-contained mock simulator and live network calls.
  - **Room Database & WorkManager Queue:**
    - Displays last successful sync timestamp.
    - Lists queued offline requests with their UUID idempotency keys.
    - **Force Reconnect & Sync Button:** Flushes queued actions immediately using `WorkManager`.
  - **Client Security & Privacy Enforcement:** Formal notice certifying zero provider secrets on client and strict driver record isolation.
  - **Logout Driver Session:** Clears session token and returns to role selection.

