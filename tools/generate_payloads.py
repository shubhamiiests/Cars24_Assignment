"""Generates every SDUI payload in data/src/main/assets/sdui.

Kept as a script rather than hand-edited JSON because the same car appears on the home
rails, the listing grid, the wishlist and the detail page, and hand-syncing four copies of
a price is how demos end up contradicting themselves.
"""
import collections
import json
import os

OUT_DIR = "data/src/main/assets/sdui"

PHOTOS = [
    "1552519507-da3b142c6e3d", "1503376780353-7e6692767b70", "1583121274602-3e2820c69888",
    "1494976388531-d1058494cdd8", "1533473359331-0135ef1b58bf", "1502877338535-766e1452684a",
    "1550355291-bbee04a92027", "1605559424843-9e4c228bf1c2", "1492144534655-ae79c964c9d7",
    "1517672651691-24622a91b550", "1568605117036-5fe5e7bab0b7", "1549317661-bd32c8ce0db2",
    "1511919884226-fd3cad34687c", "1580273916550-e323be2ae537", "1553440569-bcc63803a83d",
    "1493238792000-8113da705763", "1523983254932-c7e6571c9d60",
]


def photo(index, width=640):
    return ("https://images.unsplash.com/photo-%s?w=%d&q=72&auto=format&fit=crop"
            % (PHOTOS[index % len(PHOTOS)], width))


def node(id, type, props=None, style=None, children=None, actions=None,
         visible=None, min_schema=None, fallback=None):
    d = collections.OrderedDict(id=id, type=type)
    if props is not None:
        d["props"] = props
    if style is not None:
        d["style"] = style
    if visible is not None:
        d["visibleWhen"] = visible
    if min_schema is not None:
        d["minSchemaVersion"] = min_schema
    if actions is not None:
        d["actions"] = actions
    if children is not None:
        d["children"] = children
    if fallback is not None:
        d["fallback"] = fallback
    return d


def action(type, params=None, content=None, then=None):
    d = collections.OrderedDict(type=type)
    if params:
        d["params"] = params
    if content:
        d["content"] = content
    if then:
        d["then"] = then
    return d


def nav(route, **params):
    return action("navigate", dict(route=route, **params))


def gap(id, size):
    return node(id, "spacer", {"size": size})


def text(id, value, style="body_medium", color=None, align=None, weight=None, pad=16):
    props = {"value": value, "style": style}
    if color:
        props["color"] = color
    if align:
        props["align"] = align
    if weight:
        props["weight"] = weight
    return node(id, "text", props, style={"padding": {"horizontal": pad}})


def header(id, title, subtitle=None, action_label=None, route=None, **route_params):
    props = {"title": title}
    if subtitle:
        props["subtitle"] = subtitle
    if action_label:
        props["actionLabel"] = action_label
    actions = {"onClick": nav(route, **route_params)} if route else None
    return node(id, "section_header", props, actions=actions)


def cta(id, label, route, variant="accent", fill=True, **route_params):
    return node(id, "button",
                {"label": label, "variant": variant, "fillWidth": fill},
                style={"padding": {"horizontal": 16}},
                actions={"onClick": nav(route, **route_params)})


# ---------------------------------------------------------------- catalogue

CarSpec = collections.namedtuple(
    "CarSpec", "id name price emi specs photo savings badge assured")


def car_spec(id, name, price, emi, specs, photo_index,
             savings=None, badge=None, assured=False):
    return CarSpec(id, name, price, emi, specs, photo_index, savings, badge, assured)


CATALOGUE = [
    car_spec("swift_vxi", "Maruti Swift VXi", "Rs 5.24 L", "Rs 11,400/mo",
             ["2019", "42,150 km", "Petrol", "Manual"], 0, savings="Save 38k", assured=True),
    car_spec("i20_sportz", "Hyundai i20 Sportz", "Rs 6.85 L", "Rs 14,820/mo",
             ["2020", "31,900 km", "Petrol", "Manual"], 1, badge="Popular", assured=True),
    car_spec("nexon_xz", "Tata Nexon XZ+", "Rs 7.95 L", "Rs 17,190/mo",
             ["2019", "55,600 km", "Diesel", "Manual"], 2),
    car_spec("wagonr_lxi", "Maruti WagonR LXi", "Rs 4.60 L", "Rs 9,950/mo",
             ["2021", "28,400 km", "CNG", "Manual"], 3, badge="Low km"),
    car_spec("baleno_zeta", "Maruti Baleno Zeta", "Rs 6.40 L", "Rs 13,850/mo",
             ["2020", "36,800 km", "Petrol", "AMT"], 4, assured=True),
    car_spec("i10_nios", "Hyundai Grand i10 Nios", "Rs 5.10 L", "Rs 11,040/mo",
             ["2021", "22,300 km", "Petrol", "Manual"], 5, badge="Low km"),
    car_spec("ecosport_titanium", "Ford EcoSport Titanium", "Rs 6.75 L", "Rs 14,600/mo",
             ["2018", "68,200 km", "Diesel", "Manual"], 6, savings="Save 52k"),
    car_spec("alto_k10", "Maruti Alto K10 VXi", "Rs 3.85 L", "Rs 8,330/mo",
             ["2022", "19,700 km", "CNG", "Manual"], 7, assured=True),
    car_spec("creta_sx", "Hyundai Creta SX", "Rs 11.40 L", "Rs 24,650/mo",
             ["2020", "44,100 km", "Petrol", "Manual"], 8, assured=True),
    car_spec("venue_s", "Hyundai Venue S", "Rs 8.20 L", "Rs 17,740/mo",
             ["2021", "29,600 km", "Petrol", "Manual"], 9, badge="New"),
    car_spec("brezza_zxi", "Maruti Brezza ZXi", "Rs 9.65 L", "Rs 20,880/mo",
             ["2020", "38,500 km", "Petrol", "AMT"], 10, assured=True),
    car_spec("kwid_rxt", "Renault Kwid RXT", "Rs 3.40 L", "Rs 7,360/mo",
             ["2019", "51,200 km", "Petrol", "Manual"], 11, savings="Save 24k"),
    car_spec("city_vx", "Honda City VX", "Rs 9.10 L", "Rs 19,690/mo",
             ["2019", "47,300 km", "Petrol", "CVT"], 12, assured=True),
    car_spec("altroz_xz", "Tata Altroz XZ", "Rs 6.95 L", "Rs 15,040/mo",
             ["2021", "24,900 km", "Petrol", "Manual"], 13, badge="Popular"),
    car_spec("seltos_htk", "Kia Seltos HTK+", "Rs 12.75 L", "Rs 27,580/mo",
             ["2020", "41,700 km", "Diesel", "Manual"], 14, assured=True),
    car_spec("tiago_xt", "Tata Tiago XT", "Rs 4.95 L", "Rs 10,710/mo",
             ["2020", "33,400 km", "Petrol", "Manual"], 15, savings="Save 19k"),
]

