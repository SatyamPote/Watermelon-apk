package com.watermelon.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.watermelon.core.designsystem.theme.WatermelonRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumScreen() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Premium") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(WatermelonRed, Color.Black)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = "Premium",
                    tint = Color.White,
                    modifier = Modifier.size(56.dp)
                )
            }

            Text(
                text = "Go Premium",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Unlock offline downloads, highest audio quality, and an ad-free experience.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            FeatureItem("\u2713 Offline Downloads")
            FeatureItem("\u2713 Hi-Fi Audio Quality")
            FeatureItem("\u2713 No Ads, Ever")
            FeatureItem("\u2713 Exclusive Content")

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { /* Razorpay integration deferred */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = WatermelonRed)
            ) {
                Text(
                    "Upgrade Now",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun FeatureItem(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.fillMaxWidth()
    )
}
