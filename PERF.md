# PERF.md

SDUI versus a hardcoded build of the same screen, measured on **a physical device and an
emulator**, because the two disagree completely and that disagreement is the most useful
thing in this document.

**Headline: on a real phone there is no measurable startup cost to SDUI.** Median time to
first content was 186 ms for SDUI against 194 ms for the hardcoded build - SDUI came out
8 ms *ahead*, which is inside run-to-run noise, so the honest reading is "no difference".
JSON parsing is 8 ms of it. Scroll jank is 0.25% of frames for both builds.

**The same code on an emulator says SDUI costs +31%.** That number is an artefact. Section 1
explains why, and it is why every figure below is given twice.

## 1. Two devices, and why both are in here

| | Physical device | Emulator |
|---|---|---|
| Model | **vivo V2561**, Android 16 (SDK 36) | Pixel 6 AVD, Android 14 (SDK 34) |
| SoC | MediaTek Dimensity 9400 (MT6991) | host-emulated on Apple Silicon |
| Build | `benchmark` type: R8 minified, resources shrunk, `profileable`, debug-signed | same APK |
| Payload | `home.json` - 43 sections, 49,660 bytes | same |

I started on the emulator, wrote up +31% overhead, then got a physical device and re-ran
everything. The gap is large enough that publishing only the emulator numbers would have been
misleading:

| Metric | Device | Emulator |
|---|---|---|
| TTR overhead | **-8 ms (-4%)** | +128 ms (+31%) |
| Full page overhead | **+153 ms (+5%)** | +350 ms (+10%) |
| JSON parse | **8 ms** | 16 ms |
| Janky frames, SDUI | **0.25%** | 86% |
| Janky frames, static | **0.24%** | 96% |

The emulator janks on ~90% of frames **in both builds**. It cannot sustain the frame rate at
all, so every per-frame number it produces is measuring the emulator. Its startup numbers are
inflated the same way - emulated CPU makes a fixed amount of parsing and view-building look
like a third of the launch instead of a rounding error.

**So: trust the device column. The emulator column is here to show what a plausible-looking
wrong answer looks like**, since an emulator is what most people would have measured on.

### What is still imperfect about the device numbers

- **n is small.** 9 cold launches per variant for TTR, 5-6 for full page, 3 for jank. Enough
  to rule out a large effect, not enough to resolve an 8 ms one - which is exactly why I
  report it as "no difference" rather than "SDUI is faster".
- **Not a clean lab.** A retail vivo with its own launcher, services and thermal policy, not a
  rooted device on a locked clock. Runs were interleaved (SDUI, static, SDUI, static) so drift
  hits both.
- **Macrobenchmark would not run on it.** Two attempts, 27 and 41 minutes, one test passing
  and one timing out each time, no results file. The harness is in the repo and works on the
  emulator; on this device I fell back to in-app marks and `dumpsys gfxinfo`. Details in §5.
- **A mid-range phone would land between the two columns.** The Dimensity 9400 is
  flagship-class; on a Snapdragon 4-series I would expect a real but small overhead, not the
  emulator's +31%.

## 2. What is actually being compared

Both variants live in **one APK**, chosen by intent extra, so they are measured by the same
harness on the same hardware minutes apart. Two APKs would have added install order and
thermal drift as variables.

```bash
# SDUI
adb shell am start -n com.cars24.sdui/com.cars24.sdui.MainActivity
# hardcoded baseline
adb shell am start -n com.cars24.sdui/com.cars24.sdui.MainActivity --ez com.cars24.sdui.STATIC_BASELINE true
```

Three things were done specifically to stop the comparison flattering SDUI:

1. **The baseline uses the same `LazyColumn`-of-sections structure** as `SduiPageHost`. A
   hand-written `Column` would have looked slower, and I would have been measuring
   lazy-versus-eager instead of SDUI-versus-static.
2. **The baseline reuses the same design-system components** (`Cars24Card`, `Cars24Button`,
   `NetworkImage`, `Cars24Tag`). Inlining them there would have handed the baseline a
   composition-count advantage unrelated to the question.
3. **Both load byte-identical images.** `StaticHomeData` carries the exact Unsplash URLs the
   payload sends, extracted from `home.json` rather than retyped. Before that, SDUI was paying
   for 15 image decodes the baseline was not, and the gap was meaninglessly large.

