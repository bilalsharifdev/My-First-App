package com.example.ui.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.CategorySpendingSummary
import com.example.ui.DailyTrendPoint
import com.example.ui.LedgerUiState
import com.example.ui.formatCurrency
import com.example.ui.getCategoryIcon
import com.example.ui.theme.JetBrainsMonoFontFamily

@Composable
fun AnalyticsChartsScreen(
    uiState: LedgerUiState,
    onNavigateMonth: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val summary = uiState.reportSummary
    val currency = uiState.settings.currencySymbol
    var showCumulativeTrend by remember { mutableStateOf(false) }
    var selectedDayIndex by remember { mutableIntStateOf(-1) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("analytics_charts_screen"),
        contentPadding = PaddingValues(16.dp, 12.dp, 16.dp, 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Month Switcher Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "VISUAL SPENDING & SAVING TRENDS",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = summary.displayMonth,
                        style = MaterialTheme.typography.headlineLarge
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { onNavigateMonth(-1) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Previous Month"
                        )
                    }
                    IconButton(onClick = { onNavigateMonth(1) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Next Month"
                        )
                    }
                }
            }
        }

        // 1. Daily & Cumulative Spending vs Saving Trend Chart Card
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (showCumulativeTrend) {
                                    "Cumulative Month Trajectory"
                                } else {
                                    "Daily Expenses vs Savings"
                                },
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                text = "Track how daily costs & savings evolve across ${summary.displayMonth}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = !showCumulativeTrend,
                            onClick = { showCumulativeTrend = false },
                            label = { Text("Daily Bars") },
                            modifier = Modifier.testTag("chart_mode_daily_bars")
                        )
                        FilterChip(
                            selected = showCumulativeTrend,
                            onClick = { showCumulativeTrend = true },
                            label = { Text("Cumulative Trend Line") },
                            modifier = Modifier.testTag("chart_mode_cumulative_line")
                        )
                    }

                    // Legend
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LegendDot(
                            color = MaterialTheme.colorScheme.primary,
                            label = "Savings (${formatCurrency(summary.totalSavings, currency)})"
                        )
                        LegendDot(
                            color = MaterialTheme.colorScheme.secondary,
                            label = "Expenses (${formatCurrency(summary.totalExpenses, currency)})"
                        )
                    }

                    if (showCumulativeTrend) {
                        CumulativeTrajectoryLineChart(
                            points = summary.dailyTrendPoints,
                            savingColor = MaterialTheme.colorScheme.primary,
                            expenseColor = MaterialTheme.colorScheme.secondary,
                            gridColor = MaterialTheme.colorScheme.outline,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                        )
                    } else {
                        DailyComparisonBarChart(
                            points = summary.dailyTrendPoints,
                            savingColor = MaterialTheme.colorScheme.primary,
                            expenseColor = MaterialTheme.colorScheme.secondary,
                            gridColor = MaterialTheme.colorScheme.outline,
                            onSelectDay = { selectedDayIndex = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Day 1",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Day 15",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Day ${summary.totalDaysInMonth}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (selectedDayIndex in summary.dailyTrendPoints.indices) {
                        val pt = summary.dailyTrendPoints[selectedDayIndex]
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Day ${pt.dayOfMonth} Summary:",
                                    style = MaterialTheme.typography.labelLarge
                                )
                                Text(
                                    text = "Saved: ${formatCurrency(pt.savingAmount, currency)} · Spent: ${formatCurrency(pt.expenseAmount, currency)}",
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. Category Expense Distribution Donut Chart Card
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
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Expense Breakdown by Category",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = "Proportion of school fees, electricity, gas, water bills, and daily costs",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (summary.expenseCategoryBreakdown.isEmpty()) {
                        Text(
                            text = "No expenses logged for ${summary.displayMonth} yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            Box(
                                modifier = Modifier.size(150.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CategoryDonutChart(
                                    categories = summary.expenseCategoryBreakdown,
                                    modifier = Modifier.size(140.dp)
                                )
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "Spent",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = formatCurrency(summary.totalExpenses, currency),
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontFamily = JetBrainsMonoFontFamily,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }

                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                summary.expenseCategoryBreakdown.take(5).forEach { item ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(10.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(item.categoryColorHex))
                                            )
                                            Text(
                                                text = item.categoryName,
                                                style = MaterialTheme.typography.bodySmall,
                                                maxLines = 1
                                            )
                                        }
                                        Text(
                                            text = "${item.percentageOfType.toInt()}%",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Detailed Category Ranking Bars (School Fees, Electricity, Gas, Water, etc.)
        item {
            Text(
                text = "Category Ranking & Share",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        items(
            items = summary.expenseCategoryBreakdown,
            key = { "exp_cat_${it.categoryId}" }
        ) { catSummary ->
            CategoryBreakdownRowCard(
                item = catSummary,
                currencySymbol = currency
            )
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun DailyComparisonBarChart(
    points: List<DailyTrendPoint>,
    savingColor: Color,
    expenseColor: Color,
    gridColor: Color,
    onSelectDay: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val maxVal = remember(points) {
        points.maxOfOrNull { maxOf(it.expenseAmount, it.savingAmount) }?.coerceAtLeast(10.0) ?: 100.0
    }

    Canvas(
        modifier = modifier.clickable {
            val activeIdx = points.indexOfLast { it.expenseAmount > 0 || it.savingAmount > 0 }
            onSelectDay(activeIdx)
        }
    ) {
        val width = size.width
        val height = size.height
        if (points.isEmpty() || width <= 0f || height <= 0f) return@Canvas

        // Horizontal reference grid lines
        for (i in 0..3) {
            val y = height * (i / 3f)
            drawLine(
                color = gridColor.copy(alpha = 0.45f),
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1.dp.toPx()
            )
        }

        val slotWidth = width / points.size.toFloat()
        val barWidth = (slotWidth * 0.36f).coerceAtLeast(2f)

        points.forEachIndexed { idx, pt ->
            val baseX = idx * slotWidth
            if (pt.savingAmount > 0) {
                val savHeight = ((pt.savingAmount / maxVal).toFloat() * (height * 0.88f)).coerceAtLeast(4f)
                drawRoundRect(
                    color = savingColor,
                    topLeft = Offset(baseX + slotWidth * 0.12f, height - savHeight),
                    size = Size(barWidth, savHeight),
                    cornerRadius = CornerRadius(4f, 4f)
                )
            }
            if (pt.expenseAmount > 0) {
                val expHeight = ((pt.expenseAmount / maxVal).toFloat() * (height * 0.88f)).coerceAtLeast(4f)
                drawRoundRect(
                    color = expenseColor,
                    topLeft = Offset(baseX + slotWidth * 0.52f, height - expHeight),
                    size = Size(barWidth, expHeight),
                    cornerRadius = CornerRadius(4f, 4f)
                )
            }
        }
    }
}

@Composable
private fun CumulativeTrajectoryLineChart(
    points: List<DailyTrendPoint>,
    savingColor: Color,
    expenseColor: Color,
    gridColor: Color,
    modifier: Modifier = Modifier
) {
    val maxCumulative = remember(points) {
        points.maxOfOrNull { maxOf(it.cumulativeExpense, it.cumulativeSaving) }?.coerceAtLeast(10.0) ?: 100.0
    }

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        if (points.size < 2 || width <= 0f || height <= 0f) return@Canvas

        for (i in 0..3) {
            val y = height * (i / 3f)
            drawLine(
                color = gridColor.copy(alpha = 0.45f),
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1.dp.toPx()
            )
        }

        val savingPath = Path()
        val expensePath = Path()

        points.forEachIndexed { idx, pt ->
            val x = (idx.toFloat() / (points.size - 1).toFloat()) * width
            val savY = height - ((pt.cumulativeSaving / maxCumulative).toFloat() * (height * 0.88f))
            val expY = height - ((pt.cumulativeExpense / maxCumulative).toFloat() * (height * 0.88f))

            if (idx == 0) {
                savingPath.moveTo(x, savY)
                expensePath.moveTo(x, expY)
            } else {
                savingPath.lineTo(x, savY)
                expensePath.lineTo(x, expY)
            }
        }

        drawPath(
            path = savingPath,
            color = savingColor,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )
        drawPath(
            path = expensePath,
            color = expenseColor,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}

@Composable
private fun CategoryDonutChart(
    categories: List<CategorySpendingSummary>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val strokeWidth = 22.dp.toPx()
        val diameter = size.minDimension - strokeWidth
        val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)

        var startAngle = -90f
        categories.forEach { cat ->
            val sweep = (cat.percentageOfType / 100f) * 360f
            drawArc(
                color = Color(cat.categoryColorHex),
                startAngle = startAngle,
                sweepAngle = (sweep - 2f).coerceAtLeast(2f),
                useCenter = false,
                topLeft = topLeft,
                size = Size(diameter, diameter),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
            )
            startAngle += sweep
        }
    }
}

@Composable
private fun CategoryBreakdownRowCard(
    item: CategorySpendingSummary,
    currencySymbol: String
) {
    val catColor = Color(item.categoryColorHex)
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(catColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getCategoryIcon(item.categoryIcon),
                            contentDescription = item.categoryName,
                            tint = catColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = item.categoryName,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "${item.entryCount} ${if (item.entryCount == 1) "entry" else "entries"} this month",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = formatCurrency(item.totalAmount, currencySymbol),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = "${String.format("%.1f", item.percentageOfType)}% of expenses",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            LinearProgressIndicator(
                progress = { (item.percentageOfType / 100f).coerceIn(0f, 1f) },
                color = catColor,
                trackColor = catColor.copy(alpha = 0.15f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
            )
        }
    }
}
