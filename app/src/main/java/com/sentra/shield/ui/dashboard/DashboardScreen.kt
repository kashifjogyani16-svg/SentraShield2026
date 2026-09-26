package com.sentra.shield.ui.dashboard

import android.content.Intent
import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.sentra.shield.data.db.SentraDatabase
import com.sentra.shield.service.MonitorService
import com.sentra.shield.service.WaterIslandService
import com.sentra.shield.ui.settings.SettingsScreen
import com.sentra.shield.util.ActivityTracker
import com.sentra.shield.util.GeminiAnalyzer
import com.sentra.shield.util.PermissionUtil
import com.sentra.shield.util.VirusTotalChecker
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var monitoring by remember { mutableStateOf(false) }
    var testResult by remember { mutableStateOf("") }
    var showSettings by remember { mutableStateOf(false) }
    var emoji by remember { mutableStateOf("🥳") } // ✅ Emoji state

    val db = remember { SentraDatabase.getInstance(context) }
    val appUsageList by db.appUsageDao().getAllUsage().collectAsState(initial = emptyList())
    val threatLogs by db.threatLogDao().getAllLogs().collectAsState(initial = emptyList())
    val hasUsageAccess = remember { ActivityTracker.hasUsageAccess(context) }

    // ✅ Status ke hisaab se emoji badlein
    LaunchedEffect(threatLogs.size) {
        emoji = when {
            threatLogs.any { it.reason.contains("virus", true) } -> "😱"
            threatLogs.any { it.reason.contains("suspicious", true) } -> "🥺"
            else -> "🥳"
        }
    }

    if (showSettings) {
        SettingsScreen(onBack = { showSettings = false })
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("SentraShield")
                        Text("Network Monitor", style = MaterialTheme.typography.labelSmall)
                    }
                },
                actions = {
                    IconButton(onClick = { showSettings = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        }
    ) { padding ->
        // ✅ Outer LazyColumn (sab scroll hoga)
        LazyColumn(
            modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ✅ Emoji Header
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(emoji, style = MaterialTheme.typography.displayMedium)
                        Spacer(Modifier.height(4.dp))
                        Text("🟢 System Safe", style = MaterialTheme.typography.titleMedium)
                        Text("42 apps monitored • 2 suspicious",
                            style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            // Monitoring Status
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Monitoring Status", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = {
                            monitoring = !monitoring
                            val intent = Intent(context, MonitorService::class.java)
                            if (monitoring) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                                    context.startForegroundService(intent)
                                else context.startService(intent)
                            } else {
                                context.stopService(intent)
                            }
                        }) { Text(if (monitoring) "Stop Monitoring" else "Start Monitoring") }
                    }
                }
            }

            // Dynamic Island
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Dynamic Island", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = {
                                if (!PermissionUtil.canDrawOverlay(context)) PermissionUtil.requestOverlay(context)
                                else WaterIslandService.start(context)
                            }) { Text("Start Island") }
                            Button(onClick = {
                                WaterIslandService.showAlert(context, "test", "Test Alert", "Testing", 70)
                            }) { Text("Test Alert") }
                        }
                    }
                }
            }

            // VirusTotal
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Antivirus (VirusTotal)", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = {
                            scope.launch {
                                testResult = "Checking..."
                                testResult = VirusTotalChecker.checkFileHash(context, "44d88612fea8a8f36de82e1278abb02f")
                            }
                        }) { Text("Test VirusTotal") }
                    }
                }
            }

            // Gemini AI
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Gemini AI Analysis", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = {
                            scope.launch {
                                testResult = "Analyzing..."
                                testResult = GeminiAnalyzer.analyzeApp(
                                    context, "com.example.test",
                                    listOf("INTERNET", "SEND_SMS", "READ_CONTACTS")
                                )
                            }
                        }) { Text("Test Gemini") }
                    }
                }
            }

            // Test Result
            if (testResult.isNotEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp)) {
                            Text("Test Result:", style = MaterialTheme.typography.titleSmall)
                            Spacer(Modifier.height(4.dp))
                            Text(testResult, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            // Usage Access
            if (!hasUsageAccess) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp)) {
                            Text("⚠️ Usage Access Needed", style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(8.dp))
                            Button(onClick = {
                                context.startActivity(Intent(android.provider.Settings.ACTION_USAGE_ACCESS_SETTINGS))
                            }) { Text("Open Settings") }
                        }
                    }
                }
            }

            // Recent Activity Header
            item {
                Text("Recent Activity", style = MaterialTheme.typography.titleMedium)
            }

            // Recent Activity Items (✅ ab scroll honge)
            if (threatLogs.isEmpty()) {
                item {
                    Text("No activity yet", style = MaterialTheme.typography.bodySmall)
                }
            } else {
                items(threatLogs.take(5)) { log ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp)) {
                            Text(log.appLabel, style = MaterialTheme.typography.bodyLarge)
                            Text(log.reason, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            // Monitored Apps Header
            item {
                Text("Monitored Apps", style = MaterialTheme.typography.titleMedium)
            }

            // Monitored Apps Items
            items(appUsageList.take(10)) { app ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        Modifier.padding(12.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(app.label, style = MaterialTheme.typography.bodyLarge)
                            Text("↓ ${app.rxBytes / 1024} KB  ↑ ${app.txBytes / 1024} KB",
                                style = MaterialTheme.typography.labelSmall)
                        }
                        AssistChip(onClick = {}, label = { Text("Monitored") })
                    }
                }
            }
        }
    }
}
