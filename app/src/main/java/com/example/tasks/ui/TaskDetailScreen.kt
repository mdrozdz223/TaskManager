package com.example.tasks.ui

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.tasks.model.Priority
import com.example.tasks.model.Recurrence
import com.example.tasks.model.Task
import com.example.tasks.viewmodel.TaskViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(viewModel: TaskViewModel, taskId: Int, onBack: () -> Unit) {
    val allTasks by viewModel.filteredTasks.collectAsState()
    val task = allTasks.find { it.id == taskId }

    var title by remember(task) { mutableStateOf(task?.title ?: "") }
    var description by remember(task) { mutableStateOf(task?.description ?: "") }
    var selectedPriority by remember(task) { mutableStateOf(task?.priority ?: Priority.MEDIUM) }
    var selectedDate by remember(task) { mutableStateOf(task?.dueDate) }

    var selectedCategory by remember(task) { mutableStateOf(task?.category ?: "Ogólne") }
    val categoriesList = listOf("Ogólne", "Praca", "Dom", "Zakupy", "Osobiste")

    var selectedRecurrence by remember(task) { mutableStateOf(task?.recurrence ?: Recurrence.NONE) }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDate)
    val timePickerState = rememberTimePickerState(initialHour = 12, initialMinute = 0)

    val context = LocalContext.current

    var selectedImageUris by remember(task) {
        val json = task?.attachmentsJson
        val initialList: List<Uri> = if (json.isNullOrBlank()) {
            emptyList()
        } else {
            json.split(",").map { Uri.parse(it) }
        }
        mutableStateOf(initialList)
    }

    val multiplePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(),
        onResult = { uris ->
            val newUris = uris.filter { it !in selectedImageUris }
            selectedImageUris = selectedImageUris + newUris

            newUris.forEach { uri ->
                try {
                    context.contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    )

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
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
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

                    Spacer(modifier = Modifier.height(16.dp))
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

                    Spacer(modifier = Modifier.height(16.dp))
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

                    OutlinedButton(
                        onClick = { showDatePicker = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                    ) {
                        Icon(Icons.Default.DateRange, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        val dateText = selectedDate?.let {
                            SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(it))
                        } ?: "Ustaw datę i godzinę"
                        Text(text = "Termin: $dateText")
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Załączniki (Zdjęcia):", style = MaterialTheme.typography.labelLarge)

                    OutlinedButton(
                        onClick = {
                            multiplePhotoPickerLauncher.launch(
                                androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                    ) {
                        Text("Dodaj zdjęcia z galerii")
                    }

                    if (selectedImageUris.isNotEmpty()) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                        ) {
                            items(selectedImageUris) { uri ->
                                AsyncImage(
                                    model = uri,
                                    contentDescription = "Załączone zdjęcie",
                                    modifier = Modifier.size(80.dp),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                }

                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    onClick = {
                        val attachmentsString = if (selectedImageUris.isEmpty()) null
                        else selectedImageUris.joinToString(",") { it.toString() }

                        val updatedTask = t.copy(
                            title = title,
                            description = description,
                            priority = selectedPriority,
                            category = selectedCategory,
                            recurrence = selectedRecurrence,
                            dueDate = selectedDate,
                            attachmentsJson = attachmentsString
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
                        showDatePicker = false
                        showTimePicker = true
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
                text = { TimePicker(state = timePickerState) },
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

                        selectedDate = localCalendar.timeInMillis
                        showTimePicker = false
                    }) { Text("OK") }
                },
                dismissButton = {
                    TextButton(onClick = { showTimePicker = false }) { Text("Anuluj") }
                }
            )
        }
    }
}