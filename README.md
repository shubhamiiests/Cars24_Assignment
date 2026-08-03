# Cars24 SDUI

A server-driven UI system for Android. The server sends JSON, the client renders the page.
Eleven Cars24 screens are built with it; the client contains one screen composable.

- [PERF.md](PERF.md):- SDUI vs a hardcoded build of the same screen, measured
- [COVERAGE.md](COVERAGE.md):- the registry, the coverage claim, and what the schema cannot do
- [AI_WORKFLOW.md](AI_WORKFLOW.md):- how it was built, including where the AI was wrong


## The screen I chose, and why

**The Cars24 home / landing page.** It was the right choice because it is the densest screen
in the app and every hard SDUI problem is already on it:

- Nine visually distinct section types (gradient search header, quick-action tiles, offer
  banners, category chips, car rails, EMI panel, trust strip, assured grid, FAQ accordion,
  footer CTA) - the brief asked for five.
- A horizontal carousel (`LazyRow` rails, banner carousel) **and** a vertical grid.
- Genuinely interactive elements driven only by JSON: fuel-type tabs that swap which cars
  are listed, an EMI tenure selector that rewrites the monthly figure, a city picker, a
  wishlist heart.
- Money, which forced the decision that the client does no formatting and no arithmetic.

It is also the screen where getting SDUI wrong is most expensive, which is the point: if the
overhead is acceptable here it is acceptable anywhere.

Then, because the brief says the real test is the *next* screen, I built ten more from the
same registry — listing, car detail, wishlist, search, sell flow, loan, insurance, offer,
returns policy, loan application. **No new destination code**: every one is a JSON file.


## Setup

```bash
git clone <repo> && cd Cars24
./gradlew :app:installDebug
```

That is all - Firebase is optional (see below) and the mock server is a bundled asset.

Requirements: JDK 17, Android SDK 37, AGP 9.3.1 (the Gradle wrapper handles Gradle itself).

### Run the hardcoded baseline instead

```bash
adb shell am start -n com.cars24.sdui.debug/com.cars24.sdui.MainActivity \
  --ez com.cars24.sdui.STATIC_BASELINE true
```

### Change the page without rebuilding

```bash
adb push data/src/main/assets/sdui/home.json \
  /sdcard/Android/data/com.cars24.sdui.debug/files/sdui/home.json
```

Edit the file, push, and pull to refresh. `FileOverridePageDataSource` checks that path
before the bundled asset, so no rebuild and no reinstall.

### Tests and benchmarks

```bash
./gradlew testDebugUnitTest          # 19 JVM tests, no emulator
./gradlew :benchmark:connectedBenchmarkAndroidTest   # PERF.md numbers
```


## Firebase Analytics setup, end to end

Firebase Analytics is the only Firebase product used. **The app runs without it** - with no
`google-services.json` the DI graph provides `NoOpAnalyticsLogger` and logs one line saying
so. That is deliberate: `google-services.json` is tied to one Firebase project and is
gitignored, and a reviewer cloning this repo still has to get a working app.

### On the Firebase website

1. Go to <https://console.firebase.google.com> and sign in with a Google account.
2. **Add project** --> give it a name (e.g. `cars24-sdui`) --> Continue.
3. Google Analytics step: **leave it enabled**. Without it there is no Analytics, which is
   the entire point here. Continue.
4. Pick or create a **Google Analytics account** (the default "Default Account for Firebase"
   is fine) --> **Create project** --> wait --> Continue.
5. On the project overview, click the **Android** icon ("Add app").
6. **Android package name** - this must match exactly, or the config is rejected at runtime:
   - `com.cars24.sdui` for release/benchmark builds
   - `com.cars24.sdui.debug` for debug builds (the debug build type adds `.debug`)

   Register **both** if you want Analytics in debug as well: register `com.cars24.sdui`
   first, then use **Add app** again for `com.cars24.sdui.debug`. One
   `google-services.json` can hold both.
