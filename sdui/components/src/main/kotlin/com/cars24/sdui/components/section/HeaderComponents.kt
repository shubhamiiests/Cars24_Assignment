package com.cars24.sdui.components.section

import com.cars24.sdui.components.SduiComponentType
import com.cars24.sdui.components.ComponentTrigger
import com.cars24.sdui.components.R
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.cars24.core.designsystem.theme.Cars24
import com.cars24.core.designsystem.theme.Radii
import com.cars24.core.designsystem.theme.Spacing
import com.cars24.sdui.runtime.registry.SduiComponent
import com.cars24.sdui.runtime.render.SduiScope
import com.cars24.sdui.runtime.render.SduiTriggers
import com.cars24.sdui.runtime.render.rememberProps
import com.cars24.sdui.schema.SduiNode
import kotlinx.serialization.Serializable
import androidx.compose.ui.tooling.preview.Preview
import com.cars24.sdui.components.preview.SduiNodePreview

@Serializable
data class SearchHeaderProps(
    val city: String,
    val greeting: String? = null,
    val searchHint: String,
    val brandName: String = "CARS24",
)

class SearchHeaderComponent : SduiComponent {
    override val type = SduiComponentType.SEARCH_HEADER

    @Composable
    override fun Render(node: SduiNode, scope: SduiScope) {
        val props = rememberProps<SearchHeaderProps>(node, scope) ?: return
        val gradient = Cars24.colors.brandGradient

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(gradient))
                .padding(
                    start = Spacing.lg,
                    end = Spacing.lg,
                    top = Spacing.lg,
                    bottom = Spacing.xl,
                ),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Row(
                    modifier = Modifier
                        .clip(Radii.sm)
                        .clickable { scope.dispatch(node, ComponentTrigger.ON_CITY_CLICK) }
                        .padding(vertical = Spacing.xxs, horizontal = Spacing.xxs),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                Icon(
                    imageVector = Icons.Filled.LocationOn,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(Spacing.xs))
                Column {
                    Text(
                        text = stringResource(R.string.cmp_delivering_to),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.7f),
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = props.city,
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.White,
                        )
                        Icon(
                            imageVector = Icons.Filled.KeyboardArrowDown,
                            contentDescription = stringResource(R.string.cmp_cd_change_city),
                            tint = Color.White,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
                }
                Spacer(Modifier.weight(1f))
                Text(
                    text = props.brandName,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                )
            }

            if (props.greeting != null) {
                Spacer(Modifier.height(Spacing.lg))
                Text(
                    text = props.greeting,
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                )
            }

            Spacer(Modifier.height(Spacing.lg))

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { scope.dispatch(node, SduiTriggers.ON_CLICK) },
                shape = Radii.md,
                color = Color.White,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.md),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = null,
                        tint = Cars24.colors.textTertiary,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(Modifier.width(Spacing.sm))
                    Text(
                        text = props.searchHint,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Cars24.colors.textTertiary,
                    )
                }
            }
        }
    }
}

@Serializable
data class SectionHeaderProps(
    val title: String,
    val subtitle: String? = null,
    val actionLabel: String? = null,
)

class SectionHeaderComponent : SduiComponent {
    override val type = SduiComponentType.SECTION_HEADER

    @Composable
    override fun Render(node: SduiNode, scope: SduiScope) {
        val props = rememberProps<SectionHeaderProps>(node, scope) ?: return
        val colors = Cars24.colors

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.lg),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = props.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = colors.textPrimary,
                )
                if (props.subtitle != null) {
                    Text(
                        text = props.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary,
                    )
                }
            }

            if (props.actionLabel != null) {
                Row(
                    modifier = Modifier
                        .clickable { scope.dispatch(node, SduiTriggers.ON_CLICK) }
                        .padding(Spacing.xs),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End,
                ) {
                    Text(
                        text = props.actionLabel,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 380)
@Composable
private fun SearchHeaderComponentPreview() = SduiNodePreview(
    json = """
    {
      "id": "p", "type": "search_header",
      "props": { "city": "{{state.city|Gurgaon}}", "greeting": "Find your next car",
                 "searchHint": "Search Swift, Baleno, i20, Nexon..." }
    }
    """,
    state = mapOf("city" to "Mumbai"),
    padded = false,
)

@Preview(showBackground = true, widthDp = 380)
@Composable
private fun SectionHeaderComponentPreview() = SduiNodePreview(
    """
    {
      "id": "p", "type": "section_header",
      "props": { "title": "Cars in your budget", "subtitle": "Under 8 lakh, ready to drive",
                 "actionLabel": "View all" }
    }
    """,
)
