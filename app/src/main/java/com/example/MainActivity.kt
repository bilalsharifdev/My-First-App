package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.EntryType
import com.example.data.LedgerDatabase
import com.example.data.LedgerEntryEntity
import com.example.data.LedgerRepository
import com.example.notifications.DailyReminderManager
import com.example.ui.LedgerViewModel
import com.example.ui.components.AddEditEntrySheet
import com.example.ui.screens.AnalyticsChartsScreen
import com.example.ui.screens.LedgerHomeScreen
import com.example.ui.screens.MonthlyFinalReportScreen
import com.example.ui.screens.SettingsAndRemindersScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.UniformAppFontFamily

enum class MainDestination(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    LEDGER("Home", Icons.Filled.MenuBook, Icons.Outlined.MenuBook, "nav_tab_ledger"),
    ANALYTICS("Charts", Icons.Filled.BarChart, Icons.Outlined.BarChart, "nav_tab_analytics"),
    REPORT("Report", Icons.Filled.Assessment, Icons.Outlined.Assessment, "nav_tab_report"),
    REMINDERS("Reminders", Icons.Filled.Notifications, Icons.Outlined.Notifications, "nav_tab_reminders")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = LedgerDatabase.getInstance(applicationContext)
        val repository = LedgerRepository(database.ledgerDao())
        val openAddFromNotification =
            intent?.getBooleanExtra(DailyReminderManager.EXTRA_OPEN_ADD_SHEET, false) == true

        DailyReminderManager.createNotificationChannel(applicationContext)

        setContent {
            MyApplicationTheme {
                val viewModel: LedgerViewModel = viewModel(
                    factory = LedgerViewModel.provideFactory(repository)
                )
                LedgerFlowApp(
                    viewModel = viewModel,
                    openSheetInitially = openAddFromNotification
                )
            }
        }
    }
}

@Composable
fun LedgerFlowApp(
    viewModel: LedgerViewModel,
    openSheetInitially: Boolean = false
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var currentDestination by rememberSaveable { mutableStateOf(MainDestination.LEDGER) }

    var showEntrySheet by rememberSaveable { mutableStateOf(openSheetInitially) }
    var editingEntry by remember { mutableStateOf<LedgerEntryEntity?>(null) }
    var initialEntryType by remember { mutableStateOf(EntryType.EXPENSE) }
    var initialCategoryId by remember { mutableStateOf<Long?>(null) }
    var initialTimestamp by remember { mutableLongStateOf(System.currentTimeMillis()) }

    if (currentDestination != MainDestination.LEDGER) {
        BackHandler {
            currentDestination = MainDestination.LEDGER
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    editingEntry = null
                    initialEntryType = EntryType.EXPENSE
                    initialCategoryId = null
                    initialTimestamp = System.currentTimeMillis()
                    showEntrySheet = true
                },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Log Entry"
                    )
                },
                text = {
                    Text(
                        text = "Add Entry",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontFamily = UniformAppFontFamily,
                            fontWeight = FontWeight.Bold
                        ),
                        maxLines = 1
                    )
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                modifier = Modifier.testTag("fab_log_entry")
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                MainDestination.entries.forEach { dest ->
                    val selected = currentDestination == dest
                    NavigationBarItem(
                        selected = selected,
                        onClick = { currentDestination = dest },
                        icon = {
                            Icon(
                                imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                contentDescription = dest.label
                            )
                        },
                        label = {
                            Text(
                                text = dest.label,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontFamily = UniformAppFontFamily,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag(dest.testTag)
                    )
                }
            }
        }
    ) { innerPadding ->
        when (currentDestination) {
            MainDestination.LEDGER -> {
                LedgerHomeScreen(
                    uiState = uiState,
                    onNavigateMonth = { delta -> viewModel.navigateMonth(delta) },
                    onSelectFilterType = { filter -> viewModel.setFilterType(filter) },
                    onSelectCategoryFilter = { catId -> viewModel.setCategoryFilter(catId) },
                    onSearchQueryChange = { q -> viewModel.setSearchQuery(q) },
                    onOpenQuickLog = { type, category, customTimestamp ->
                        editingEntry = null
                        initialEntryType = type
                        initialCategoryId = category?.id
                        initialTimestamp = customTimestamp ?: System.currentTimeMillis()
                        showEntrySheet = true
                    },
                    onEditEntry = { entry ->
                        editingEntry = entry
                        showEntrySheet = true
                    },
                    onOpenMonthlyReport = {
                        currentDestination = MainDestination.REPORT
                    },
                    modifier = Modifier.padding(innerPadding)
                )
            }

            MainDestination.ANALYTICS -> {
                AnalyticsChartsScreen(
                    uiState = uiState,
                    onNavigateMonth = { delta -> viewModel.navigateMonth(delta) },
                    modifier = Modifier.padding(innerPadding)
                )
            }

            MainDestination.REPORT -> {
                MonthlyFinalReportScreen(
                    uiState = uiState,
                    onNavigateMonth = { delta -> viewModel.navigateMonth(delta) },
                    modifier = Modifier.padding(innerPadding)
                )
            }

            MainDestination.REMINDERS -> {
                SettingsAndRemindersScreen(
                    uiState = uiState,
                    onUpdateSettings = { symbol, budget, savingGoal, reminderOn, hour, min ->
                        viewModel.updateSettings(
                            context = context,
                            currencySymbol = symbol,
                            monthlyBudgetTarget = budget,
                            monthlySavingsTarget = savingGoal,
                            reminderEnabled = reminderOn,
                            reminderHour = hour,
                            reminderMinute = min
                        )
                    },
                    onAddCustomCategory = { name, iconName, colorHex, entryType ->
                        viewModel.addCustomCategory(name, iconName, colorHex, entryType)
                    },
                    onDeleteCategory = { cat -> viewModel.deleteCategory(cat) },
                    onRestoreSampleData = { viewModel.restoreSampleEntries() },
                    onClearAllEntries = { viewModel.clearAllEntries() },
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }

    if (showEntrySheet) {
        AddEditEntrySheet(
            categories = uiState.categories,
            currencySymbol = uiState.settings.currencySymbol,
            existingEntry = editingEntry,
            initialEntryType = initialEntryType,
            initialCategoryId = initialCategoryId,
            initialTimestamp = initialTimestamp,
            onDismiss = {
                showEntrySheet = false
                editingEntry = null
            },
            onSave = { existingId, title, amount, type, category, timestamp, method, notes ->
                viewModel.saveLedgerEntry(
                    existingId = existingId,
                    title = title,
                    amount = amount,
                    entryType = type,
                    category = category,
                    transactionTimestamp = timestamp,
                    paymentMethod = method,
                    notes = notes
                )
            },
            onDelete = { entry ->
                viewModel.deleteEntry(entry)
            },
            onAddCustomCategory = { name, iconName, colorHex, type, onCreated ->
                viewModel.addCustomCategory(name, iconName, colorHex, type, onCreated)
            }
        )
    }
}