BY_ID = {c.id: c for c in CATALOGUE}
FUEL_OF = {c.id: c.specs[2].lower() for c in CATALOGUE}


def car_card(car, id_suffix="", fill=False, layout="vertical"):
    props = collections.OrderedDict(
        name=car.name,
        price=car.price,
        emi=car.emi,
        specs=list(car.specs),
        imageUrl=photo(car.photo),
        wishKey="wish_%s" % car.id,
    )
    if car.badge:
        props["badge"] = car.badge
    if car.savings:
        props["savings"] = car.savings
    if car.assured:
        props["assured"] = True
    if layout != "vertical":
        props["layout"] = layout
    if fill:
        props["fillWidth"] = True

    return node(
        "card_%s%s" % (car.id, id_suffix),
        "car_card",
        props,
        actions={"onClick": nav("car_detail", carId=car.id, name=car.name,
                                price=car.price, emi=car.emi,
                                specs=" | ".join(car.specs), image=photo(car.photo, 900))},
    )


# ---------------------------------------------------------------- shared bits

CITIES = ["Gurgaon", "Delhi", "Noida", "Mumbai", "Bengaluru", "Hyderabad", "Pune", "Jaipur"]


def city_sheet_content():
    """The city picker: a heading plus one list_item per city.

    There is no city-picker component. Each row is a generic list_item that ticks itself
    when page state matches, and whose action writes the city and closes the sheet.
    """
    rows = [
        node("sheet_city_title", "text",
             {"value": "Choose your city", "style": "headline_small"},
             style={"padding": {"horizontal": 20, "top": 4, "bottom": 8}}),
        node("sheet_city_note", "text",
             {"value": "Delivery charges and stock vary by city.",
              "style": "body_small", "color": "text_secondary"},
             style={"padding": {"horizontal": 20, "bottom": 8}}),
    ]
    for city in CITIES:
        rows.append(node(
            "sheet_city_%s" % city.lower(),
            "list_item",
            {"title": city, "icon": "location",
             "selectedWhenKey": "city", "selectedWhenValue": city},
            actions={"onClick": action(
                "set_state", {"key": "city", "value": city},
                then=[action("track_event", {"name": "city_changed", "city": city}),
                      action("dismiss_bottom_sheet")])},
        ))
    rows.append(gap("sheet_city_end", 16))
    return rows


TRUST_STRIP = {
    "heading": "Every Cars24 car comes with",
    "items": [
        {"title": "140-point", "caption": "inspection", "icon": "inspection"},
        {"title": "7-day", "caption": "money back", "icon": "return"},
        {"title": "Free RC", "caption": "transfer", "icon": "paperwork"},
        {"title": "1-year", "caption": "warranty", "icon": "warranty"},
    ],
}

TENURES = [("36 mo", "36", "Rs 18,240", "Rs 6,56,640"),
           ("48 mo", "48", "Rs 14,820", "Rs 7,11,360"),
           ("60 mo", "60", "Rs 12,410", "Rs 7,44,600"),
           ("72 mo", "72", "Rs 10,780", "Rs 7,76,160")]


def tenure_option(label, months, monthly, total):
    return {
        "label": label, "value": months,
        "action": action("set_state", {"key": "tenure", "value": months}, then=[
            action("set_state", {"key": "emi_monthly", "value": monthly}),
            action("set_state", {"key": "emi_total", "value": total}),
            action("set_state", {"key": "emi_tenure_label", "value": label}),
            action("track_event", {"name": "emi_tenure_selected", "tenure": months}),
        ]),
    }


def tenure_chips(id="tenure_chips"):
    return node(id, "chip_group",
                {"stateKey": "tenure", "scrollable": False,
                 "options": [tenure_option(*t) for t in TENURES]},
                style={"padding": {"horizontal": 16}})


