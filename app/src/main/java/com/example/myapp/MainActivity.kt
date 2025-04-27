package com.example.myapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApp()
        }
    }
}

@Composable
fun MyApp() {
    val navController = rememberNavController()
    NavHost(navController, startDestination = "loginScreen") {
        composable("loginScreen") { LoginScreen(navController) }
        composable("registerScreen") { RegisterScreen(navController) }
        composable("taskListScreen") { TaskListScreen(navController) }
        composable(
            "addTaskScreen/{taskId}",
            arguments = listOf(navArgument("taskId") { defaultValue = "-1" })
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId")?.toInt() ?: -1
            AddTaskScreen(navController, taskId)
        }
    }
}

// ---------------- Pantalla de Login ----------------
@Composable
fun LoginScreen(navController: NavHostController) {
    val context = LocalContext.current
    val dbHelper = remember { DBHelper(context) }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    Column(Modifier.padding(16.dp)) {
        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = {
                if (dbHelper.loginUser(email, password)) {
                    navController.navigate("taskListScreen") {
                        popUpTo("loginScreen") { inclusive = true }
                    }
                } else {
                    errorMessage = "Email o contraseña incorrectos"
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Iniciar sesión")
        }
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = { navController.navigate("registerScreen") }) {
            Text("¿No tienes cuenta? Regístrate")
        }
        if (errorMessage.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Text(errorMessage, color = MaterialTheme.colorScheme.error)
        }
    }
}

// ---------------- Pantalla de Registro ----------------
@Composable
fun RegisterScreen(navController: NavHostController) {
    val context = LocalContext.current
    val dbHelper = remember { DBHelper(context) }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    Column(Modifier.padding(16.dp)) {
        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = {
                if (dbHelper.registerUser(email, password)) {
                    navController.navigate("loginScreen") {
                        popUpTo("registerScreen") { inclusive = true }
                    }
                } else {
                    errorMessage = "Error al registrar el usuario"
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Registrarse")
        }
        if (errorMessage.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Text(errorMessage, color = MaterialTheme.colorScheme.error)
        }
    }
}

// ---------------- Pantalla de Lista de Tareas ----------------
@Composable
fun TaskListScreen(navController: NavHostController) {
    val context = LocalContext.current
    val dbHelper = remember { DBHelper(context) }
    val tasks = remember { mutableStateListOf<Task>() }

    LaunchedEffect(Unit) {
        tasks.clear()
        tasks.addAll(dbHelper.getAllTasks())
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("addTaskScreen/-1") }) {
                Text("+")
            }
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(24.dp)
                .padding(padding)
        ) {
            Text("Lista de Tareas", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(16.dp))

            if (tasks.isEmpty()) {
                Text("No hay tareas todavía")
            } else {
                tasks.forEach { task ->
                    TaskItem(task, navController, dbHelper, tasks)
                }
            }
        }
    }
}

@Composable
fun TaskItem(task: Task, navController: NavHostController, dbHelper: DBHelper, tasks: SnapshotStateList<Task>) {
    Card(Modifier
        .fillMaxWidth()
        .padding(8.dp)) {
        Column(Modifier.padding(16.dp)) {
            Text("Tarea: ${task.name}")
            Text("Inicio: ${task.startDate}")
            Text("Fin: ${task.endDate}")

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(onClick = { navController.navigate("addTaskScreen/${task.id}") }) {
                    Text("Modificar")
                }
                Button(onClick = {
                    dbHelper.deleteTask(task.id)
                    tasks.remove(task)
                }) {
                    Text("Eliminar")
                }
            }
        }
    }
}

// ---------------- Pantalla de Agregar/Modificar Tarea ----------------
@Composable
fun AddTaskScreen(navController: NavHostController, taskId: Int) {
    val context = LocalContext.current
    val dbHelper = remember { DBHelper(context) }

    var name by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }

    LaunchedEffect(taskId) {
        if (taskId != -1) {
            val task = dbHelper.getTaskById(taskId)
            task?.let {
                name = it.name
                startDate = it.startDate
                endDate = it.endDate
            }
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nombre de tarea") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = startDate,
            onValueChange = { startDate = it },
            label = { Text("Fecha de inicio") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = endDate,
            onValueChange = { endDate = it },
            label = { Text("Fecha de fin") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                if (taskId == -1) {
                    dbHelper.addTask(name, startDate, endDate, 1)
                } else {
                    dbHelper.updateTask(taskId, name, startDate, endDate)
                }
                navController.popBackStack()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Guardar tarea")
        }
    }
}

// ---------------- Modelo de Tarea ----------------
data class Task(
    val id: Int = 0,
    val name: String = "",
    val startDate: String = "",
    val endDate: String = ""
)












