package com.example.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AppSettingsEntity
import com.example.data.CategoryEntity
import com.example.data.EntryType
import com.example.data.LedgerEntryEntity
import com.example.data.LedgerRepository
import com.example.notifications.DailyReminderManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class LedgerFilterType(val label: String) {
    ALL("All Entries"),
    EXPENSE("Expenses"),
    SAVING("Savings"),
    BACKDATED("Backdated")
}

data class CategorySpendingSummary(
    val categoryId: Long,
    val categoryName: String,
    val categoryIcon: String,
    val categoryColorHex: Long,
    val entryType: String,
    val totalAmount: Double,
    val entryCount: Int,
    val percentageOfType: Float
)

data class DailyTrendPoint(
    val dayOfMonth: Int,
    val expenseAmount: Double,
    val savingAmount: Double,
    val cumulativeExpense: Double,
    val cumulativeSaving: Double
)

data class MonthlyReportSummary(
    val yearMonth: String,
    val displayMonth: String,
    val totalExpenses: Double,
    val totalSavings: Double,
    val netBalance: Double,
    val savingsRatePercent: Float,
    val dailyAverageExpense: Double,
    val dailyAverageSaving: Double,
    val totalEntriesCount: Int,
    val backdatedCount: Int,
    val highestExpenseEntry: LedgerEntryEntity?,
    val expenseCategoryBreakdown: List<CategorySpendingSummary>,
    val savingCategoryBreakdown: List<CategorySpendingSummary>,
    val dailyTrendPoints: List<DailyTrendPoint>,
    val daysLoggedCount: Int,
    val totalDaysInMonth: Int,
    val unloggedDaysUpToToday: List<Int>
)

data class LedgerUiState(
    val selectedYearMonth: String = SimpleDateFormat("yyyy-MM", Locale.US).format(Date()),
    val availableYearMonths: List<String> = emptyList(),
    val allEntriesForMonth: List<LedgerEntryEntity> = emptyList(),
    val filteredEntriesForMonth: List<LedgerEntryEntity> = emptyList(),
    val categories: List<CategoryEntity> = emptyList(),
    val settings: AppSettingsEntity = AppSettingsEntity(),
    val filterType: LedgerFilterType = LedgerFilterType.ALL,
    val selectedCategoryFilterId: Long? = null,
    val searchQuery: String = "",
    val reportSummary: MonthlyReportSummary = emptyReportSummary()
)

private fun emptyReportSummary(): MonthlyReportSummary {
    val ym = SimpleDateFormat("yyyy-MM", Locale.US).format(Date())
    return MonthlyReportSummary(
        yearMonth = ym,
        displayMonth = formatYearMonthDisplay(ym),
        totalExpenses = 0.0,
        totalSavings = 0.0,
        netBalance = 0.0,
        savingsRatePercent = 0f,
        dailyAverageExpense = 0.0,
        dailyAverageSaving = 0.0,
        totalEntriesCount = 0,
        backdatedCount = 0,
        highestExpenseEntry = null,
        expenseCategoryBreakdown = emptyList(),
        savingCategoryBreakdown = emptyList(),
        dailyTrendPoints = emptyList(),
        daysLoggedCount = 0,
        totalDaysInMonth = 30,
        unloggedDaysUpToToday = emptyList()
    )
}

