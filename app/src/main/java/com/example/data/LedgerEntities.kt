package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class EntryType(val label: String) {
    EXPENSE("Expense"),
    SAVING("Saving")
}

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val iconName: String,
    val colorHex: Long,
    val entryType: String, // "EXPENSE" or "SAVING"
    val isDefault: Boolean = false
)

@Entity(tableName = "ledger_entries")
data class LedgerEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val amount: Double,
    val entryType: String, // "EXPENSE" or "SAVING"
    val categoryId: Long,
    val categoryName: String,
    val categoryIcon: String,
    val categoryColorHex: Long,
    val transactionTimestamp: Long, // Exact date & time of the transaction (supports backdating)
    val createdAtTimestamp: Long,   // Actual log timestamp
    val isBackdated: Boolean,       // True when logged for a previous calendar date
    val yearMonth: String,          // "YYYY-MM" for monthly aggregation
    val dayOfMonth: Int,            // 1..31
    val paymentMethod: String = "Cash",
    val notes: String = ""
)

@Entity(tableName = "app_settings")
data class AppSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val currencySymbol: String = "PKR",
    val monthlyBudgetTarget: Double = 120000.0,
    val monthlySavingsTarget: Double = 45000.0,
    val reminderEnabled: Boolean = true,
    val reminderHour: Int = 20,
    val reminderMinute: Int = 0
)
