# PERF.md

SDUI versus a hardcoded build of the same screen.

**Headline: the SDUI page costs about +128 ms (+31%) to first content and +170 ms (+17%)
to full display on the device I had. JSON parsing is ~16 ms of that. The rest is view
build.** Details, caveats and the one number that looks good but is not, below.



## 1. The device, and why you should discount these numbers

| | |
|---|---|
| Device | **Pixel 6 API 34 emulator** (`sdk_gphone64_arm64`), host: Apple Silicon macOS 25.5 |
| Build | `benchmark` build type: R8 minified, resources shrunk, `isProfileable=true`, debug-signed |
| Payload | `home.json`, 43 sections, 49,660 bytes |
| Macrobenchmark | `androidx.benchmark:benchmark-macro-junit4:1.4.1`, 10 iterations, `StartupMode.COLD` |

**I did not have a physical device for this.** Everything below is an emulator number and
should be read as a ratio, not an absolute. Concretely, what the emulator got wrong:

- **Variance is large.** Cold-start TTR ranged 407-891 ms for SDUI and 274-761 ms for
  static. The coefficient of variation on `timeToFullDisplay` was around 25%, where a real
  device on a fixed clock is usually under 5%. With n=10 the medians are indicative, not
  tight.
- **Jank could not be measured at all.** See section 5. This is the biggest gap.
- Benchmark errors had to be suppressed (`EMULATOR,LOW-BATTERY,UNLOCKED,DEBUGGABLE,NOT-PROFILEABLE`).
  AndroidX raises those precisely because the results are not trustworthy, and suppressing
  a warning does not make the warning wrong.

To reproduce on real hardware:

```bash
./gradlew :benchmark:connectedBenchmarkAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.cars24.benchmark.HomeStartupBenchmark
```



## 2. What is actually being compared

Both variants live in **one APK** and are chosen by intent extra, so they are measured by
the same harness, on the same device, in the same session, minutes apart. Two APKs would
have added install order and thermal drift as variables.

```bash
# SDUI
adb shell am start -n com.cars24.sdui/com.cars24.sdui.MainActivity
# hardcoded baseline
adb shell am start -n com.cars24.sdui/com.cars24.sdui.MainActivity --ez com.cars24.sdui.STATIC_BASELINE true
```

Three things were done specifically to stop the comparison flattering SDUI:

1. **The baseline uses the same `LazyColumn`-of-sections structure** as `SduiPageHost`. A
   hand-written `Column` would have looked slower and I would have been measuring
   lazy-versus-eager, not SDUI-versus-static.
2. **The baseline reuses the same design-system components** (`Cars24Card`, `Cars24Button`,
   `NetworkImage`, `Cars24Tag`). Inlining them there would have handed the baseline a
   composition-count advantage unrelated to the question.
3. **Both load byte-identical images.** `StaticHomeData` carries the exact same Unsplash
   URLs the payload sends, extracted from `home.json` rather than retyped. Before I did
   this, SDUI was paying for 15 image decodes that the baseline was not, and the gap was
   meaninglessly large.

**Known remaining asymmetry:** the payload contains two sections with no baseline
counterpart - `ar_showroom_360` (unregistered type) and `loyalty_tier_card`
(`minSchemaVersion: 5`), both of which render a degraded placeholder. A hardcoded screen
cannot have an unknown section, so the baseline has 2 fewer items. That is a real ~1-2 ms
handicap against SDUI that I chose to keep, because removing the landmines would mean the
fallback paths were not being exercised in the measured run.


## 3. Results

### Time to first content (TTR)

Measured in-app from `Process.getStartUptimeMillis()` to the frame where the first section
has been laid out, via `StartupTrace`. **This is the number I trust most**, because it is
measured identically in both variants and it is measured from real process start rather
than from `Activity.onCreate`.

10 cold launches each, `pm clear` between every run so nothing is cached:

