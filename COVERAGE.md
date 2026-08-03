# COVERAGE.md

What this schema can express, what it cannot, and an honest guess at the surprise-screen
round.

## 1. The registry

20 components. The split matters more than the count: **eleven are generic** and would
render a food-delivery app unchanged, **eight are Cars24 vocabulary**, and one is the
degraded placeholder the renderer falls back to.

### Generic (11): know nothing about cars

| Type | Props that matter | Notes |
|---|---|---|
| `column` | `spacing`, `align` | Non-lazy. Any depth. |
| `row` | `spacing`, `align`, `arrangement` | `space_between`, `space_around`, `center`, `end` |
| `carousel` | `itemSpacing`, `startPadding`, `endPadding` | `LazyRow`. Any children. |
| `grid` | `columns`, `itemSpacing`, `rowSpacing` | Rows of weighted columns, not a lazy grid — see §5 |
| `spacer` | `size` | |
| `divider` | `thickness`, `insetStart`, `insetEnd` | |
| `text` | `value`, `style`, `color`, `align`, `weight`, `maxLines` | `value` is template-aware |
| `image` | `url`, `seed`, `cornerRadius`, `height` | Gradient placeholder underneath, from `seed` |
| `button` | `label`, `variant`, `fillWidth` | `primary` / `accent` / `outline` |
| `chip_group` | `stateKey`, `options[]`, `scrollable` | **The interactive primitive.** Writes page state |
| `list_item` | `title`, `subtitle`, `trailing`, `icon`, `selectedWhenKey/Value`, `showChevron` | Rows, pickers, spec tables, menus |
| `tag_row` | `tags[]`, `emphasisedFirst` | |

### Cars24 vocabulary (8)

| Type | What it is |
|---|---|
| `search_header` | Gradient chrome: city selector, wordmark, search field. Two triggers: `onClick`, `onCityClick` |
| `section_header` | Title / subtitle / "View all". Used 22 times across the payloads |
| `banner_carousel` | Offer banners, each slide with its own action and gradient |
| `quick_actions` | The four-up entry tiles |
| `car_card` | One vehicle. Serves rails **and** grids via `layout`/`fillWidth`. Owns the wishlist heart |
| `value_props` | The trust strip |
| `emi_summary` | Gradient EMI panel with rows and a CTA |
| `faq_item` | Expandable question |

### Style, applied by the renderer to every node regardless of type

`padding`, `margin` (both with `all` / `horizontal` / `vertical` / per-side precedence),
`background`, `gradient`, `cornerRadius`, `borderWidth`, `borderColor`, `elevation`,
`width`, `height` (`fill` / `wrap` / dp), `aspectRatio`, `alpha`.

Colours accept **theme tokens** (`surface`, `accent`, `success`, `danger`, `divider`,
`text_secondary`, `price`, …) or hex. Tokens follow the user into dark mode; `#FFFFFF` does
not. This is why a payload should almost always use tokens.

### Actions

`navigate` · `open_bottom_sheet` · `dismiss_bottom_sheet` · `set_state` · `toggle_state` ·
`track_event` · `open_url` · `refresh`

Plus two composition features that remove the need for most new action types: **`then`**
chains actions (so "set tenure, set the EMI figure, set the label, track it" is one
declaration), and **`content`** carries nested SDUI nodes (so a bottom sheet body is itself
server-driven).

### Conditionals and data binding

- `visibleWhen`: `equals`, `notEquals`, `oneOf`, `exists` against one page-state key.
- `{{state.key}}` and `{{state.key|default}}` in any string prop.
- `initialState` seeds defaults; `sharedStateKeys` marks keys as app-wide rather than
  per-page.
- `navigate` params land in the destination's state map.

## 2. What is actually built with it

Eleven pages, **213 sections / 373 nodes / 173 KB of JSON**, and one screen composable.

