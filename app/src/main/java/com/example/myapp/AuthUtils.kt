package com.example.myapp

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

// Firebase Authentication Instance
val auth: FirebaseAuth = FirebaseAuth.getInstance()
val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

// Función para registrar un nuevo usuario con email y contraseña
suspend fun registerUser(email: String, password: String): FirebaseUser? {
    return try {
        // Intentamos registrar al usuario
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        result.user
    } catch (e: Exception) {
        Log.e("Auth", "Error al registrar el usuario", e)
        null
    }
}

// Función para iniciar sesión con email y contraseña
suspend fun loginUser(email: String, password: String): FirebaseUser? {
    return try {
        // Intentamos loguear al usuario
        val result = auth.signInWithEmailAndPassword(email, password).await()
        result.user
    } catch (e: Exception) {
        Log.e("Auth", "Error al iniciar sesión", e)
        null
    }
}

// Función para obtener el usuario actual
fun getCurrentUser(): FirebaseUser? {
    return auth.currentUser
}

// Función para cerrar sesión
fun logoutUser() {
    auth.signOut()
}