EMI_SHEET = [
    node("sheet_title", "text", {"value": "EMI breakdown", "style": "headline_small"},
         style={"padding": {"horizontal": 20, "top": 4}}),
    node("sheet_sub", "text",
         {"value": "For {{state.emi_tenure_label}} at 9.7% p.a.",
          "style": "body_small", "color": "text_secondary"},
         style={"padding": {"horizontal": 20, "top": 4}}),
    gap("sheet_gap_1", 20),
    node("sheet_rows", "emi_summary", {
        "heading": "Monthly instalment",
        "monthly": "{{state.emi_monthly}}",
        "monthlyCaption": "x {{state.emi_tenure_label}}",
        "rows": [
            {"label": "Total payable", "value": "{{state.emi_total}}"},
            {"label": "Down payment", "value": "Rs 1,20,000"},
            {"label": "Interest rate", "value": "9.7% p.a."},
            {"label": "Processing fee", "value": "Rs 3,499"},
        ],
    }),
    gap("sheet_gap_2", 16),
    node("sheet_note", "text", {
        "value": "Rates are indicative and depend on your credit profile. Final terms are "
                 "shared by the lending partner before you sign.",
        "style": "label_small", "color": "text_tertiary",
    }, style={"padding": {"horizontal": 20}}),
    gap("sheet_gap_3", 16),
    node("sheet_cta", "button",
         {"label": "Apply for this loan", "variant": "accent", "fillWidth": True},
         style={"padding": {"horizontal": 20}},
         actions={"onClick": action("navigate", {"route": "loan_application"},
                                    then=[action("dismiss_bottom_sheet")])}),
    gap("sheet_gap_4", 12),
]

SHARED_KEYS = ["city"] + ["wish_%s" % c.id for c in CATALOGUE]


def page(page_id, title, sections, initial=None, analytics=None, background="page"):
    return collections.OrderedDict(
        pageId=page_id,
        schemaVersion=2,
        title=title,
        analyticsName=analytics or page_id,
        background=background,
        initialState=initial or {},
        sharedStateKeys=SHARED_KEYS,
        sections=sections,
    )


def steps(prefix, items, icon="check"):
    out = []
    for index, (title, subtitle) in enumerate(items, start=1):
        out.append(node("%s_%d" % (prefix, index), "list_item",
                        {"title": "%d. %s" % (index, title), "subtitle": subtitle,
                         "icon": icon}))
    return out


def faq_block(prefix, entries, first_open=True):
    out = []
    for index, (question, answer) in enumerate(entries, start=1):
        out.append(node("%s_%d" % (prefix, index), "faq_item",
                        {"question": question, "answer": answer,
                         "startExpanded": first_open and index == 1}))
        out.append(gap("%s_gap_%d" % (prefix, index), 8))
    return out


def card_panel(id, children, pad=20):
    """A white rounded panel. Composed from a styled column, not a new component."""
    return node(id, "column", {"spacing": 4},
                style={"margin": {"horizontal": 16}, "padding": {"all": pad},
                       "background": "surface", "cornerRadius": 16,
                       "borderWidth": 1, "borderColor": "divider"},
                children=children)


# ---------------------------------------------------------------- home