**Known remaining asymmetry:** the payload has two sections with no baseline counterpart -
`ar_showroom_360` (unregistered type) and `loyalty_tier_card` (`minSchemaVersion: 5`), both
rendering a degraded placeholder. A hardcoded screen cannot have an unknown section, so the
baseline has 2 fewer items. That is a small handicap against SDUI which I kept, because
removing the landmines would mean the fallback paths were not exercised in the measured run.

## 3. Results

### The five metrics the brief asks for

Device column first, since that is the one that should inform a decision.

| Metric                                                 | Static (device) | SDUI (device) | Device overhead   | Static (emu) | SDUI (emu) | Emu overhead      |
|--------------------------------------------------------|---|---|-------------------|---|---|-------------------|
| **TTR** - cold open --> above the fold                 | **194 ms** | **186 ms** | **-8 ms (-4%)**   | 410 ms | 538 ms | +128 ms (+31%)    |
| **TTI** - cold open --> scrollable/tappable              | 194 ms | 186 ms | same as TTR       | 410 ms | 538 ms | same as TTR       |
| **Full page** - open --> all sections rendered           | **2,951 ms** | **3,104 ms** | **+153 ms (+5%)** | 3,502 ms | 3,852 ms | +350 ms (+10%)    |
| **SDUI breakdown** - fetch / parse / view-build        | n/a | fetch **0-1 ms** · parse **8 ms** · view-build the rest | -                 | n/a | fetch 1-3 ms · parse 16 ms | -                 |
| **Scroll jank** - janky frames, fixed 16-fling gesture | **0.24%** | **0.25%** | **+0.01 pt**      | 96% | 86% | emulator unusable |

Distributions, since medians alone hide the overlap:

| | n | min | median | max |
|---|---|---|---|---|
| Device, static TTR | 9 | 180 ms | **194 ms** | 208 ms |
| Device, SDUI TTR | 9 | 170 ms | **186 ms** | 196 ms |
| Device, static full page | 6 | 2,930 ms | **2,951 ms** | 2,996 ms |
| Device, SDUI full page | 5 | 3,082 ms | **3,104 ms** | 3,133 ms |
| Device, parse | 8 | 6 ms | **8 ms** | 12 ms |

The TTR ranges overlap heavily (170-196 against 180-208). **That is the finding: on this
hardware the SDUI indirection is smaller than the launch-to-launch variance of the launch
itself.**

### TTI is not a separate number, and I am not going to pretend it is

TTI is measured by a genuinely different signal - a second mark waiting on
`listState.canScrollForward`, i.e. the list has laid out enough to accept a scroll - rather
than re-reporting the TTR timestamp under another name.

It lands **0-1 ms after TTR on the device and 0-6 ms on the emulator, in all 40 runs.** That
is not a measurement failure, it is how Compose works: once a `LazyColumn` has measured and
placed its first items it is already scrollable and already hit-testable. There is no
meaningful window between "there are pixels" and "you can touch them".

I report it as its own row because the brief asks for it, but **the honest statement is that
TTR and TTI are the same event in a Compose app.** A table showing them 40 ms apart would mean
somebody picked two arbitrary signals to make the table look richer.

### "Full page" is mostly a measurement of how fast I swiped

The page is a lazy list, so sections below the fold genuinely are not rendered until they
scroll into view. There is no honest way to report "all sections rendered" without reaching
the bottom, so the mark fires when the last section's index becomes visible after a scripted
fling - 12 identical swipes for both builds.

**Most of those ~3 seconds is gesture and fling-settle time, not rendering.** The +153 ms
delta is the part attributable to the build. On the device the ranges do *not* overlap
(3,082-3,133 against 2,930-2,996), so unlike TTR this one looks like a real difference: SDUI
does a little more work per section as sections scroll in.

An eager `Column` would give a clean full-page number and a much worse TTR. That trade is the
right way round for a page like this; the metric is awkward, not the implementation.

### Scroll jank

Measured with `dumpsys gfxinfo` framestats: reset counters, run an identical 16-fling gesture,
read the frame histogram. Three runs per variant per platform.

