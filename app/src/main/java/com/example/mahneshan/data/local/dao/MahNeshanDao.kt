package com.example.mahneshan.data.local.dao

import androidx.room.*
import com.example.mahneshan.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MahNeshanDao {

    // --- مدیریت دوره‌های قاعدگی ---
    @Query("SELECT * FROM cycles ORDER BY startDateEpochDay DESC")
    fun getAllCycles(): Flow<List<CycleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCycle(cycle: CycleEntity): Long

    @Update
    suspend fun updateCycle(cycle: CycleEntity)

    @Delete
    suspend fun deleteCycle(cycle: CycleEntity)

    // --- مدیریت علائم روزانه ---
    @Query("SELECT * FROM daily_logs ORDER BY dateEpochDay DESC")
    fun getAllDailyLogs(): Flow<List<DailyLogEntity>>

    @Query("SELECT * FROM daily_logs WHERE dateEpochDay = :epochDay LIMIT 1")
    suspend fun getLogForDate(epochDay: Long): DailyLogEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateDailyLog(log: DailyLogEntity)

    // --- مدیریت داروها و مصرف روزانه ---
    @Query("SELECT * FROM medications WHERE isActive = 1")
    fun getActiveMedications(): Flow<List<MedicationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedication(medication: MedicationEntity): Long

    @Delete
    suspend fun deleteMedication(medication: MedicationEntity)

    @Query("SELECT * FROM medication_logs WHERE dateEpochDay = :epochDay")
    fun getMedicationLogsForDate(epochDay: Long): Flow<List<MedicationLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicationLog(log: MedicationLogEntity)
}