package com.example.conscia.data.rule

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rules")
data class RuleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val packageName: String,
    val appName: String,
    val intentionLabel: String,
    val dailyLimitMinutes: Int,
    val trackingEnabled: Boolean = true,
    val warningEnabled: Boolean = true,
    val extensionMinutes: Int = 0, // Tổng số phút được gia hạn thêm trong ngày
    val extensionCount: Int = 0,   // Số lần đã nhấn gia hạn trong ngày
    val lastExtensionDate: String = "", // Định dạng yyyy-MM-dd để reset mỗi ngày
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
