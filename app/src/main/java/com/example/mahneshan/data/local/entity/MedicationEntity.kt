package com.example.mahneshan.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medications")
data class MedicationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,               // نام قرص یا مکمل (مثلاً: قرص آهن)
    val hour: Int,                  // ساعت یادآوری
    val minute: Int,                // دقیقه یادآوری
    val isActive: Boolean = true
)

@Entity(
    tableName = "medication_logs",
    primaryKeys = ["medicationId", "dateEpochDay"]
)
data class MedicationLogEntity(
    val medicationId: Long,
    val dateEpochDay: Long,
    val isTaken: Boolean
)
