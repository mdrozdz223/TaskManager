package com.example.tasks

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.tasks.ui.AddTaskScreen
import com.example.tasks.ui.TaskListScreen
import com.example.tasks.ui.TaskDetailScreen
import com.example.tasks.ui.theme.TasksTheme
import com.example.tasks.viewmodel.TaskViewModel
import com.example.tasks.viewmodel.TaskViewModelFactory
import com.example.tasks.ui.SettingsScreen
import android.os.Build
import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    if (!isGranted) {
                    }
                }

                LaunchedEffect(Unit) {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }

            val app = applicationContext as TaskApplication

            val alarmScheduler = TaskAlarmScheduler(applicationContext)

            val viewModel: TaskViewModel = viewModel(
                factory = TaskViewModelFactory(app.repository, alarmScheduler)
            )

            val isDarkMode by viewModel.isDarkMode.collectAsState()

            TasksTheme(darkTheme = isDarkMode) {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = "list") {
                    composable("list") {
                        TaskListScreen(
                            viewModel = viewModel,
                            onTaskClick = { task -> navController.navigate("detail/${task.id}") },
                            onAddTask = { navController.navigate("add") },
                            onSettingsClick = { navController.navigate("settings") }
                        )
                    }
                    composable("detail/{taskId}") { backStackEntry ->
                        val taskId = backStackEntry.arguments?.getString("taskId")?.toInt() ?: 0
                        TaskDetailScreen(viewModel, taskId, onBack = { navController.popBackStack() })
                    }
                    composable("add") {
                        AddTaskScreen(viewModel, onNavigateBack = { navController.popBackStack() })
                    }
                    composable("settings") {
                        SettingsScreen(viewModel, onNavigateBack = { navController.popBackStack() })
                    }
                }
            }
        }
    }
}