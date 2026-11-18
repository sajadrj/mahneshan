package com.example.mahneshan

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mahneshan.presentation.home.calendar.CalendarScreen
import com.example.mahneshan.presentation.home.HomeScreen
import com.example.mahneshan.presentation.home.HomeViewModel
import com.example.mahneshan.ui.theme.MahNeshanTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MahNeshanTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    val sharedViewModel: HomeViewModel = viewModel()
                    var showCalendar by remember { mutableStateOf(false) }

                    if (showCalendar) {
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