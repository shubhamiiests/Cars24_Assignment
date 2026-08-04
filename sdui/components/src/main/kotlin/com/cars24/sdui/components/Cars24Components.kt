package com.cars24.sdui.components

import com.cars24.sdui.components.atom.ButtonComponent
import com.cars24.sdui.components.atom.ChipGroupComponent
import com.cars24.sdui.components.atom.ImageComponent
import com.cars24.sdui.components.atom.ListItemComponent
import com.cars24.sdui.components.atom.TagRowComponent
import com.cars24.sdui.components.atom.TextComponent
import com.cars24.sdui.components.layout.CarouselComponent
import com.cars24.sdui.components.layout.ColumnComponent
import com.cars24.sdui.components.layout.DividerComponent
import com.cars24.sdui.components.layout.GridComponent
import com.cars24.sdui.components.layout.RowComponent
import com.cars24.sdui.components.layout.SpacerComponent
import com.cars24.sdui.components.section.BannerCarouselComponent
import com.cars24.sdui.components.section.CarCardComponent
import com.cars24.sdui.components.section.EmiSummaryComponent
import com.cars24.sdui.components.section.FaqItemComponent
import com.cars24.sdui.components.section.QuickActionsComponent
import com.cars24.sdui.components.section.SearchHeaderComponent
import com.cars24.sdui.components.section.SectionHeaderComponent
import com.cars24.sdui.components.section.ValuePropsComponent
import com.cars24.sdui.runtime.registry.ComponentRegistry
import com.cars24.sdui.runtime.registry.SduiComponent
import androidx.compose.ui.tooling.preview.Preview
import com.cars24.sdui.components.preview.SduiNodePreview
import androidx.compose.runtime.Composable

object Cars24Components {

    val layoutAndAtoms: List<SduiComponent> = listOf(
        ColumnComponent(),
        RowComponent(),
        CarouselComponent(),
        GridComponent(),
        SpacerComponent(),
        DividerComponent(),
        TextComponent(),
        ImageComponent(),
        ButtonComponent(),
        ChipGroupComponent(),
        TagRowComponent(),
        ListItemComponent(),
    )

    val sections: List<SduiComponent> = listOf(
        SearchHeaderComponent(),
        SectionHeaderComponent(),
        BannerCarouselComponent(),
        QuickActionsComponent(),
        CarCardComponent(),
        ValuePropsComponent(),
        EmiSummaryComponent(),
        FaqItemComponent(),
    )

    val all: List<SduiComponent> = layoutAndAtoms + sections

    fun registry(): ComponentRegistry = ComponentRegistry(all)
}

@Preview(showBackground = true, widthDp = 380)
@Composable
private fun UnknownComponentTypePreview() = SduiNodePreview(
    """{ "id": "p", "type": "ar_showroom_360", "props": { "carId": "creta_sx" } }""",
)

@Preview(showBackground = true, widthDp = 380)
@Composable
private fun SchemaTooNewFallbackPreview() = SduiNodePreview(
    """
    {
      "id": "p", "type": "loyalty_tier_card", "minSchemaVersion": 99,
      "props": { "tier": "gold" },
      "fallback": { "id": "fb", "type": "value_props",
        "props": { "heading": "Cars24 rewards",
                   "items": [ { "title": "Update", "caption": "to see rewards", "icon": "warranty" } ] } }
    }
    """,
)
