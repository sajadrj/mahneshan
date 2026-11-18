package com.example.mahneshan.presentation.home

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mahneshan.data.backup.BackupManager
import com.example.mahneshan.data.local.AppDatabase
import com.example.mahneshan.data.local.entity.CycleEntity
import com.example.mahneshan.data.local.entity.DailyLogEntity
import com.example.mahneshan.data.local.entity.MedicationEntity
import com.example.mahneshan.data.local.entity.MedicationLogEntity
import com.example.mahneshan.data.preferences.AppMode
import com.example.mahneshan.data.preferences.UserPreferencesManager
import com.example.mahneshan.domain.CycleCalculator
import com.example.mahneshan.domain.CycleSummary
import com.example.mahneshan.domain.SmartInsight
import com.example.mahneshan.domain.SmartInsightEngine
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

data class MedicationUiItem(
    val medication: MedicationEntity,
    val isTakenToday: Boolean
)

data class HomeUiState(
    val appMode: AppMode = AppMode.TRACKING,
    val summary: CycleSummary = CycleCalculator.calculateSummary(emptyList(), AppMode.TRACKING),
    val insights: List<SmartInsight> = emptyList(),
    val todayMedications: List<MedicationUiItem> = emptyList(),
    val cycles: List<CycleEntity> = emptyList()
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getDatabase(application).mahNeshanDao()
    private val prefs = UserPreferencesManager(application)
    private val todayEpoch = LocalDate.now().toEpochDay()

    val allDailyLogs: Flow<List<DailyLogEntity>> = dao.getAllDailyLogs()

    val uiState: StateFlow<HomeUiState> = combine(
        dao.getAllCycles(),
        dao.getAllDailyLogs(),
        dao.getActiveMedications(),
        dao.getMedicationLogsForDate(todayEpoch),
        prefs.appMode
    ) { cycles, dailyLogs, medications, medLogs, mode ->

        val summary = CycleCalculator.calculateSummary(
            cycles = cycles,
            mode = mode
        )

        val insights = if (mode != AppMode.PREGNANCY) {
            SmartInsightEngine.generateInsights(
                cycles = cycles,
                dailyLogs = dailyLogs,
                avgCycleLen = summary.averageCycleLength,
                todayEpochDay = todayEpoch
            )
        } else {
            emptyList()
        }

        val takenIds = medLogs.filter { it.isTaken }.map { it.medicationId }.toSet()
        val medUiItems = medications.map { med ->
            MedicationUiItem(
                medication = med,
                isTakenToday = med.id in takenIds
            )
        }

        HomeUiState(
            appMode = mode,
            summary = summary,
            insights = insights,
            todayMedications = medUiItems,
            cycles = cycles
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )

    // تغییر حالت برنامه (عادی / اقدام / بارداری)
    fun setAppMode(mode: AppMode) {
        viewModelScope.launch {
            prefs.setAppMode(mode)
        }
    }

    // ثبت شروع پریود جدید (امروز یا تاریخ دلخواه)
    fun logPeriodStart(startEpochDay: Long = LocalDate.now().toEpochDay()) {
        viewModelScope.launch {
            val currentCycles = uiState.value.cycles
            val previousStart = currentCycles.firstOrNull()?.startDateEpochDay

            // محاسبه طول چرخه نسبت به دوره قبلی (در صورت وجود)
            val calculatedLength = if (previousStart != null && startEpochDay > previousStart) {
                (startEpochDay - previousStart).toInt()
            } else {
                null
            }

            dao.insertCycle(
                CycleEntity(
                    startDateEpochDay = startEpochDay,
                    cycleLength = calculatedLength
                )
            )
        }
    }

    // ثبت پایان پریود فعلی
    fun logPeriodEnd(cycleId: Long, endEpochDay: Long = LocalDate.now().toEpochDay()) {
        viewModelScope.launch {
            val targetCycle = uiState.value.cycles.find { it.id == cycleId } ?: return@launch
            dao.updateCycle(targetCycle.copy(endDateEpochDay = endEpochDay))
        }
    }

    // ثبت یا ویرایش یادداشت و علائم روزانه
    fun saveDailyLog(
        dateEpochDay: Long,
        flowLevel: Int,
        mood: String,
        symptoms: List<String>,
        note: String
    ) {
        viewModelScope.launch {
            dao.insertOrUpdateDailyLog(
                DailyLogEntity(
                    dateEpochDay = dateEpochDay,
                    flowLevel = flowLevel,
                    mood = mood,
                    symptoms = symptoms.joinToString(","),
                    note = note
                )
            )
        }
    }

    // افزودن داروی جدید
    fun addMedication(name: String, hour: Int, minute: Int) {
        if (name.isBlank()) return
        viewModelScope.launch {
            dao.insertMedication(
                MedicationEntity(name = name.trim(), hour = hour, minute = minute)
            )
        }
    }

    // تیک زدن «امروز خوردم» یا برداشتن تیک
    fun toggleMedicationTaken(medicationId: Long, currentlyTaken: Boolean) {
        viewModelScope.launch {
            dao.insertMedicationLog(
                MedicationLogEntity(
                    medicationId = medicationId,
                    dateEpochDay = todayEpoch,
                    isTaken = !currentlyTaken
                )
            )
        }
    }

    private val backupManager = BackupManager(application)

    fun exportDatabase(uri: Uri, onResult: (String) -> Unit) {
        viewModelScope.launch {
            val result = backupManager.exportToUri(uri)
            if (result.isSuccess) {
                onResult("فایل پشتیبان با موفقیت ذخیره شد.")
            } else {
                onResult("خطا در ذخیره فایل پشتیبان.")
            }
        }
    }

    fun importDatabase(uri: Uri, onResult: (String) -> Unit) {
        viewModelScope.launch {
            val result = backupManager.importFromUri(uri)
            if (result.isSuccess) {
                onResult("اطلاعات با موفقیت بازیابی شد.")
            } else {
                onResult("فایل انتخاب‌شده معتبر نیست.")
            }
        }
    }
}
