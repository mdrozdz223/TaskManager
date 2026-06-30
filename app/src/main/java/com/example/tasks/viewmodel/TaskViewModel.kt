package com.example.tasks.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.tasks.TaskAlarmScheduler
import com.example.tasks.GeofenceHelper
import com.example.tasks.data.TaskRepository
import com.example.tasks.model.Recurrence
import com.example.tasks.model.Status
import com.example.tasks.model.Task
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.Calendar

enum class TaskFilter { ALL, TODAY, OVERDUE }

class TaskViewModel(
    private val repository: TaskRepository,
    private val alarmScheduler: TaskAlarmScheduler,
    private val geofenceHelper: GeofenceHelper
) : ViewModel() {

    private val _currentFilter = MutableStateFlow(TaskFilter.ALL)
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

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

    val filteredTasks: StateFlow<List<Task>> = combine(
        repository.allTasks,
        _currentFilter,
        _searchQuery
    ) { tasks, filter, query ->
        val today = LocalDate.now().toEpochDay()

        var filtered = when (filter) {
            TaskFilter.ALL -> tasks
            TaskFilter.TODAY -> tasks.filter { task ->
                task.dueDate?.let { LocalDate.ofEpochDay(it / (24 * 60 * 60 * 1000)).toEpochDay() == today } ?: false
            }
            TaskFilter.OVERDUE -> tasks.filter { task ->
                task.dueDate?.let { it / (24 * 60 * 60 * 1000) < today && task.status != Status.DONE } ?: false
            }
        }

        if (query.isNotBlank()) {
            filtered = filtered.filter { it.title.contains(query, ignoreCase = true) }
        }

        filtered.sortedWith(
            compareBy<Task> { it.status == Status.DONE }
                .thenByDescending { it.priority.ordinal }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setFilter(filter: TaskFilter) {
        _currentFilter.value = filter
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun insertTask(task: Task) = viewModelScope.launch {
        val generatedId = repository.insertTask(task)
        val taskWithRealId = task.copy(id = generatedId.toInt())

        if (isNotificationsEnabled.value) {
            alarmScheduler.schedule(taskWithRealId)
        }
        geofenceHelper.addGeofence(taskWithRealId)
    }

    fun updateTask(task: Task) = viewModelScope.launch {
        repository.updateTask(task)
        if (isNotificationsEnabled.value && task.status != Status.DONE) {
            alarmScheduler.schedule(task)
        } else {
            alarmScheduler.cancel(task)
        }
        geofenceHelper.addGeofence(task)
    }

    fun deleteTask(task: Task) = viewModelScope.launch {
        repository.deleteTask(task)
        alarmScheduler.cancel(task)
        geofenceHelper.removeGeofence(task.title)
    }

    fun markAsDone(task: Task) = viewModelScope.launch {
        val updatedTask = task.copy(status = Status.DONE)
        repository.updateTask(updatedTask)
        alarmScheduler.cancel(task)
        geofenceHelper.removeGeofence(task.title)

        if (task.recurrence != Recurrence.NONE && task.dueDate != null) {
            val nextDueDate = calculateNextDate(task.dueDate, task.recurrence)
            val newTask = task.copy(id = 0, dueDate = nextDueDate, status = Status.TODO)
            insertTask(newTask)
        }
    }

    private fun calculateNextDate(currentDate: Long, recurrence: Recurrence): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = currentDate
        when (recurrence) {
            Recurrence.DAILY -> calendar.add(Calendar.DAY_OF_YEAR, 1)
            Recurrence.WEEKLY -> calendar.add(Calendar.WEEK_OF_YEAR, 1)
            Recurrence.MONTHLY -> calendar.add(Calendar.MONTH, 1)
            else -> {}
        }
        return calendar.timeInMillis
    }

    fun getTaskById(id: Int): Flow<Task> = repository.getTaskById(id)
}

class TaskViewModelFactory(
    private val repository: TaskRepository,
    private val alarmScheduler: TaskAlarmScheduler,
    private val geofenceHelper: GeofenceHelper
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TaskViewModel(repository, alarmScheduler, geofenceHelper) as T
        }
        throw IllegalArgumentException("Nieznana klasa ViewModel")
    }
}