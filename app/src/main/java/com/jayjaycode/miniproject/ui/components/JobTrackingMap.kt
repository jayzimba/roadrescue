package com.jayjaycode.miniproject.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.PersonPinCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jayjaycode.miniproject.ui.theme.GreenAccent
import com.jayjaycode.miniproject.ui.theme.NavyDark
import com.jayjaycode.miniproject.ui.theme.OrangePrimary
import com.jayjaycode.miniproject.ui.theme.TextSecondary

@Composable
fun JobTrackingMap(
    progress: Float?,
    providerName: String,
    etaMinutes: Int,
    isTowing: Boolean,
    modifier: Modifier = Modifier,
) {
    val enRoute = progress == null || progress < 1f
    val displayProgress = progress?.coerceIn(0f, 1f) ?: 0.35f

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = NavyDark.copy(alpha = 0.08f)),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
            ) {
                Text("Live tracking", fontWeight = FontWeight.SemiBold)
                Text(
                    when {
                        progress != null && progress >= 1f -> "Arrived"
                        enRoute -> "En route · ~$etaMinutes min"
                        else -> "~$etaMinutes min away"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = if (progress != null && progress >= 1f) GreenAccent else OrangePrimary,
                    fontWeight = FontWeight.Medium,
                )
            }
            Spacer(Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(NavyDark.copy(alpha = 0.12f)),
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 16.dp)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(GreenAccent.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.PersonPinCircle, null, tint = GreenAccent, modifier = Modifier.size(22.dp))
                }

                LinearProgressIndicator(
                    progress = { displayProgress },
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth(0.7f)
                        .height(4.dp)
                        .clip(CircleShape),
                    color = OrangePrimary,
                    trackColor = OrangePrimary.copy(alpha = 0.2f),
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .fillMaxWidth(displayProgress.coerceIn(0.15f, 0.85f))
                        .padding(end = 8.dp),
                    contentAlignment = Alignment.CenterEnd,
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(OrangePrimary),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Default.LocalShipping,
                            contentDescription = null,
                            tint = androidx.compose.ui.graphics.Color.White,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            Text(
                if (isTowing) "$providerName · Tow truck" else "$providerName · Mobile mechanic",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
            )
        }
    }
}
