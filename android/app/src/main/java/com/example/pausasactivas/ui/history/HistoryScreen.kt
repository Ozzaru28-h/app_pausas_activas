package com.example.pausasactivas.ui.history

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.pausasactivas.data.api.SessionDto
import com.example.pausasactivas.ui.HistoryUiState
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

@Composable
fun HistoryScreen(
    state: HistoryUiState,
    onBack: () -> Unit,
    onRefresh: () -> Unit
) {
    BackHandler(onBack = onBack)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(onClick = onBack) { Text("← Volver") }
            TextButton(onClick = onRefresh, enabled = !state.loading) { Text("Actualizar") }
        }
        Text(
            text = "Mi historial",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        when {
            state.loading -> CenteredBox { CircularProgressIndicator() }

            state.error != null -> CenteredBox {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = state.error,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                    Button(
                        onClick = onRefresh,
                        modifier = Modifier.padding(top = 12.dp)
                    ) { Text("Reintentar") }
                }
            }

            state.sessions.isEmpty() -> CenteredBox {
                Text(
                    text = "Aún no tienes sesiones guardadas. Completa un ejercicio para verlo aquí.",
                    textAlign = TextAlign.Center
                )
            }

            else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(state.sessions) { session -> SessionCard(session) }
            }
        }
    }
}

@Composable
private fun CenteredBox(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
private fun SessionCard(session: SessionDto) {
    val telemetry = session.telemetria
    val precision = telemetry?.precisionPromedioPct
    val isValid = telemetry?.posturaValida == true

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = session.ejercicio?.nombre ?: "Ejercicio sin nombre",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = formatDate(session.fechaRegistro),
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = if (precision != null) "Precisión de postura: ${precision.toInt()} %"
                else "Precisión no disponible",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = if (isValid) "Resultado: postura válida" else "Resultado: por mejorar",
                style = MaterialTheme.typography.bodyMedium
            )
            telemetry?.metricasError.orEmpty().forEach { error ->
                Text(
                    text = "• ${error.articulacionFalla ?: "Articulación"}: " +
                        "${error.anguloDetectado?.toInt() ?: "?"}° detectado " +
                        "(esperado ${error.anguloEsperado?.toInt() ?: "?"}°)",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

/** Convierte "2026-11-10T18:05:00.000Z" a "10/11/2026 13:05" (hora local del teléfono). */
private fun formatDate(iso: String?): String {
    if (iso.isNullOrBlank()) return "Fecha desconocida"
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val date = parser.parse(iso) ?: return iso
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.forLanguageTag("es-CO")).format(date)
    } catch (e: Exception) {
        iso.take(16).replace('T', ' ')
    }
}