7. App nickname is optional. **Debug signing certificate SHA-1 is not needed** for Analytics
   (it is for Auth and Dynamic Links). Skip it.
8. **Register app** --> **Download google-services.json**.
9. Skip the SDK instructions Firebase shows next - the Gradle wiring is already in this repo.
   Click through to **Continue to console**.

### In Android Studio

10. Switch the Project pane from *Android* to **Project** view (the Android view hides the
    real folder layout and this is the most common place people go wrong).
11. Drop `google-services.json` into the **`app/`** directory - the same folder as
    `app/build.gradle.kts`. Not the project root, not `app/src/`.
12. **Sync Project with Gradle Files**.

That is it. The plugin application is conditional, so it only activates once the file exists:

```kotlin
// app/build.gradle.kts
if (file("google-services.json").exists()) {
    apply(plugin = libs.plugins.google.services.get().pluginId)
} else {
    logger.lifecycle("[cars24] app/google-services.json missing - Firebase Analytics will no-op.")
}
```

### Verify events are arriving

Firebase batches events, so nothing shows up immediately. Force debug mode:

```bash
adb shell setprop debug.firebase.analytics.app com.cars24.sdui.debug
adb shell am force-stop com.cars24.sdui.debug
adb logcat -s FA FA-SVC     # events appear as they are logged
```

Then open **Firebase console --> Analytics --> Realtime** or **DebugView**. Events this app
sends:

| Event | When                                                                                                                                                               |
|---|--------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `screen_view` | Page rendered, using the payload's `analyticsName`                                                                                                                 |
| `sdui_page_rendered` | With `origin`, `fetch_ms`, `parse_ms`, `payload_bytes`, `section_count`                                                                                            |
| `sdui_unsupported_component` | Renderer met a type this build cannot draw. **Deduplicated per type per session** - it is called from composition, so unguarded it would fire on every scroll pass |
| `sdui_action_unsupported` | An action type this build does not implement                                                                                                                       |
| `sdui_page_offline` | First launch with no connection and nothing cached                                                                                                                 |
| Anything the payload asks for | Via the `track_event` action, e.g. `emi_tenure_selected`, `city_changed`                                                                                           |

Turn debug mode off again with:

```bash
adb shell setprop debug.firebase.analytics.app .none.
```

**Nothing above the `:core:analytics` module imports Firebase.** The rest of the app depends
on the `AnalyticsLogger` interface, which is why the renderer can be unit-tested without a
Firebase project.


## Architecture

```
:app                   Application, MainActivity, NavHost, DI startup
:core:common           MVI base, dispatchers, connectivity, StartupTrace
:core:designsystem     Theme, tokens, shared atoms, offline/error/skeleton states
:core:analytics        AnalyticsLogger + Firebase impl + no-op impl
:sdui:schema           The wire contract. No Android or Compose imports in source
:sdui:runtime          Renderer, registry interface, style resolver, action parser
:sdui:components       The 20 concrete components
:data                  Mock server, offline-first repository, persistence, the payloads
:feature:sduipage      MVI ViewModel + the one screen that renders any page
:feature:staticbaseline The hardcoded control group for PERF.md
:benchmark             Macrobenchmark harness
```

**The dependency direction is the architecture.** `:sdui:runtime` has no Gradle dependency on
`:sdui:components` - the arrow points the other way. That is what makes "adding a component
changes zero renderer code" a structural guarantee rather than a promise in a README.

```
:sdui:components ──► :sdui:runtime ──► :sdui:schema
                            │
                            └──► :core:designsystem
```

### Request flow

```
SduiPageViewModel ──► SduiPageRepository ──► FileOverride ──► Asset (mock server)
        │                     │                                     │
        │                     └──► PagePayloadCache (DataStore) ◄────┘
        ▼
   PageUiState ──► SduiPageScreen ──► SduiPageHost ──► SduiNodeRenderer ──► SduiComponent
        ▲                                                    │
        └──────────── PageIntent.Command ◄── SduiScope.dispatch
```

