# DEMO.md

Shot list for the 3-5 minute screen recording, and the tooling to drive it.

---

## The four things the brief asks the recording to show

| # | Beat | Where it is |
|---|---|---|
| 1 | The page rendering from JSON | Cold open. 43 sections, none of them written in Kotlin |
| 2 | Tenure selector + bottom sheet working | Scroll to "Plan your EMI" |
| 3 | Unknown-component fallback | Scroll past the assured grid — two placeholders in a row |
| 4 | A live JSON edit with no client code change | `adb push` + pull to refresh |

---

## Fastest path: let the script drive the device

```bash
python3 tools/record_demo.py
```

It resets to a clean first-run state, starts `adb screenrecord`, drives every beat, pushes
`tools/home_live_edit.json` for the live edit, pulls the video to
`cars24_sdui_demo.mp4`, and cleans up after itself. Takes about 2m10s of device time.

**Two things it cannot do**, both of which are why you should record your own screen over the
top of it:

- **It cannot show the JSON file being edited.** `adb screenrecord` captures the device only.
  The most convincing beat in the whole recording is your editor and the phone side by side,
  and that needs desktop capture (QuickTime → New Screen Recording, or OBS).
- **It cannot narrate.** "Nothing in this page exists in Kotlin" is the sentence that makes
  the demo land, and it has to be said out loud.

The coordinates in the script are calibrated against this payload at 1080x2400. On a
different screen size, re-run the calibration block in the header comment first.

---

## If you record it yourself, this ordering works

Have the JSON open in your editor on one side, the device mirrored on the other.

**0:00 — What you are looking at (20s)**
Show `data/src/main/assets/sdui/home.json` in the editor. Scroll it. Say the count: 43
sections, 1,300 lines. Then show `SduiPageScreen.kt` next to it and point out that it is
about 120 lines and contains no mention of a car, a banner or an EMI.

**0:20 — Cold open (25s)**
Launch. The shimmer skeleton appears first, then content. Scroll the whole page slowly — the
gradient header, quick actions, banners, chips, the rail, the EMI panel, the trust strip, the
grid, the FAQs, the footer. Say: every one of those came out of the file you just showed.

**0:45 — Interactivity with no client code (30s)**
Tap Diesel → the rail changes to two diesel cars. Tap CNG → two CNG cars. Then switch to the
editor and show the `visibleWhen` block on `rail_diesel`, and the `chip_group`'s `stateKey`.
Say: the server sends every variant, each guarded by a condition, and the chip writes the key.
No round trip and no Kotlin.

**1:15 — Tenure selector and the bottom sheet (45s)**
Scroll to "Plan your EMI". Tap 36 mo → 72 mo → 48 mo, letting the figure change each time.
Then in the editor show one tenure option's chained `set_state` actions and the
`{{state.emi_monthly}}` binding on the EMI card. Say: the client does no loan arithmetic —
the server sends the answer for each tenure, so changing the interest rate is a payload edit.

Tap "See full breakdown". While the sheet is open, show that the sheet's contents are the
`content` array on the `open_bottom_sheet` action — the sheet body is itself SDUI, rendered by
the same renderer with live state bindings.

**2:00 — Graceful degradation (35s)**
Scroll to the two placeholders. Show in the editor that `ar_showroom_360` has no matching
component anywhere in Kotlin, and that `loyalty_tier_card` declares `minSchemaVersion: 5`
against a client that supports 2 — and that the second one draws the server's own `fallback`
node instead of a placeholder.

Then run `adb logcat -s Cars24Sdui` and show the two warnings. Say: these two sections are in
the production payload on purpose, so both degradation paths run on every launch rather than
only when someone remembers to test them.

**2:35 — Live edit, no rebuild (45s)**
The money shot. Keep both windows visible.

```bash
adb push tools/home_live_edit.json \
  /sdcard/Android/data/com.cars24.sdui.debug/files/sdui/home.json
```

Then pull to refresh on the device. The greeting, the search hint, a section title and an
extra banner all change. Say clearly: same APK, no rebuild, no reinstall, no Play Store.

If you prefer to show yourself editing rather than pushing a prepared file, edit
`tools/home_live_edit.json` on camera first — change the greeting to something obviously
yours — then push.

**3:20 — Optional extras, if you want to reach 4-5 minutes**
- **It generalises**: tap Buy car, Sell car, Car loan, Insurance. Four screens, four JSON
  files, zero destination code. Then show `SduiNavHost.kt` — one `composable` entry for all
  of them.
- **Shared state**: tap a heart, open Saved cars, and show the wishlist page is just cards
  gated on `visibleWhen`. Then kill the app from recents and reopen — the sheet, the tab, the
  tenure and the wishlist all come back.
- **Offline**: aeroplane mode, clear data, launch → offline screen. Re-enable, tap Try again.
- **Dark mode**: toggle the system theme and scroll. Same payload; the colours are tokens.

---

## Resetting between takes

```bash
adb shell rm -f /sdcard/Android/data/com.cars24.sdui.debug/files/sdui/home.json
adb shell pm clear com.cars24.sdui.debug
```

The first line removes the pushed override so the bundled payload is served again; the second
clears persisted state so the next launch is a true first run.
