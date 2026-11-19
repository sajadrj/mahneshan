package com.example.mahneshan.domain

import com.example.mahneshan.data.local.entity.CycleEntity
import com.example.mahneshan.data.preferences.AppMode
import java.time.LocalDate

data class PhaseHealthTip(
    val phaseName: String,
    val title: String,
    val description: String,
    val tips: List<String>,
    val containerColorHex: String
)

object HealthTipEngine {

    fun getTipForPhase(
        cycles: List<CycleEntity>,
        mode: AppMode,
        todayEpochDay: Long = LocalDate.now().toEpochDay(),
        defaultCycleLen: Int = 28,
        defaultPeriodLen: Int = 6
    ): PhaseHealthTip {
        if (mode == AppMode.PREGNANCY) {
            return PhaseHealthTip(
                phaseName = "دوران بارداری 🤰",
                title = "توصیه‌های سلامت و تغذیه بارداری",
                description = "مراقبت‌های روزانه برای سلامتی شما و جنین",
                tips = listOf(
                    "مصرف منظم اسید فولیک و ویتامین‌های تجویز شده توسط پزشک",
                    "نوشیدن حداقل ۸ تا ۱۰ لیوان آب در طول روز برای هیدراته ماندن",
                    "پیاده‌روی ملایم ۲۰ تا ۳۰ دقیقه‌ای در صورت تأیید پزشک",
                    "پرهیز از مصرف ماهی‌های حاوی جیوه، گوشت خام و کافئین زیاد",
                    "استراحت کافی و خواب منظم به صورت خوابیده به سمت چپ"
                ),
                containerColorHex = "#F3E5F5"
            )
        }

        if (cycles.isEmpty()) {
            return PhaseHealthTip(
                phaseName = "توصیه روزانه 🌸",
                title = "خوش آمدید به ماه‌نشان",
                description = "نکات پایه برای حفظ سلامت چرخه قاعدگی",
                tips = listOf(
                    "نوشیدن آب کافی و تغذیه سالم و متوازن",
                    "ثبت منظم تاریخ شروع پریود برای پیش‌بینی دقیق‌تر فازها",
                    "خواب کافی ۷ تا ۸ ساعت در شبانه‌روز"
                ),
                containerColorHex = "#E8EAF6"
            )
        }

        val recordedLengths = cycles.mapNotNull { it.cycleLength }.filter { it in 20..45 }
        val avgCycleLen = if (recordedLengths.isNotEmpty()) recordedLengths.average().toInt() else defaultCycleLen

        val latestCycle = cycles.first()
        val lastStart = latestCycle.startDateEpochDay
        val estimatedEnd = latestCycle.endDateEpochDay ?: (lastStart + defaultPeriodLen - 1)
        val isOnPeriod = todayEpochDay in lastStart..estimatedEnd && latestCycle.endDateEpochDay == null

        if (isOnPeriod) {
            return PhaseHealthTip(
                phaseName = "فاز قاعدگی (پریود) 🩸",
                title = "توصیه‌های مراقبتی و تغذیه‌ای روزهای قاعدگی",
                description = "کاهش درد و تعادل انرژی در دوران خونریزی",
                tips = listOf(
                    "نوشیدن دمنوش بابونه، زنجبیل یا دارچین برای کاهش دردهای عضلانی و اسپاسم",
                    "پرهیز از مصرف کافئین، نوشیدنی‌های گازدار و غذاهای بسیار شور یا پرادویه",
                    "مصرف خوراکی‌های غنی از آهن مانند اسفناج، عدس، مغزیجات و گوشت قرمز کم‌چرب",
                    "استفاده از کیسه آب گرم روی شکم و کمر برای تسکین درد",
                    "انجام کشش‌های بسیار سبک یوگا و پرهیز از ورزش‌های سنگین"
                ),
                containerColorHex = "#FCE4EC"
            )
        }

        val nextPeriodStart = lastStart + avgCycleLen
        val ovulationDay = nextPeriodStart - 14
        val fertileStart = ovulationDay - 5
        val fertileEnd = ovulationDay + 1
        val daysToNext = nextPeriodStart - todayEpochDay

        return when {
            todayEpochDay in fertileStart..fertileEnd -> {
                PhaseHealthTip(
                    phaseName = "فاز تخمک‌گذاری و باروری 🌸",
                    title = "توصیه‌های اوج انرژی و شادابی",
                    description = "استفاده حداکثری از انرژی و آمادگی جسمانی",
                    tips = listOf(
                        "مصرف میوه‌های تازه، سبزیجات برگی و خوراکی‌های حاوی آنتی‌اکسیدان",
                        "انجام تمرینات ورزشی منظم و فعالیت‌های هوازی (بهترین زمان برای ورزش)",
                        "نوشیدن مایعات فراوان جهت شادابی پوست و تنظیم هورمون‌ها",
                        "مصرف منابع پروتئینی سالم مانند ماهی، تخم‌مرغ و حبوبات"
                    ),
                    containerColorHex = "#E0F2F1"
                )
            }
            daysToNext in 1..5 || daysToNext == 0L -> {
                PhaseHealthTip(
                    phaseName = "فاز لوتئال (نزدیک به پریود / PMS) 🌙",
                    title = "توصیه‌های کاهش علائم پیش از قاعدگی",
                    description = "آمادگی جسمی و روحی برای شروع دوره جدید",
                    tips = listOf(
                        "کاهش مصرف نمک و قندهای مصنوعی برای جلوگیری از نفخ و تورم",
                        "نوشیدن دمنوش بادرنجبویه یا گل‌گاوزبان برای کاهش اضطراب و آرامش اعصاب",
                        "مصرف مواد غذایی حاوی منیزیم و ویتامین B6 (مانند موز، شکلات تلخ و گردو)",
                        "تمرینات تنفسی، یوگا و پیاده‌روی آرام برای کاهش استرس",
                        "خواب کافی و پرهیز از تصمیم‌گیری‌های پراسترس"
                    ),
                    containerColorHex = "#FBE9E7"
                )
            }
            else -> {
                PhaseHealthTip(
                    phaseName = "فاز فولیکولار (پس از پریود) 🌿",
                    title = "توصیه‌های بازسازی انرژی و شادابی",
                    description = "افزایش تدریجی سطح هورمون استروژن و انرژی",
                    tips = listOf(
                        "مصرف غذاهای سبک و پرفیبر مانند سبزیجات تازه و پروبیوتیک‌ها",
                        "شروع برنامه‌های ورزشی جدید و فعالیت‌های چالش‌برانگیز",
                        "مصرف ویتامین C و مرکبات برای جذب بهتر آهن باقیمانده از دوره قبل",
                        "برنامه‌ریزی برای کارهای پرانرژی و پروژه‌های جدید"
                    ),
                    containerColorHex = "#E8F5E9"
                )
            }
        }
    }
}
