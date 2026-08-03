package com.cars24.sdui.components.section

import com.cars24.sdui.components.SduiComponentType
import com.cars24.sdui.components.ComponentTrigger
import com.cars24.sdui.components.CardLayout
import com.cars24.sdui.components.R
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cars24.core.designsystem.component.Cars24Card
import com.cars24.core.designsystem.component.Cars24Tag
import com.cars24.core.designsystem.component.NetworkImage
import com.cars24.core.designsystem.theme.Cars24
import com.cars24.core.designsystem.theme.PriceTextStyle
import com.cars24.core.designsystem.theme.Radii
import com.cars24.core.designsystem.theme.Spacing
import com.cars24.sdui.runtime.action.SduiCommand
import com.cars24.sdui.runtime.registry.SduiComponent
import com.cars24.sdui.runtime.render.SduiScope
import com.cars24.sdui.runtime.render.SduiTriggers
import com.cars24.sdui.runtime.render.sduiStateValue
import com.cars24.sdui.runtime.render.rememberProps
import com.cars24.sdui.schema.SduiNode
import kotlinx.serialization.Serializable

@Serializable
data class CarCardProps(
    val name: String,
    val price: String,
    val emi: String? = null,
    val specs: List<String> = emptyList(),
    val badge: String? = null,
    val savings: String? = null,
    val assured: Boolean = false,
    val layout: String = CardLayout.VERTICAL,
    val width: Int = 220,
    val fillWidth: Boolean = false,
    val imageUrl: String? = null,
    val wishKey: String? = null,
)

private val CardTopShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
private val SavedHeart = Color(0xFFFF4D5E)
private const val WISHLISTED = "1"

class CarCardComponent : SduiComponent {
    override val type = SduiComponentType.CAR_CARD

    @Composable
    override fun Render(node: SduiNode, scope: SduiScope) {
        val props = rememberProps<CarCardProps>(node, scope) ?: return
        val horizontal = props.layout == CardLayout.HORIZONTAL
        val stretch = horizontal || props.fillWidth

        val saved = sduiStateValue(props.wishKey) == WISHLISTED
        val onWishlistTap: (() -> Unit)? = props.wishKey?.let { key ->
            {
                if (scope.hasAction(node, ComponentTrigger.ON_WISHLIST)) {
                    scope.dispatch(node, ComponentTrigger.ON_WISHLIST)
                } else {
                    scope.dispatch(SduiCommand.ToggleState(key, WISHLISTED, ""))
                }
            }
        }

        Cars24Card(
            modifier = Modifier
                .then(if (stretch) Modifier.fillMaxWidth() else Modifier.width(props.width.dp))
                .clickable { scope.dispatch(node, SduiTriggers.ON_CLICK) },
        ) {
            if (horizontal) {
                Row(Modifier.padding(Spacing.md)) {
                    CarThumbnail(
                        props = props,
                        saved = saved,
                        onWishlistTap = onWishlistTap,
                        modifier = Modifier
                            .width(120.dp)
                            .height(90.dp),
                        shape = RoundedCornerShape(12.dp),
                    )
                    Spacer(Modifier.width(Spacing.md))
                    Column(Modifier.weight(1f)) { CarDetails(props) }
                }
            } else {
                Column {
                    CarThumbnail(
                        props = props,
                        saved = saved,
                        onWishlistTap = onWishlistTap,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        shape = CardTopShape,
                    )
                    Column(Modifier.padding(Spacing.md)) { CarDetails(props) }
                }
            }
        }
    }
}

@Composable
private fun CarThumbnail(
    props: CarCardProps,
    saved: Boolean,
    onWishlistTap: (() -> Unit)?,
    modifier: Modifier,
    shape: Shape,
) {
    Box(modifier = modifier) {
        NetworkImage(
            url = props.imageUrl,
            seed = props.name,
            shape = shape,
            modifier = Modifier.matchParentSize(),
        )

        if (props.badge != null) {
            Surface(
                modifier = Modifier.padding(Spacing.sm),
                shape = Radii.sm,
                color = Color.White.copy(alpha = 0.92f),
            ) {
                Text(
                    text = props.badge,
                    style = MaterialTheme.typography.labelSmall,
                    color = Cars24.colors.textPrimary,
                    modifier = Modifier.padding(horizontal = Spacing.sm, vertical = Spacing.xxs),
                )
            }
        }

        if (onWishlistTap != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(Spacing.xs)
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.22f))
                    .clickable(onClick = onWishlistTap),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (saved) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = stringResource(
                        if (saved) R.string.cmp_cd_remove_from_wishlist
                        else R.string.cmp_cd_save_to_wishlist,
                    ),
                    tint = if (saved) SavedHeart else Color.White,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

@Composable
private fun CarDetails(props: CarCardProps) {
    val colors = Cars24.colors

    if (props.specs.isNotEmpty()) {
        Text(
            text = props.specs.joinToString(" • "),
            style = MaterialTheme.typography.labelSmall,
            color = colors.textTertiary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(Spacing.xxs))
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = props.name,
            style = MaterialTheme.typography.titleSmall,
            color = colors.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false),
        )
        if (props.assured) {
            Spacer(Modifier.width(Spacing.xs))
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = stringResource(R.string.cmp_cd_assured),
                tint = colors.success,
                modifier = Modifier.size(14.dp),
            )
        }
    }

    Spacer(Modifier.height(Spacing.sm))

    Row(verticalAlignment = Alignment.Bottom) {
        Text(text = props.price, style = PriceTextStyle, color = colors.price)
        if (props.savings != null) {
            Spacer(Modifier.width(Spacing.sm))
            Text(
                text = props.savings,
                style = MaterialTheme.typography.labelSmall,
                color = colors.success,
                modifier = Modifier.padding(bottom = 2.dp),
            )
        }
    }

    if (props.emi != null) {
        Spacer(Modifier.height(Spacing.xs))
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
            Cars24Tag(text = props.emi)
        }
    }
}
