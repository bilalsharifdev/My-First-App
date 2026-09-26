package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.CategoryEntity
import com.example.data.EntryType
import com.example.data.LedgerEntryEntity
import com.example.ui.LedgerFilterType
import com.example.ui.LedgerUiState
import com.example.ui.formatCurrency
import com.example.ui.formatDayHeader
import com.example.ui.formatTimeOnly
import com.example.ui.getCategoryIcon
import com.example.ui.theme.AmberContainer
import com.example.ui.theme.ForestEmeraldLight
import com.example.ui.theme.ForestEmeraldPrimary
import com.example.ui.theme.UniformAppFontFamily
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun LedgerHomeScreen(
    uiState: LedgerUiState,
    onNavigateMonth: (Int) -> Unit,
    onSelectFilterType: (LedgerFilterType) -> Unit,
    onSelectCategoryFilter: (Long?) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onOpenQuickLog: (EntryType, CategoryEntity?, Long?) -> Unit,
    onEditEntry: (LedgerEntryEntity) -> Unit,
    onOpenMonthlyReport: () -> Unit,
    modifier: Modifier = Modifier
) {
    val summary = uiState.reportSummary
    val currency = uiState.settings.currencySymbol

    // Live Device Date & Time Ticker
    var currentDeviceTimeMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            currentDeviceTimeMillis = System.currentTimeMillis()
            delay(1000L)
        }
    }

    val liveDeviceDateText = remember(currentDeviceTimeMillis) {
        SimpleDateFormat("EEEE, d MMM yyyy • hh:mm a", Locale.US).format(Date(currentDeviceTimeMillis))
    }

    val groupedByDay = remember(uiState.filteredEntriesForMonth) {
        uiState.filteredEntriesForMonth.groupBy { entry ->
            val cal = Calendar.getInstance().apply { timeInMillis = entry.transactionTimestamp }
            cal.get(Calendar.YEAR) * 10000 +
                (cal.get(Calendar.MONTH) + 1) * 100 +
                cal.get(Calendar.DAY_OF_MONTH)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("ledger_home_list"),
        contentPadding = PaddingValues(16.dp, 10.dp, 16.dp, 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 0. Live Exact Device Date & Time Banner
        item {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("device_current_datetime_banner")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = liveDeviceDateText,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontFamily = UniformAppFontFamily,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // 1. Simple Month Switcher Bar
        item {
            SimpleMonthHeader(
                displayMonth = summary.displayMonth,
                onPreviousMonth = { onNavigateMonth(-1) },
                onNextMonth = { onNavigateMonth(1) }
            )
        }

        // 2. Easy-to-Read Navy Blue Monthly Summary Card (Starts at PKR 0)
        item {
            SimpleMonthlySummaryCard(
                displayMonth = summary.displayMonth,
                totalSaved = summary.totalSavings,
                totalExpenses = summary.totalExpenses,
                netBalance = summary.netBalance,
                currencySymbol = currency,
                onOpenReport = onOpenMonthlyReport
            )
        }

        // 3. Big Action Buttons: Add Expense, Add Saving, or Backdate Missed Day
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { onOpenQuickLog(EntryType.EXPENSE, null, System.currentTimeMillis()) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 12.dp),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .testTag("big_add_expense_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Add Expense",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Button(
                        onClick = { onOpenQuickLog(EntryType.SAVING, null, System.currentTimeMillis()) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 12.dp),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .testTag("big_add_saving_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Add Saving",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Big Easy Button for Forgotten / Backdated Day
                Button(
                    onClick = {
                        val yesterday = Calendar.getInstance().apply {
                            add(Calendar.DAY_OF_YEAR, -1)
                        }.timeInMillis
                        onOpenQuickLog(EntryType.EXPENSE, null, yesterday)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("quick_backdate_missed_day_chip")
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Enter Past Date (Backdate)",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // 4. Big 2-Column Buttons for Quick Bills
        item {
            QuickBigCategoryGrid(
                categories = uiState.categories.take(6),
                onSelectCategory = { cat ->
                    val type = if (cat.entryType == EntryType.SAVING.name) EntryType.SAVING else EntryType.EXPENSE
                    onOpenQuickLog(type, cat, System.currentTimeMillis())
                }
            )
        }

        // 5. Simple 3-Button Filter Bar (All / Expenses / Savings)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Daily Entries (${summary.displayMonth})",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        LedgerFilterType.ALL to "All",
                        LedgerFilterType.EXPENSE to "Expenses",
                        LedgerFilterType.SAVING to "Savings"
                    ).forEach { (filter, label) ->
                        val isSelected = uiState.filterType == filter
                        Button(
                            onClick = { onSelectFilterType(filter) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                            ),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("filter_chip_${filter.name.lowercase()}")
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelLarge,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        // 6. Daily Entries List or Clean Zero State
        if (groupedByDay.isEmpty()) {
            item {
                EmptyLedgerCard(
                    onAddToday = { onOpenQuickLog(EntryType.EXPENSE, null, System.currentTimeMillis()) }
                )
            }
        } else {
            groupedByDay.forEach { (_, dayEntries) ->
                val sampleTimestamp = dayEntries.first().transactionTimestamp
                val dayExpenseSum = dayEntries
                    .filter { it.entryType == EntryType.EXPENSE.name }
                    .sumOf { it.amount }
                val daySavingSum = dayEntries
                    .filter { it.entryType == EntryType.SAVING.name }
                    .sumOf { it.amount }

                item(key = "header_${dayEntries.first().dayOfMonth}_${dayEntries.first().yearMonth}") {
                    DayGroupHeader(
                        dateLabel = formatDayHeader(sampleTimestamp),
                        dayExpenseSum = dayExpenseSum,
                        daySavingSum = daySavingSum,
                        currencySymbol = currency
                    )
                }

                items(
                    items = dayEntries,
                    key = { entry -> entry.id }
                ) { entry ->
                    LedgerEntryRowCard(
                        entry = entry,
                        currencySymbol = currency,
                        onClick = { onEditEntry(entry) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SimpleMonthHeader(
    displayMonth: String,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onPreviousMonth,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .height(42.dp)
                    .testTag("prev_month_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "Previous Month",
                    modifier = Modifier.size(18.dp)
                )
                Text("Prev", style = MaterialTheme.typography.labelMedium, maxLines = 1)
            }

            Text(
                text = displayMonth,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 6.dp)
            )

            Button(
                onClick = onNextMonth,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .height(42.dp)
                    .testTag("next_month_button")
            ) {
                Text("Next", style = MaterialTheme.typography.labelMedium, maxLines = 1)
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Next Month",
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun SimpleMonthlySummaryCard(
    displayMonth: String,
    totalSaved: Double,
    totalExpenses: Double,
    netBalance: Double,
    currencySymbol: String,
    onOpenReport: () -> Unit
) {
    val gradient = Brush.linearGradient(
        colors = listOf(ForestEmeraldPrimary, ForestEmeraldLight)
    )
    Card(
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("hero_monthly_ledger_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(gradient)
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "$displayMonth Summary",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    color = Color.White.copy(alpha = 0.14f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Expenses",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.9f),
                            maxLines = 1
                        )
                        Text(
                            text = formatCurrency(totalExpenses, currencySymbol),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontFamily = UniformAppFontFamily,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Surface(
                    color = Color.White.copy(alpha = 0.14f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Savings",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.9f),
                            maxLines = 1
                        )
                        Text(
                            text = formatCurrency(totalSaved, currencySymbol),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontFamily = UniformAppFontFamily,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            HorizontalDivider(color = Color.White.copy(alpha = 0.2f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Balance",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.85f),
                        maxLines = 1
                    )
                    Text(
                        text = formatCurrency(netBalance, currencySymbol),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = UniformAppFontFamily,
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Button(
                    onClick = onOpenReport,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = ForestEmeraldPrimary
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .height(42.dp)
                        .testTag("header_month_report_chip")
                ) {
                    Icon(
                        imageVector = Icons.Default.Assessment,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Report",
                        style = MaterialTheme.typography.labelLarge,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickBigCategoryGrid(
    categories: List<CategoryEntity>,
    onSelectCategory: (CategoryEntity) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Tap to Add Common Bill:",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )

        categories.chunked(2).forEach { rowCats ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowCats.forEach { cat ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onSelectCategory(cat) }
                            .testTag("quick_log_cat_${cat.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = getCategoryIcon(cat.iconName),
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Text(
                                text = cat.name,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
                if (rowCats.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun DayGroupHeader(
    dateLabel: String,
    dayExpenseSum: Double,
    daySavingSum: Double,
    currencySymbol: String
) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = dateLabel,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            if (dayExpenseSum > 0) {
                Text(
                    text = formatCurrency(dayExpenseSum, currencySymbol),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    maxLines = 1
                )
            } else if (daySavingSum > 0) {
                Text(
                    text = formatCurrency(daySavingSum, currencySymbol),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun LedgerEntryRowCard(
    entry: LedgerEntryEntity,
    currencySymbol: String,
    onClick: () -> Unit
) {
    val isSaving = entry.entryType == EntryType.SAVING.name

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("ledger_entry_card_${entry.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getCategoryIcon(entry.categoryIcon),
                        contentDescription = entry.categoryName,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = entry.title,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${entry.categoryName} • ${formatTimeOnly(entry.transactionTimestamp)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (entry.isBackdated) {
                        Surface(
                            color = AmberContainer,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "Backdated",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF78350F),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatCurrency(entry.amount, currencySymbol),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = UniformAppFontFamily,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1
                )
                Text(
                    text = if (isSaving) "SAVING" else "EXPENSE",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun EmptyLedgerCard(
    onAddToday: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_empty_ledger_1790414502170),
                contentDescription = "Ledger illustration",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
            Text(
                text = "Expenses: PKR 0 • Savings: PKR 0",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = UniformAppFontFamily,
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Tap below to enter your first expense or saving.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Button(
                onClick = onAddToday,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Add First Entry",
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1
                )
            }
        }
    }
}
