package com.example.pausasactivas.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pausasactivas.ui.auth.AuthScreen
import com.example.pausasactivas.ui.camera.CameraScreen
import com.example.pausasactivas.ui.camera.CameraViewModel
import com.example.pausasactivas.ui.components.CameraPermissionWrapper
import com.example.pausasactivas.ui.history.HistoryScreen
import com.example.pausasactivas.ui.home.HomeScreen

/**
 * Raíz de la app: navegación simple por estado (acceso → inicio → análisis / historial).
 */
@Composable
fun PausasApp(
    appViewModel: AppViewModel = viewModel(),
    cameraViewModel: CameraViewModel = viewModel()
) {
    val screen by appViewModel.screen.collectAsState()
    val user by appViewModel.user.collectAsState()
    val auth by appViewModel.auth.collectAsState()
    val history by appViewModel.history.collectAsState()

    // Sin usuario recordado solo se puede estar en la pantalla de acceso.
    val currentScreen = if (user == null) Screen.LOGIN else screen

    when (currentScreen) {
        Screen.LOGIN -> AuthScreen(
            auth = auth,
            onLogin = appViewModel::login,
            onRegister = appViewModel::register,
            onClearError = appViewModel::clearAuthError
        )

        Screen.HOME -> HomeScreen(
            user = user,
            onStartExercise = { exercise ->
                user?.let {
                    cameraViewModel.start(exercise, it.id)
                    appViewModel.navigate(Screen.CAMERA)
                }
            },
            onOpenHistory = { appViewModel.navigate(Screen.HISTORY) },
            onLogout = appViewModel::logout
        )

        Screen.CAMERA -> CameraPermissionWrapper(
            onBack = { appViewModel.navigate(Screen.HOME) }
        ) {
            CameraScreen(
                onClose = { appViewModel.navigate(Screen.HOME) },
                viewModel = cameraViewModel
            )
        }

        Screen.HISTORY -> {
            LaunchedEffect(Unit) { appViewModel.loadHistory() }
            HistoryScreen(
                state = history,
                onBack = { appViewModel.navigate(Screen.HOME) },
                onRefresh = appViewModel::loadHistory
            )
        }
    }
}
