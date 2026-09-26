package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.LedgerUiState
import com.example.ui.formatCurrency
import com.example.ui.getCategoryIcon
import com.example.ui.theme.JetBrainsMonoFontFamily

@Composable
fun MonthlyFinalReportScreen(
    uiState: LedgerUiState,
    onNavigateMonth: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val summary = uiState.reportSummary
    val currency = uiState.settings.currencySymbol

    val financialGrade = when {
        summary.savingsRatePercent >= 35f -> "A+ · Exemplary Saver"
        summary.savingsRatePercent >= 20f -> "A · Strong Monthly Surplus"
        summary.savingsRatePercent >= 10f -> "B · Positive Saver"
        summary.netBalance >= 0.0 -> "C · Balanced Month"
        else -> "D · Expenses Exceeded Savings"
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("monthly_final_report_screen"),
        contentPadding = PaddingValues(16.dp, 12.dp, 16.dp, 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header with Month Switcher & Share Button
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "END-OF-MONTH FINAL STATEMENT",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { onNavigateMonth(-1) }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                contentDescription = "Previous Month"
                            )
                        }
                        Text(
                            text = summary.displayMonth,
                            style = MaterialTheme.typography.headlineLarge
                        )
                        IconButton(onClick = { onNavigateMonth(1) }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = "Next Month"
                            )
                        }
                    }
                }

                Button(
                    onClick = {
                        val reportText = buildString {
                            appendLine("=== LEDGERFLOW FINAL MONTHLY REPORT ===")
                            appendLine("Month: ${summary.displayMonth}")
                            appendLine("Total Whole Month Savings: ${formatCurrency(summary.totalSavings, currency)}")
                            appendLine("Total Whole Month Expenses: ${formatCurrency(summary.totalExpenses, currency)}")
                            appendLine("Net Monthly Balance: ${formatCurrency(summary.netBalance, currency)}")
                            appendLine("Savings Rate: ${String.format("%.1f", summary.savingsRatePercent)}%")
                            appendLine("Days Logged: ${summary.daysLoggedCount} / ${summary.totalDaysInMonth}")
                            appendLine("\n--- Expense Breakdown ---")
                            summary.expenseCategoryBreakdown.forEach { cat ->
                                appendLine("• ${cat.categoryName}: ${formatCurrency(cat.totalAmount, currency)} (${String.format("%.1f", cat.percentageOfType)}%)")
                            }
                            appendLine("\n--- Savings Breakdown ---")
                            summary.savingCategoryBreakdown.forEach { cat ->
                                appendLine("• ${cat.categoryName}: ${formatCurrency(cat.totalAmount, currency)}")
                            }
                        }
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, "${summary.displayMonth} Financial Report")
                            putExtra(Intent.EXTRA_TEXT, reportText)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share Monthly Report"))
                    },
                    modifier = Modifier.testTag("share_monthly_report_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Export")
                }
            }
        }

        // Formal Statement Certificate Card
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Final Monthly Report",
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                text = financialGrade,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }

                    HorizontalDivider()

                    // Big Whole-Month Totals Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "WHOLE MONTH SAVING",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = formatCurrency(summary.totalSavings, currency),
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Avg ${formatCurrency(summary.dailyAverageSaving, currency)}/day",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "WHOLE MONTH EXPENSES",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Text(
                                    text = formatCurrency(summary.totalExpenses, currency),
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Text(
                                    text = "Avg ${formatCurrency(summary.dailyAverageExpense, currency)}/day",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    HorizontalDivider()

                    // Key Statement Line Items
                    StatementMetricRow(
                        label = "Net Monthly Surplus / Balance",
                        value = (if (summary.netBalance >= 0) "+" else "") + formatCurrency(summary.netBalance, currency),
                        highlightColor = if (summary.netBalance >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                    StatementMetricRow(
                        label = "Monthly Savings Rate",
                        value = "${String.format("%.1f", summary.savingsRatePercent)}%"
                    )
                    StatementMetricRow(
                        label = "Active Days Logged in Month",
                        value = "${summary.daysLoggedCount} of ${summary.totalDaysInMonth} days"
                    )
                    StatementMetricRow(
                        label = "Total Transactions Recorded",
                        value = "${summary.totalEntriesCount} (${summary.backdatedCount} backdated)"
                    )
                    summary.highestExpenseEntry?.let { top ->
                        StatementMetricRow(
                            label = "Largest Single Expense",
                            value = "${top.title} (${formatCurrency(top.amount, currency)})"
                        )
                    }
                }
            }
        }

        // Complete Expense Ledger Table
        item {
            Text(
                text = "Whole Month Expenses by Category",
                style = MaterialTheme.typography.titleLarge
            )
        }

        if (summary.expenseCategoryBreakdown.isEmpty()) {
            item {
                Text(
                    text = "No expenses recorded in ${summary.displayMonth}.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(
                items = summary.expenseCategoryBreakdown,
                key = { "report_exp_${it.categoryId}" }
            ) { item ->
                StatementCategoryItemRow(
                    categoryName = item.categoryName,
                    iconName = item.categoryIcon,
                    colorHex = item.categoryColorHex,
                    entryCount = item.entryCount,
                    amount = item.totalAmount,
                    percentage = item.percentageOfType,
                    currencySymbol = currency,
                    isSaving = false
                )
            }
        }

        // Complete Savings Ledger Table
        item {
            Text(
                text = "Whole Month Savings by Category",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (summary.savingCategoryBreakdown.isEmpty()) {
            item {
                Text(
                    text = "No savings entries recorded in ${summary.displayMonth}.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(
                items = summary.savingCategoryBreakdown,
                key = { "report_sav_${it.categoryId}" }
            ) { item ->
                StatementCategoryItemRow(
                    categoryName = item.categoryName,
                    iconName = item.categoryIcon,
                    colorHex = item.categoryColorHex,
                    entryCount = item.entryCount,
                    amount = item.totalAmount,
                    percentage = item.percentageOfType,
                    currencySymbol = currency,
                    isSaving = true
                )
            }
        }
    }
}

@Composable
private fun StatementMetricRow(
    label: String,
    value: String,
    highlightColor: Color? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge.copy(
                fontFamily = JetBrainsMonoFontFamily,
                fontWeight = FontWeight.Bold
            ),
            color = highlightColor ?: MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun StatementCategoryItemRow(
    categoryName: String,
    iconName: String,
    colorHex: Long,
    entryCount: Int,
    amount: Double,
    percentage: Float,
    currencySymbol: String,
    isSaving: Boolean
) {
    val catColor = Color(colorHex)
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
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
                        imageVector = getCategoryIcon(iconName),
                        contentDescription = categoryName,
                        tint = catColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Column {
                    Text(
                        text = categoryName,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "$entryCount ${if (entryCount == 1) "entry" else "entries"} · ${String.format("%.1f", percentage)}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                text = (if (isSaving) "+" else "-") + formatCurrency(amount, currencySymbol),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = JetBrainsMonoFontFamily,
                    fontWeight = FontWeight.Bold
                ),
                color = if (isSaving) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
            )
        }
    }
}