| | min | median | p90 | max |
|---|---|---|---|---|
| Static | 274 ms | **410 ms** | 591 ms | 761 ms |
| SDUI | 407 ms | **538 ms** | 851 ms | 891 ms |
| **Overhead** | | **+128 ms (+31.3%)** | | |

### Macrobenchmark

| Metric | Static (median) | SDUI (median) | Overhead                                  |
|---|---|---|-------------------------------------------|
| `timeToInitialDisplay` | 972.7 ms | 616.1 ms | **-37% - do not believe this, see below** |
| `timeToFullDisplay` | 1008.3 ms | 1177.9 ms | **+169.6 ms (+16.8%)**                    |
| `sdui_json_parse` (sum) | n/a | **15.8 ms** (min 4.1, max 69.9) | -                                         |

### The number that looks good and is not

`timeToInitialDisplay` says SDUI is 37% *faster*. It is not. TTID stops at the first frame
with content, and the SDUI build's first frame is the **shimmer skeleton**, which it can
draw before the payload has even been read. The static build has nothing to draw until it
draws the real thing.

So SDUI genuinely feels faster to first pixel, and that is a real product benefit worth
having - but it is not the same measurement, and reporting it as a 37% win would be
dishonest. `timeToFullDisplay` (driven by explicit `ReportDrawnWhen` calls in both
variants) and the in-app TTR mark are the comparable numbers, and both say SDUI is slower.

### Fetch versus parse versus view-build

| Stage | Cost | How measured |
|---|---|---|
| Asset read ("fetch") | ~1-3 ms | `System.nanoTime()` around `PageDataSource.fetch` |
| JSON parse | **15.8 ms** median | `sdui_json_parse` atrace section, read by `TraceSectionMetric` |
| View build | **remainder, ~110 ms of the 128 ms TTR gap** | by subtraction |

The useful conclusion: **parsing is not the problem.** Roughly 12% of the overhead is
deserialisation; the rest is the renderer's own work - registry lookup, per-node props
decode, building a `Modifier` chain from `SduiStyle`, and the extra `Box` wrapper each node
carries. Optimisation effort belongs there, not in the parser.

`fetch` is an asset read, not a network call, because the mock server is a bundled JSON
file. **A real network fetch would add its own latency, but it would add the same latency
to any client architecture**, so leaving it out isolates the SDUI-specific cost rather than
hiding it. What is not simulated is a fake delay - padding the read with a `sleep` and
reporting that as "fetch time" would make this whole section fiction.


## 4. Measure, then optimise: what I tried

### Worked, and it is the one big win: the page-wide recomposition I had caused myself

`SduiScope` originally held the page state map. Every section takes the scope, so rebuilding
it on any state change made every section a changed input.

I measured this by logging every entry into `SduiNodeRenderer`, scrolling so the tenure
chips and the (non-templated) trust strip were on screen together, clearing logcat, and
tapping one tenure chip:

| | Section renders on one tenure tap |
|---|---|
| Scope rebuilt per state change (original) | **18** |
| Stable scope, state in a CompositionLocal (current) | **1** |

The 18 included `trust_strip`, `offer_banners`, `budget_header`, `emi_header`, two car
cards and seven spacers - none of which can observe `tenure`.

The fix: state moved into a `compositionLocalOf` and `SduiScope` became `@Stable` and built
once per screen. `compositionLocalOf` rather than `staticCompositionLocalOf` matters, since
the static variant invalidates every reader in the subtree and would have achieved nothing.

### Tried, and it turned out to be redundant: identity-preserving template resolution

`SduiTemplate.resolve` returns the same instance when nothing matched, and `rememberProps`
keys its decode on that identity. I expected this to be the headline optimisation and wrote
it up as such before measuring it. It is not.

I logged every props deserialisation and compared the current code against a deliberately
naive version (always re-resolve, always allocate a fresh props object, unguarded state
read). On a fuel-chip tap: **3 decodes both ways.** On a tenure-chip tap: **1 decode both
ways** - and in both cases the decodes were for nodes that had genuinely just entered
composition and had to be decoded.

Two reasons the win I imagined was not there:

