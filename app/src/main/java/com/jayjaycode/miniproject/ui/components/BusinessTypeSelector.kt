package com.jayjaycode.miniproject.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jayjaycode.miniproject.data.BusinessType
import com.jayjaycode.miniproject.ui.theme.GreenAccent
import com.jayjaycode.miniproject.ui.theme.OrangePrimary
import com.jayjaycode.miniproject.ui.theme.TextSecondary

private fun BusinessType.icon(): ImageVector = when (this) {
    BusinessType.AUTO_COMPANY -> Icons.Default.LocalShipping
    BusinessType.MECHANIC -> Icons.Default.Build
    BusinessType.AUTO_SHOP -> Icons.Default.Store
}

@Composable
fun BusinessTypeSelector(
    selected: BusinessType,
    onSelected: (BusinessType) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Business type", fontWeight = FontWeight.SemiBold)
        Text(
            "Choose the category that best describes your business.",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
        )
        BusinessType.entries.forEach { type ->
            BusinessTypeCard(
                type = type,
                selected = selected == type,
                onClick = { onSelected(type) },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BusinessTypeCard(
    type: BusinessType,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val containerColor = if (selected) OrangePrimary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
    val borderColor = if (selected) OrangePrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(if (selected) 2.dp else 1.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (selected) 4.dp else 0.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                type.icon(),
                contentDescription = null,
                tint = if (selected) OrangePrimary else TextSecondary,
                modifier = Modifier.size(32.dp),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(type.label, fontWeight = FontWeight.SemiBold)
                Text(
                    type.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
            }
            Icon(
                imageVector = if (selected) Icons.Default.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (selected) GreenAccent else TextSecondary,
            )
        }
    }
}
