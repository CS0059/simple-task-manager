package com.example.myapplication.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "todos")
data class Todo(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val description: String,
    val isCompleted: Boolean,
    val createdDate: Long,
    val startTime: Long? = null,
    val endTime: Long? = null,
    val startDate: Long? = null,
    val endDate: Long? = null
) 