package com.example.mahneshan.utils

import java.time.LocalDate

data class PersianDate(
    val year: Int,
    val month: Int,
    val day: Int
) {
    val monthName: String
        get() = when (month) {
            1 -> "فروردین"
            2 -> "اردیبهشت"
            3 -> "خرداد"
            4 -> "تیر"
            5 -> "مرداد"
            6 -> "شهریور"
            7 -> "مهر"
            8 -> "آبان"
            9 -> "آذر"
            10 -> "دی"
            11 -> "بهمن"
            12 -> "اسفند"
            else -> ""
        }

    fun formatFull(): String = "$day $monthName $year"
    fun formatShort(): String = "$day $monthName"
}

object PersianDateUtils {

    fun toPersian(date: LocalDate): PersianDate {
        val gYear = date.year
        val gMonth = date.monthValue
        val gDay = date.dayOfMonth

        val gDaysInMonth = intArrayOf(0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334)
        val gy2 = if (gMonth > 2) gYear + 1 else gYear
        var days = 355666 + (365 * gYear) + ((gy2 + 3) / 4) - ((gy2 + 99) / 100) + ((gy2 + 399) / 400) + gDay + gDaysInMonth[gMonth - 1]
        var jy = -1595 + (33 * (days / 12053))
        days %= 12053
        jy += 4 * (days / 1461)
        days %= 1461
        if (days > 365) {
            jy += (days - 1) / 365
            days = (days - 1) % 365
        }
        val jm = if (days < 186) 1 + (days / 31) else 7 + ((days - 186) / 30)
        val jd = 1 + if (days < 186) (days % 31) else ((days - 186) % 30)

        return PersianDate(jy, jm, jd)
    }

    fun toGregorian(jYear: Int, jMonth: Int, jDay: Int): LocalDate {
        val jy = jYear + 1595
        var days = -355668 + (365 * jy) + ((jy / 33) * 8) + (((jy % 33) + 3) / 4) + jDay +
                if (jMonth < 7) (jMonth - 1) * 31 else ((jMonth - 7) * 30) + 186
        var gy = 400 * (days / 146097)
        days %= 146097
        if (days > 36524) {
            gy += 100 * (--days / 36524)
            days %= 36524
            if (days >= 365) days++
        }
        gy += 4 * (days / 1461)
        days %= 1461
        if (days > 365) {
            gy += (days - 1) / 365
            days = (days - 1) % 365
        }
        var gd = days + 1
        val salA = intArrayOf(0, 31, if ((gy % 4 == 0 && gy % 100 != 0) || (gy % 400 == 0)) 29 else 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        var gm = 0
        while (gm < 13 && gd > salA[gm]) {
            gd -= salA[gm]
            gm++
        }
        return LocalDate.of(gy, gm, gd)
    }

    fun isLeapPersianYear(year: Int): Boolean {
        val remainder = year % 33
        return remainder in listOf(1, 5, 9, 13, 17, 22, 26, 30)
    }

    fun getDaysInPersianMonth(year: Int, month: Int): Int {
        return when (month) {
            in 1..6 -> 31
            in 7..11 -> 30
            12 -> if (isLeapPersianYear(year)) 30 else 29
            else -> 30
        }
    }

    // تبدیل DayOfWeek میلادی به اندیس ستون‌های تقویم فارسی (شنبه = 0 تا جمعه = 6)
    fun getPersianDayOfWeekIndex(date: LocalDate): Int {
        return (date.dayOfWeek.value + 1) % 7
    }

    fun todayPersian(): PersianDate = toPersian(LocalDate.now())
}