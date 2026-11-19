package com.example.mahneshan

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mahneshan.presentation.about.AboutDeveloperScreen
import com.example.mahneshan.presentation.history.CycleHistoryScreen
import com.example.mahneshan.presentation.home.HomeScreen
import com.example.mahneshan.presentation.home.HomeViewModel
import com.example.mahneshan.presentation.home.calendar.CalendarScreen
import com.example.mahneshan.presentation.onboarding.OnboardingScreen
import com.example.mahneshan.ui.theme.MahNeshanTheme

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        setContent {
            MahNeshanTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    val sharedViewModel: HomeViewModel = viewModel()
                    val isOnboardingCompleted by sharedViewModel.isOnboardingCompleted.collectAsState()
                    var currentScreen by remember { mutableStateOf("HOME") } // "HOME", "CALENDAR", "HISTORY", "ABOUT"

                    // بازگشت به صفحه خانه هنگام زدن دکمه Back گوشی در صفحات فرعی
                    BackHandler(enabled = isOnboardingCompleted && currentScreen != "HOME") {
                        currentScreen = "HOME"
                    }

                    if (!isOnboardingCompleted) {
                        OnboardingScreen(
                            viewModel = sharedViewModel,
                            onFinished = {}
                        )
                    } else {
                        when (currentScreen) {
                            "CALENDAR" -> {
                                CalendarScreen(
                                    viewModel = sharedViewModel,
                                    onBackClick = { currentScreen = "HOME" }
                                )
                            }
                            "HISTORY" -> {
                                CycleHistoryScreen(
                                    viewModel = sharedViewModel,
                                    onBackClick = { currentScreen = "HOME" }
                                )
                            }
                            "ABOUT" -> {
                                AboutDeveloperScreen(
                                    onBackClick = { currentScreen = "HOME" }
                                )
                            }
                            else -> {
                                HomeScreen(
                                    viewModel = sharedViewModel,
                                    onNavigateToCalendar = { currentScreen = "CALENDAR" },
                                    onNavigateToHistory = { currentScreen = "HISTORY" },
                                    onNavigateToAbout = { currentScreen = "ABOUT" }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}