class LedgerViewModel(
    private val repository: LedgerRepository
) : ViewModel() {

    private val currentYm = SimpleDateFormat("yyyy-MM", Locale.US).format(Date())
    private val _selectedYearMonth = MutableStateFlow(currentYm)
    private val _filterType = MutableStateFlow(LedgerFilterType.ALL)
    private val _selectedCategoryFilterId = MutableStateFlow<Long?>(null)
    private val _searchQuery = MutableStateFlow("")

    init {
        viewModelScope.launch {
            repository.ensureSeeded()
        }
    }

    private val filterControlFlow = combine(
        _selectedYearMonth,
        _filterType,
        _selectedCategoryFilterId,
        _searchQuery
    ) { ym, filter, catId, query ->
        FilterControls(ym, filter, catId, query)
    }

    val uiState: StateFlow<LedgerUiState> = combine(
        repository.allEntries,
        repository.allCategories,
        repository.appSettings,
        filterControlFlow
    ) { allEntries, categories, settingsOrNull, controls ->
        val settings = settingsOrNull ?: AppSettingsEntity()
        val ym = controls.yearMonth

        val distinctMonths = (allEntries.map { it.yearMonth } + currentYm + ym)
            .distinct()
            .sortedDescending()

        val monthEntries = allEntries.filter { it.yearMonth == ym }
            .sortedByDescending { it.transactionTimestamp }

        val filtered = monthEntries.filter { entry ->
            val matchesType = when (controls.filterType) {
                LedgerFilterType.ALL -> true
                LedgerFilterType.EXPENSE -> entry.entryType == EntryType.EXPENSE.name
                LedgerFilterType.SAVING -> entry.entryType == EntryType.SAVING.name
                LedgerFilterType.BACKDATED -> entry.isBackdated
            }
            val matchesCategory = controls.categoryId == null || entry.categoryId == controls.categoryId
            val matchesQuery = controls.searchQuery.isBlank() ||
                entry.title.contains(controls.searchQuery, ignoreCase = true) ||
                entry.categoryName.contains(controls.searchQuery, ignoreCase = true) ||
                entry.notes.contains(controls.searchQuery, ignoreCase = true)

            matchesType && matchesCategory && matchesQuery
        }

        val summary = buildMonthlyReportSummary(ym, monthEntries)

        LedgerUiState(
            selectedYearMonth = ym,
            availableYearMonths = distinctMonths,
            allEntriesForMonth = monthEntries,
            filteredEntriesForMonth = filtered,
            categories = categories,
            settings = settings,
            filterType = controls.filterType,
            selectedCategoryFilterId = controls.categoryId,
            searchQuery = controls.searchQuery,
            reportSummary = summary
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = LedgerUiState()
    )

    private data class FilterControls(
        val yearMonth: String,
        val filterType: LedgerFilterType,
        val categoryId: Long?,
        val searchQuery: String
    )

    private fun buildMonthlyReportSummary(
        yearMonth: String,
        monthEntries: List<LedgerEntryEntity>
    ): MonthlyReportSummary {
        val expenses = monthEntries.filter { it.entryType == EntryType.EXPENSE.name }
        val savings = monthEntries.filter { it.entryType == EntryType.SAVING.name }

        val totalExpenses = expenses.sumOf { it.amount }
        val totalSavings = savings.sumOf { it.amount }
        val netBalance = totalSavings - totalExpenses
        val totalVolume = totalSavings + totalExpenses
        val savingsRate = if (totalVolume > 0.0) {
            ((totalSavings / totalVolume) * 100.0).toFloat().coerceIn(0f, 100f)
        } else 0f

        // Calculate days in selected month
        val cal = Calendar.getInstance()
        val parts = yearMonth.split("-")
        val year = parts.getOrNull(0)?.toIntOrNull() ?: cal.get(Calendar.YEAR)
        val monthOneBased = parts.getOrNull(1)?.toIntOrNull() ?: (cal.get(Calendar.MONTH) + 1)

        val monthCal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, monthOneBased - 1)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        val totalDaysInMonth = monthCal.getActualMaximum(Calendar.DAY_OF_MONTH)

        val nowCal = Calendar.getInstance()
        val isCurrentMonth = nowCal.get(Calendar.YEAR) == year &&
            (nowCal.get(Calendar.MONTH) + 1) == monthOneBased
        val elapsedDays = if (isCurrentMonth) {
            nowCal.get(Calendar.DAY_OF_MONTH).coerceAtLeast(1)
        } else {
            totalDaysInMonth
        }

        val dailyAvgExpense = totalExpenses / elapsedDays.toDouble()
        val dailyAvgSaving = totalSavings / elapsedDays.toDouble()

        val expenseByCategory = expenses.groupBy { it.categoryId }.map { (catId, list) ->
            val first = list.first()
            val sum = list.sumOf { it.amount }
            val pct = if (totalExpenses > 0) ((sum / totalExpenses) * 100.0).toFloat() else 0f
            CategorySpendingSummary(
                categoryId = catId,
                categoryName = first.categoryName,
                categoryIcon = first.categoryIcon,
                categoryColorHex = first.categoryColorHex,
                entryType = EntryType.EXPENSE.name,
                totalAmount = sum,
                entryCount = list.size,
                percentageOfType = pct
            )
        }.sortedByDescending { it.totalAmount }

        val savingByCategory = savings.groupBy { it.categoryId }.map { (catId, list) ->
            val first = list.first()
            val sum = list.sumOf { it.amount }
            val pct = if (totalSavings > 0) ((sum / totalSavings) * 100.0).toFloat() else 0f
            CategorySpendingSummary(
                categoryId = catId,
                categoryName = first.categoryName,
                categoryIcon = first.categoryIcon,
                categoryColorHex = first.categoryColorHex,
                entryType = EntryType.SAVING.name,
                totalAmount = sum,
                entryCount = list.size,
                percentageOfType = pct
            )
        }.sortedByDescending { it.totalAmount }

        // Build daily trend points for 1..totalDaysInMonth
        var runningExpense = 0.0
        var runningSaving = 0.0
        val trendPoints = (1..totalDaysInMonth).map { day ->
            val dayExp = expenses.filter { it.dayOfMonth == day }.sumOf { it.amount }
            val daySav = savings.filter { it.dayOfMonth == day }.sumOf { it.amount }
            runningExpense += dayExp
            runningSaving += daySav
            DailyTrendPoint(
                dayOfMonth = day,
                expenseAmount = dayExp,
                savingAmount = daySav,
                cumulativeExpense = runningExpense,
                cumulativeSaving = runningSaving
            )
        }

        val loggedDaysSet = monthEntries.map { it.dayOfMonth }.toSet()
        val unloggedDays = (1..elapsedDays).filter { it !in loggedDaysSet }

        return MonthlyReportSummary(
            yearMonth = yearMonth,
            displayMonth = formatYearMonthDisplay(yearMonth),
            totalExpenses = totalExpenses,
            totalSavings = totalSavings,
            netBalance = netBalance,
            savingsRatePercent = savingsRate,
            dailyAverageExpense = dailyAvgExpense,
            dailyAverageSaving = dailyAvgSaving,
            totalEntriesCount = monthEntries.size,
            backdatedCount = monthEntries.count { it.isBackdated },
            highestExpenseEntry = expenses.maxByOrNull { it.amount },
            expenseCategoryBreakdown = expenseByCategory,
            savingCategoryBreakdown = savingByCategory,
            dailyTrendPoints = trendPoints,
            daysLoggedCount = loggedDaysSet.size,
            totalDaysInMonth = totalDaysInMonth,
            unloggedDaysUpToToday = unloggedDays
        )
    }

    fun selectYearMonth(yearMonth: String) {
        _selectedYearMonth.value = yearMonth
    }

    fun navigateMonth(delta: Int) {
        val current = _selectedYearMonth.value
        val parts = current.split("-")
        val year = parts.getOrNull(0)?.toIntOrNull() ?: return
        val month = parts.getOrNull(1)?.toIntOrNull() ?: return
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, 1)
            add(Calendar.MONTH, delta)
        }
        val ymFormat = SimpleDateFormat("yyyy-MM", Locale.US)
        _selectedYearMonth.value = ymFormat.format(cal.time)
    }

    fun setFilterType(filterType: LedgerFilterType) {
        _filterType.value = filterType
    }

    fun setCategoryFilter(categoryId: Long?) {
        _selectedCategoryFilterId.value =
            if (_selectedCategoryFilterId.value == categoryId) null else categoryId
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun saveLedgerEntry(
        existingId: Long = 0L,
        title: String,
        amount: Double,
        entryType: EntryType,
        category: CategoryEntity,
        transactionTimestamp: Long,
        paymentMethod: String,
        notes: String
    ) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val cal = Calendar.getInstance().apply { timeInMillis = transactionTimestamp }
            val ym = SimpleDateFormat("yyyy-MM", Locale.US).format(cal.time)
            val dayOfMonth = cal.get(Calendar.DAY_OF_MONTH)
            val backdated = isDateBackdated(transactionTimestamp, now)

            val entity = LedgerEntryEntity(
                id = existingId,
                title = title.trim().ifEmpty { category.name },
                amount = amount,
                entryType = entryType.name,
                categoryId = category.id,
                categoryName = category.name,
                categoryIcon = category.iconName,
                categoryColorHex = category.colorHex,
                transactionTimestamp = transactionTimestamp,
                createdAtTimestamp = now,
                isBackdated = backdated,
                yearMonth = ym,
                dayOfMonth = dayOfMonth,
                paymentMethod = paymentMethod,
                notes = notes.trim()
            )
            if (existingId == 0L) {
                repository.insertEntry(entity)
            } else {
                repository.updateEntry(entity)
            }
            // Automatically switch view to the month of the entry so the user sees it immediately
            _selectedYearMonth.value = ym
        }
    }

    fun deleteEntry(entry: LedgerEntryEntity) {
        viewModelScope.launch {
            repository.deleteEntry(entry)
        }
    }

    fun addCustomCategory(
        name: String,
        iconName: String,
        colorHex: Long,
        entryType: EntryType,
        onCreated: (CategoryEntity) -> Unit = {}
    ) {
        viewModelScope.launch {
            val trimmed = name.trim()
            if (trimmed.isEmpty()) return@launch
            val newCat = CategoryEntity(
                name = trimmed,
                iconName = iconName,
                colorHex = colorHex,
                entryType = entryType.name,
                isDefault = false
            )
            val newId = repository.insertCategory(newCat)
            onCreated(newCat.copy(id = newId))
        }
    }

    fun deleteCategory(category: CategoryEntity) {
        viewModelScope.launch {
            if (!category.isDefault) {
                repository.deleteCategory(category)
                if (_selectedCategoryFilterId.value == category.id) {
                    _selectedCategoryFilterId.value = null
                }
            }
        }
    }

    fun updateSettings(
        context: Context,
        currencySymbol: String,
        monthlyBudgetTarget: Double,
        monthlySavingsTarget: Double,
        reminderEnabled: Boolean,
        reminderHour: Int,
        reminderMinute: Int
    ) {
        viewModelScope.launch {
            val updated = AppSettingsEntity(
                id = 1,
                currencySymbol = currencySymbol,
                monthlyBudgetTarget = monthlyBudgetTarget,
                monthlySavingsTarget = monthlySavingsTarget,
                reminderEnabled = reminderEnabled,
                reminderHour = reminderHour,
                reminderMinute = reminderMinute
            )
            repository.saveSettings(updated)
            DailyReminderManager.scheduleDailyReminder(
                context = context,
                enabled = reminderEnabled,
                hourOfDay = reminderHour,
                minute = reminderMinute
            )
        }
    }

    fun clearAllEntries() {
        viewModelScope.launch {
            repository.clearAllEntries()
        }
    }

    fun restoreSampleEntries() {
        viewModelScope.launch {
            repository.seedSampleEntries()
        }
    }

    companion object {
        fun provideFactory(repository: LedgerRepository): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return LedgerViewModel(repository) as T
                }
            }
        }
    }
}
