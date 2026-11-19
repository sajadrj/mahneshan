package com.example.mahneshan.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.widget.Toast
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mahneshan.data.preferences.AppMode
import com.example.mahneshan.domain.InsightType
import com.example.mahneshan.utils.PersianDateUtils
import androidx.core.graphics.toColorInt
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel(),
    onNavigateToCalendar: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    var showAddMedDialog by remember { mutableStateOf(false) }
    var showPregnancyDialog by remember { mutableStateOf(false) }

    if (showPregnancyDialog) {
        AlertDialog(
            onDismissRequest = { showPregnancyDialog = false },
            title = { Text("ثبت بارداری مبارک! 🌸") },
            text = {
                Text("با تغییر حالت به بارداری، برنامه بر اساس تاریخ شروع آخرین قاعدگی ثبت‌شده شما، سن دقیق بارداری (هفته و روز) و تاریخ تقریبی زایمان را محاسبه خواهد کرد.\n\nآیا آخرین تاریخ پریود خود را در تقویم یا صفحه اصلی ثبت کرده‌اید؟")
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.setAppMode(AppMode.PREGNANCY)
                    showPregnancyDialog = false
                }) {
                    Text("بله، ثبت شد")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPregnancyDialog = false }) {
                    Text("خیر / انصراف")
                }
            }
        )
    }

    val context = LocalContext.current
    var showBackupMenu by remember { mutableStateOf(false) }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let {
            viewModel.exportDatabase(it) { message ->
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            viewModel.importDatabase(it) { message ->
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    // رنگ اصلی حلقه بر اساس فاز چرخه و حالت انتخاب‌شده
    val ringColor = remember(state.summary.phaseColorHex, state.appMode) {
        try {
            Color(android.graphics.Color.parseColor(state.summary.phaseColorHex))
        } catch (_: Exception) {
            if (state.appMode == AppMode.PREGNANCY) Color(0xFF7E57C2) else Color(0xFFE91E63)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "ماه‌نشان",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "امروز: ${PersianDateUtils.todayPersian().formatFull()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToCalendar) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "تقویم شمسی"
                        )
                    }
                    Box {
                        IconButton(onClick = { showBackupMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "پشتیبان‌گیری")
                        }
                        DropdownMenu(
                            expanded = showBackupMenu,
                            onDismissRequest = { showBackupMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("پشتیبان‌گیری از اطلاعات (Export)") },
                                onClick = {
                                    showBackupMenu = false
                                    exportLauncher.launch("MahNeshan_Backup.json")
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("بازیابی اطلاعات (Import)") },
                                onClick = {
                                    showBackupMenu = false
                                    importLauncher.launch(arrayOf("application/json"))
                                }
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // ۱. نوار تغییر هدف (Mode Switcher)
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(AppMode.entries) { mode ->
                        FilterChip(
                            selected = state.appMode == mode,
                            onClick = {
                                if (mode == AppMode.PREGNANCY && state.appMode != AppMode.PREGNANCY) {
                                    showPregnancyDialog = true
                                } else {
                                    viewModel.setAppMode(mode)
                                }
                            },
                            label = { Text(mode.title) },
                            leadingIcon = if (state.appMode == mode) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null
                        )
                    }
                }
            }

            // ۲. حلقه شمارشگر مرکزی
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(230.dp)
                            .clip(CircleShape)
                            .background(ringColor.copy(alpha = 0.08f))
                            .border(width = 6.dp, color = ringColor, shape = CircleShape)
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = state.summary.mainTitle,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = ringColor,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = state.summary.subTitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // دکمه ثبت سریع (در حالت بارداری مخفی می‌شود)
                    if (state.appMode != AppMode.PREGNANCY) {
                        if (state.summary.isCurrentlyOnPeriod && state.summary.activeCycleId != null) {
                            Button(
                                onClick = { viewModel.logPeriodEnd(state.summary.activeCycleId!!) },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.DoneAll, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("ثبت پایان پریود")
                            }
                        } else {
                            val latestCycle = state.cycles.firstOrNull()
                            val endedToday = latestCycle != null && latestCycle.endDateEpochDay == LocalDate.now().toEpochDay()

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(
                                    onClick = { viewModel.logPeriodStart() },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.WaterDrop, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("امروز پریود شدم")
                                }

                                if (endedToday) {
                                    OutlinedButton(
                                        onClick = { viewModel.undoPeriodEnd(latestCycle.id) },
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("لغو پایان")
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ۳. کارت اطلاعات خلاصه چرخه (در حالت غیربارداری)
            if (state.appMode != AppMode.PREGNANCY && state.cycles.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            InfoColumn(title = "پریود بعدی", value = state.summary.nextPeriodDatePersian)
                            InfoColumn(title = "تخمک‌گذاری", value = state.summary.ovulationDatePersian)
                            InfoColumn(title = "میانگین چرخه", value = "${state.summary.averageCycleLength} روز")
                        }
                    }
                }
            }

            // ۴. بخش یادآور و چک‌لیست داروهای امروز
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "داروها و مکمل‌های امروز",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = { showAddMedDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("افزودن دارو")
                    }
                }

                if (state.todayMedications.isEmpty()) {
                    Text(
                        text = "دارو یا مکملی ثبت نشده است.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            items(state.todayMedications) { medItem ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            viewModel.toggleMedicationTaken(medItem.medication.id, medItem.isTakenToday)
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Medication,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = medItem.medication.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Medium
                                )
                                val timeFormatted = String.format("%02d:%02d", medItem.medication.hour, medItem.medication.minute)
                                Text(
                                    text = "ساعت یادآوری: $timeFormatted",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = medItem.isTakenToday,
                                onCheckedChange = {
                                    viewModel.toggleMedicationTaken(medItem.medication.id, medItem.isTakenToday)
                                }
                            )
                            IconButton(onClick = { viewModel.deleteMedication(medItem.medication) }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "حذف دارو",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }

            // ۵. بخش تحلیل هوشمند الگوها (Smart Insights)
            if (state.insights.isNotEmpty()) {
                item {
                    Text(
                        text = "تحلیل هوشمند چرخه شما",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(state.insights) { insight ->
                    val cardColor = when (insight.type) {
                        InsightType.WARNING -> MaterialTheme.colorScheme.errorContainer
                        InsightType.POSITIVE -> MaterialTheme.colorScheme.primaryContainer
                        InsightType.INFO -> MaterialTheme.colorScheme.secondaryContainer
                    }
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = cardColor)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = insight.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = insight.description,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }

        // دیالوگ افزودن داروی جدید
        if (showAddMedDialog) {
            AddMedicationDialog(
                onDismiss = { showAddMedDialog = false },
                onConfirm = { name, hour, minute ->
                    viewModel.addMedication(name, hour, minute)
                    showAddMedDialog = false
                }
            )
        }
    }
}

@Composable
private fun InfoColumn(title: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun AddMedicationDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Int, Int) -> Unit
) {
    var medName by remember { mutableStateOf("") }
    var hourText by remember { mutableStateOf("09") }
    var minuteText by remember { mutableStateOf("00") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("افزودن دارو یا مکمل") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = medName,
                    onValueChange = { medName = it },
                    label = { Text("نام دارو (مثلاً: قرص آهن)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = hourText,
                        onValueChange = { if (it.length <= 2) hourText = it },
                        label = { Text("ساعت (۰-۲۳)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = minuteText,
                        onValueChange = { if (it.length <= 2) minuteText = it },
                        label = { Text("دقیقه (۰-۵۹)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val h = hourText.toIntOrNull()?.coerceIn(0, 23) ?: 9
                val m = minuteText.toIntOrNull()?.coerceIn(0, 59) ?: 0
                if (medName.isNotBlank()) {
                    onConfirm(medName, h, m)
                }
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
