package com.app.vitaltrack.screens.progresso

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.vitaltrack.screens.gamification.GamificationViewModel
import com.app.vitaltrack.ui.theme.*
import com.app.vitaltrack.ui.widgets.GamificationProgressCard
import com.app.vitaltrack.ui.widgets.VitalTrackBottomNavigation

@Composable
fun ProgressoScreen(
    onBackClick: () -> Unit,
    onNavigateToExercicios: () -> Unit,
    onNavigateToConfig: () -> Unit,
    onNavigateToExport: () -> Unit,
    onNavigateToGamification: () -> Unit,
    viewModel: GamificationViewModel = viewModel()
) {
    val gamificationState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(GradientTop, GradientBottom)
                )
            )
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                ProgressoHeader(onBackClick)
            },
            bottomBar = {
                VitalTrackBottomNavigation(
                    selectedItem = 2,
                    onItemClick = { index ->
                        when (index) {
                            0 -> onBackClick()
                            1 -> onNavigateToExercicios()
                            2 -> {} // Já está em progresso
                            3 -> onNavigateToExport()
                            4 -> onNavigateToConfig()
                        }
                    }
                )
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {
                item {
                    Text(
                        text = "Seu progresso",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                gamificationState?.let { state ->
                    item {
                        GamificationProgressCard(
                            state = state,
                            showDetails = true,
                            onClick = onNavigateToGamification
                        )
                    }
                } ?: item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = TealLight)
                    }
                }
            }
        }
    }
}

@Composable
fun ProgressoHeader(onBackClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
            .statusBarsPadding(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(CardBackground)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Voltar",
                tint = TextPrimary
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column {
            Text(
                text = "Progresso",
                color = TextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Veja sua evolução e conquistas.",
                color = TextSecondary,
                fontSize = 12.sp
            )
        }
    }
}
