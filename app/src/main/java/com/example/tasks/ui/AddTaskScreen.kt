package com.example.tasks.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import com.example.tasks.model.Priority
import com.example.tasks.model.Recurrence
import com.example.tasks.model.Status
import com.example.tasks.model.Task
import com.example.tasks.viewmodel.TaskViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskScreen(viewModel: TaskViewModel, onNavigateBack: () -> Unit) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedPriority by remember { mutableStateOf(Priority.MEDIUM) }

    var selectedCategory by remember { mutableStateOf("Ogólne") }
    val categoriesList = listOf("Ogólne", "Praca", "Dom", "Zakupy", "Osobiste")

    var selectedRecurrence by remember { mutableStateOf(Recurrence.NONE) }

    var selectedDateMillis by remember { mutableStateOf<Long?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState()
    val timePickerState = rememberTimePickerState(initialHour = 12, initialMinute = 0)

    val focusManager = LocalFocusManager.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nowe Zadanie") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Wstecz")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Tytuł") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Opis") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text("Priorytet:", style = MaterialTheme.typography.labelLarge)
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Priority.entries.forEach { priority ->
                    FilterChip(
                        selected = selectedPriority == priority,
                        onClick = { selectedPriority = priority },
                        label = { Text(priority.name) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text("Kategoria:", style = MaterialTheme.typography.labelLarge)
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categoriesList.forEach { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        label = { Text(category) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text("Powtarzaj:", style = MaterialTheme.typography.labelLarge)
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Recurrence.entries.forEach { recurrence ->
                    FilterChip(
                        selected = selectedRecurrence == recurrence,
                        onClick = { selectedRecurrence = recurrence },
                        label = { Text(recurrence.name) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.DateRange, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(
                    selectedDateMillis?.let {
                        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(it))
                    } ?: "Wybierz datę i godzinę powiadomienia"
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                onClick = {
                    if (title.isNotBlank()) {
                        val newTask = Task(
                            title = title,
                            description = description,
                            priority = selectedPriority,
                            category = selectedCategory,
                            recurrence = selectedRecurrence,
                            status = Status.TODO,
                            dueDate = selectedDateMillis
                        )
                        viewModel.insertTask(newTask)
                        focusManager.clearFocus()
                        onNavigateBack()
                    }
                }
            ) {
                Text("Dodaj zadanie")
            }
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    showDatePicker = false
                    showTimePicker = true // Po dacie otwieramy czas!
                }) { Text("Dalej") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Anuluj") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Wybierz godzinę") },
            text = {
                TimePicker(state = timePickerState)
            },
            confirmButton = {
                TextButton(onClick = {
                    val utcCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                    datePickerState.selectedDateMillis?.let { utcCalendar.timeInMillis = it }

                    val localCalendar = Calendar.getInstance()
                    localCalendar.set(Calendar.YEAR, utcCalendar.get(Calendar.YEAR))
                    localCalendar.set(Calendar.MONTH, utcCalendar.get(Calendar.MONTH))
                    localCalendar.set(Calendar.DAY_OF_MONTH, utcCalendar.get(Calendar.DAY_OF_MONTH))
                    localCalendar.set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                    localCalendar.set(Calendar.MINUTE, timePickerState.minute)
                    localCalendar.set(Calendar.SECOND, 0)
                    localCalendar.set(Calendar.MILLISECOND, 0)

                    selectedDateMillis = localCalendar.timeInMillis
                    showTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Anuluj") }
            }
        )
    }
}