| Page | Sections | Nodes | Bytes |
|---|---|---|---|
| `home` | 43 | 84 | 49,660 |
| `car_detail` | 25 | 38 | 15,243 |
| `wishlist` | 23 | 39 | 25,902 |
| `search` | 18 | 33 | 20,623 |
| `returns_policy` | 18 | 22 | 5,391 |
| `sell_flow` | 18 | 22 | 5,417 |
| `buy_listing` | 16 | 35 | 24,523 |
| `insurance` | 13 | 31 | 7,749 |
| `loan` | 13 | 26 | 12,752 |
| `loan_application` | 13 | 23 | 5,467 |
| `offer` | 13 | 20 | 4,792 |

Four things in that list are worth pointing at, because they are the actual test of whether
the schema generalises:

1. **The wishlist page contains no wishlist code.** Every card is gated on `visibleWhen`
   against its own shared `wish_<id>` key. Tapping a heart on the home page changes what
   that page shows, with nothing in between.
2. **The city picker is not a component.** It is a `column` of `list_item`s that tick
   themselves when state matches, each with a `set_state` + `dismiss_bottom_sheet` action.
3. **`car_detail` is one payload for every car.** The tapped card's `navigate` params become
   the destination's state, and the page reads `{{state.name}}`, `{{state.price}}`,
   `{{state.image}}`.
4. **The EMI tenure selector is not a component either.** It is `chip_group` +
   `emi_summary`, where each chip carries a chained `set_state` that writes the
   pre-computed monthly figure. **The client performs no loan arithmetic at all** - changing
   the interest rate is a payload edit.

Also worth noting what is *not* a component: the footer CTA (styled `column` + `text` +
`button`), the "how it works" step lists (`list_item` × N), the insurance plan cards
(styled `column` + `list_item`s), and the white panels throughout (`column` with
`background` + `cornerRadius` + `borderWidth`).

## 3. The coverage claim

**For a new Cars24 screen built from the patterns already in the app, I would expect
roughly 80–85% to render from JSON alone**, with the remainder needing a component.

That number is a judgement, not a measurement, so here is the reasoning rather than just the
figure. Taking the node-type census across all eleven payloads:

| Category | Node instances | Share |
|---|---|---|
| Generic primitives (`spacer`, `column`, `text`, `list_item`, `button`, `grid`, `carousel`, `row`, `image`, `chip_group`, `tag_row`) | 253 | **68%** |
| Cars24 sections (`car_card`, `section_header`, `faq_item`, `value_props`, `emi_summary`, `banner_carousel`, `search_header`, `quick_actions`) | 118 | 32% |
| Deliberate unknowns | 2 | <1% |

68% of what I actually built is generic. The 80–85% claim is higher than that because a new
screen would reuse the existing Cars24 sections too - `car_card` and `section_header` alone
account for 84 instances and any car-related screen wants both.

**Where I would expect to lose the other 15–20%,** in order of likelihood:

1. **A genuinely new visual pattern.** A comparison table, a 360° viewer, a map, a photo
   gallery with pinch-zoom, a stepper form with validation. No amount of schema design
   avoids this; it is new pixels.
2. **Text input.** There is no input component at all (see §5). Any screen with a real form
   needs one.
3. **An icon we do not ship.** Icons are name-referenced against a fixed set (§5).
4. **A layout the primitives cannot express** — see the honest list below.


## 4. If you hand me a screen I have not seen

What I would do, in order:

1. Write the JSON against the existing registry. Push it with
   `adb push page.json /sdcard/Android/data/com.cars24.sdui/files/sdui/<name>.json` - the
   `FileOverridePageDataSource` picks it up ahead of the bundled asset, so **no rebuild and
   no reinstall**, pull to refresh and it is on screen.
2. Run `./gradlew :sdui:components:testDebugUnitTest`. `PayloadContractTest` fails on an
   unregistered type, a `navigate` to a page nobody wrote, a duplicate node id, a
   `visibleWhen` on a key nothing sets, or a `{{state.x}}` with no source and no default.
   That is the list of mistakes the renderer is deliberately too forgiving to surface at
   runtime.
3. For anything left over, add a component: one class implementing `SduiComponent`, one line
   in `Cars24Components.all`. **Nothing in `:sdui:runtime` changes** - the runtime depends on
   the `SduiComponent` interface and has no Gradle dependency on `:sdui:components`, which is
   what makes that guarantee structural rather than a promise.

