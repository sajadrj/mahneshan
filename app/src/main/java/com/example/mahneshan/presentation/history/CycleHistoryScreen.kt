package com.example.mahneshan.presentation.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mahneshan.data.local.entity.CycleEntity
import com.example.mahneshan.presentation.home.HomeViewModel
import com.example.mahneshan.utils.PersianDateUtils
import java.time.LocalDate

enum class HistoryFilter(val title: String) {
    MONTHS_3("۳ ماه اخیر"),
    MONTHS_6("۶ ماه اخیر"),
    ALL("همه سوابق")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CycleHistoryScreen(
    viewModel: HomeViewModel,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedFilter by remember { mutableStateOf(HistoryFilter.MONTHS_3) }
    var cycleToDelete by remember { mutableStateOf<CycleEntity?>(null) }

    val todayEpoch = remember { LocalDate.now().toEpochDay() }

    val filteredCycles = remember(uiState.cycles, selectedFilter) {
        when (selectedFilter) {
            HistoryFilter.ALL -> uiState.cycles
            HistoryFilter.MONTHS_3 -> uiState.cycles.filter { (todayEpoch - it.startDateEpochDay) <= 90 }
            HistoryFilter.MONTHS_6 -> uiState.cycles.filter { (todayEpoch - it.startDateEpochDay) <= 180 }
        }
    }

    val avgCycleLen = remember(uiState.cycles) {
        val lengths = uiState.cycles.mapNotNull { it.cycleLength }.filter { it in 20..45 }
        if (lengths.isNotEmpty()) lengths.average().toInt() else 28
    }

    val avgPeriodLen = remember(uiState.cycles) {
        val periodLengths = uiState.cycles.mapNotNull {
            if (it.endDateEpochDay != null) (it.endDateEpochDay - it.startDateEpochDay + 1).toInt() else null
        }.filter { it in 2..15 }
        if (periodLengths.isNotEmpty()) periodLengths.average().toInt() else 6
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("تاریخچه دوره‌های ثبت‌شده") },
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
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ۱. فیلتر بازه زمانی (۳ ماه، ۶ ماه، همه)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HistoryFilter.entries.forEach { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter.title) }
                    )
                }
            }

            // ۲. خلاصه آماری دوره‌ها جهت ارائه به پزشک
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    HistoryStatItem(title = "تعداد دوره‌ها", value = "${filteredCycles.size} دوره")
                    HistoryStatItem(title = "میانگین چرخه", value = "$avgCycleLen روز")
                    HistoryStatItem(title = "میانگین خونریزی", value = "$avgPeriodLen روز")
                }
            }

            // پیام راهنما جهت ارائه به پزشک
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "این لیست جهت ارائه به پزشک یا بررسی فواصل دوره‌ها طراحی شده است.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // ۳. لیست سوابق ثبت شده
            if (filteredCycles.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "هیچ سابقه‌ای در این بازه زمانی ثبت نشده است.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(filteredCycles) { cycle ->
                        CycleHistoryCard(
                            cycle = cycle,
                            onDeleteClick = { cycleToDelete = cycle }
                        )
                    }
                }
            }
        }
    }

    // دیالوگ تأیید حذف تاریخ اشتباه
    cycleToDelete?.let { cycle ->
        val startDatePersian = PersianDateUtils.toPersian(LocalDate.ofEpochDay(cycle.startDateEpochDay)).formatFull()
        AlertDialog(
            onDismissRequest = { cycleToDelete = null },
            title = { Text("حذف تاریخ ثبت‌شده") },
            text = { Text("آیا از حذف دوره مربوط به تاریخ «$startDatePersian» اطمینان دارید؟ این عمل قابل بازگشت نیست.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteCycle(cycle)
                        cycleToDelete = null
                    }
                ) {
                    Text("حذف شود", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { cycleToDelete = null }) {
                    Text("انصراف")
                }
            }
        )
    }
}

@Composable
private fun HistoryStatItem(title: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun CycleHistoryCard(
    cycle: CycleEntity,
    onDeleteClick: () -> Unit
) {
    val startDate = PersianDateUtils.toPersian(LocalDate.ofEpochDay(cycle.startDateEpochDay)).formatFull()
    val endDate = if (cycle.endDateEpochDay != null) {
        PersianDateUtils.toPersian(LocalDate.ofEpochDay(cycle.endDateEpochDay)).formatFull()
    } else {
        "درحال انجام"
    }

    val periodDays = if (cycle.endDateEpochDay != null) {
        val days = (cycle.endDateEpochDay - cycle.startDateEpochDay + 1).toInt()
        "$days روز خونریزی"
    } else {
        "پریود جاری"
    }

    val cycleLengthText = if (cycle.cycleLength != null) {
        "طول چرخه: ${cycle.cycleLength} روز"
    } else {
        "اولین دوره ثبت‌شده"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "$startDate  تا  $endDate",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SuggestionChip(
                        onClick = {},
                        label = { Text(periodDays, style = MaterialTheme.typography.labelSmall) },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = Color(0xFFE91E63).copy(alpha = 0.1f),
                            labelColor = Color(0xFFD81B60)
                        )
                    )

                    SuggestionChip(
                        onClick = {},
                        label = { Text(cycleLengthText, style = MaterialTheme.typography.labelSmall) },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            labelColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    )
                }
            }

            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "حذف این دوره",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
