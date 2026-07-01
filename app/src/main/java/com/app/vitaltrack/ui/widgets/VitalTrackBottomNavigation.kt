package com.app.vitaltrack.ui.widgets

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.app.vitaltrack.ui.theme.BackgroundDark
import com.app.vitaltrack.ui.theme.TealLight

@Composable
fun VitalTrackBottomNavigation(
    selectedItem: Int,
    onItemClick: (Int) -> Unit
) {
    NavigationBar(
        containerColor = BackgroundDark,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Início") },
            label = { Text("Início") },
            selected = selectedItem == 0,
            onClick = { onItemClick(0) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = TealLight,
                selectedTextColor = TealLight,
                unselectedIconColor = Color.White.copy(alpha = 0.5f),
                unselectedTextColor = Color.White.copy(alpha = 0.5f),
                indicatorColor = Color.Transparent
            )
        )

        NavigationBarItem(
            icon = { Icon(Icons.Default.FitnessCenter, contentDescription = "Exercícios") },
            label = { Text("Exercícios") },
            selected = selectedItem == 1,
            onClick = { onItemClick(1) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = TealLight,
                selectedTextColor = TealLight,
                unselectedIconColor = Color.White.copy(alpha = 0.5f),
                unselectedTextColor = Color.White.copy(alpha = 0.5f),
                indicatorColor = Color.Transparent
            )
        )

        NavigationBarItem(
            icon = { Icon(Icons.Default.Timeline, contentDescription = "Progresso") },
            label = { Text("Progresso") },
            selected = selectedItem == 2,
            enabled = true,
            onClick = { onItemClick(2) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = TealLight,
                selectedTextColor = TealLight,
                unselectedIconColor = Color.White.copy(alpha = 0.5f),
                unselectedTextColor = Color.White.copy(alpha = 0.5f),
                indicatorColor = Color.Transparent
            )
        )

        NavigationBarItem(
            icon = { Icon(Icons.Default.Upload, contentDescription = "Exportar para Access") },
            label = { Text("Exportar") },
            selected = selectedItem == 3,
            onClick = { onItemClick(3) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = TealLight,
                selectedTextColor = TealLight,
                unselectedIconColor = Color.White.copy(alpha = 0.5f),
                unselectedTextColor = Color.White.copy(alpha = 0.5f),
                indicatorColor = Color.Transparent
            )
        )

/*
        NavigationBarItem(
            icon = { Icon(Icons.Default.Settings, contentDescription = "Config") },
            label = { Text("Config") },
            selected = selectedItem == 4,
            enabled = false,
            onClick = { onItemClick(4) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = TealLight,
                selectedTextColor = TealLight,
                unselectedIconColor = Color.White.copy(alpha = 0.5f),
                unselectedTextColor = Color.White.copy(alpha = 0.5f),
                disabledIconColor = Color.White.copy(alpha = 0.2f),
                disabledTextColor = Color.White.copy(alpha = 0.2f),
                indicatorColor = Color.Transparent
            )
        )
*/
    }
}