def build_home():
    s = []
    s.append(node("hero_header", "search_header", {
        "city": "{{state.city|Gurgaon}}",
        "greeting": "Find your next car",
        "searchHint": "Search Swift, Baleno, i20, Nexon...",
    }, actions={
        "onClick": nav("search"),
        "onCityClick": action("open_bottom_sheet",
                              {"sheetId": "city_picker", "title": "Choose your city"},
                              content=city_sheet_content()),
    }))
    s.append(gap("gap_after_header", 16))

    s.append(node("quick_actions", "quick_actions", {"actions": [
        {"label": "Buy car", "icon": "buy", "caption": "12,400 cars",
         "action": nav("buy_listing")},
        {"label": "Sell car", "icon": "sell", "caption": "Best price",
         "action": nav("sell_flow")},
        {"label": "Car loan", "icon": "loan", "caption": "From 9.7%",
         "action": nav("loan")},
        {"label": "Insurance", "icon": "insurance", "caption": "Renew fast",
         "action": nav("insurance")},
    ]}))
    s.append(gap("gap_after_quick", 20))

    s.append(node("offer_banners", "banner_carousel", {"height": 150, "slides": [
        {"title": "Zero down payment", "subtitle": "On 2,000+ assured cars this month",
         "ctaLabel": "Check eligibility", "gradient": ["#1B2065", "#5865C4"],
         "action": nav("offer", offerId="zero_dp")},
        {"title": "Sell in a single visit", "subtitle": "Instant payment, free RC transfer",
         "ctaLabel": "Get a quote", "gradient": ["#0B8A6B", "#3FCFA8"],
         "action": nav("sell_flow")},
        {"title": "7-day money back", "subtitle": "Not in love with it? Return it.",
         "ctaLabel": "How it works", "gradient": ["#E8890C", "#FFC46B"],
         "action": nav("returns_policy")},
    ]}))
    s.append(gap("gap_after_banners", 24))

    s.append(header("budget_header", "Cars in your budget",
                    "Under 8 lakh, ready to drive", "View all",
                    route="buy_listing", filter="budget"))
    s.append(gap("gap_before_fuel_tabs", 12))

    counts = collections.Counter(FUEL_OF.values())
    s.append(node("fuel_tabs", "chip_group", {
        "stateKey": "fuel",
        "options": [
            {"label": "All", "value": "all", "supporting": "412"},
            {"label": "Petrol", "value": "petrol", "supporting": "268"},
            {"label": "Diesel", "value": "diesel", "supporting": "91"},
            {"label": "CNG", "value": "cng", "supporting": "53"},
        ],
    }, actions={"onSelect": action("track_event", {"name": "home_fuel_filter_changed"})}))
    s.append(gap("gap_after_fuel_tabs", 12))

    budget = [c for c in CATALOGUE[:8]]
    rails = {
        "all": budget[:4],
        "petrol": [c for c in budget if FUEL_OF[c.id] == "petrol"],
        "diesel": [c for c in budget if FUEL_OF[c.id] == "diesel"],
        "cng": [c for c in budget if FUEL_OF[c.id] == "cng"],
    }
    for fuel, cars in rails.items():
        s.append(node("rail_%s" % fuel, "carousel", {"itemSpacing": 12},
                      visible={"key": "fuel", "equals": fuel},
                      children=[car_card(c, "_%s" % fuel) for c in cars]))
    s.append(gap("gap_after_rails", 28))

    s.append(header("emi_header", "Plan your EMI",
                    "On a Rs 6.85 L car with Rs 1.2 L down payment"))
    s.append(gap("gap_before_tenure", 12))
    s.append(tenure_chips())
    s.append(gap("gap_after_tenure", 16))
    s.append(node("emi_card", "emi_summary", {
        "heading": "Your monthly EMI",
        "monthly": "{{state.emi_monthly}}",
        "monthlyCaption": "for {{state.emi_tenure_label}}",
        "rows": [
            {"label": "Total payable", "value": "{{state.emi_total}}"},
            {"label": "Interest rate", "value": "9.7% p.a."},
            {"label": "Down payment", "value": "Rs 1,20,000"},
        ],
        "ctaLabel": "See full breakdown",
    }, actions={"onClick": action(
        "open_bottom_sheet", {"sheetId": "emi_breakdown", "title": "EMI breakdown"},
        content=EMI_SHEET,
        then=[action("track_event", {"name": "emi_sheet_opened"})])}))
    s.append(gap("gap_after_emi", 28))

    s.append(node("trust_strip", "value_props", TRUST_STRIP))
    s.append(gap("gap_after_trust", 20))

    s.append(card_panel("saved_entry", [
        node("saved_row", "list_item",
             {"title": "Saved cars", "subtitle": "Everything you tapped the heart on",
              "icon": "check", "showChevron": True},
             actions={"onClick": nav("wishlist")}),
    ], pad=4))
    s.append(gap("gap_after_saved", 28))

    s.append(header("assured_header", "Assured cars near you",
                    "Inspected, serviced and ready for delivery", "See all",
                    route="buy_listing", filter="assured"))
    s.append(gap("gap_before_grid", 12))
    s.append(node("assured_grid", "grid",
                  {"columns": 2, "itemSpacing": 12, "rowSpacing": 12},
                  style={"padding": {"horizontal": 16}},
                  children=[car_card(c, "_grid", fill=True) for c in CATALOGUE[8:12]]))
    s.append(gap("gap_after_grid", 28))

    # Two deliberate landmines so the degradation paths run on every launch rather than
    # only in a contrived demo.
    s.append(node("showroom_360", "ar_showroom_360",
                  {"carId": "creta_sx", "hotspots": ["interior", "boot", "engine"]}))
    s.append(node("loyalty_tier", "loyalty_tier_card", {"tier": "gold", "points": 4820},
                  min_schema=5,
                  fallback=node("loyalty_tier_fb", "value_props", {
                      "heading": "Cars24 rewards",
                      "items": [{"title": "Update", "caption": "to see rewards",
                                 "icon": "warranty"}],
                  })))
    s.append(gap("gap_after_experiments", 28))

    s.append(header("faq_header", "Questions, answered"))
    s.append(gap("gap_before_faq", 12))
    s.extend(faq_block("faq", [
        ("How does the 7-day money back work?",
         "Drive the car for up to 7 days or 350 km. If it is not right for you, return it "
         "at any Cars24 hub and we refund the full amount, no questions asked."),
        ("Is the RC transfer really free?",
         "Yes. We handle the paperwork end to end and absorb the transfer fee. You get an "
         "SMS from the RTO once it completes, usually within 45 days."),
        ("Can I get a loan without a credit history?",
         "Often yes. We work with 15 lending partners and some approve first-time "
         "borrowers against income proof. The EMI above is indicative until a partner "
         "quotes you."),
    ]))
    s.append(gap("gap_before_footer", 20))

    s.append(node("sell_cta", "column", {"spacing": 12, "align": "center"},
                  style={"margin": {"horizontal": 16}, "padding": {"all": 24},
                         "gradient": ["#11144B", "#2E3A8C"], "cornerRadius": 20},
                  children=[
                      node("sell_cta_title", "text",
                           {"value": "Sell your car in a single visit",
                            "style": "title_large", "color": "white", "align": "center"}),
                      node("sell_cta_body", "text",
                           {"value": "Free evaluation, instant payment, and we take care "
                                     "of the paperwork.",
                            "style": "body_small", "color": "white", "align": "center"}),
                      node("sell_cta_button", "button",
                           {"label": "Get a free quote", "variant": "accent"},
                           actions={"onClick": nav("sell_flow", source="home_footer")}),
                  ]))
    s.append(gap("gap_footer_bottom", 32))

    return page("home", "Cars24", s,
                initial=collections.OrderedDict([
                    ("city", "Gurgaon"), ("fuel", "all"), ("tenure", "48"),
                    ("emi_monthly", "Rs 14,820"), ("emi_total", "Rs 7,11,360"),
                    ("emi_tenure_label", "48 mo"),
                ]),
                analytics="home_landing")


