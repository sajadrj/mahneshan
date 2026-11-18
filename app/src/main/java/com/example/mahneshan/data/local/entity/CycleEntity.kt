package com.example.mahneshan.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cycles")
data class CycleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val startDateEpochDay: Long,    // تاریخ شروع به صورت EpochDay (تعداد روز از 1970)
    val endDateEpochDay: Long? = null, // تاریخ پایان (تا وقتی پایان ثبت نشود null است)
    val cycleLength: Int? = null    // طول چرخه نسبت به دوره قبلی
)
