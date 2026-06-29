package com.example.tasks.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.tasks.TaskAlarmScheduler
import com.example.tasks.data.TaskRepository
import com.example.tasks.model.Status
import com.example.tasks.model.Task
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

enum class TaskFilter { ALL, TODAY, OVERDUE }

class TaskViewModel(
    private val repository: TaskRepository,
    private val alarmScheduler: TaskAlarmScheduler
) : ViewModel() {

    private val _currentFilter = MutableStateFlow(TaskFilter.ALL)
    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _isNotificationsEnabled = MutableStateFlow(true)
    val isNotificationsEnabled: StateFlow<Boolean> = _isNotificationsEnabled.asStateFlow()

    fun toggleNotifications() {
        _isNotificationsEnabled.value = !_isNotificationsEnabled.value
    }
    fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
    }
    val currentFilter: StateFlow<TaskFilter> = _currentFilter.asStateFlow()

    val filteredTasks: StateFlow<List<Task>> = combine(repository.allTasks, _currentFilter) { tasks, filter ->
        val today = LocalDate.now().toEpochDay()

        val filtered = when (filter) {
            TaskFilter.ALL -> tasks
            TaskFilter.TODAY -> tasks.filter { task ->
                task.dueDate?.let { LocalDate.ofEpochDay(it / (24 * 60 * 60 * 1000)).toEpochDay() == today } ?: false
            }
            TaskFilter.OVERDUE -> tasks.filter { task ->
                task.dueDate?.let { it / (24 * 60 * 60 * 1000) < today && task.status != Status.DONE } ?: false
            }
        }

        filtered.sortedWith(
            compareBy<Task> { it.status == Status.DONE }
                .thenByDescending { it.priority.ordinal }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setFilter(filter: TaskFilter) {
        _currentFilter.value = filter
    }


    fun insertTask(task: Task) = viewModelScope.launch {
        val generatedId = repository.insertTask(task)

        val taskWithRealId = task.copy(id = generatedId.toInt())

        if (isNotificationsEnabled.value) {
            alarmScheduler.schedule(taskWithRealId)
        }
    }

    fun updateTask(task: Task) = viewModelScope.launch {
        repository.updateTask(task)
        if (isNotificationsEnabled.value && task.status != Status.DONE) {
            alarmScheduler.schedule(task)
        } else {
            alarmScheduler.cancel(task)
        }
    }

    fun deleteTask(task: Task) = viewModelScope.launch {
        repository.deleteTask(task)
        alarmScheduler.cancel(task)
    }

    fun markAsDone(task: Task) = viewModelScope.launch {
        val updatedTask = task.copy(status = Status.DONE)
        repository.updateTask(updatedTask)
        alarmScheduler.cancel(task)
    }

    fun getTaskById(id: Int): Flow<Task> = repository.getTaskById(id)
}

class TaskViewModelFactory(
    private val repository: TaskRepository,
    private val alarmScheduler: TaskAlarmScheduler
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TaskViewModel(repository, alarmScheduler) as T
        }
        throw IllegalArgumentException("Nieznana klasa ViewModel")
    }
}