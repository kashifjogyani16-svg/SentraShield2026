package com.sentra.shield.ui.dashboard.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.sentra.shield.ui.theme.SentraSafe

@Composable
fun StatusCard(
    isMonitoring: Boolean,
    overlayGranted: Boolean,
    monitoredAppCount: Int,
    onToggleMonitoring: () -> Unit,
    onGrantOverlay: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row {
                    Icon(
                        imageVector = Icons.Filled.Shield,
                        contentDescription = null,
                        tint = if (isMonitoring) SentraSafe else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                    Column(modifier = Modifier.padding(start = 10.dp)) {
                        Text(
                            text = if (isMonitoring) "Protection Active" else "Protection Paused",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "$monitoredAppCount apps monitored",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onToggleMonitoring,
                    modifier = Modifier,
                    colors = if (isMonitoring) {
                        androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    } else {
                        androidx.compose.material3.ButtonDefaults.buttonColors()
                    }
                ) {
                    Icon(
                        imageVector = if (isMonitoring) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                        contentDescription = null
                    )
                    Text(
                        text = if (isMonitoring) "Stop Monitoring" else "Start Monitoring",
                        modifier = Modifier.padding(start = 6.dp)
                    )
                }

                if (!overlayGranted) {
                    OutlinedButton(onClick = onGrantOverlay) {
                        Text("Grant Overlay")
                    }
                }
            }

            if (!overlayGranted) {
                Text(
                    text = "Overlay permission needed for the Dynamic Island alerts",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFFFB300),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}
