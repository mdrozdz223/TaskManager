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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.tasks.model.Priority
import com.example.tasks.model.Recurrence
import com.example.tasks.model.Status
import com.example.tasks.model.Task
import com.example.tasks.viewmodel.TaskViewModel
import java.text.SimpleDateFormat
import java.util.*
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

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
    val context = LocalContext.current

    var selectedImageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }

    val multiplePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(),
        onResult = { uris ->
            selectedImageUris = uris
            uris.forEach { uri ->
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

    var selectedLocation by remember { mutableStateOf<LatLng?>(null) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(51.7592, 19.4560), 5f) // Początkowo widok na Łódź/Polskę
    }

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
                    Text("Wybierz zdjęcia z galerii")
                }

                if (selectedImageUris.isNotEmpty()) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                    ) {
                        items(selectedImageUris) { uri ->
                            AsyncImage(
                                model = uri,
                                contentDescription = "Wybrane zdjęcie",
                                modifier = Modifier.size(80.dp),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Lokalizacja (Wybierz na mapie):", style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                ) {
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState,
                        onMapClick = { latLng ->
                            selectedLocation = latLng
                        }
                    ) {
                        selectedLocation?.let { location ->
                            Marker(
                                state = MarkerState(position = location),
                                title = "Lokalizacja nowego zadania",
                                snippet = title
                            )
                        }
                    }
                }

                if (selectedLocation != null) {
                    TextButton(
                        onClick = { selectedLocation = null },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Usuń lokalizację", color = MaterialTheme.colorScheme.error)
                    }
                }
            }

            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                onClick = {
                    if (title.isNotBlank()) {
                        val attachmentsString = if (selectedImageUris.isEmpty()) null
                        else selectedImageUris.joinToString(",") { it.toString() }

                        val newTask = Task(
                            title = title,
                            description = description,
                            priority = selectedPriority,
                            category = selectedCategory,
                            recurrence = selectedRecurrence,
                            status = Status.TODO,
                            dueDate = selectedDateMillis,
                            attachmentsJson = attachmentsString,
                            latitude = selectedLocation?.latitude,
                            longitude = selectedLocation?.longitude
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