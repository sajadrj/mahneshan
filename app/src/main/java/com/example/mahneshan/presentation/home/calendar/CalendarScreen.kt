package com.example.mahneshan.presentation.home.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.mahneshan.data.local.entity.CycleEntity
import com.example.mahneshan.data.local.entity.DailyLogEntity
import com.example.mahneshan.domain.CycleCalculator
import com.example.mahneshan.domain.DayStatus
import com.example.mahneshan.presentation.home.HomeViewModel
import com.example.mahneshan.utils.PersianDate
import com.example.mahneshan.utils.PersianDateUtils
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: HomeViewModel,
    onBackClick: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val dailyLogs by viewModel.allDailyLogs.collectAsState(initial = emptyList())

    val todayPersian = remember { PersianDateUtils.todayPersian() }
    val todayEpoch = remember { LocalDate.now().toEpochDay() }

    var currentYear by remember { mutableIntStateOf(todayPersian.year) }
    var currentMonth by remember { mutableIntStateOf(todayPersian.month) }

    // روز انتخاب‌شده برای نمایش دیالوگ ثبت علائم
    var selectedEpochDay by remember { mutableStateOf<Long?>(null) }

    val weekDays = listOf("ش", "ی", "د", "س", "چ", "پ", "ج")
    val daysInMonth = PersianDateUtils.getDaysInPersianMonth(currentYear, currentMonth)
    val firstDayGregorian = PersianDateUtils.toGregorian(currentYear, currentMonth, 1)
    val startOffset = PersianDateUtils.getPersianDayOfWeekIndex(firstDayGregorian)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("تقویم شمسی و علائم") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // هدر انتخاب ماه و سال
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    if (currentMonth == 1) {
                        currentMonth = 12
                        currentYear--
                    } else {
                        currentMonth--
                    }
                }) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "ماه قبل")
                }

                Text(
                    text = "${PersianDate(currentYear, currentMonth, 1).monthName} $currentYear",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                IconButton(onClick = {
                    if (currentMonth == 12) {
                        currentMonth = 1
                        currentYear++
                    } else {
                        currentMonth++
                    }
                }) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "ماه بعد")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ردیف نام روزهای هفته
            Row(modifier = Modifier.fillMaxWidth()) {
                weekDays.forEach { dayName ->
                    Text(
                        text = dayName,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // شبکه روزهای ماه
            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // خانه‌های خالی ابتدای ماه
                items(startOffset) {
                    Box(modifier = Modifier.aspectRatio(1f))
                }

                // روزهای ماه (1 تا 29/30/31)
                items(daysInMonth) { index ->
                    val day = index + 1
                    val dateEpoch = PersianDateUtils.toGregorian(currentYear, currentMonth, day).toEpochDay()
                    val status = CycleCalculator.getDayStatus(
                        targetEpochDay = dateEpoch,
                        cycles = state.cycles,
                        mode = state.appMode
                    )
                    val hasLog = dailyLogs.any { it.dateEpochDay == dateEpoch }
                    val isToday = dateEpoch == todayEpoch

                    val bgColor = when (status) {
                        DayStatus.PERIOD_RECORDED -> Color(0xFFE91E63)
                        DayStatus.PERIOD_PREDICTED -> Color(0xFFE91E63).copy(alpha = 0.25f)
                        DayStatus.OVULATION_DAY -> Color(0xFF009688)
                        DayStatus.FERTILE_WINDOW -> Color(0xFF009688).copy(alpha = 0.25f)
                        DayStatus.NORMAL -> Color.Transparent
                    }

                    val textColor = when (status) {
                        DayStatus.PERIOD_RECORDED, DayStatus.OVULATION_DAY -> Color.White
                        else -> MaterialTheme.colorScheme.onSurface
                    }

                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(CircleShape)
                            .background(bgColor)
                            .then(
                                if (isToday) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                else Modifier
                            )
                            .clickable { selectedEpochDay = dateEpoch },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = day.toString(),
                                color = textColor,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                            )
                            if (hasLog) {
                                Box(
                                    modifier = Modifier
                                        .size(4.dp)
                                        .clip(CircleShape)
                                        .background(if (textColor == Color.White) Color.White else MaterialTheme.colorScheme.primary)
                                )
                            }
                        }
                    }
                }
            }

            // راهنمای رنگ‌های تقویم
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    LegendItem(color = Color(0xFFE91E63), label = "پریود")
                    LegendItem(color = Color(0xFFE91E63).copy(alpha = 0.3f), label = "پیش‌بینی")
                    LegendItem(color = Color(0xFF009688), label = "تخمک‌گذاری")
                    LegendItem(color = Color(0xFF009688).copy(alpha = 0.3f), label = "باروری")
                }
            }
        }

        // دیالوگ ثبت علائم و وضعیت روز انتخاب‌شده
        selectedEpochDay?.let { epoch ->
            val existingLog = dailyLogs.find { it.dateEpochDay == epoch }
            val cycleOnThisDay = state.cycles.find { 
                it.startDateEpochDay == epoch || (it.endDateEpochDay != null && epoch in it.startDateEpochDay..it.endDateEpochDay)
            }
            DailyLogDialog(
                epochDay = epoch,
                existingLog = existingLog,
                cycleOnThisDay = cycleOnThisDay,
                onDismiss = { selectedEpochDay = null },
                onMarkPeriodStart = {
                    viewModel.logPeriodStart(epoch)
                    selectedEpochDay = null
                },
                onDeleteCycle = { cycle ->
                    viewModel.deleteCycle(cycle)
                    selectedEpochDay = null
                },
                onSaveLog = { flow, mood, symptoms, note ->
                    viewModel.saveDailyLog(epoch, flow, mood, symptoms, note)
                    selectedEpochDay = null
                }
            )
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, style = MaterialTheme.typography.bodySmall)
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun DailyLogDialog(
    epochDay: Long,
    existingLog: DailyLogEntity?,
    cycleOnThisDay: CycleEntity?,
    onDismiss: () -> Unit,
    onMarkPeriodStart: () -> Unit,
    onDeleteCycle: (CycleEntity) -> Unit,
    onSaveLog: (Int, String, List<String>, String) -> Unit
) {
    val persianDate = PersianDateUtils.toPersian(LocalDate.ofEpochDay(epochDay))

    var flowLevel by remember { mutableIntStateOf(existingLog?.flowLevel ?: 0) }
    var selectedMoods by remember {
        mutableStateOf(
            existingLog?.mood?.split(",")?.map { it.trim() }?.filter { it.isNotBlank() }?.toSet() ?: emptySet()
        )
    }
    var selectedSymptoms by remember {
        mutableStateOf(
            existingLog?.symptoms?.split(",")?.map { it.trim() }?.filter { it.isNotBlank() }?.toSet() ?: emptySet()
        )
    }
    var note by remember { mutableStateOf(existingLog?.note ?: "") }

    val flowOptions = listOf("ندارد", "کم", "متوسط", "زیاد")
    val moodOptions = listOf("عادی", "حساس", "خسته", "بی‌حوصله", "عصبی", "پرانرژی", "مضطرب", "شاد")
    val symptomOptions = listOf("دل‌درد", "سردرد", "نفخ", "تهوع", "کمردرد", "خستگی", "گرفتگی عضلانی")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("وضعیت روز ${persianDate.formatFull()}") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // دکمه ثبت شروع پریود در این تاریخ یا حذف دوره ثبت‌شده
                if (cycleOnThisDay != null) {
                    Button(
                        onClick = { onDeleteCycle(cycleOnThisDay) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("حذف ثبت پریود در این تاریخ", fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = onMarkPeriodStart,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE91E63),
                            contentColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                    ) {
                        Icon(Icons.Default.WaterDrop, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("ثبت شروع پریود در این تاریخ", fontWeight = FontWeight.Bold)
                    }
                }

                // میزان خونریزی
                Text("میزان خونریزی:", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    flowOptions.forEachIndexed { index, label ->
                        FilterChip(
                            selected = flowLevel == index,
                            onClick = { flowLevel = index },
                            label = { Text(label, style = MaterialTheme.typography.bodySmall) }
                        )
                    }
                }

                // حالت روحی
                Text("حالت روحی (انتخاب چندگانه):", style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    moodOptions.forEach { mood ->
                        val isSelected = mood in selectedMoods
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedMoods = if (isSelected) selectedMoods - mood else selectedMoods + mood
                            },
                            label = { Text(mood, style = MaterialTheme.typography.bodySmall) }
                        )
                    }
                }

                // علائم جسمی
                Text("علائم جسمی:", style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    symptomOptions.forEach { symptom ->
                        val isSelected = symptom in selectedSymptoms
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedSymptoms = if (isSelected) selectedSymptoms - symptom else selectedSymptoms + symptom
                            },
                            label = { Text(symptom, style = MaterialTheme.typography.bodySmall) }
                        )
                    }
                }

                // یادداشت
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("یادداشت شخصی") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSaveLog(flowLevel, selectedMoods.joinToString(","), selectedSymptoms.toList(), note)
            }) {
                Text("ذخیره")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("انصراف")
            }
        }
    )
}
