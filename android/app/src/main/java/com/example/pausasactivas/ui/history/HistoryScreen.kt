package com.example.pausasactivas.ui.history

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.pausasactivas.data.api.SessionDto
import com.example.pausasactivas.domain.model.ExerciseCatalog
import com.example.pausasactivas.ui.HistoryUiState
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

/** Cada cuánto se vuelve a consultar la API para ver los cambios hechos desde la web. */
private const val AUTO_REFRESH_MS = 20_000L

@Composable
fun HistoryScreen(
    state: HistoryUiState,
    userName: String,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onSilentRefresh: () -> Unit,
    onExerciseFilterChange: (String?) -> Unit
) {
    BackHandler(onBack = onBack)

    // Sincronización con la web: refresco silencioso periódico mientras la pantalla está abierta.
    val currentSilentRefresh by rememberUpdatedState(onSilentRefresh)
    LaunchedEffect(Unit) {
        while (true) {
            delay(AUTO_REFRESH_MS)
            currentSilentRefresh()
        }
    }

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
            modifier = Modifier.padding(bottom = 8.dp)
        )

        ExerciseFilterRow(
            selected = state.exerciseFilter,
            onSelect = onExerciseFilterChange
        )

        // La lista ocupa el espacio libre; el panel de logs queda fijo abajo de todo.
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            val visible = state.filteredSessions
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

                visible.isEmpty() -> CenteredBox {
                    Text(
                        text = "No hay sesiones de este ejercicio.",
                        textAlign = TextAlign.Center
                    )
                }

                else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(visible, key = { it.id ?: it.hashCode() }) { session -> SessionCard(session) }
                }
            }
        }

        ActivityLogPanel(
            sessions = state.sessions,
            userName = userName,
            modifier = Modifier.padding(top = 12.dp)
        )
    }
}

/** Chips para filtrar el historial por ejercicio / zona del cuerpo (cabeza, brazos, tronco). */
@Composable
private fun ExerciseFilterRow(
    selected: String?,
    onSelect: (String?) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = selected == null,
            onClick = { onSelect(null) },
            label = { Text("Todos") }
        )
        ExerciseCatalog.demo.forEach { exercise ->
            FilterChip(
                selected = selected == exercise.codigo,
                onClick = {
                    onSelect(if (selected == exercise.codigo) null else exercise.codigo)
                },
                label = { Text(exercise.zona.ifBlank { exercise.nombre }) }
            )
        }
    }
}

/**
 * Rectángulo fijo en la parte inferior con el registro de actividad:
 * "<usuario> realizó <ejercicio> el <fecha>". Muestra todas las sesiones (sin filtro).
 */
@Composable
private fun ActivityLogPanel(
    sessions: List<SessionDto>,
    userName: String,
    modifier: Modifier = Modifier
) {
    val name = userName.ifBlank { "El usuario" }
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(170.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            Text(
                text = "Registro de actividad (${sessions.size})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
            if (sessions.isEmpty()) {
                Text(
                    text = "Sin actividad registrada.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(sessions, key = { "log-" + (it.id ?: it.hashCode()) }) { session ->
                        Text(
                            text = "[${formatDate(session.fechaRegistro)}] $name realizó " +
                                "\"${session.ejercicio?.nombre ?: "un ejercicio"}\"",
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
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

/** Zona horaria en la que se muestran las fechas (igual que en la web). */
private val BOGOTA: TimeZone = TimeZone.getTimeZone("America/Bogota")

/**
 * Convierte "2026-11-10T18:05:00.000Z" (fecha_registro en UTC) a "10/11/2026 01:05 p. m."
 * en hora de Colombia.
 */
private fun formatDate(iso: String?): String {
    if (iso.isNullOrBlank()) return "Fecha desconocida"
    val patterns = listOf("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", "yyyy-MM-dd'T'HH:mm:ss'Z'")
    for (pattern in patterns) {
        try {
            val parser = SimpleDateFormat(pattern, Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
                isLenient = false
            }
            val date = parser.parse(iso) ?: continue
            return SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.forLanguageTag("es-CO")).apply {
                timeZone = BOGOTA
            }.format(date)
        } catch (e: Exception) {
            // probar el siguiente formato
        }
    }
    // Formato desconocido (p. ej. una fecha antigua guardada como texto): se muestra tal cual.
    return iso
}
