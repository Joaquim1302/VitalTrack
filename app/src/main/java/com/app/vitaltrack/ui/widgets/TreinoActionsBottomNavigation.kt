package com.app.vitaltrack.ui.widgets

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.app.vitaltrack.ui.theme.BackgroundDark
import com.app.vitaltrack.ui.theme.TealLight

/**
 * Barra de navegação inferior personalizada para ações de treino.
 * Utilizada tanto na tela de listagem/academia quanto na tela de execução.
 */
@Composable
fun TreinoActionsBottomNavigation(
    onIniciar: () -> Unit = {},
    onImportar: () -> Unit = {},
    onCancelar: () -> Unit = {},
    onConcluir: () -> Unit = {},
    iniciarEnabled: Boolean = true,
    importarEnabled: Boolean = true,
    cancelarEnabled: Boolean = true,
    concluirEnabled: Boolean = true
) {
    NavigationBar(
        containerColor = BackgroundDark,
        tonalElevation = 8.dp
    ) {
        // Ação: Iniciar Treino
        NavigationBarItem(
            icon = { Icon(Icons.Default.PlayArrow, contentDescription = "Iniciar Treino") },
            label = { Text("Iniciar") },
            selected = false,
            enabled = iniciarEnabled,
            onClick = onIniciar,
            colors = NavigationBarItemDefaults.colors(
                unselectedIconColor = TealLight,
                unselectedTextColor = TealLight,
                disabledIconColor = Color.White.copy(alpha = 0.2f),
                disabledTextColor = Color.White.copy(alpha = 0.2f),
                indicatorColor = Color.Transparent
            )
        )

        // Ação: Importar Treino (Markdown)
        NavigationBarItem(
            icon = { Icon(Icons.Default.Description, contentDescription = "Importar Treino") },
            label = { Text("Importar") },
            selected = false,
            enabled = importarEnabled,
            onClick = onImportar,
            colors = NavigationBarItemDefaults.colors(
                unselectedIconColor = TealLight,
                unselectedTextColor = TealLight,
                disabledIconColor = Color.White.copy(alpha = 0.2f),
                disabledTextColor = Color.White.copy(alpha = 0.2f),
                indicatorColor = Color.Transparent
            )
        )

        // Ação: Cancelar Treino
        NavigationBarItem(
            icon = { Icon(Icons.Default.Close, contentDescription = "Cancelar Treino") },
            label = { Text("Cancelar") },
            selected = false,
            enabled = cancelarEnabled,
            onClick = onCancelar,
            colors = NavigationBarItemDefaults.colors(
                unselectedIconColor = TealLight,
                unselectedTextColor = TealLight,
                disabledIconColor = Color.White.copy(alpha = 0.2f),
                disabledTextColor = Color.White.copy(alpha = 0.2f),
                indicatorColor = Color.Transparent
            )
        )

        // Ação: Concluir Treino
        NavigationBarItem(
            icon = { Icon(Icons.Default.Done, contentDescription = "Concluir Treino") },
            label = { Text("Concluir") },
            selected = false,
            enabled = concluirEnabled,
            onClick = onConcluir,
            colors = NavigationBarItemDefaults.colors(
                unselectedIconColor = TealLight,
                unselectedTextColor = TealLight,
                disabledIconColor = Color.White.copy(alpha = 0.2f),
                disabledTextColor = Color.White.copy(alpha = 0.2f),
                indicatorColor = Color.Transparent
            )
        )
    }
}