# ---------------------------------------------------------------- destinations

def build_buy_listing():
    s = [
        gap("top", 8),
        header("listing_header", "12,400 cars in {{state.city|Gurgaon}}",
               "Every one inspected on 140 points"),
        gap("gap_1", 12),
        node("budget_chips", "chip_group", {
            "stateKey": "budget",
            "options": [
                {"label": "Under 5L", "value": "u5"},
                {"label": "5-8L", "value": "u8"},
                {"label": "8-12L", "value": "u12"},
                {"label": "12L+", "value": "o12"},
            ],
        }),
        gap("gap_2", 16),
    ]

    buckets = {
        "u5": ["wagonr_lxi", "alto_k10", "kwid_rxt", "tiago_xt"],
        "u8": ["swift_vxi", "i20_sportz", "nexon_xz", "baleno_zeta",
               "i10_nios", "ecosport_titanium", "altroz_xz"],
        "u12": ["creta_sx", "venue_s", "brezza_zxi", "city_vx"],
        "o12": ["seltos_htk", "creta_sx"],
    }
    for bucket, ids in buckets.items():
        s.append(node("grid_%s" % bucket, "grid",
                      {"columns": 2, "itemSpacing": 12, "rowSpacing": 12},
                      style={"padding": {"horizontal": 16}},
                      visible={"key": "budget", "equals": bucket},
                      children=[car_card(BY_ID[i], "_%s" % bucket, fill=True) for i in ids]))

    s += [
        gap("gap_3", 24),
        node("listing_trust", "value_props", TRUST_STRIP),
        gap("gap_4", 24),
        header("listing_help", "Need help choosing?"),
        gap("gap_5", 12),
        card_panel("listing_help_panel", [
            node("help_emi", "list_item",
                 {"title": "Work out an EMI", "subtitle": "Pick a tenure that fits",
                  "icon": "check", "showChevron": True},
                 actions={"onClick": nav("loan")}),
            node("help_saved", "list_item",
                 {"title": "See your saved cars", "subtitle": "Compare side by side",
                  "icon": "check", "showChevron": True},
                 actions={"onClick": nav("wishlist")}),
        ], pad=4),
        gap("bottom", 32),
    ]
    return page("buy_listing", "Buy a car", s, initial={"budget": "u8"})


def build_wishlist():
    """Only saved cars render.

    Every card is gated on its own shared wish_<id> key, so tapping a heart on the home
    page changes what this page shows with no code involved on either side.
    """
    s = [
        gap("top", 12),
        header("wishlist_header", "Saved cars",
               "Tap the heart on any car to add it here"),
        gap("gap_1", 16),
    ]
    for car in CATALOGUE:
        s.append(node("wish_row_%s" % car.id, "column", {"spacing": 0},
                      style={"padding": {"horizontal": 16, "bottom": 12}},
                      visible={"key": "wish_%s" % car.id, "equals": "1"},
                      children=[car_card(car, "_wish", fill=True, layout="horizontal")]))

    s += [
        # Worded to read correctly whether or not anything is saved. A true empty state
        # would need a condition that can ask "is this set of keys all empty", and
        # visibleWhen is single-key by design. Noted in COVERAGE.md rather than papered
        # over with a client-side counter.
        node("wishlist_note", "text",
             {"value": "Saved cars stay here even after you close the app.",
              "style": "body_small", "color": "text_tertiary", "align": "center"},
             style={"padding": {"horizontal": 32, "vertical": 8}}),
        gap("gap_2", 20),
        cta("wishlist_browse", "Browse all cars", "buy_listing", variant="primary"),
        gap("bottom", 32),
    ]
    return page("wishlist", "Saved cars", s)


