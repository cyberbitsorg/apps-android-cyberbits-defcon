package com.cyberbits.defcon.ui.settings

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.cyberbits.defcon.AppContainer
import com.cyberbits.defcon.BuildConfig
import com.cyberbits.defcon.ui.common.RelativeTime

@Composable
fun SettingsScreen(container: AppContainer) {
    val vm: SettingsViewModel = viewModel(
        factory = remember(container) {
            viewModelFactory { initializer { SettingsViewModel(container) } }
        }
    )
    val settings by vm.settings.collectAsState()
    val context = LocalContext.current

    var permissionGranted by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            else true
        )
    }
    val permLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        permissionGranted = it
    }

    LazyColumn(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
        item {
            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Refresh", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(15, 30, 60).forEach { mins ->
                            FilterChip(
                                selected = settings.refreshIntervalMinutes == mins,
                                onClick = { vm.setRefreshIntervalMinutes(context, mins) },
                                label = { Text("$mins min") },
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Button(onClick = { vm.refreshNow() }) { Text("Refresh now") }
                        Spacer(Modifier.height(0.dp))
                        val last = settings.lastRefreshedAt
                        Text(
                            text = "  Last: ${if (last == null) "never" else RelativeTime.format(last)}",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
        }
        item {
            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Notifications", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !permissionGranted) {
                        Button(onClick = { permLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) }) {
                            Text("Grant notification permission")
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                    SettingRow("DEFCON alerts", settings.defconNotificationsEnabled, vm::setDefconNotificationsEnabled)
                    Spacer(Modifier.height(4.dp))
                    Text("Threshold (level or lower triggers)", style = MaterialTheme.typography.labelMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(1 to "Critical", 2 to "High", 3 to "Elevated").forEach { (lvl, label) ->
                            FilterChip(
                                selected = settings.notificationThreshold == lvl,
                                onClick = { vm.setNotificationThreshold(lvl) },
                                label = { Text(label) },
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    SettingRow("New articles", settings.newArticleNotificationsEnabled, vm::setNewArticleNotificationsEnabled)
                }
            }
        }
        item {
            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("About", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Text("Cyberbits DEFCON")
                    Text("Version ${BuildConfig.VERSION_NAME}", style = MaterialTheme.typography.bodySmall)
                    Text("Standalone — no account needed", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun SettingRow(label: String, value: Boolean, onChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Text(label, modifier = Modifier.weight(1f))
        Switch(checked = value, onCheckedChange = onChange)
    }
}
