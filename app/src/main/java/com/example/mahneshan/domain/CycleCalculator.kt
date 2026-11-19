package com.example.mahneshan.domain

import com.example.mahneshan.data.local.entity.CycleEntity
import com.example.mahneshan.data.preferences.AppMode
import com.example.mahneshan.utils.PersianDateUtils
import java.time.LocalDate

enum class DayStatus {
    PERIOD_RECORDED,    // پریود ثبت‌شده قطعی
    PERIOD_PREDICTED,   // پیش‌بینی پریود آینده
    FERTILE_WINDOW,     // بازه باروری و احتمال بالای بارداری
    OVULATION_DAY,      // روز اوج تخمک‌گذاری
    NORMAL              // روز عادی
}

data class CycleSummary(
    val mainTitle: String,          // تیتر اصلی داخل حلقه (مثلاً: ۵ روز تا پریود بعدی)
    val subTitle: String,           // توضیح پایین حلقه (مثلاً: فاز لوتئال - احتمال بارداری کم)
    val nextPeriodDatePersian: String,
    val ovulationDatePersian: String,
    val averageCycleLength: Int,
    val isCurrentlyOnPeriod: Boolean,
    val activeCycleId: Long? = null,
    val phaseColorHex: String = "#E91E63"
)

object CycleCalculator {

    fun calculateSummary(
        cycles: List<CycleEntity>,
        mode: AppMode,
        defaultCycleLen: Int = 28,
        defaultPeriodLen: Int = 6,
        today: LocalDate = LocalDate.now()
    ): CycleSummary {
        val todayEpoch = today.toEpochDay()

        if (cycles.isEmpty()) {
            return CycleSummary(
                mainTitle = "خوش آمدید",
                subTitle = "اولین تاریخ پریود خود را ثبت کنید",
                nextPeriodDatePersian = "نامشخص",
                ovulationDatePersian = "نامشخص",
                averageCycleLength = defaultCycleLen,
                isCurrentlyOnPeriod = false,
                phaseColorHex = "#E91E63"
            )
        }

        // محاسبه میانگین هوشمند طول چرخه از روی سوابق ثبت‌شده
        val recordedLengths = cycles.mapNotNull { it.cycleLength }.filter { it in 20..45 }
        val avgCycleLen = if (recordedLengths.isNotEmpty()) recordedLengths.average().toInt() else defaultCycleLen

        val latestCycle = cycles.first() // مرتب‌شده نزولی بر اساس تاریخ شروع
        val lastStart = latestCycle.startDateEpochDay

        // حالت ۳: دوران بارداری
        if (mode == AppMode.PREGNANCY) {
            val daysPregnant = (todayEpoch - lastStart).coerceAtLeast(0)
            val weeks = daysPregnant / 7
            val days = daysPregnant % 7
            val dueDate = LocalDate.ofEpochDay(lastStart + 280)
            val dueDatePersian = PersianDateUtils.toPersian(dueDate).formatFull()

            return CycleSummary(
                mainTitle = "هفته $weeks و $days روز",
                subTitle = "تاریخ تقریبی زایمان: $dueDatePersian",
                nextPeriodDatePersian = "-",
                ovulationDatePersian = "-",
                averageCycleLength = avgCycleLen,
                isCurrentlyOnPeriod = false,
                phaseColorHex = "#7E57C2" // بنفش بارداری
            )
        }

        val nextPeriodStart = lastStart + avgCycleLen
        val ovulationDay = nextPeriodStart - 14
        val fertileStart = ovulationDay - 5
        val fertileEnd = ovulationDay + 1

        val nextPeriodPersian = PersianDateUtils.toPersian(LocalDate.ofEpochDay(nextPeriodStart)).formatShort()
        val ovulationPersian = PersianDateUtils.toPersian(LocalDate.ofEpochDay(ovulationDay)).formatShort()

        // بررسی اینکه آیا کاربر هم‌اکنون در دوره خونریزی است یا نه
        val estimatedEnd = latestCycle.endDateEpochDay ?: (lastStart + defaultPeriodLen - 1)
        val isOnPeriod = todayEpoch in lastStart..estimatedEnd && latestCycle.endDateEpochDay == null

        if (isOnPeriod) {
            val currentDayOfPeriod = (todayEpoch - lastStart + 1).toInt()
            return CycleSummary(
                mainTitle = "روز $currentDayOfPeriod قاعدگی",
                subTitle = "برای ثبت پایان دوره، دکمه پایان را بزنید",
                nextPeriodDatePersian = nextPeriodPersian,
                ovulationDatePersian = ovulationPersian,
                averageCycleLength = avgCycleLen,
                isCurrentlyOnPeriod = true,
                activeCycleId = latestCycle.id,
                phaseColorHex = "#E91E63" // صورتی/قرمز دوره
            )
        }

        // وضعیت روزهای دیگر بسته به فاز چرخه
        val daysToNext = nextPeriodStart - todayEpoch
        val mainText: String
        val subText: String
        val phaseColor: String

        when {
            todayEpoch == ovulationDay -> {
                mainText = "روز تخمک‌گذاری"
                subText = "اوج احتمال بارداری و تخمک‌گذاری"
                phaseColor = "#009688" // سبز فیروزه‌ای تخمک‌گذاری
            }
            todayEpoch in fertileStart..fertileEnd -> {
                mainText = "بازه باروری"
                subText = "احتمال بارداری بالا (تخمک‌گذاری: $ovulationPersian)"
                phaseColor = "#26A69A" // فیروزه‌ای روشن بازه باروری
            }
            daysToNext in 1..5 -> {
                mainText = "$daysToNext روز تا پریود"
                subText = "پیش‌بینی شروع دوره: $nextPeriodPersian (تخمک‌گذاری: $ovulationPersian)"
                phaseColor = "#FF7043" // نارنجی گرم (نزدیک به پریود)
            }
            daysToNext > 5 -> {
                mainText = "$daysToNext روز تا پریود"
                subText = "پیش‌بینی شروع دوره: $nextPeriodPersian (تخمک‌گذاری: $ovulationPersian)"
                phaseColor = "#EC407A" // صورتی ملایم فاز فولیکولار/لوتئال
            }
            daysToNext == 0L -> {
                mainText = "امروز موعد پریود"
                subText = "در صورت شروع، دکمه ثبت را بزنید"
                phaseColor = "#FF7043"
            }
            else -> {
                val lateDays = -daysToNext
                mainText = "$lateDays روز تأخیر"
                subText = "موعد پیش‌بینی‌شده: $nextPeriodPersian"
                phaseColor = "#D32F2F" // قرمز تأخیر
            }
        }

        return CycleSummary(
            mainTitle = mainText,
            subTitle = subText,
            nextPeriodDatePersian = nextPeriodPersian,
            ovulationDatePersian = ovulationPersian,
            averageCycleLength = avgCycleLen,
            isCurrentlyOnPeriod = false,
            phaseColorHex = phaseColor
        )
    }

