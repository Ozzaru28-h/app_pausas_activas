package com.example.pausasactivas.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.pausasactivas.data.local.UserSession
import com.example.pausasactivas.domain.model.Exercise
import com.example.pausasactivas.domain.model.ExerciseCatalog

@Composable
fun HomeScreen(
    user: UserSession?,
    onStartExercise: (Exercise) -> Unit,
    onOpenHistory: () -> Unit,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Hola, ${user?.nombre.orEmpty().ifBlank { "estudiante" }}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Elige un ejercicio de calentamiento",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            TextButton(onClick = onLogout) { Text("Cerrar sesión") }
        }

        ExerciseCatalog.demo.forEach { exercise ->
            ExerciseCard(exercise = exercise, onStart = { onStartExercise(exercise) })
        }

        OutlinedButton(
            onClick = onOpenHistory,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Mi historial")
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Demostración académica: el análisis usa reglas simples de ángulos sobre " +
                "los puntos que detecta la cámara. No es un diagnóstico médico ni una " +
                "evaluación clínica.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ExerciseCard(
    exercise: Exercise,
    onStart: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = exercise.nombre,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(text = exercise.resumen, style = MaterialTheme.typography.bodyMedium)
            exercise.instrucciones.forEachIndexed { index, step ->
                Text(
                    text = "${index + 1}. $step",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Text(
                text = "Regla de la demo: ${exercise.regla}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(
                onClick = onStart,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Comenzar")
            }
        }
    }
}
