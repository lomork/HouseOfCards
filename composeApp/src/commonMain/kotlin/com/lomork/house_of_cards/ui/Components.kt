package com.lomork.house_of_cards.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lomork.house_of_cards.data.StoreItemDef
import com.lomork.house_of_cards.theme.Hoc

fun chipTextColor(argb: Long): Color {
    val r = (argb shr 16) and 0xFF
    val g = (argb shr 8) and 0xFF
    val b = argb and 0xFF
    val lum = 0.299 * r + 0.587 * g + 0.114 * b
    return if (lum > 160) Color(0xFF1B1A15) else Color(0xFFF4EEE0)
}

/** A two-tone poker chip (used in the store, profile, and on the board). */
@Composable
fun PokerChip(item: StoreItemDef, size: Dp = 44.dp, ring: Boolean = true) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(Color(item.secondary))
            .then(if (ring) Modifier.border(1.5.dp, Hoc.Gold.copy(alpha = 0.4f), CircleShape) else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier.fillMaxSize(0.72f).clip(CircleShape).background(Color(item.primary)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                item.label,
                color = chipTextColor(item.primary),
                fontSize = (size.value * 0.38f).sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
fun HocButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    secondary: Boolean = false,
    onClick: () -> Unit,
) {
    if (secondary) {
        OutlinedButton(
            onClick = onClick,
            modifier = modifier.fillMaxWidth(),
            enabled = enabled,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
        ) { Text(text) }
    } else {
        Button(
            onClick = onClick,
            modifier = modifier.fillMaxWidth(),
            enabled = enabled,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                disabledContainerColor = Hoc.SurfaceVariant,
                disabledContentColor = Hoc.TextMuted,
            ),
        ) { Text(text, fontWeight = FontWeight.SemiBold) }
    }
}

@Composable
fun HocCard(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Hoc.SurfaceHigh,
        border = BorderStroke(1.dp, Hoc.Divider),
    ) {
        Box(Modifier.padding(contentPadding)) { content() }
    }
}

@Composable
fun StatTile(label: String, value: String, modifier: Modifier = Modifier, accent: Color = Hoc.Gold) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Hoc.SurfaceHigh)
            .border(1.dp, Hoc.Divider, RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(value, color = accent, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(2.dp))
        Text(label.uppercase(), color = Hoc.TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun BackHeader(
    title: String,
    onBack: (() -> Unit)? = null,
    trailing: @Composable () -> Unit = {},
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onBack != null) {
            TextButton(onClick = onBack, contentPadding = PaddingValues(8.dp)) {
                Text("\u2190", fontSize = 20.sp, color = Hoc.Gold)
            }
        }
        Text(
            title,
            style = MaterialTheme.typography.titleLarge,
            color = Hoc.TextPrimary,
            modifier = Modifier.weight(1f),
        )
        trailing()
    }
}

@Composable
fun SectionHeader(title: String, modifier: Modifier = Modifier) {
    Text(
        title.uppercase(),
        style = MaterialTheme.typography.labelLarge,
        color = Hoc.TextSecondary,
        modifier = modifier.padding(horizontal = 4.dp),
    )
}