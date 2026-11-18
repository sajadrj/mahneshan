package com.example.mahneshan.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_logs")
data class DailyLogEntity(
    @PrimaryKey
    val dateEpochDay: Long,         // هر روز یک رکورد یکتا دارد
    val flowLevel: Int = 0,         // میزان خونریزی: 0 (ندارد)، 1 (کم)، 2 (متوسط)، 3 (زیاد)
    val mood: String = "",          // حالت روحی (عادی، حساس، خسته، شاد و...)
    val symptoms: String = "",      // علائم جسمی جداشده با ویرگول (مثلاً: "دل‌درد,سردرد")
    val note: String = ""           // یادداشت شخصی
)
