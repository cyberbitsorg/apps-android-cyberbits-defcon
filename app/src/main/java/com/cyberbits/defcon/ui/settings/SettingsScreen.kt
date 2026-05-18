package com.cyberbits.defcon.ui.settings

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.cyberbits.defcon.AppContainer
import com.cyberbits.defcon.BuildConfig
import com.cyberbits.defcon.data.AppearanceMode
import com.cyberbits.defcon.ui.common.RelativeTime
import com.cyberbits.defcon.ui.common.SectionHeader
import com.cyberbits.defcon.ui.theme.adaptiveBackground
import com.cyberbits.defcon.ui.theme.adaptiveSurface
import com.cyberbits.defcon.ui.theme.colorHex
import com.cyberbits.defcon.ui.theme.textSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(container: AppContainer) {
    val vm: SettingsViewModel = viewModel(
        factory = remember(container) {
            viewModelFactory { initializer { SettingsViewModel(container) } }
        }
    )
    val settings by vm.settings.collectAsState()
    val context = LocalContext.current
    val isRefreshing by container.appState.isRefreshing.collectAsState()

    var permissionGranted by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                ContextCompat.checkSelfPermission(
                    context, Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            else true
        )
    }
    val permLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        permissionGranted = it
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Settings") }) },
        containerColor = adaptiveBackground(),
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(adaptiveBackground()),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Section("Refresh") {
                    PickerRow(
                        label = "Interval",
                        options = listOf(15 to "Every 15 min", 30 to "Every 30 min", 60 to "Every 60 min"),
                        selected = settings.refreshIntervalMinutes,
                        onSelect = { vm.setRefreshIntervalMinutes(context, it) },
                    )
                    HorizontalDivider()
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Refresh Now", style = TextStyle(fontSize = 14.sp))
                            val last = settings.lastRefreshedAt
                            Text(
                                if (last == null) "Never refreshed" else RelativeTime.format(last),
                                color = textSecondary(),
                                style = TextStyle(fontSize = 11.sp),
                            )
                        }
                        Button(
                            onClick = { vm.refreshNow() },
                            enabled = !isRefreshing,
                            colors = ButtonDefaults.textButtonColors(contentColor = colorHex("dc2626")),
                        ) {
                            if (isRefreshing) {
                                CircularProgressIndicator(modifier = Modifier.height(16.dp))
                            } else {
                                Icon(Icons.Filled.Refresh, contentDescription = null)
                                Spacer(Modifier.height(4.dp))
                                Text("Refresh", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            item {
                Section("Appearance") {
                    PickerRow(
                        label = "Theme",
                        options = AppearanceMode.entries.map { it.value to it.label },
                        selected = settings.appearance.value,
                        onSelect = { vm.setAppearance(AppearanceMode.from(it)) },
                    )
                }
            }

            item {
                Section("Notifications") {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !permissionGranted) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                "Notification permission required",
                                modifier = Modifier.weight(1f),
                                style = TextStyle(fontSize = 13.sp),
                            )
                            Button(onClick = { permLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) }) {
                                Text("Grant", fontSize = 12.sp)
                            }
                        }
                        HorizontalDivider()
                    }
                    ToggleRow(
                        label = "Alert on DEFCON Level Rise",
                        checked = settings.defconNotificationsEnabled,
                        onCheckedChange = vm::setDefconNotificationsEnabled,
                    )
                    if (settings.defconNotificationsEnabled) {
                        HorizontalDivider()
                        PickerRow(
                            label = "Minimum Level",
                            options = listOf(
                                3 to "DEFCON 3 — Round House or worse",
                                2 to "DEFCON 2 — Fast Pace or worse",
                                1 to "DEFCON 1 — Cocked Pistol only",
                            ),
                            selected = settings.notificationThreshold,
                            onSelect = vm::setNotificationThreshold,
                        )
                    }
                    HorizontalDivider()
                    ToggleRow(
                        label = "Alert on New Articles",
                        checked = settings.newArticleNotificationsEnabled,
                        onCheckedChange = vm::setNewArticleNotificationsEnabled,
                    )
                }
            }

            item {
                Section("About") {
                    LabeledRow("App", "Cyberbits DEFCON")
                    HorizontalDivider()
                    LabeledRow("Version", BuildConfig.VERSION_NAME)
                    HorizontalDivider()
                    LabeledRow("Developer", "Cyberbits Systems")
                }
            }
        }
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Column {
        SectionHeader(title, modifier = Modifier.padding(start = 16.dp, bottom = 6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(adaptiveSurface()),
        ) {
            Column { content() }
        }
    }
}

@Composable
private fun <T> PickerRow(
    label: String,
    options: List<Pair<T, String>>,
    selected: T,
    onSelect: (T) -> Unit,
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(label, style = TextStyle(fontSize = 14.sp))
        Spacer(Modifier.height(6.dp))
        options.forEach { (value, optLabel) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (value == selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        else androidx.compose.ui.graphics.Color.Transparent
                    )
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                androidx.compose.material3.RadioButton(
                    selected = value == selected,
                    onClick = { onSelect(value) },
                )
                Spacer(Modifier.height(0.dp))
                Text(optLabel, style = TextStyle(fontSize = 13.sp))
            }
        }
    }
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, modifier = Modifier.weight(1f), style = TextStyle(fontSize = 14.sp))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun LabeledRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, modifier = Modifier.weight(1f), style = TextStyle(fontSize = 14.sp))
        Text(value, color = textSecondary(), style = TextStyle(fontSize = 13.sp))
    }
}
