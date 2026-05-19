package com.example.conscia.data.rule

import androidx.room.Entity
import androidx.room.ColumnInfo
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
    @ColumnInfo(defaultValue = "0")
    val extensionMinutes: Int = 0,
    @ColumnInfo(defaultValue = "0")
    val extensionCount: Int = 0,
    @ColumnInfo(defaultValue = "''")
    val lastExtensionDate: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
