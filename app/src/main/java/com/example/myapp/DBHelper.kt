package com.example.myapp

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "MyAppDatabase.db"
        private const val DATABASE_VERSION = 1

        // Tablas
        const val TABLE_USERS = "Users"
        const val TABLE_TASKS = "Tasks"

        // Columnas de usuarios
        const val USER_ID = "id"
        const val USER_EMAIL = "email"
        const val USER_PASSWORD = "password"

        // Columnas de tareas
        const val TASK_ID = "id"
        const val TASK_NAME = "name"
        const val TASK_START_DATE = "startDate"
        const val TASK_END_DATE = "endDate"
        const val TASK_USER_ID = "userId"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createUsersTable = """
            CREATE TABLE $TABLE_USERS (
                $USER_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $USER_EMAIL TEXT UNIQUE,
                $USER_PASSWORD TEXT
            )
        """.trimIndent()

        val createTasksTable = """
            CREATE TABLE $TABLE_TASKS (
                $TASK_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $TASK_NAME TEXT,
                $TASK_START_DATE TEXT,
                $TASK_END_DATE TEXT,
                $TASK_USER_ID INTEGER,
                FOREIGN KEY ($TASK_USER_ID) REFERENCES $TABLE_USERS($USER_ID)
            )
        """.trimIndent()

        db.execSQL(createUsersTable)
        db.execSQL(createTasksTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_TASKS")
        onCreate(db)
    }

    // --------- Métodos para Usuario ---------
    fun registerUser(email: String, password: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(USER_EMAIL, email)
            put(USER_PASSWORD, password)
        }
        val result = db.insert(TABLE_USERS, null, values)
        db.close()
        return result != -1L
    }

    fun loginUser(email: String, password: String): Boolean {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            arrayOf(USER_ID),
            "$USER_EMAIL=? AND $USER_PASSWORD=?",
            arrayOf(email, password),
            null, null, null
        )
        val loggedIn = cursor.moveToFirst()
        cursor.close()
        db.close()
        return loggedIn
    }

    // --------- Métodos CRUD para Tareas ---------
    fun addTask(name: String, startDate: String, endDate: String, userId: Int): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(TASK_NAME, name)
            put(TASK_START_DATE, startDate)
            put(TASK_END_DATE, endDate)
            put(TASK_USER_ID, userId)
        }
        val result = db.insert(TABLE_TASKS, null, values)
        db.close()
        return result != -1L
    }

    fun updateTask(id: Int, name: String, startDate: String, endDate: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(TASK_NAME, name)
            put(TASK_START_DATE, startDate)
            put(TASK_END_DATE, endDate)
        }
        val result = db.update(TABLE_TASKS, values, "$TASK_ID=?", arrayOf(id.toString()))
        db.close()
        return result > 0
    }

    fun deleteTask(id: Int): Boolean {
        val db = writableDatabase
        val result = db.delete(TABLE_TASKS, "$TASK_ID=?", arrayOf(id.toString()))
        db.close()
        return result > 0
    }

    fun getTaskById(taskId: Int): Task? {
        val db = readableDatabase
        var task: Task? = null
        val cursor = db.query(
            TABLE_TASKS,
            arrayOf(TASK_ID, TASK_NAME, TASK_START_DATE, TASK_END_DATE),
            "$TASK_ID=?",
            arrayOf(taskId.toString()),
            null, null, null
        )
        if (cursor.moveToFirst()) {
            task = Task(
                id = cursor.getInt(cursor.getColumnIndexOrThrow(TASK_ID)),
                name = cursor.getString(cursor.getColumnIndexOrThrow(TASK_NAME)),
                startDate = cursor.getString(cursor.getColumnIndexOrThrow(TASK_START_DATE)),
                endDate = cursor.getString(cursor.getColumnIndexOrThrow(TASK_END_DATE))
            )
        }
        cursor.close()
        db.close()
        return task
    }

    fun getAllTasks(): List<Task> {
        val taskList = mutableListOf<Task>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_TASKS,
            arrayOf(TASK_ID, TASK_NAME, TASK_START_DATE, TASK_END_DATE),
            null, null, null, null, "$TASK_ID DESC"
        )
        while (cursor.moveToNext()) {
            val task = Task(
                id = cursor.getInt(cursor.getColumnIndexOrThrow(TASK_ID)),
                name = cursor.getString(cursor.getColumnIndexOrThrow(TASK_NAME)),
                startDate = cursor.getString(cursor.getColumnIndexOrThrow(TASK_START_DATE)),
                endDate = cursor.getString(cursor.getColumnIndexOrThrow(TASK_END_DATE))
            )
            taskList.add(task)
        }
        cursor.close()
        db.close()
        return taskList
    }
}



