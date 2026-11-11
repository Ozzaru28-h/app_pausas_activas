package com.example.pausasactivas.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.pausasactivas.data.api.RetrofitClient
import com.example.pausasactivas.ui.AuthUiState

@Composable
fun AuthScreen(
    auth: AuthUiState,
    onLogin: (correo: String) -> Unit,
    onRegister: (correo: String, nombre: String, carrera: String) -> Unit,
    onClearError: () -> Unit
) {
    var registerMode by rememberSaveable { mutableStateOf(false) }
    var correo by rememberSaveable { mutableStateOf("") }
    var nombre by rememberSaveable { mutableStateOf("") }
    var carrera by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Pausas Activas",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = if (registerMode) "Crea tu usuario con tu correo institucional"
            else "Ingresa con tu correo institucional",
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = correo,
            onValueChange = {
                correo = it
                onClearError()
            },
            label = { Text("Correo (@uniautonoma.edu.co)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )

        if (registerMode) {
            OutlinedTextField(
                value = nombre,
                onValueChange = {
                    nombre = it
                    onClearError()
                },
                label = { Text("Nombre") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = carrera,
                onValueChange = { carrera = it },
                label = { Text("Carrera (opcional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (auth.error != null) {
            Text(
                text = auth.error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Button(
            onClick = {
                if (registerMode) onRegister(correo, nombre, carrera) else onLogin(correo)
            },
            enabled = !auth.loading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (auth.loading) {
                CircularProgressIndicator(
                    modifier = Modifier.height(20.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text(if (registerMode) "Registrarme" else "Ingresar")
            }
        }

        TextButton(
            onClick = {
                registerMode = !registerMode
                onClearError()
            },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text(
                if (registerMode) "¿Ya tienes usuario? Ingresa"
                else "¿Primera vez? Regístrate"
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Acceso de demostración: solo se comprueba que el correo esté registrado. " +
                "No hay contraseña ni autenticación segura.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "Servidor: ${RetrofitClient.BASE_URL}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