def build_car_detail():
    s = [
        node("detail_image", "image",
             {"seed": "{{state.name|Cars24}}", "url": "{{state.image}}",
              "cornerRadius": 0, "height": 240}),
        gap("gap_1", 16),
        text("detail_name", "{{state.name|Your next car}}", "headline_small"),
        gap("gap_2", 4),
        text("detail_specs", "{{state.specs|Inspected on 140 points}}",
             "body_small", color="text_secondary"),
        gap("gap_3", 16),
        card_panel("detail_price_panel", [
            node("detail_price", "text",
                 {"value": "{{state.price|Rs 6.85 L}}", "style": "price"}),
            node("detail_emi", "text",
                 {"value": "or {{state.emi|Rs 14,820/mo}} with zero down payment",
                  "style": "body_small", "color": "text_secondary"}),
            gap("detail_price_gap", 12),
            node("detail_tags", "tag_row",
                 {"tags": ["Cars24 Assured", "Fixed price", "Free RC transfer"],
                  "emphasisedFirst": True}),
        ]),
        gap("gap_4", 20),
        node("detail_trust", "value_props", TRUST_STRIP),
        gap("gap_5", 24),
        header("detail_emi_header", "Make it monthly"),
        gap("gap_6", 12),
        tenure_chips("detail_tenure_chips"),
        gap("gap_7", 16),
        node("detail_emi_card", "emi_summary", {
            "heading": "Your monthly EMI",
            "monthly": "{{state.emi_monthly}}",
            "monthlyCaption": "for {{state.emi_tenure_label}}",
            "rows": [
                {"label": "Total payable", "value": "{{state.emi_total}}"},
                {"label": "Interest rate", "value": "9.7% p.a."},
            ],
            "ctaLabel": "See full breakdown",
        }, actions={"onClick": action(
            "open_bottom_sheet", {"sheetId": "emi_breakdown", "title": "EMI breakdown"},
            content=EMI_SHEET)}),
        gap("gap_8", 24),
        header("detail_faq_header", "Before you book"),
        gap("gap_9", 12),
    ]
    s += faq_block("detail_faq", [
        ("Can I take a test drive?",
         "Yes. Book a free home test drive and we bring the car to you, or visit any hub "
         "seven days a week."),
        ("What if I find a problem after delivery?",
         "The 7-day return window covers exactly that. Return the car for a full refund, "
         "or let us fix it under the one-year warranty."),
    ])
    s += [
        gap("gap_10", 12),
        cta("detail_book", "Book a free test drive", "sell_flow"),
        gap("bottom", 32),
    ]
    return page("car_detail", "Car details", s,
                initial=collections.OrderedDict([
                    ("tenure", "48"), ("emi_monthly", "Rs 14,820"),
                    ("emi_total", "Rs 7,11,360"), ("emi_tenure_label", "48 mo"),
                ]))


def build_sell_flow():
    s = [
        gap("top", 12),
        header("sell_header", "Sell in a single visit",
               "Free evaluation, payment the same day"),
        gap("gap_1", 16),
        card_panel("sell_steps", steps("sell_step", [
            ("Tell us about the car", "Make, model, year and kilometres - two minutes"),
            ("Free inspection", "At your home or any Cars24 hub, 140 checkpoints"),
            ("Accept the price", "Fixed, no haggling, valid for seven days"),
            ("Get paid the same day", "We handle the RC transfer and the loan closure"),
        ]), pad=8),
        gap("gap_2", 24),
        node("sell_props", "value_props", {
            "heading": "Why people sell to us",
            "items": [
                {"title": "Same-day", "caption": "payment", "icon": "return"},
                {"title": "Free", "caption": "RC transfer", "icon": "paperwork"},
                {"title": "No", "caption": "hidden charges", "icon": "inspection"},
                {"title": "5,000+", "caption": "cars a week", "icon": "warranty"},
            ],
        }),
        gap("gap_3", 24),
        header("sell_faq_header", "Common questions"),
        gap("gap_4", 12),
    ]
    s += faq_block("sell_faq", [
        ("Is the evaluation really free?",
         "Yes, and there is no obligation to sell. If the price does not work for you, "
         "walk away at no cost."),
        ("What if the car still has a loan on it?",
         "We settle the outstanding amount with your lender directly and pay you the "
         "balance. It adds a couple of days for the NOC."),
        ("What documents do I need?",
         "RC, insurance, both keys, and a photo ID. Bring the service history if you have "
         "it - it usually improves the price."),
    ])
    s += [
        gap("gap_5", 12),
        cta("sell_cta_button", "Get my free quote", "loan_application"),
        gap("bottom", 32),
    ]
    return page("sell_flow", "Sell your car", s)


def build_loan():
    s = [
        gap("top", 12),
        header("loan_header", "Car loans from 9.7%",
               "15 lending partners, one application"),
        gap("gap_1", 16),
        tenure_chips("loan_tenure_chips"),
        gap("gap_2", 16),
        node("loan_emi", "emi_summary", {
            "heading": "Your monthly EMI",
            "monthly": "{{state.emi_monthly}}",
            "monthlyCaption": "for {{state.emi_tenure_label}}",
            "rows": [
                {"label": "Total payable", "value": "{{state.emi_total}}"},
                {"label": "Interest rate", "value": "9.7% p.a."},
                {"label": "Down payment", "value": "Rs 1,20,000"},
            ],
            "ctaLabel": "See full breakdown",
        }, actions={"onClick": action(
            "open_bottom_sheet", {"sheetId": "emi_breakdown", "title": "EMI breakdown"},
            content=EMI_SHEET)}),
        gap("gap_3", 24),
        header("loan_eligibility", "What you need"),
        gap("gap_4", 12),
        card_panel("loan_elig_panel", [
            node("elig_1", "list_item",
                 {"title": "Age 21 to 60", "subtitle": "At the end of the loan term",
                  "icon": "check"}),
            node("elig_2", "list_item",
                 {"title": "Income proof", "subtitle": "Three months of salary slips or ITR",
                  "icon": "check"}),
            node("elig_3", "list_item",
                 {"title": "KYC", "subtitle": "Aadhaar and PAN", "icon": "check"}),
            node("elig_4", "list_item",
                 {"title": "No credit history? Still fine",
                  "subtitle": "Some partners approve first-time borrowers", "icon": "check"}),
        ], pad=8),
        gap("gap_5", 20),
        cta("loan_apply", "Check my eligibility", "loan_application"),
        gap("bottom", 32),
    ]
    return page("loan", "Car loan", s,
                initial=collections.OrderedDict([
                    ("tenure", "48"), ("emi_monthly", "Rs 14,820"),
                    ("emi_total", "Rs 7,11,360"), ("emi_tenure_label", "48 mo"),
                ]))


