package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.data.CategoryEntity
import com.example.data.EntryType
import com.example.notifications.DailyReminderManager
import com.example.ui.LedgerUiState
import com.example.ui.components.CreateCategoryDialog
import com.example.ui.getCategoryIcon
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsAndRemindersScreen(
    uiState: LedgerUiState,
    onUpdateSettings: (
        currencySymbol: String,
        monthlyBudgetTarget: Double,
        monthlySavingsTarget: Double,
        reminderEnabled: Boolean,
        reminderHour: Int,
        reminderMinute: Int
    ) -> Unit,
    onAddCustomCategory: (name: String, iconName: String, colorHex: Long, entryType: EntryType) -> Unit,
    onDeleteCategory: (CategoryEntity) -> Unit,
    onRestoreSampleData: () -> Unit,
    onClearAllEntries: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val settings = uiState.settings

    var budgetText by remember(settings.monthlyBudgetTarget) {
        mutableStateOf(settings.monthlyBudgetTarget.toInt().toString())
    }
    var savingsTargetText by remember(settings.monthlySavingsTarget) {
        mutableStateOf(settings.monthlySavingsTarget.toInt().toString())
    }
    var showTimePicker by remember { mutableStateOf(false) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var pendingTestNotification by remember { mutableStateOf(false) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onUpdateSettings(
                settings.currencySymbol,
                settings.monthlyBudgetTarget,
                settings.monthlySavingsTarget,
                true,
                settings.reminderHour,
                settings.reminderMinute
            )
            if (pendingTestNotification) {
                DailyReminderManager.showReminderNotification(context, isTest = true)
                Toast.makeText(context, "Daily reminder notification sent!", Toast.LENGTH_SHORT).show()
                pendingTestNotification = false
            }
        } else {
            Toast.makeText(
                context,
                "Notification permission is required for daily expense reminders.",
                Toast.LENGTH_LONG
            ).show()
            pendingTestNotification = false
        }
    }

    LaunchedEffect(Unit) {
        DailyReminderManager.createNotificationChannel(context)
    }

    val formattedReminderTime = remember(settings.reminderHour, settings.reminderMinute) {
        val amPm = if (settings.reminderHour >= 12) "PM" else "AM"
        val hour12 = when {
            settings.reminderHour == 0 -> 12
            settings.reminderHour > 12 -> settings.reminderHour - 12
            else -> settings.reminderHour
        }
        String.format(Locale.US, "%02d:%02d %s", hour12, settings.reminderMinute, amPm)
    }

    val currencies = listOf("PKR", "Rs", "$", "€", "£", "₹", "AED", "SAR")

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("settings_reminders_screen"),
        contentPadding = PaddingValues(16.dp, 12.dp, 16.dp, 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "REMINDERS, CATEGORIES & GOALS",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Settings & Daily Reminders",
                    style = MaterialTheme.typography.headlineLarge
                )
            }
        }

        // 1. Daily Reminder Notification System Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Column {
                                Text(
                                    text = "Daily Expense Reminder",
                                    style = MaterialTheme.typography.titleLarge
                                )
                                Text(
                                    text = "Get notified daily so you never forget to log expenses or savings",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Switch(
                            checked = settings.reminderEnabled,
                            onCheckedChange = { enabled ->
                                if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    val granted = ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.POST_NOTIFICATIONS
                                    ) == PackageManager.PERMISSION_GRANTED
                                    if (!granted) {
                                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                        return@Switch
                                    }
                                }
                                onUpdateSettings(
                                    settings.currencySymbol,
                                    settings.monthlyBudgetTarget,
                                    settings.monthlySavingsTarget,
                                    enabled,
                                    settings.reminderHour,
                                    settings.reminderMinute
                                )
                            },
                            modifier = Modifier.testTag("daily_reminder_switch")
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showTimePicker = true },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("change_reminder_time_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Time: $formattedReminderTime")
                        }

                        Button(
                            onClick = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    val granted = ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.POST_NOTIFICATIONS
                                    ) == PackageManager.PERMISSION_GRANTED
                                    if (!granted) {
                                        pendingTestNotification = true
                                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                        return@Button
                                    }
                                }
                                val sent = DailyReminderManager.showReminderNotification(context, isTest = true)
                                Toast.makeText(
                                    context,
                                    if (sent) "Daily reminder sent! Check your notifications."
                                    else "Enable notifications to receive reminders.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("test_reminder_notification_button")
                        ) {
                            Text("Test Alert Now")
                        }
                    }
                }
            }
        }

        // 2. Currency & Monthly Budget Targets Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Currency & Monthly Targets",
                        style = MaterialTheme.typography.titleLarge
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        currencies.forEach { symbol ->
                            FilterChip(
                                selected = settings.currencySymbol == symbol,
                                onClick = {
                                    onUpdateSettings(
                                        symbol,
                                        settings.monthlyBudgetTarget,
                                        settings.monthlySavingsTarget,
                                        settings.reminderEnabled,
                                        settings.reminderHour,
                                        settings.reminderMinute
                                    )
                                },
                                label = { Text(symbol) }
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = budgetText,
                            onValueChange = { budgetText = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Expense Limit (${settings.currencySymbol})") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = savingsTargetText,
                            onValueChange = { savingsTargetText = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Saving Goal (${settings.currencySymbol})") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Button(
                        onClick = {
                            val b = budgetText.toDoubleOrNull() ?: settings.monthlyBudgetTarget
                            val s = savingsTargetText.toDoubleOrNull() ?: settings.monthlySavingsTarget
                            onUpdateSettings(
                                settings.currencySymbol,
                                b,
                                s,
                                settings.reminderEnabled,
                                settings.reminderHour,
                                settings.reminderMinute
                            )
                            Toast.makeText(context, "Monthly targets saved!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Save Monthly Targets")
                    }
                }
            }
        }

        // 3. Expense & Saving Categories Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Expense & Saving Options",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = "Includes School Fees, Electricity, Gas, Water & custom options",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Button(
                    onClick = { showAddCategoryDialog = true },
                    modifier = Modifier.testTag("settings_add_category_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Option")
                }
            }
        }

        items(
            items = uiState.categories,
            key = { "cat_setting_${it.id}" }
        ) { cat ->
            val catColor = Color(cat.colorHex)
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(catColor.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = getCategoryIcon(cat.iconName),
                                contentDescription = cat.name,
                                tint = catColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column {
                            Text(
                                text = cat.name,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = if (cat.entryType == EntryType.SAVING.name) "Saving Option" else "Expense Option",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (!cat.isDefault) {
                        IconButton(onClick = { onDeleteCategory(cat) }) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Delete custom category",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }

        // 4. Data Management Actions
        item {
            OutlinedButton(
                onClick = onClearAllEntries,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Text(
                    text = "Reset / Clear All Entries",
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1
                )
            }
        }
    }

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = settings.reminderHour,
            initialMinute = settings.reminderMinute,
            is24Hour = false
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Daily Reminder Notification Time") },
            text = {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    TimePicker(state = timePickerState)
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onUpdateSettings(
                            settings.currencySymbol,
                            settings.monthlyBudgetTarget,
                            settings.monthlySavingsTarget,
                            true,
                            timePickerState.hour,
                            timePickerState.minute
                        )
                        showTimePicker = false
                        Toast.makeText(context, "Daily reminder scheduled!", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Save Reminder Time")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showAddCategoryDialog) {
        CreateCategoryDialog(
            onDismiss = { showAddCategoryDialog = false },
            onConfirm = { name, iconName, colorHex, entryType ->
                onAddCustomCategory(name, iconName, colorHex, entryType)
                showAddCategoryDialog = false
            }
        )
    }
}