| Platform | Variant | Frames | Janky | p50 | p90 | p95 | p99 |
|---|---|---|---|---|---|---|---|
| Device | SDUI | 372-400 | **0.25%** | 10-19 ms | 15-25 ms | 18-26 ms | 28-53 ms |
| Device | Static | 406-414 | **0.24%** | 10 ms | 13-14 ms | 16-17 ms | 23-27 ms |
| Emulator | SDUI | 110-113 | 86% | 65-69 ms | 93-97 ms | 101-121 ms | 125-150 ms |
| Emulator | Static | 90-109 | 96% | 73-93 ms | 105-150 ms | 113-150 ms | 121-200 ms |

**On the device both builds are effectively jank-free** - one janky frame in ~390, in both.
SDUI's tail is worse (p99 up to 53 ms against 27 ms) and its p50 was higher in one of three
runs, which is consistent with the extra per-section work the full-page number shows. It is
not enough to be visible.

Two honest notes. The device's first SDUI run shows p50 19 ms against 10 ms for the others -
that is image decoding still in flight on a freshly cleared app, not a steady-state number.
And the emulator rows are in the table only to show that **90% jank in both builds means the
emulator was never measuring the app.**

### Fetch versus parse versus view-build

| Stage | Device | Emulator | How measured |
|---|---|---|---|
| Asset read ("fetch") | **0-1 ms** | 1-3 ms | `System.nanoTime()` around `PageDataSource.fetch`, logged under `Cars24Perf` |
| JSON parse | **8 ms** median | 16 ms median | `sdui_json_parse` atrace section; also logged per load |
| View build | remainder | ~110 ms of the 128 ms emulator gap | by subtraction |

`adb logcat -s Cars24Perf` prints it on any build:

```
payload origin=Network bytes=49660 fetch=1ms parse=8ms sections=43
```

**Parsing 49 KB of JSON costs 8 ms on a current phone, off the main thread.** That is the
number that settles the "is SDUI too slow" question, and it is why the device TTR delta is
inside noise: 8 ms of parse against a ~190 ms launch, and it does not even block the frame.

`fetch` is an asset read, not a network call, because the mock server is a bundled JSON file.
**A real network fetch would add its own latency, but it would add the same latency to any
client architecture**, so leaving it out isolates the SDUI-specific cost rather than hiding
it. What is *not* simulated is a fake delay - padding the read with a `sleep` and reporting it
as "fetch time" would make this section fiction.

## 4. Measure, then optimise: what I tried

### Worked, and it is the one big win: a page-wide recomposition I had caused myself

`SduiScope` originally held the page state map. Every section takes the scope, so rebuilding
it on any state change made every section a changed input.

Measured by logging every entry into `SduiNodeRenderer`, scrolling so the tenure chips and the
(non-templated) trust strip were on screen together, clearing logcat, and tapping one tenure
chip:

| | Section renders on one tenure tap |
|---|---|
| Scope rebuilt per state change (original) | **18** |
| Stable scope, state in a CompositionLocal (current) | **1** |

The 18 included `trust_strip`, `offer_banners`, `budget_header`, `emi_header`, two car cards
and seven spacers - none of which can observe `tenure`.

Fix: state moved into a `compositionLocalOf` and `SduiScope` became `@Stable`, built once per
screen. `compositionLocalOf` rather than `staticCompositionLocalOf` matters - the static
variant invalidates every reader in the subtree and would have achieved nothing.

### Tried, and it turned out to be redundant: identity-preserving template resolution

`SduiTemplate.resolve` returns the same instance when nothing matched, and `rememberProps`
keys its decode on that identity. I expected this to be the headline optimisation and wrote it
up as such **before measuring it.** It is not.

I logged every props deserialisation and compared the current code against a deliberately
naive version (always re-resolve, always allocate fresh props, unguarded state read). On a
fuel-chip tap: **3 decodes both ways.** On a tenure-chip tap: **1 decode both ways** - and in
both cases those were nodes that had genuinely just entered composition.

Two reasons the win I imagined was not there:

1. **`LazyColumn` already bounds the working set.** Off-screen sections are not composed at
   all, so "re-decodes all 43 sections" was never possible. The exposure was only ever the 4-5
   sections on screen.
2. **Kotlin 2.x strong skipping gets there first.** Once the scope was stable, unchanged
   sections skip before the CompositionLocal read executes, so they never subscribe.