Every tap on a server-driven node becomes one `SduiCommand`, so the whole screen's behaviour
is one `when` in the ViewModel rather than logic scattered across 20 components.


## Schema design rationale

The full vocabulary is in [COVERAGE.md](COVERAGE.md). The three decisions that shaped
everything:

### 1. `props` stays an undecoded `JsonObject`

```json
{ "id": "swift", "type": "car_card", "props": { "name": "Maruti Swift VXi", "price": "Rs 5.24 L" } }
```

The renderer never looks inside `props`; the component that claims the type decodes its own
props class. Consequences:

- Adding a component is a new class plus one line in a list. No shared file grows a branch.
- **An unknown type is a registry miss, not a parse failure.** The alternative - a sealed
  hierarchy with `@SerialName` per component - fails deserialisation of the *entire payload*
  when the server sends one unknown type, which would blank the page on every older build.
  See AI_WORKFLOW.md §3 story 1; this was the first thing I rejected.

### 2. Actions are an open `type` string on the wire, typed on the client

`SduiAction(type, params, content, then)` is what travels; `SduiActionParser` turns it into a
sealed `SduiCommand` with an `Unsupported` branch. A server that starts sending
`share_deeplink` tomorrow cannot crash a build shipped today, but the client still gets an
exhaustive `when`.

`then` chains actions and `content` carries nested nodes, which between them remove the need
for most new action types - a bottom sheet body is itself SDUI, and "set the tenure, set the
figure, set the label, track it" is one declaration.

### 3. Conditions plus templates make interactivity JSON-only

```json
{ "id": "rail_diesel", "type": "carousel", "visibleWhen": { "key": "fuel", "equals": "diesel" } }
{ "id": "emi", "type": "emi_summary", "props": { "monthly": "{{state.emi_monthly}}" } }
```

The server sends every variant of a section, each guarded by a condition on a state key, and
a `set_state` action flips which one is in the tree. No round trip, and no client code that
knows what a fuel type or an EMI is.

This is why **the EMI tenure selector is not a component** - it is `chip_group` +
`emi_summary`, where each chip writes the pre-computed monthly figure. Changing the interest
rate is a payload edit.

### What the client deliberately cannot do

No arithmetic and no formatting. Every price and EMI figure arrives pre-formatted, because
formatting money on the client bakes lakh/crore conventions and rounding rules into the
binary — three things that would then need a release to change. The cost is that the server
sends four `set_state` chains for four tenures. Worth it.


## Versioning story

Four mechanisms, because they fail at different granularities.

**Field level — `ignoreUnknownKeys = true`.** The single most important line in
`SduiJson.kt`. A server adding a field for the next app version must not throw on every
build already in the wild. Strict parsing here would make the whole system a liability
rather than a saving.

**Section level, unknown type.** Registry miss --> the server-supplied `fallback` if there is
one, otherwise a visible placeholder naming the type, plus an
`sdui_unsupported_component` event. The rest of the page is unaffected. The placeholder is
visible by default because a section quietly missing from a live page is the failure mode
that goes unnoticed for a week; production would flip `showUnknownPlaceholders` to false and
rely on the event, which is a config change rather than a code change.

**Section level, known-but-too-new - `minSchemaVersion` + `fallback`.** The server marks a
section as needing a newer client **and says what an older client should draw instead**:

```json
{
  "id": "loyalty_tier", "type": "loyalty_tier_card", "minSchemaVersion": 5,
  "fallback": { "id": "loyalty_fb", "type": "value_props", "props": { "heading": "Cars24 rewards" } }
}
```

Fallback recursion is capped at one level, so a server cannot write a chain that spins.

**Page level - `SduiJson.SUPPORTED_SCHEMA_VERSION`** (currently `2`). Bumped when the shape
of the contract changes in a way an older build cannot cope with. Adding a component does
*not* bump it, because unknown types already degrade.

**Both degradation paths run on every launch of the home page**, not only in a demo:
`ar_showroom_360` has no component and `loyalty_tier_card` declares `minSchemaVersion: 5`.
If either regresses, the home page shows it without anyone remembering to test for it.

