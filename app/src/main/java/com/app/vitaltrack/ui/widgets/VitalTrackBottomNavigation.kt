package com.app.vitaltrack.ui.widgets

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Timeline
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
            icon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
            label = { Text("Buscar") },
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
            icon = { Icon(Icons.Default.Person, contentDescription = "Config") },
            label = { Text("Config") },
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
    }
}
