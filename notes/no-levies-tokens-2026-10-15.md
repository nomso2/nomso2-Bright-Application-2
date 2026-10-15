# No levies, compensation tokens only, User Refund, Home cleanup (2026-10-15)

Branch `hark/no-levies-compensation-tokens`, based on `origin/main` (PR #16 was already merged).

## 1. Transformer Dues / CDA levy ledger removed
- `EstateExcoAndSlaDossierDialog`: the "Transformer Dues" tab, `TransformerDuesTabContent` and the add-dues form are gone.
- `BrightViewModel`: `transformerDuesEntries` / `addTransformerDuesContribution` removed (and its reset on account delete).
- `MainActivity`: dues plumbing removed. `model/Models.kt`: `TransformerDuesEntry` removed (no other users; it was in-memory only, never in Room).
- Wording: MoreScreen subtitle no longer says "Estate dues"; GridHub card subtitle is now "NERC PDF Dossier export".

## 2. No token buying or recharging; compensation tokens only
- New `data/meter/CompensationTokens.kt`: NERC limits (24h phase fault = `PHASE_FAILURE`; 48h transformer = `BURNT_TRANSFORMER`, `TRANSFORMER_SPARKING`). Other fault types are not covered, so no token is owed for them.
  Entries are derived from the user's complaint tickets (Room `complaints`, read only). Hours late = (resolvedAt or now) − reportedAt − limit.
  kWh owed = the kWh the user typed from the DisCo-issued token, otherwise an estimate (hours late × monthly use ÷ 720, monthly use from the Refund Tracker setting, 150 kWh default) labelled "estimate".
  Status: Pending → Claimed (user marked the claim sent) → Issued (user saved the 20-digit token the DisCo issued). Claim state lives in SharedPreferences `bright_compensation_tokens`; cleared on account delete.
- New `ui/meter/CompensationTokensSection.kt`: "Compensation tokens" heading + explanation line, one card per overdue ticket (fault ref, type, hours late, kWh owed, status), Send claim / Mark claimed / Enter the token your DisCo issued / Copy / Keypad steps.
- Where it replaced token-buying UI:
  - `MeterConnectHub` section 4 "Load an STS token" (push via HES, push to DEMO meter, ₦/kWh token history) → "4. Compensation tokens".
  - `HesGatewaysPanel` per-manufacturer "STS token to push" row → one line pointing to Compensation tokens.
  - `SmartMeterServerGatewayDialog` OTA token push modal → "Compensation tokens" modal for that meter (same per-meter button).
  - `TokenEscrowClearinghouseDialog` escrow vault ₦ balance, random rebate tokens and "Trigger SLA Default Rebate (₦5,132)" → compensation section. (This dialog is no longer opened; see 4.)
  - Solution #15 "Offline Token Vending via USSD" → "Compensation tokens" (`CompensationTokensFeature`); catalog and Solutions30 text updated.
  - DisCo quick channels: removed the "Quick Energy Vend Pay" USSD code; GridHub "OTA Top-up Ready" / "Token Recharge Active" / "Recharge Amount" / "Purchase tokens" wording reworded; Refund Tracker "Units you buy" → "Units you use".
  - `generateSlaCompensationAssessment` no longer invents a random 20-digit token; letter wording no longer says "recharge"/"token vending".
- Room: no schema change, no version bump, no destructive migration. `sol_token_queue` (old offline purchase queue) and its DAO stay in place, unused. Old `bright_token_history` prefs are no longer shown and are still cleared on account delete.

## 3. Home greeting
- Header headline is now "Good morning / afternoon / evening / night, <name>" from the device clock (05:00–11:59, 12:00–16:59, 17:00–20:59, 21:00–04:59), refreshed each minute; just the greeting when the name is blank (`homeGreeting()` in HomeScreen.kt). The "THE BRIGHT PROJECT" label it replaced is gone; the DisCo • Band line stays.
- No tagline line and no "power is on" claim: the empty-state heading "Your Lights are Bright!" is now "No active reports".
  Note: main had no "Our lights are bright…" headline; the only such text was this empty-state heading.

## 4. Refunds moved out of Estate → User Refund
- Estate & Dossier now shows only the NERC PDF Dossier (tab row and SLA Refund Calculator removed).
- New `ui/components/UserRefundDialog.kt` ("User Refund"):
  (a) Band refund calculator: hours short (band-auditor log: sum of max(0, band hours − supplied) over logged days, or typed override) × average load kW (saved, `avg_load_kw`) × tariff ₦/kWh (shared `tariffPaid` setting). Live.
  (b) Late-repair compensation tokens (the shared section).
  (c) Refund claim letter built from (a) and (b), with Copy and Send.
- Every former Rebates / Refunds / Token Escrow entry opens User Refund: More row (now "User Refund"), Home `onOpenClearinghouse`, and the (unused) AuditingMatrixCard tile, relabelled "BAND REFUND DUE / Open User Refund".
  The old clearinghouse "Compile NERC Regulatory Penalty Filing" button is no longer reachable (it lived only in that dialog).

## 5. More > Tools and Home cleanup
- More > Tools now starts with Smart Meter, Estate & Dossier, User Refund, History, then the existing tools (same actions). Help stays pinned above it.
- Home: "MORE TOOLS & PROTOCOLS" card removed (the Tools grid "More" tile and bottom-nav More still go to More).
- Home: red Hazard/SOS fast-track card removed. The same three hazards (same names, same `reportQuickEmergencyHazard` logic) are now a calm "Something dangerous?" card on the Report screen with plain-language buttons; after a tap it opens History. Home Tools grid "Hazard" tile is unchanged.
- TransformerOverloadCard: was already not rendered on Home on main (only imported); the import is removed, the composable file is kept untouched.

## 6. History entry points
- Bottom nav History → `navigateToTab(HISTORY)`; Home Tools grid "History" tile → `HomeActions.onNavigateHistory` → `navigateToTab(HISTORY)`; Report submit → History. All are wired to `HistoryScreen` in code on main.
- More had no History entry; added "History" to More > Tools (→ `navigateToTab(HISTORY)`).
- Could not reproduce a dead tile without a device (no Android SDK in the sandbox); static wiring is correct on this branch.
