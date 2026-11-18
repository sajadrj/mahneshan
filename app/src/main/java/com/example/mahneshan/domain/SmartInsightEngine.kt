package com.example.mahneshan.domain

import com.example.mahneshan.data.local.entity.CycleEntity
import com.example.mahneshan.data.local.entity.DailyLogEntity
import java.time.LocalDate

enum class InsightType { WARNING, INFO, POSITIVE }

data class SmartInsight(
    val title: String,
    val description: String,
    val type: InsightType
)

object SmartInsightEngine {

    fun generateInsights(
        cycles: List<CycleEntity>,
        dailyLogs: List<DailyLogEntity>,
        avgCycleLen: Int,
        todayEpochDay: Long = LocalDate.now().toEpochDay()
    ): List<SmartInsight> {
        val insights = mutableListOf<SmartInsight>()

        if (cycles.isEmpty()) return insights

        // ۱. بررسی نظم یا نوسان طول چرخه اخیر
        val recordedLengths = cycles.mapNotNull { it.cycleLength }.filter { it in 20..45 }
        if (recordedLengths.size >= 2) {
            val lastLen = recordedLengths.first()
            val diff = lastLen - avgCycleLen
            when {
                diff >= 3 -> insights.add(
                    SmartInsight(
                        title = "چرخه طولانی‌تر از معمول",
                        description = "دوره قبلی شما $diff روز طولانی‌تر از میانگین همیشگی ($avgCycleLen روز) بود.",
                        type = InsightType.INFO
                    )
                )
                diff <= -3 -> insights.add(
                    SmartInsight(
                        title = "چرخه کوتاه‌تر از معمول",
                        description = "دوره قبلی شما ${-diff} روز زودتر از میانگین همیشگی ($avgCycleLen روز) شروع شد.",
                        type = InsightType.INFO
                    )
                )
                else -> insights.add(
                    SmartInsight(
                        title = "چرخه منظم",
                        description = "نظم چرخه‌های اخیر شما در محدوده طبیعی ($avgCycleLen روز) قرار دارد.",
                        type = InsightType.POSITIVE
                    )
                )
            }
        }

        // ۲. تشخیص علائم پرتکرار پیش از قاعدگی (PMS)
        val nextPeriodStart = cycles.first().startDateEpochDay + avgCycleLen
        val daysUntilNext = nextPeriodStart - todayEpochDay

        if (daysUntilNext in 1..4) {
            val recurringSymptom = findRecurringPrePeriodSymptom(cycles, dailyLogs)
            if (recurringSymptom != null) {
                insights.add(
                    SmartInsight(
                        title = "آمادگی پیش از قاعدگی",
                        description = "طبق سوابق شما، معمولاً چند روز قبل از شروع دوره دچار «$recurringSymptom» می‌شوید. استراحت و مصرف مایعات گرم را فراموش نکنید.",
                        type = InsightType.WARNING
                    )
                )
            }
        }

        return insights
    }

    private fun findRecurringPrePeriodSymptom(
        cycles: List<CycleEntity>,
        logs: List<DailyLogEntity>
    ): String? {
        val symptomCounts = mutableMapOf<String, Int>()
        for (cycle in cycles) {
            val windowStart = cycle.startDateEpochDay - 3
            val windowEnd = cycle.startDateEpochDay - 1
            logs.filter { it.dateEpochDay in windowStart..windowEnd }
                .flatMap { it.symptoms.split(",") }
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .forEach { symptom ->
                    symptomCounts[symptom] = (symptomCounts[symptom] ?: 0) + 1
                }
        }
        return symptomCounts.filter { it.value >= 2 }.maxByOrNull { it.value }?.key
    }
}
