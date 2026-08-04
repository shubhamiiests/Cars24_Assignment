# AI_WORKFLOW.md

How this was actually built, including the parts where the AI was confidently wrong.


## 1. Tool stack

| Tool | Used for |
|---|---|
| **Claude Code** (Opus, agentic CLI) | Nearly all of the code. Multi-file edits, running Gradle, reading failures, driving the emulator over adb |
| **Android Studio** | Project scaffold, previews, occasional structural eyeball |
| **adb + uiautomator** | Verification. The agent drove taps and read the view hierarchy back rather than asking me to click things |
| **Macrobenchmark** | PERF.md numbers |
| **A Python generator** (`tools/generate_payloads.py`) | The eleven payloads. See §3 story 3 |

The thing that changed the quality of output most was not prompt wording. It was giving the
agent **a way to check itself**: an emulator it could install onto, logcat it could read,
uiautomator dumps it could assert against, and a unit test suite it could run. Almost every
bug in §4 was caught by the loop, not by me reading a diff.


## 2. The context I wrote first

Before asking for any code I wrote down the constraints, because the default output for
"build an SDUI system" is a `when (type)` block in a single file and a README calling it
extensible.

```
CONSTRAINTS - non-negotiable

Architecture
- Multi-module. The renderer must not depend on concrete components; adding a component
  must be provably a zero-line change to the renderer. Enforce with Gradle dependency
  direction, not comments.
- MVI: one immutable state, intents in, effects out. Anything that must survive process
  death is STATE, not an effect.
- DI everywhere. No manual construction in composables.

The schema is the deliverable
- Props stay undecoded (JsonObject). The renderer never looks inside.
- Actions are an open type string on the wire, typed on the client.
- An unknown type/field/action is a normal event, never an exception.
- Interactivity must be JSON-only. If a tab change needs Kotlin, the design failed.

Honesty
- Never write a perf number you did not measure.
- Never claim an optimisation worked without a before/after.
- Prefer "I could not measure this" to a plausible figure.

Kotlin
- Explicit imports, trailing commas, no wildcard imports.
- Comments explain WHY, and only where the reason is not obvious. No comment restating
  the line below it.
```

The last two blocks did the most work. The honesty block is why PERF.md carries both a
device and an emulator column instead of quietly keeping whichever looked better - and it is why §3 story 2 below ends the
way it does.


## 3. Three prompt --> outcome stories

### Story 1:- Rejected: the schema the AI wanted to write

**Prompt:** *"Design the JSON schema for a server-driven page. It has to render the Cars24
landing page and generalise to a screen I have not seen. Show me the Kotlin data classes and
defend the decisions."*

**What came back** was clean, idiomatic, and wrong in one specific way. Roughly:

```kotlin
@Serializable
sealed class SduiComponent {
    @Serializable @SerialName("car_card")
    data class CarCard(val name: String, val price: String, val emi: String) : SduiComponent()
    @Serializable @SerialName("banner")
    data class Banner(val title: String, val imageUrl: String) : SduiComponent()
    // ...one subclass per component
}
```

**Why I rejected it.** Every component becomes a branch of a sealed hierarchy that the
renderer must know about, so:

- Adding a component means editing a shared file — the opposite of the open/closed property
  the whole exercise is about.
- Worse, a `type` the client has never seen is a **deserialisation failure**, and kotlinx
  polymorphic decoding fails the *whole payload*, not the one node. One new section from the
  server would blank the entire page on every older build. That is a schema that makes the
  release-cycle problem worse, not better.

**What I wrote instead:** `SduiNode(id, type, props: JsonObject, …)` with props left
undecoded and each component decoding its own props class. The renderer never looks inside
`props`. Unknown type is a registry miss, which is a survivable event with a placeholder.

When I put this back to the AI it agreed immediately and produced the version I wanted -
which is the point. It optimises for the code looking right, and "type-safe sealed
hierarchy" looks extremely right. It does not weigh "what happens to a build shipped six
months ago" unless you make that the question.

### Story 2:- Rewritten: the perf section it wrote before measuring anything

**Prompt:** *"Write the optimisation section of PERF.md covering the props caching I
implemented."*

**What came back** included, in confident prose: *"Measured with a debug counter around the
decode: props decodes per chip tap went from 43 to 2."*

I had not measured that. Neither had the AI — it could not have, there was no counter in the
code. It had reasoned "43 sections in the payload, so 43 decodes" and written the reasoning
as a measurement. The number is even internally implausible, and I nearly shipped it.

**What I did:** built the counter, ran it, and ran a deliberately naive variant to compare
against. Result: **3 decodes both ways.** The optimisation I had written up as the headline
win does nothing measurable on this screen, because `LazyColumn` already bounds the working
set to what is on screen and Kotlin 2.x strong skipping handles the rest.

So I kept digging and found the optimisation that *does* matter - a page-wide recomposition I
had caused myself by putting the state map on `SduiScope` - and measured it properly:
**18 section renders per tap down to 1.**

