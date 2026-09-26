package com.sentra.shield.ui.dashboard

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sentra.shield.data.db.SentraDatabase
import com.sentra.shield.data.db.entity.AppUsageEntity
import com.sentra.shield.data.db.entity.ThreatLog
import com.sentra.shield.service.MonitorService
import com.sentra.shield.ui.dashboard.components.StatusCard
import com.sentra.shield.ui.theme.SentraDanger
import com.sentra.shield.ui.theme.SentraInfo
import com.sentra.shield.ui.theme.SentraSafe
import com.sentra.shield.ui.theme.SentraWarning
import com.sentra.shield.util.PermissionUtil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen() {
    val context = LocalContext.current
    val db = remember { SentraDatabase.getInstance(context) }

    var isMonitoring by remember { mutableStateOf(false) }
    var overlayGranted by remember { mutableStateOf(PermissionUtil.hasOverlayPermission(context)) }

    val usageFlow = remember { db.appUsageDao().observeAll() }
    val threatFlow = remember { db.threatLogDao().observeAll() }
    val usageList by usageFlow.collectAsState(initial = emptyList())
    val threatList by threatFlow.collectAsState(initial = emptyList())

    // Latest risk score per package, derived from the threat log stream.
    val latestScoreByPackage = remember(threatList) {
        threatList.groupBy { it.packageName }
            .mapValues { (_, logs) -> logs.maxByOrNull { it.timestamp }?.riskScore ?: 0 }
    }

    LaunchedEffect(Unit) {
        overlayGranted = PermissionUtil.hasOverlayPermission(context)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("SentraShield", fontWeight = FontWeight.Bold)
                        Text(
                            text = "Network Shield & Threat Monitor",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                StatusCard(
                    isMonitoring = isMonitoring,
                    overlayGranted = overlayGranted,
                    monitoredAppCount = usageList.size,
                    onToggleMonitoring = {
                        isMonitoring = !isMonitoring
                        toggleMonitorService(context, isMonitoring)
                    },
                    onGrantOverlay = {
                        context.startActivity(
                            PermissionUtil.overlaySettingsIntent(context).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        )
                    },
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            item {
                Text(
                    text = "Monitored Apps",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
            }

            if (usageList.isEmpty()) {
                item {
                    Text(
                        text = "No usage data yet — start monitoring to begin collecting stats.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            items(usageList, key = { it.packageName }) { usage ->
                AppUsageRow(usage = usage, riskScore = latestScoreByPackage[usage.packageName] ?: 0)
            }
        }
    }
}

@Composable
private fun AppUsageRow(usage: AppUsageEntity, riskScore: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.padding(end = 8.dp)) {
                Text(usage.label, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                Text(usage.packageName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                Row(modifier = Modifier.padding(top = 4.dp)) {
                    Icon(Icons.Filled.ArrowDownward, contentDescription = null, modifier = Modifier.padding(end = 2.dp))
                    Text(formatBytes(usage.rxBytes), style = MaterialTheme.typography.bodySmall)
                    Icon(Icons.Filled.ArrowUpward, contentDescription = null, modifier = Modifier.padding(start = 10.dp, end = 2.dp))
                    Text(formatBytes(usage.txBytes), style = MaterialTheme.typography.bodySmall)
                }
            }
            RiskChip(riskScore)
        }
    }
}

@Composable
private fun RiskChip(score: Int) {
    val color = when {
        score >= 70 -> SentraDanger
        score >= 40 -> SentraWarning
        score >= 15 -> SentraInfo
        else -> SentraSafe
    }
    AssistChip(
        onClick = {},
        label = { Text("$score") },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = color.copy(alpha = 0.15f),
            labelColor = color
        )
    )
}

private fun formatBytes(bytes: Long): String {
    if (bytes < 1024) return "${bytes}B"
    val kb = bytes / 1024.0
    if (kb < 1024) return "%.1fKB".format(kb)
    val mb = kb / 1024.0
    if (mb < 1024) return "%.1fMB".format(mb)
    return "%.2fGB".format(mb / 1024.0)
}

private fun toggleMonitorService(context: Context, start: Boolean) {
    val intent = Intent(context, MonitorService::class.java)
    if (start) {
        androidx.core.content.ContextCompat.startForegroundService(context, intent)
    } else {
        context.stopService(intent)
    }
}
