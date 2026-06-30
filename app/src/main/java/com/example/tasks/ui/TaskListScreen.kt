package com.example.tasks.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.tasks.model.Task
import com.example.tasks.viewmodel.TaskViewModel
import com.example.tasks.viewmodel.TaskFilter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    viewModel: TaskViewModel,
    onTaskClick: (Task) -> Unit,
    onAddTask: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val filteredTasks by viewModel.filteredTasks.collectAsState()
    val currentFilter by viewModel.currentFilter.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Moje Zadania") },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Ustawienia"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onAddTask() }) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Dodaj")
            }
        }
    ) { paddingValues ->

        Column(modifier = Modifier.padding(paddingValues)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TaskFilter.entries.forEach { filter ->
                    FilterChip(
                        selected = currentFilter == filter,
                        onClick = { viewModel.setFilter(filter) },
                        label = { Text(filter.name.lowercase().replaceFirstChar { it.uppercase() }) }
                    )
                }
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                label = { Text("Szukaj zadania...") },
                singleLine = true
            )

            if (filteredTasks.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Brak zadań w tym widoku.")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(filteredTasks, key = { it.id }) { task ->
                        TaskItem(
                            task = task,
                            onDelete = { viewModel.deleteTask(task) },
                            onDone = { viewModel.markAsDone(task) },
                            onClick = { onTaskClick(task) }
                        )
                    }
                }
            }
        }
    }
}