package com.example.mahneshan

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mahneshan.presentation.home.calendar.CalendarScreen
import com.example.mahneshan.presentation.home.HomeScreen
import com.example.mahneshan.presentation.home.HomeViewModel
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
                    var showCalendar by remember { mutableStateOf(false) }

                    if (!isOnboardingCompleted) {
                        OnboardingScreen(
                            viewModel = sharedViewModel,
                            onFinished = {}
                        )
                    } else if (showCalendar) {
                        CalendarScreen(
                            viewModel = sharedViewModel,
                            onBackClick = { showCalendar = false }
                        )
                    } else {
                        HomeScreen(
                            viewModel = sharedViewModel,
                            onNavigateToCalendar = { showCalendar = true }
                        )
                    }
                }
            }
        }
    }
}
