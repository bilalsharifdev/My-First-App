package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface LedgerDao {

    // Categories
    @Query("SELECT * FROM categories ORDER BY isDefault DESC, name ASC")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun getCategoryCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryEntity>)

    @Delete
    suspend fun deleteCategory(category: CategoryEntity)

    // Ledger Entries
    @Query("SELECT * FROM ledger_entries WHERE yearMonth = :yearMonth ORDER BY transactionTimestamp DESC")
    fun getEntriesForMonth(yearMonth: String): Flow<List<LedgerEntryEntity>>

    @Query("SELECT * FROM ledger_entries ORDER BY transactionTimestamp DESC")
    fun getAllEntries(): Flow<List<LedgerEntryEntity>>

    @Query("SELECT DISTINCT yearMonth FROM ledger_entries ORDER BY yearMonth DESC")
    fun getAvailableYearMonths(): Flow<List<String>>

    @Query("SELECT COUNT(*) FROM ledger_entries")
    suspend fun getEntryCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: LedgerEntryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntries(entries: List<LedgerEntryEntity>)

    @Update
    suspend fun updateEntry(entry: LedgerEntryEntity)

    @Delete
    suspend fun deleteEntry(entry: LedgerEntryEntity)

    @Query("DELETE FROM ledger_entries")
    suspend fun deleteAllEntries()

    // App Settings
    @Query("SELECT * FROM app_settings WHERE id = 1")
    fun getAppSettings(): Flow<AppSettingsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveAppSettings(settings: AppSettingsEntity)
}
