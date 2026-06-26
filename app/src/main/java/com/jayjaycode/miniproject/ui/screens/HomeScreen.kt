package com.jayjaycode.miniproject.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jayjaycode.miniproject.data.MechanicShop
import com.jayjaycode.miniproject.data.MockRepository
import com.jayjaycode.miniproject.ui.components.LusakaCenter
import com.jayjaycode.miniproject.ui.components.OnlineBadge
import com.jayjaycode.miniproject.ui.components.RescueMap
import com.jayjaycode.miniproject.ui.components.SectionTitle
import com.jayjaycode.miniproject.ui.theme.GreenAccent
import com.jayjaycode.miniproject.ui.theme.NavyDark
import com.jayjaycode.miniproject.ui.theme.OrangePrimary
import com.jayjaycode.miniproject.ui.theme.TextSecondary

@Composable
fun HomeScreen(
    onRequestTowing: () -> Unit,
    onRequestMechanic: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenMarketplace: () -> Unit,
    onBookService: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NavyDark)
                    .padding(24.dp),
            ) {
                Column {
                    Text(
                        "RoadRescue",
                        style = MaterialTheme.typography.headlineMedium,
                        color = androidx.compose.ui.graphics.Color.White,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        "Breakdown? Get help in minutes.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, null, tint = GreenAccent, modifier = Modifier.size(18.dp))
                        Text(" Near you · 3 shops online", color = androidx.compose.ui.graphics.Color.White.copy(0.8f), style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        item {
            RescueMap(
                modifier = Modifier.padding(horizontal = 16.dp),
                userLocation = LusakaCenter,
                height = 180.dp,
            )
        }

        item {
            SectionTitle("Need help now?", "Request towing or a mobile mechanic")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                RescueActionCard(
                    modifier = Modifier.weight(1f),
                    title = "Towing",
                    subtitle = "Flatbed · winch · roadside",
                    icon = Icons.Default.LocalShipping,
                    onClick = onRequestTowing,
                )
                RescueActionCard(
                    modifier = Modifier.weight(1f),
                    title = "Mechanic",
                    subtitle = "On-site repair",
                    icon = Icons.Default.Build,
                    onClick = onRequestMechanic,
                )
            }
        }

        item {
            SectionTitle("More services")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                RescueActionCard(
                    modifier = Modifier.weight(1f),
                    title = "History",
                    subtitle = "Past requests",
                    icon = Icons.Default.History,
                    onClick = onOpenHistory,
                )
                RescueActionCard(
                    modifier = Modifier.weight(1f),
                    title = "Spare Parts",
                    subtitle = "Marketplace",
                    icon = Icons.Default.Store,
                    onClick = onOpenMarketplace,
                )
            }
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            ) {
                RescueActionCard(
                    modifier = Modifier.fillMaxWidth(),
                    title = "Book Service",
                    subtitle = "Scheduled maintenance",
                    icon = Icons.Default.Build,
                    onClick = onBookService,
                )
            }
        }

        item {
            SectionTitle("Nearby shops", "Only online shops can bid on your request")
        }

        items(MockRepository.onlineShops) { shop ->
            ShopCard(shop, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
        }
    }
}

@Composable
private fun RescueActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(OrangePrimary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = OrangePrimary)
            }
            Spacer(Modifier.height(12.dp))
            Text(title, fontWeight = FontWeight.Bold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        }
    }
}

@Composable
private fun ShopCard(shop: MechanicShop, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(shop.name, fontWeight = FontWeight.SemiBold)
                Text(
                    "★ ${shop.rating} · ${shop.reviewCount} reviews · ${"%.1f".format(shop.distanceKm)} km",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
                Text(
                    shop.services.joinToString(" · "),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                )
            }
            OnlineBadge(shop.isOnline)
        }
    }
}
