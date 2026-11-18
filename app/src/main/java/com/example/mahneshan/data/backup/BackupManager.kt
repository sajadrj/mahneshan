package com.example.mahneshan.data.backup

import android.content.Context
import android.net.Uri
import com.example.mahneshan.data.local.AppDatabase
import com.example.mahneshan.data.local.entity.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class BackupManager(private val context: Context) {

    private val dao = AppDatabase.getDatabase(context).mahNeshanDao()

    // خروجی گرفتن از کل دیتابیس در مسیر انتخاب‌شده توسط کاربر
    suspend fun exportToUri(uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val rootJson = JSONObject()

            // ۱. دوره‌های قاعدگی
            val cyclesArray = JSONArray()
            dao.getAllCyclesOnce().forEach { cycle ->
                cyclesArray.put(JSONObject().apply {
                    put("id", cycle.id)
                    put("startDateEpochDay", cycle.startDateEpochDay)
                    put("endDateEpochDay", cycle.endDateEpochDay ?: JSONObject.NULL)
                    put("cycleLength", cycle.cycleLength ?: JSONObject.NULL)
                })
            }
            rootJson.put("cycles", cyclesArray)

            // ۲. علائم روزانه
            val logsArray = JSONArray()
            dao.getAllDailyLogsOnce().forEach { log ->
                logsArray.put(JSONObject().apply {
                    put("dateEpochDay", log.dateEpochDay)
                    put("flowLevel", log.flowLevel)
                    put("mood", log.mood)
                    put("symptoms", log.symptoms)
                    put("note", log.note)
                })
            }
            rootJson.put("daily_logs", logsArray)

            // ۳. داروها
            val medsArray = JSONArray()
            dao.getAllMedicationsOnce().forEach { med ->
                medsArray.put(JSONObject().apply {
                    put("id", med.id)
                    put("name", med.name)
                    put("hour", med.hour)
                    put("minute", med.minute)
                    put("isActive", med.isActive)
                })
            }
            rootJson.put("medications", medsArray)

            // ۴. سوابق مصرف دارو
            val medLogsArray = JSONArray()
            dao.getAllMedicationLogsOnce().forEach { mLog ->
                medLogsArray.put(JSONObject().apply {
                    put("medicationId", mLog.medicationId)
                    put("dateEpochDay", mLog.dateEpochDay)
                    put("isTaken", mLog.isTaken)
                })
            }
            rootJson.put("medication_logs", medLogsArray)

            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(rootJson.toString(2).toByteArray(Charsets.UTF_8))
            } ?: return@withContext Result.failure(Exception("خطا در باز کردن فایل خروجی"))

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // بازیابی اطلاعات از فایل بکاپ انتخاب‌شده
    suspend fun importFromUri(uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val jsonString = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.bufferedReader(Charsets.UTF_8).readText()
            } ?: return@withContext Result.failure(Exception("فایل قابل خواندن نیست"))

            val rootJson = JSONObject(jsonString)

            // ۱. خواندن دوره‌ها
            val cycles = mutableListOf<CycleEntity>()
            val cyclesArray = rootJson.optJSONArray("cycles") ?: JSONArray()
            for (i in 0 until cyclesArray.length()) {
                val obj = cyclesArray.getJSONObject(i)
                cycles.add(
                    CycleEntity(
                        id = obj.getLong("id"),
                        startDateEpochDay = obj.getLong("startDateEpochDay"),
                        endDateEpochDay = if (obj.isNull("endDateEpochDay")) null else obj.getLong("endDateEpochDay"),
                        cycleLength = if (obj.isNull("cycleLength")) null else obj.getInt("cycleLength")
                    )
                )
            }

            // ۲. خواندن علائم روزانه
            val dailyLogs = mutableListOf<DailyLogEntity>()
            val logsArray = rootJson.optJSONArray("daily_logs") ?: JSONArray()
            for (i in 0 until logsArray.length()) {
                val obj = logsArray.getJSONObject(i)
                dailyLogs.add(
                    DailyLogEntity(
                        dateEpochDay = obj.getLong("dateEpochDay"),
                        flowLevel = obj.optInt("flowLevel", 0),
                        mood = obj.optString("mood", ""),
                        symptoms = obj.optString("symptoms", ""),
                        note = obj.optString("note", "")
                    )
                )
            }

            // ۳. خواندن داروها
            val medications = mutableListOf<MedicationEntity>()
            val medsArray = rootJson.optJSONArray("medications") ?: JSONArray()
            for (i in 0 until medsArray.length()) {
                val obj = medsArray.getJSONObject(i)
                medications.add(
                    MedicationEntity(
                        id = obj.getLong("id"),
                        name = obj.getString("name"),
                        hour = obj.getInt("hour"),
                        minute = obj.getInt("minute"),
                        isActive = obj.optBoolean("isActive", true)
                    )
                )
            }

            // ۴. خواندن سوابق مصرف دارو
            val medLogs = mutableListOf<MedicationLogEntity>()
            val medLogsArray = rootJson.optJSONArray("medication_logs") ?: JSONArray()
            for (i in 0 until medLogsArray.length()) {
                val obj = medLogsArray.getJSONObject(i)
                medLogs.add(
                    MedicationLogEntity(
                        medicationId = obj.getLong("medicationId"),
                        dateEpochDay = obj.getLong("dateEpochDay"),
                        isTaken = obj.optBoolean("isTaken", false)
                    )
                )
            }

            dao.restoreFullBackup(cycles, dailyLogs, medications, medLogs)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}