Realistically a new leaf component is 30–60 lines: a `@Serializable` props class, a
`Render` that reads `rememberProps` and dispatches `SduiTriggers.ON_CLICK`. I would expect
15–25 minutes for something like a comparison row, longer for anything with gestures.

Meanwhile the page **already renders** - the unknown section shows a placeholder naming the
type, and everything around it works. That is the part I would want judged: not that the
first attempt is 100%, but that 85% ships immediately and the missing 15% is a visible,
named gap rather than a crash.


## 5. What the schema cannot do

The interesting half of this document.

### No text input
There is no `text_field`. Every "form" in these payloads is informational with a CTA at the
end. A real sell-flow or loan application needs input, which drags in validation rules,
keyboard types, error copy and submission — that is a schema extension, not a component,
and I would want to design it deliberately rather than bolt it on.

### Conditions are single-key
`visibleWhen` takes one key. It cannot express "show this if *nothing* is saved", which is
why the wishlist page has a note that reads correctly whether or not cars are saved rather
than a proper empty state. Options are a server-computed flag, or `allOf`/`anyOf` in the
condition schema. I chose neither, because an expression language in JSON is a debugging
surface nobody on either side of the API wants at 2am.

### No client-side computation, by design
The client cannot derive a value. Every number is server-supplied and pre-formatted, EMI
figures included. This is deliberate — formatting money on the client bakes lakh/crore
conventions and rounding into the binary — but it means the server has to send every
variant of a computed figure. Four tenures means four `set_state` chains.

### Icons come from a fixed set
`material-icons-extended` is pinned at 1.7.8 while the Compose BOM is on 2026.02, and a
stale transitive was not worth six glyphs. So payloads reference icons by name against
`material-icons-core` (~48 icons), and unknown names fall back rather than showing an empty
box. **A new icon is a client change.** A real implementation would ship vector drawables in
a resource pack, or accept remote SVGs and take the security review that comes with it.

### Grids are not lazy
`grid` builds rows of weighted columns because nesting a `LazyVerticalGrid` inside the page's
`LazyColumn` crashes at runtime. A 200-item grid would compose all 200 rows. The outer list
is lazy so it only happens when the grid scrolls into view, but a payload author could still
build something pathological. A `lazy_grid` section that owns its own vertical scrolling
would fix it.

### No animation vocabulary
Transitions are whatever each component hardcodes (the chip colour crossfade, the FAQ
chevron rotation). A payload cannot ask for a shared-element transition or a staggered
entrance.

### One state scope, two buckets
State is either page-local or app-wide (`sharedStateKeys`). There is no per-section scope,
so two `chip_group`s on one page must use different `stateKey`s - a real constraint, and
`PayloadContractTest` does not currently catch a collision.

### Nesting depth is unbounded
Nothing stops a payload declaring 500 levels of nested `column`. It would stack-overflow
during composition. A depth cap in the renderer is a five-line fix I have not made.

### Fallback chains are capped at one level
Deliberate: a `fallback` whose type is also unknown collapses rather than recursing, so a
server cannot write a chain that spins.

## 6. Versioning

Three independent mechanisms, because they fail at different granularities:

| Mechanism | Granularity | Behaviour |
|---|---|---|
| `ignoreUnknownKeys = true` | Field | A new server field is ignored by old builds. This is what makes the whole system safe to iterate on |
| Unknown `type` | Section | Placeholder naming the type; `sdui_unsupported_component` analytics event; rest of the page unaffected |
| `minSchemaVersion` + `fallback` | Section | Server marks a section as needing a newer client **and supplies what an old client should draw instead** |
| `SduiJson.SUPPORTED_SCHEMA_VERSION` | Page | The contract version this build understands. Currently `2` |

Both degradation paths run on **every launch of the home page**, not only in a demo:
`ar_showroom_360` has no component (--> placeholder), and `loyalty_tier_card` declares
`minSchemaVersion: 5` (--> renders its server-supplied `value_props` fallback). If either
path regresses, the home page shows it immediately.

Unknown **action** types degrade the same way: `SduiActionParser` returns
`SduiCommand.Unsupported`, which reports to analytics and shows "Update the app to use
this" rather than doing nothing silently.
