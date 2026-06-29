package com.example.tasks.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.tasks.model.Priority
import com.example.tasks.model.Task
import com.example.tasks.viewmodel.TaskViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(viewModel: TaskViewModel, taskId: Int, onBack: () -> Unit) {
    val allTasks by viewModel.filteredTasks.collectAsState()
    val task = allTasks.find { it.id == taskId }

    var title by remember(task) { mutableStateOf(task?.title ?: "") }
    var description by remember(task) { mutableStateOf(task?.description ?: "") }
    var selectedPriority by remember(task) { mutableStateOf(task?.priority ?: Priority.MEDIUM) }
    var selectedDate by remember(task) { mutableStateOf(task?.dueDate) }

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDate)

    var selectedCategory by remember(task) { mutableStateOf(task?.category ?: "Ogólne") }
    val categoriesList = listOf("Ogólne", "Praca", "Dom", "Zakupy", "Osobiste")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edytuj zadanie") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Wstecz")
                    }
                }
            )
        }
    ) { paddingValues ->
        task?.let { t ->
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
                    minLines = 3
                )

                Spacer(modifier = Modifier.height(24.dp))
                Text("Priorytet:", style = MaterialTheme.typography.labelLarge)
                Row(modifier = Modifier.padding(vertical = 8.dp)) {
                    Priority.entries.forEach { priority ->
                        FilterChip(
                            selected = selectedPriority == priority,
                            onClick = { selectedPriority = priority },
                            label = { Text(priority.name) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }

                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    val dateText = selectedDate?.let {
                        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(it))
                    } ?: "Ustaw datę"
                    Text(text = "Termin: $dateText")
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    onClick = {
                        val updatedTask = t.copy(
                            title = title,
                            description = description,
                            priority = selectedPriority,
                            category = selectedCategory,
                            dueDate = selectedDate
                        )
                        viewModel.updateTask(updatedTask)
                        onBack()
                    }
                ) {
                    Text("Zapisz zmiany")
                }
            }
        }

        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        selectedDate = datePickerState.selectedDateMillis
                        showDatePicker = false
                    }) { Text("OK") }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) { Text("Anuluj") }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }
    }
}