The same instinct is what saved the headline number later. I had written up "+31% startup
overhead" from emulator runs. When a physical device became available I re-ran everything, and
the overhead was **-8 ms, i.e. none**; the emulator had also been reporting 90% janky frames in
*both* builds, which is the tell that it was measuring itself rather than the app. PERF.md now
publishes both columns. Had I not re-measured, the entire performance section would have been
confidently wrong in the direction that made my architecture look worse.

PERF.md §4 now leads with the real win and has a subsection titled *"Tried, and it turned
out to be redundant"* for the one I had been about to take credit for.

**The lesson I actually took:** an AI writing your performance documentation will produce
prose in the register of measurement without the measurement, because that is what perf
docs sound like in its training data. The mitigation is not a better prompt. It is refusing
to let a number into a document unless you can point at the command that produced it.

### Story 3:- Kept and extended: generating the payloads instead of writing them

**Prompt:** *"I need eleven SDUI payloads with consistent car data across four pages. Cars
appear on home rails, the listing grid, the wishlist and the detail page."*

**What came back** was a suggestion I had not considered: write a Python generator rather
than eleven JSON files, because the same car's price appears in four places and hand-syncing
them is how a demo ends up contradicting itself.

**Why I kept it,** and extended it: the generator became the reason the payloads are
internally consistent at all. One `CATALOGUE` list is the source of truth for sixteen cars;
`car_card()` builds the node, including the `wishKey` and the `navigate` params for the
detail page. When I added images, one edit gave every car a photo on all four pages. When I
changed the wishlist copy, one edit.

**What I added that it did not suggest:** `PayloadContractTest`, which checks the generated
output against the registry the app actually ships — unregistered types, dangling
`navigate` routes, duplicate node ids, `visibleWhen` on keys nothing writes, `{{state.x}}`
with no source and no default. The generator makes payloads consistent with *each other*;
the test makes them consistent with the *client*. Both are needed, and only the second one
catches the mistake where I invent a component type in JSON that does not exist in Kotlin.

## 4. Where AI led me wrong

### The main failure: Hilt, KSP, and AGP 9

This one cost the most time and is a good example of the failure mode: **the AI's advice was
correct for last year's toolchain, and stated with no uncertainty.**

Asked to set up DI, it produced the standard Hilt setup - `com.google.dagger.hilt.android`
plugin, KSP, `@HiltAndroidApp`, `@HiltViewModel`. This is right in almost every Android
project written in the last four years and is overwhelmingly what its training data
contains.

This project is on **AGP 9.3.1**, which ships its own built-in Kotlin plugin. The build
failed four times in a row, each failure teaching something:

1. `Failed to apply plugin 'com.google.dagger.hilt.android'. > Android BaseExtension not found`
   - Hilt's Gradle plugin uses an API AGP 9 removed. Tried Hilt 2.60.1, the latest. Same.
2. `KSP is not compatible with Android Gradle Plugin's built-in Kotlin. Please disable by
   adding android.builtInKotlin=false` — so KSP needs the standalone Kotlin plugin.
3. Adding it: `Error resolving plugin [id: 'org.jetbrains.kotlin.android', version: '2.2.21']
   > already on the classpath with an unknown version`.
4. Applying it version-less instead:
   `ApplicationExtensionImpl cannot be cast to com.android.build.gradle.BaseExtension` — the
   KGP that AGP puts on the classpath is not itself AGP-9-ready.

**How I caught it:** the compiler. This class of error cannot survive a build, which is why
it was annoying rather than dangerous.

**The decision:** stop trying to force the recommended stack and switch to **Koin**. No
Gradle plugin, no annotation processing, no version matrix — DI became a plain Kotlin
module, and the whole class of problem disappeared. Documented in the commit message so the
next person does not retry Hilt.

**The general lesson:** on a toolchain newer than the model's training data, AI advice is
confidently retrospective. It does not say "AGP 9 may have changed this." Anything
build-related on a bleeding-edge AGP should be treated as a hypothesis to test, not an
answer — and I now check the actual artifact metadata (`maven-metadata.xml`, `.module`
files) before believing a version number exists. That habit also caught Coil 3.5.0
constraining `kotlin-stdlib` to 2.4.0, which the bundled compiler cannot read.

### The subtle failure: a bug the compiler could not catch

Navigation was silently dead. Every CTA did nothing — no crash, no log, no snackbar. State
changes (bottom sheets) worked fine, so dispatch was clearly working.

```kotlin
// what the AI wrote, and what I reviewed and approved
LaunchedEffect(viewModel) {
    viewModel.effects.collect { effect ->
        when (effect) {
            is PageEffect.Navigate -> onNavigate(effect.route, effect.params)
            // ...
        }
    }
}
```

