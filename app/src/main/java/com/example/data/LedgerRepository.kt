package com.example.data

import kotlinx.coroutines.flow.Flow

class LedgerRepository(private val dao: LedgerDao) {

    val allCategories: Flow<List<CategoryEntity>> = dao.getAllCategories()
    val allEntries: Flow<List<LedgerEntryEntity>> = dao.getAllEntries()
    val appSettings: Flow<AppSettingsEntity?> = dao.getAppSettings()

    fun getEntriesForMonth(yearMonth: String): Flow<List<LedgerEntryEntity>> {
        return dao.getEntriesForMonth(yearMonth)
    }

    suspend fun insertCategory(category: CategoryEntity): Long {
        return dao.insertCategory(category)
    }

    suspend fun deleteCategory(category: CategoryEntity) {
        dao.deleteCategory(category)
    }

    suspend fun insertEntry(entry: LedgerEntryEntity): Long {
        return dao.insertEntry(entry)
    }

    suspend fun updateEntry(entry: LedgerEntryEntity) {
        dao.updateEntry(entry)
    }

    suspend fun deleteEntry(entry: LedgerEntryEntity) {
        dao.deleteEntry(entry)
    }

    suspend fun clearAllEntries() {
        dao.deleteAllEntries()
    }

    suspend fun saveSettings(settings: AppSettingsEntity) {
        dao.saveAppSettings(settings)
    }

    suspend fun ensureSeeded() {
        if (dao.getCategoryCount() == 0) {
            val defaultCategories = listOf(
                CategoryEntity(
                    id = 1,
                    name = "Children School Fees",
                    iconName = "school",
                    colorHex = 0xFF0F2C59,
                    entryType = EntryType.EXPENSE.name,
                    isDefault = true
                ),
                CategoryEntity(
                    id = 2,
                    name = "Electricity Bill",
                    iconName = "bolt",
                    colorHex = 0xFF1D4ED8,
                    entryType = EntryType.EXPENSE.name,
                    isDefault = true
                ),
                CategoryEntity(
                    id = 3,
                    name = "Gas Bill",
                    iconName = "local_fire_department",
                    colorHex = 0xFF0284C7,
                    entryType = EntryType.EXPENSE.name,
                    isDefault = true
                ),
                CategoryEntity(
                    id = 4,
                    name = "Water Bill",
                    iconName = "water_drop",
                    colorHex = 0xFF0369A1,
                    entryType = EntryType.EXPENSE.name,
                    isDefault = true
                ),
                CategoryEntity(
                    id = 5,
                    name = "Groceries & Kitchen",
                    iconName = "shopping_cart",
                    colorHex = 0xFF1E3A8A,
                    entryType = EntryType.EXPENSE.name,
                    isDefault = true
                ),
                CategoryEntity(
                    id = 6,
                    name = "Transport & Fuel",
                    iconName = "directions_car",
                    colorHex = 0xFF2563EB,
                    entryType = EntryType.EXPENSE.name,
                    isDefault = true
                ),
                CategoryEntity(
                    id = 7,
                    name = "Healthcare & Medical",
                    iconName = "medical_services",
                    colorHex = 0xFF3B82F6,
                    entryType = EntryType.EXPENSE.name,
                    isDefault = true
                ),
                CategoryEntity(
                    id = 8,
                    name = "Internet & Phone Bill",
                    iconName = "wifi",
                    colorHex = 0xFF4F46E5,
                    entryType = EntryType.EXPENSE.name,
                    isDefault = true
                ),
                CategoryEntity(
                    id = 9,
                    name = "Home & Maintenance",
                    iconName = "home",
                    colorHex = 0xFF334155,
                    entryType = EntryType.EXPENSE.name,
                    isDefault = true
                ),
                // Savings Categories
                CategoryEntity(
                    id = 10,
                    name = "Daily Cash Saving",
                    iconName = "savings",
                    colorHex = 0xFF0A2540,
                    entryType = EntryType.SAVING.name,
                    isDefault = true
                ),
                CategoryEntity(
                    id = 11,
                    name = "Monthly Bank Saving",
                    iconName = "account_balance",
                    colorHex = 0xFF0F2C59,
                    entryType = EntryType.SAVING.name,
                    isDefault = true
                ),
                CategoryEntity(
                    id = 12,
                    name = "Emergency & Future Fund",
                    iconName = "verified_user",
                    colorHex = 0xFF1E40AF,
                    entryType = EntryType.SAVING.name,
                    isDefault = true
                )
            )
            dao.insertCategories(defaultCategories)
            dao.saveAppSettings(
                AppSettingsEntity(
                    currencySymbol = "PKR",
                    monthlyBudgetTarget = 0.0,
                    monthlySavingsTarget = 0.0
                )
            )
        }
    }

    suspend fun seedSampleEntries() {
        // Intentionally empty: user enters all expenses and savings on their own
    }
}