def build_insurance():
    s = [
        gap("top", 12),
        header("ins_header", "Renew in three minutes",
               "Instant policy, no inspection for most cars"),
        gap("gap_1", 16),
        node("ins_plans", "chip_group", {
            "stateKey": "plan", "scrollable": False,
            "options": [
                {"label": "Third party", "value": "tp"},
                {"label": "Comprehensive", "value": "comp"},
                {"label": "Zero dep", "value": "zd"},
            ],
        }),
        gap("gap_2", 16),
    ]

    plans = {
        "tp": ("Rs 3,416", "Mandatory cover", [
            ("Third-party damage", "Up to Rs 7.5 lakh"),
            ("Personal accident", "Rs 15 lakh owner cover"),
            ("Own damage", "Not covered"),
        ]),
        "comp": ("Rs 8,940", "Most popular", [
            ("Third-party damage", "Up to Rs 7.5 lakh"),
            ("Own damage", "Covered at IDV"),
            ("Roadside assistance", "24x7, unlimited km"),
        ]),
        "zd": ("Rs 12,480", "Nothing deducted", [
            ("Everything in comprehensive", "Included"),
            ("Zero depreciation", "Full claim on plastic and rubber"),
            ("Engine protection", "Included"),
        ]),
    }
    for key, (price, tagline, rows) in plans.items():
        s.append(node("plan_%s" % key, "column", {"spacing": 4},
                      style={"margin": {"horizontal": 16}, "padding": {"all": 20},
                             "background": "surface", "cornerRadius": 16,
                             "borderWidth": 1, "borderColor": "divider"},
                      visible={"key": "plan", "equals": key},
                      children=[
                          node("plan_%s_price" % key, "text",
                               {"value": price, "style": "price"}),
                          node("plan_%s_tag" % key, "text",
                               {"value": tagline, "style": "body_small",
                                "color": "success"}),
                          gap("plan_%s_gap" % key, 12),
                      ] + [
                          node("plan_%s_row_%d" % (key, i), "list_item",
                               {"title": t, "trailing": v})
                          for i, (t, v) in enumerate(rows, start=1)
                      ]))

    s += [
        gap("gap_3", 20),
        node("ins_props", "value_props", {
            "heading": "Claims, handled",
            "items": [
                {"title": "98%", "caption": "claim ratio", "icon": "inspection"},
                {"title": "7,200+", "caption": "garages", "icon": "warranty"},
                {"title": "3 min", "caption": "to renew", "icon": "return"},
                {"title": "Zero", "caption": "paperwork", "icon": "paperwork"},
            ],
        }),
        gap("gap_4", 20),
        cta("ins_cta", "Renew my policy", "loan_application"),
        gap("bottom", 32),
    ]
    return page("insurance", "Car insurance", s, initial={"plan": "comp"})


def build_offer():
    s = [
        gap("top", 12),
        node("offer_hero", "banner_carousel", {"height": 170, "slides": [
            {"title": "Zero down payment", "subtitle": "On 2,000+ assured cars",
             "ctaLabel": "Valid until 31 March", "gradient": ["#1B2065", "#5865C4"]},
        ]}),
        gap("gap_1", 24),
        header("offer_header", "How zero down payment works"),
        gap("gap_2", 12),
        card_panel("offer_steps", steps("offer_step", [
            ("Pick any assured car", "Look for the green tick on the card"),
            ("Get approved", "Soft check, no effect on your credit score"),
            ("Drive away", "We fund the full on-road price, you pay only the EMI"),
        ]), pad=8),
        gap("gap_3", 24),
        header("offer_terms_header", "The fine print"),
        gap("gap_4", 12),
        card_panel("offer_terms", [
            node("term_1", "list_item",
                 {"title": "Subject to lender approval", "subtitle": "Not everyone qualifies"}),
            node("term_2", "list_item",
                 {"title": "Interest from 9.7% p.a.", "subtitle": "Depends on your profile"}),
            node("term_3", "list_item",
                 {"title": "Processing fee Rs 3,499", "subtitle": "Charged once, upfront"}),
            node("term_4", "list_item",
                 {"title": "Cars24 assured stock only", "subtitle": "2,000+ cars right now"}),
        ], pad=8),
        gap("gap_5", 20),
        cta("offer_cta", "Check my eligibility", "loan_application"),
        gap("bottom", 32),
    ]
    return page("offer", "Zero down payment", s)


def build_returns():
    s = [
        gap("top", 12),
        header("ret_header", "7-day money back",
               "Up to 7 days or 350 km, whichever comes first"),
        gap("gap_1", 16),
        card_panel("ret_steps", steps("ret_step", [
            ("Drive it properly", "Take it to work, to the in-laws, up a hill"),
            ("Changed your mind?", "Tell us within 7 days or 350 km"),
            ("Bring it back", "Any Cars24 hub, no explanation needed"),
            ("Full refund", "Same account, within five working days"),
        ]), pad=8),
        gap("gap_2", 24),
        node("ret_props", "value_props", TRUST_STRIP),
        gap("gap_3", 24),
        header("ret_faq_header", "What people ask"),
        gap("gap_4", 12),
    ]
    s += faq_block("ret_faq", [
        ("Do I get the registration charges back too?",
         "The car price and the Cars24 fee are refunded in full. Statutory charges already "
         "paid to the RTO are not refundable, and we tell you the exact figure before you "
         "sign."),
        ("What if I have driven 400 km?",
         "Past 350 km the return window closes, but the one-year warranty still covers "
         "mechanical faults."),
        ("Can I exchange instead of refunding?",
         "Yes, and most people do. Pick anything else in stock and we adjust the difference."),
    ])
    s += [
        gap("gap_5", 12),
        cta("ret_cta", "Browse assured cars", "buy_listing", variant="primary"),
        gap("bottom", 32),
    ]
    return page("returns_policy", "7-day money back", s)


