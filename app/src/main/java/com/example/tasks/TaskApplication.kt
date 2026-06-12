package com.example.tasks

import android.app.Application
import com.example.tasks.data.TaskDatabase
import com.example.tasks.data.TaskRepository

class TaskApplication : Application() {

    val database by lazy { TaskDatabase.getDatabase(this) }
    val repository by lazy { TaskRepository(database.taskDao()) }
}