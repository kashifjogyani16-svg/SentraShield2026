package com.sentra.shield.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.sentra.shield.util.ApiKeyManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var vtKey by remember { mutableStateOf(ApiKeyManager.getVirusTotalKey(context)) }
    var geminiKey by remember { mutableStateOf(ApiKeyManager.getGeminiKey(context)) }
    var saved by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("API Keys", style = MaterialTheme.typography.titleLarge)

            Text("Yahan apni API keys daalein. Yeh phone mein hi save hongi.", 
                 style = MaterialTheme.typography.bodySmall)

            OutlinedTextField(
                value = vtKey,
                onValueChange = { vtKey = it; saved = false },
                label = { Text("VirusTotal API Key") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = geminiKey,
                onValueChange = { geminiKey = it; saved = false },
                label = { Text("Gemini API Key") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Button(
                onClick = {
                    ApiKeyManager.saveVirusTotalKey(context, vtKey.trim())
                    ApiKeyManager.saveGeminiKey(context, geminiKey.trim())
                    saved = true
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Keys")
            }

            if (saved) {
                Text("✅ Keys save ho gayi!", 
                     color = MaterialTheme.colorScheme.primary,
                     style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(Modifier.height(16.dp))
            Text("Help:", style = MaterialTheme.typography.titleMedium)
            Text("• VirusTotal key: virustotal.com se free milegi", 
                 style = MaterialTheme.typography.bodySmall)
            Text("• Gemini key: aistudio.google.com/app/apikey se milegi", 
                 style = MaterialTheme.typography.bodySmall)
        }
    }
}