def build_loan_application():
    s = [
        gap("top", 12),
        header("app_header", "You are almost there",
               "One form, 15 lenders, no effect on your credit score"),
        gap("gap_1", 16),
        card_panel("app_summary", [
            node("app_emi_label", "text",
                 {"value": "Applying for", "style": "label_medium",
                  "color": "text_secondary"}),
            node("app_emi", "text",
                 {"value": "{{state.emi_monthly|Rs 14,820}} x "
                           "{{state.emi_tenure_label|48 mo}}", "style": "price"}),
            gap("app_summary_gap", 12),
            node("app_row_1", "list_item",
                 {"title": "Down payment", "trailing": "Rs 1,20,000"}),
            node("app_row_2", "list_item",
                 {"title": "Interest rate", "trailing": "9.7% p.a."}),
            node("app_row_3", "list_item",
                 {"title": "Processing fee", "trailing": "Rs 3,499"}),
        ]),
        gap("gap_2", 24),
        header("app_next_header", "What happens next"),
        gap("gap_3", 12),
        card_panel("app_steps", steps("app_step", [
            ("Soft eligibility check", "Instant, and it does not touch your score"),
            ("Upload three documents", "PAN, Aadhaar, and income proof"),
            ("Offers from partners", "Usually within a working day"),
            ("Pick one and sign", "Digitally, from wherever you are"),
        ]), pad=8),
        gap("gap_4", 20),
        node("app_note", "text",
             {"value": "This demo stops here - there is no real lender behind it, and "
                       "nothing you tap sends data anywhere.",
              "style": "label_small", "color": "text_tertiary", "align": "center"},
             style={"padding": {"horizontal": 32}}),
        gap("gap_5", 16),
        cta("app_cta", "Back to browsing", "buy_listing", variant="primary"),
        gap("bottom", 32),
    ]
    return page("loan_application", "Loan application", s,
                initial=collections.OrderedDict([
                    ("emi_monthly", "Rs 14,820"), ("emi_tenure_label", "48 mo"),
                ]))


def build_search():
    s = [
        gap("top", 12),
        header("search_header_row", "Search cars",
               "In {{state.city|Gurgaon}} and nearby hubs"),
        gap("gap_1", 16),
        node("search_popular_label", "text",
             {"value": "POPULAR RIGHT NOW", "style": "label_small",
              "color": "text_tertiary"},
             style={"padding": {"horizontal": 16}}),
        gap("gap_2", 8),
        node("search_chips", "chip_group", {
            "stateKey": "query",
            "options": [
                {"label": "Swift", "value": "swift"},
                {"label": "i20", "value": "i20"},
                {"label": "Nexon", "value": "nexon"},
                {"label": "Creta", "value": "creta"},
                {"label": "Under 5L", "value": "u5"},
                {"label": "Automatic", "value": "auto"},
            ],
        }),
        gap("gap_3", 20),
    ]

    matches = {
        "swift": ["swift_vxi"],
        "i20": ["i20_sportz"],
        "nexon": ["nexon_xz", "altroz_xz"],
        "creta": ["creta_sx", "seltos_htk"],
        "u5": ["wagonr_lxi", "alto_k10", "kwid_rxt", "tiago_xt"],
        "auto": ["baleno_zeta", "brezza_zxi", "city_vx"],
    }
    for query, ids in matches.items():
        children = [car_card(BY_ID[i], "_q_%s" % query, fill=True, layout="horizontal")
                    for i in ids]
        s.append(node("results_%s" % query, "column", {"spacing": 12},
                      style={"padding": {"horizontal": 16}},
                      visible={"key": "query", "equals": query},
                      children=children))

    s += [
        gap("gap_4", 24),
        header("search_browse_header", "Or just browse"),
        gap("gap_5", 12),
        card_panel("search_browse", [
            node("browse_all", "list_item",
                 {"title": "All 12,400 cars", "icon": "buy", "showChevron": True},
                 actions={"onClick": nav("buy_listing")}),
            node("browse_saved", "list_item",
                 {"title": "Saved cars", "icon": "check", "showChevron": True},
                 actions={"onClick": nav("wishlist")}),
        ], pad=4),
        gap("bottom", 32),
    ]
    return page("search", "Search", s, initial={"query": "swift"})


PAGES = [build_home(), build_buy_listing(), build_car_detail(), build_wishlist(),
         build_sell_flow(), build_loan(), build_insurance(), build_offer(),
         build_returns(), build_loan_application(), build_search()]

os.makedirs(OUT_DIR, exist_ok=True)
for p in PAGES:
    path = os.path.join(OUT_DIR, "%s.json" % p["pageId"])
    with open(path, "w") as f:
        json.dump(p, f, indent=2, ensure_ascii=False)
        f.write("\n")
    print("%-26s %3d sections %6d bytes" % (p["pageId"], len(p["sections"]),
                                            os.path.getsize(path)))