I kept the identity check - four lines, correct, and it protects the case where a payload puts
fifty templated nodes inside one on-screen `column`, where skipping cannot help. But **on this
screen it buys nothing measurable, and claiming otherwise would have been the easiest false
claim in this document to get away with.**

### Worked: `LazyColumn` for sections

Sections are `items` of a lazy list, so a 43-section payload composes the 4-5 that fit on
screen. The baseline uses the same structure, so the comparison does not benefit from it - but
it is why the emulator overhead was +31% and not several hundred.

### Worked, but only on perceived speed: skeleton over blank

The shimmer skeleton is why the emulator's `timeToInitialDisplay` favoured SDUI by 37% (it
stops at the first frame with content, and SDUI's first frame is the skeleton). It does not
make the page arrive sooner and I have not counted it as a win.

### Tried, no measurable change: pre-warming the registry

`Cars24Components.registry()` allocates 20 objects and a map. Moving it to
`Application.onCreate` to get it off the first frame produced no measurable difference - it is
well under a millisecond - and it put work on the critical path before `setContent`. Reverted;
it is a `remember { }` in the screen.

### Not done, and it would matter most on a slow device: baseline profiles

`androidx.profileinstaller` is a dependency but there is **no baseline profile in this repo.**
A generated profile typically wins 15-30% of cold start. On the flagship device that would be
chasing an 8 ms difference that does not exist; on a low-end phone - where I would expect
SDUI's overhead to actually show up - it is the first thing I would add.

## 5. Why Macrobenchmark is not the source of the device numbers

`:benchmark` contains a working Macrobenchmark harness (`HomeStartupBenchmark`,
`HomeScrollBenchmark`) and it produced the emulator figures. On the vivo device it did not
work, twice:

- `HomeStartupBenchmark`: 26m40s, `sduiColdStartup` passed, `staticColdStartup` timed out.
- `HomeScrollBenchmark`: 40m48s, `staticScrollJank` passed, `sduiScrollJank` timed out.
- No `benchmarkData.json` written either time, since the task failed as a whole.

Whichever test ran second timed out, which points at the device rather than the tests - vendor
background-launch restrictions are the likely cause, and the notification shade was found
focused mid-run at one point. I did not chase it further: at 30-40 minutes per attempt the
diagnosis was costing more than the numbers were worth, and there were two cheaper instruments
that agree with each other.

What I used instead, both reproducible in a couple of minutes:

- **In-app marks** (`StartupTrace`) for TTR, TTI and full page, measured from
  `Process.getStartUptimeMillis()` - real process start, not `Activity.onCreate` - and emitted
  identically by both builds.
- **`dumpsys gfxinfo` framestats** for jank, reset before an identical scripted gesture.

Emulator numbers, if you want to reproduce the contrast:

```bash
./gradlew :benchmark:connectedBenchmarkAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.androidx.benchmark.suppressErrors=EMULATOR,LOW-BATTERY,UNLOCKED,DEBUGGABLE,NOT-PROFILEABLE
```

Note the suppression list. AndroidX raises those errors precisely because the results are not
trustworthy, and suppressing a warning does not make the warning wrong - the 90%-jank rows in
§3 are what it was warning about.

## 6. Honest summary

- **On a current physical device, SDUI costs nothing measurable at startup.** 186 ms against
  194 ms, ranges overlapping. Reported as "no difference", not as a win.
- **Parsing 49 KB of JSON is 8 ms**, off the main thread. Anyone planning to optimise an SDUI
  renderer should ignore the parser.
- **Scroll is jank-free in both builds on device** (0.25% against 0.24%). SDUI's p99 tail is
  worse - 53 ms against 27 ms at worst - which is real but not visible.
- **Full page is the one metric where SDUI is consistently slower**, +153 ms (+5%), ranges not
  overlapping. It is the extra per-section work as sections scroll in.
- **The emulator gave a wrong answer with a straight face**: +31% startup overhead and 90%
  jank in both builds. If I had only had an emulator, this document would have concluded that
  SDUI costs a third of the launch.
- The optimisation I expected to matter **measurably does nothing here**. The one that did was
  fixing a page-wide recomposition I introduced myself: 18 section renders per tap down to 1.
- **No baseline profile**, and no low-end device in the sample - which is where I would expect
  the overhead this document could not find.
