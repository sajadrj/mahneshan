package com.example.mahneshan.presentation.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mahneshan.presentation.home.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    viewModel: HomeViewModel,
    onFinished: () -> Unit
) {
    var painSeverity by remember { mutableStateOf("متوسط") }
    var flowIntensity by remember { mutableStateOf("متوسط") }
    var periodLength by remember { mutableIntStateOf(6) }
    var hasPregnancyHistory by remember { mutableStateOf(false) }
    var takesHormonalPills by remember { mutableStateOf(false) }

    val painOptions = listOf("کم", "متوسط", "زیاد")
    val flowOptions = listOf("کم", "متوسط", "زیاد")

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "تنظیمات اولیه و شخصی‌سازی هوشمند",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "لطفاً برای ارائه تحلیل‌ها و پیش‌بینی‌های دقیق‌تر، به چند سوال کوتاه پاسخ دهید.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // ۱. شدت درد
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("شدت درد معمول قاعدگی:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        painOptions.forEach { option ->
                            FilterChip(
                                selected = painSeverity == option,
                                onClick = { painSeverity = option },
                                label = { Text(option) }
                            )
                        }
                    }
                }
            }

            // ۲. شدت خونریزی
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("شدت خونریزی معمول:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        flowOptions.forEach { option ->
                            FilterChip(
                                selected = flowIntensity == option,
                                onClick = { flowIntensity = option },
                                label = { Text(option) }
                            )
                        }
                    }
                }
            }

            // ۳. مدت زمان پریود به طور میانگین
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("مدت زمان میانگین پریود ($periodLength روز):", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Slider(
                        value = periodLength.toFloat(),
                        onValueChange = { periodLength = it.toInt() },
                        valueRange = 3f..10f,
                        steps = 7
                    )
                }
            }

            // ۴. سابقه بارداری
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("سابقه بارداری داشته‌اید؟", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Switch(
                        checked = hasPregnancyHistory,
                        onCheckedChange = { hasPregnancyHistory = it }
                    )
                }
            }

            // ۵. مصرف قرص هورمونی
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("مصرف قرص هورمونی دارید؟", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Switch(
                        checked = takesHormonalPills,
                        onCheckedChange = { takesHormonalPills = it }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    viewModel.saveOnboarding(
                        painSeverity = painSeverity,
                        flowIntensity = flowIntensity,
                        periodLength = periodLength,
                        hasPregnancyHistory = hasPregnancyHistory,
                        takesHormonalPills = takesHormonalPills
                    )
                    onFinished()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("شروع و ورود به برنامه", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