    // تعیین وضعیت رنگی هر روز برای نمایش در تقویم شمسی
    fun getDayStatus(
        targetEpochDay: Long,
        cycles: List<CycleEntity>,
        mode: AppMode,
        defaultCycleLen: Int = 28,
        defaultPeriodLen: Int = 6
    ): DayStatus {
        // ۱. بررسی روزهای پریود ثبت‌شده واقعی
        for (cycle in cycles) {
            val end = cycle.endDateEpochDay ?: (cycle.startDateEpochDay + defaultPeriodLen - 1)
            if (targetEpochDay in cycle.startDateEpochDay..end) {
                return DayStatus.PERIOD_RECORDED
            }
        }

        if (cycles.isEmpty() || mode == AppMode.PREGNANCY) return DayStatus.NORMAL

        val recordedLengths = cycles.mapNotNull { it.cycleLength }.filter { it in 20..45 }
        val avgCycleLen = if (recordedLengths.isNotEmpty()) recordedLengths.average().toInt() else defaultCycleLen
        val lastStart = cycles.first().startDateEpochDay

        // پیش‌بینی تا ۳ چرخه آینده روی تقویم
        for (i in 1..3) {
            val futureStart = lastStart + (i * avgCycleLen)
            val futureEnd = futureStart + defaultPeriodLen - 1
            val ovulation = futureStart - 14

            if (targetEpochDay == ovulation) return DayStatus.OVULATION_DAY
            if (targetEpochDay in (ovulation - 5)..(ovulation + 1)) return DayStatus.FERTILE_WINDOW
            if (targetEpochDay in futureStart..futureEnd) return DayStatus.PERIOD_PREDICTED
        }

        return DayStatus.NORMAL
    }
}