1. **`LazyColumn` already bounds the working set.** Off-screen sections are not composed at
   all, so "re-decodes all 43 sections" was never a thing that could happen. The exposure
   was only ever the 4-5 sections on screen.
2. **Kotlin 2.x strong skipping gets there first.** Once the scope was stable, unchanged
   sections skip before the CompositionLocal read executes, so they never even subscribe.

I have kept the identity check - it is four lines, it is correct, and it protects the case
where a payload puts fifty templated nodes inside one `column` that is on screen at once,
where skipping cannot help because that whole subtree does recompose. But **on this screen
it buys nothing measurable, and claiming otherwise would have been the easiest false claim
in this document to get away with.**

### Worked: `LazyColumn` for sections

Sections are `items` of a lazy list, so a 43-section payload composes the 4-5 that fit on
screen. This is the single biggest reason the overhead is +31% and not several hundred. The
baseline uses the same structure, so the comparison does not benefit from it.

### Worked, but only on perceived speed: skeleton over blank

The shimmer skeleton is why `timeToInitialDisplay` looks so good. It does not make the page
arrive sooner and I have not counted it as a win above.

### Tried, no measurable change: pre-warming the registry

`Cars24Components.registry()` allocates 20 objects and a map. Moving it to
`Application.onCreate` to get it off the first frame produced no measurable difference - it
is well under a millisecond - and it put work on the critical path before `setContent`.
Reverted; it is a `remember { }` in the screen.

### Not done, and it is the obvious next step: baseline profiles

`androidx.profileinstaller` is a dependency but there is **no baseline profile in this
repo.** A generated profile typically wins 15-30% of cold start, which would plausibly
absorb most of the 128 ms. I skipped it because it needs the `androidx.baselineprofile`
plugin and a device I would trust for the generation run. I would rather report the number
without it than generate a profile on an emulator and quietly credit my architecture with
the improvement.

## 5. Scroll performance: not measured, and here is why

`HomeScrollBenchmark` is in the repo and runs, but **`FrameTimingMetric` returned only
`frameCount` on this emulator.** `frameDurationCpuMs` came back with zero samples and the
static variant failed outright:

```
java.lang.IllegalArgumentException: At least one result is necessary, 0 found for frameDurationCpuMs
```

`FrameTimingMetric` reads `actual_frame_timeline` trace data, which the emulator's
graphics stack does not reliably produce. I could show you `frameCount` medians (SDUI: 133.5
frames for a fixed 8-fling gesture) but a frame count without frame durations says nothing
about jank, and pairing it with a confident "no dropped frames" claim would be inventing a
result.

**So: I have no jank numbers, and I am not going to guess them.** On a physical device:

```bash
./gradlew :benchmark:connectedBenchmarkAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.cars24.benchmark.HomeScrollBenchmark
```

What I can say from reasoning and from watching it, neither of which is a measurement:
scrolling is a nested-lazy structure (`LazyRow` rails inside a `LazyColumn`), all items are
keyed by node id, and nothing in the scroll path parses JSON - props are decoded once per
node and remembered. The mechanism for scroll jank in an SDUI renderer would be re-decoding
props during scroll, and that path is cached. Whether that holds under a real fling on real
hardware, I have not proven.

## 6. Honest summary

- SDUI costs **+128 ms to first content (+31%)** and **+170 ms to full display (+17%)** on
  an emulator with wide variance. On a mid-range physical device I would expect the
  absolute gap to grow and the percentage to be broadly similar.
- **~16 ms of that is JSON.** The rest is view build. Anyone planning to optimise this
  should ignore the parser.
- The optimisation I expected to matter (caching props resolution) **measurably does
  nothing here.** The one that did matter was fixing a page-wide recomposition I had
  introduced myself: 18 section renders per tap down to 1.
- One metric (`timeToInitialDisplay`) favours SDUI by 37% and is not a fair comparison.
- **Jank is unmeasured.** The harness exists; the emulator cannot feed it.
- The largest available win - baseline profiles - is not implemented.