**Cache safety:** `PagePayloadCache` stores raw JSON rather than serialised Kotlin objects,
and only after a successful parse. So a payload we could not understand can never become the
thing we fall back to, and cached bytes written by an older build fail in the same place a
fresh server response would.

## The other requirements

**No internet on first launch.** `SduiPageRepositoryImpl` checks real connectivity
(`NET_CAPABILITY_VALIDATED`, so captive portals count as offline) *before* fetching. With no
connection and nothing cached you get a full-screen offline state with a retry - not a blank
page with a toast, because on a cold install there is no page to show. With a cached payload
you get the content plus a banner admitting it is old. Both verified in aeroplane mode; see
AI_WORKFLOW.md §5.

**Everything survives process death.** Selected tab, selected tenure, chosen city, wishlist,
scroll position, and **which bottom sheet is open** are all persisted through DataStore and
restored. The sheet is persisted by **id only** - storing its node tree would let a restored
sheet show a price the current payload no longer serves; re-deriving it from today's payload
cannot go stale. State merge order is `initialState` --> saved page state → route params →
shared state, deliberately: the server's default seeds keys the user never touched, and a
saved value wins for keys they did.

**DI.** Koin, three modules, constructor injection throughout. Hilt was the first choice and
did not survive contact with AGP 9 - AI_WORKFLOW.md §4 has the four build failures.

**MVI.** One immutable state, intents drained by a single consumer so reducers never race,
effects on a separate channel. The rule that earned its keep: *anything that must survive
process death is state, not an effect*, which is why the bottom sheet is in `PageUiState`.

**SOLID, where it actually shows up.** Open/closed is the Gradle dependency direction
described above. Dependency inversion is `PageDataSource`, `SduiPageRepository`,
`AnalyticsLogger`, `ConnectivityMonitor`, `DispatcherProvider` — each of which exists because
something genuinely needed substituting (a test, a missing Firebase project, an offline
path), not to hit a letter.

## Trade-offs I made, and would defend

**No convention plugins.** Ten near-identical module build files instead of a `build-logic`
included build. AGP 9's built-in-Kotlin DSL is new enough that I would have been
reverse-engineering an unstable API; ten readable build files cost more lines and less risk.

**Koin over Hilt.** Forced, then endorsed. No annotation processing anywhere in the build,
which also means no KSP in the incremental loop.

**Bundled assets as the mock server, with no fake latency.** The brief allows a local file.
Adding a `sleep` and reporting it as "fetch time" would make PERF.md fiction. Swapping in
HTTP is one new `PageDataSource` and one line of Koin.

**Navigation intents that have no payload are surfaced, not followed.** `navigate` to a route
with no JSON shows "No payload for 'x' yet" rather than pushing a destination that can only
render an error.

**Icons from `material-icons-core` only.** `material-icons-extended` is pinned at 1.7.8
against a 2026.02 Compose BOM; a stale transitive was not worth six glyphs. A new icon is
therefore a client change, listed as a real limitation in COVERAGE.md.

**Grids are not lazy.** Nesting a `LazyVerticalGrid` in the page's `LazyColumn` crashes, so
`grid` builds rows of weighted columns. Fine for the 4-8 item grids here, pathological for
200. Named in COVERAGE.md §5.

**Photos are remote URLs with a gradient underneath.** The gradient is the loading and
failure state, painted *under* the image rather than swapped in, so there is no layout shift
and the screen still looks deliberate in aeroplane mode. Deterministic per car name, so a
card keeps its colour when the payload changes the photo.

**No instrumented UI tests.** 19 JVM tests plus adb-driven verification that lives in my
shell history rather than the repo. On a real team the adb checks belong in CI as
instrumented tests; for this exercise I chose breadth of verification over automating the
harness. Stated plainly in AI_WORKFLOW.md §5.

**No baseline profile.** The largest single perf win available and it is not here.
PERF.md §4 explains why I would rather report the number without it than generate a profile
on an emulator and credit my architecture with the improvement.
