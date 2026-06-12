package com.example.tasks.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val dueDate: Long? = null,
    val status: Status = Status.TODO,
    val category: String = "Ogólne",
    val priority: Priority = Priority.MEDIUM,
    val reminderTime: Long? = null,
    val recurrence: Recurrence = Recurrence.NONE,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val locationName: String? = null,
    val attachmentsJson: String? = null
)