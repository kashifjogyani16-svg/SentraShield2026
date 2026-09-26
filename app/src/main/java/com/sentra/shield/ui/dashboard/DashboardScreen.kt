package com.sentra.shield.ui.dashboard

import android.content.Intent
import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.sentra.shield.data.db.SentraDatabase
import com.sentra.shield.data.db.entity.AppUsageEntity
import com.sentra.shield.service.MonitorService
import com.sentra.shield.service.WaterIslandService
import com.sentra.shield.util.PermissionUtil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen() {
    val context = LocalContext.current
    var monitoring by remember { mutableStateOf(false) }
    
    // Database se data fetch karne ke liye
    val db = remember { SentraDatabase.getInstance(context) }
    val appUsageList by db.appUsageDao().getAllUsage().collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(title = {
                Column {
                    Text("SentraShield")
                    Text("Network Monitor", style = MaterialTheme.typography.labelSmall)
                }
            })
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
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

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Overlay Permission", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = {
                        if (!PermissionUtil.canDrawOverlay(context)) PermissionUtil.requestOverlay(context)
                        else {
                            val i = Intent(context, WaterIslandService::class.java)
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                                context.startForegroundService(i)
                            else context.startService(i)
                        }
                    }) { Text("Enable Dynamic Island") }
                }
            }

            Text("Monitored Apps", style = MaterialTheme.typography.titleMedium)
            
            // Ab list database se aayegi
            if (appUsageList.isEmpty()) {
                Text("No apps monitored yet. Start monitoring to see data.", 
                     style = MaterialTheme.typography.bodyMedium)
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(appUsageList) { app ->
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
    }
}