This is the textbook effect-collection pattern and it is what I would have written. The bug
is not in it - it is in the interaction with the host, which resolved the set of known pages
asynchronously. `LaunchedEffect(viewModel)` never restarts, so it captured the **first**
`onNavigate` lambda: the one that closed over an empty set. Every navigation checked
`route in emptySet()`, took the else branch, and showed a snackbar so briefly I kept missing
it.

**How I caught it:** not by reading code - I had read that code and thought it was fine.
I logged at three points along the path (ViewModel emit, collector receive, host handler)
and the third log printed `known=[]`. Fifteen seconds of reading after twenty minutes of
guessing.

**Fix:** `rememberUpdatedState` on the callbacks, with a comment explaining why, because the
next person will otherwise "simplify" it back.

**The lesson:** the dangerous AI output is not the wrong-looking code, it is the correct
pattern applied without the surrounding context that makes it wrong. No amount of reviewing
that snippet in isolation finds this. Instrumentation does.

### Smaller ones, for completeness

- Invented a `Dp` helper (`private fun Int.dpFrom()`) instead of importing `.dp`. Deleted.
- Wrote a `takeWhile` extension that shadowed the stdlib flow operator, when
  `snapshotFlow{}.first{}` was the whole answer. Deleted.
- Used `Icons.Filled.Sell` / `DirectionsCar` / `Shield` / `Verified`, none of which are in
  `material-icons-core`. Caught by the compiler; fixed by mapping to icons that exist and
  documenting the limit in COVERAGE.md.
- Kept a `HashMap<PageEnvelope, String>` on the repository to smuggle raw JSON to the cache
  write, when `raw` was already in scope at the call site. Deleted.
- Built a regex that matched `StaticCar\("[^)]*?\)` and stopped at the first `)` — the inner
  `listOf(`. Corrupted 15 call sites. Reverted via git and redone with a balanced-paren
  scan. Cheap because it was committed work being modified, not new work being lost.


## 5. Verification strategy

I did not review AI-generated code line by line, which would be slow and would not have
caught the navigation bug anyway. Instead, five layers, cheapest first:

**1. The compiler, aggressively.** Multi-module with `implementation` over `api` means the
dependency direction is enforced rather than documented. `:sdui:runtime` cannot import a
concrete component because there is no Gradle edge — the open/closed claim is structural.

**2. Unit tests on the parts I refuse to re-verify by hand.** 19 tests, all plain JVM,
no emulator:

- `SduiSchemaTest` (12) - unknown keys, unknown types, unknown actions, fallback nodes, edge
  shorthand precedence, condition evaluation, template resolution, and that props resolution
  returns *the same instance* when nothing changed, since the renderer relies on that
  identity.
- `PayloadContractTest` (7) - every payload against the shipped registry: unregistered types
  (asserting **exactly** the two deliberate landmines, so a typo cannot hide among them),
  dangling `navigate` routes, duplicate node ids, `visibleWhen` on unwritten keys,
  `{{state.x}}` with no source and no default.

That second file is the one that matters most, because the payloads became the product and
the renderer is *deliberately* too forgiving to surface their mistakes at runtime.

**3. Drive the real app over adb and assert on the view hierarchy.** Not screenshots for
me to squint at - `uiautomator dump` parsed and asserted:

```
tap "Diesel"  -> rail contains exactly {Tata Nexon XZ+, Ford EcoSport Titanium}
tap "72 mo"   -> EMI reads Rs 10,780, total Rs 7,76,160, caption "for 72 mo"
tap city, "Mumbai" -> header shows Mumbai; buy_listing title shows "12,400 cars in Mumbai"
```

**4. Kill the process and check the disk.** The persistence requirement is only met if it
survives a real kill, so:

```
$ adb shell run-as com.cars24.sdui.debug cat .../sdui_ui_state.preferences_pb | strings
{"localState":{"fuel":"diesel","tenure":"72","emi_monthly":"Rs 10,780",...},
 "scrollIndex":11,"scrollOffset":529,"openSheetId":"emi_breakdown"}
$ adb shell am force-stop ... && adb shell am start ...
--> relaunches with the EMI sheet open showing Rs 10,780
```

And the shared bucket separately: `{"city":"Bengaluru","wish_swift_vxi":"1"}`, confirming
the page-local record correctly *excludes* the shared keys rather than duplicating them.

**5. Force the failure paths rather than trusting them.** Aeroplane mode with cleared data
--> offline screen. Then online once, then aeroplane mode again --> cached content with a stale
banner. Both verified by reading the hierarchy, not by reasoning about the code. And the two
degradation paths are wired into the home payload so they run on **every** launch — if
either regresses, the home page shows it without anyone remembering to test it.

**What this did not cover, honestly:** no instrumented Compose UI tests (`createComposeRule`),
no screenshot tests, and the adb assertions live in my shell history rather than in the repo
as a test suite. On a real team the §3–5 checks belong in CI as instrumented tests. For a
72-hour exercise I chose breadth of verification over automating the harness.
