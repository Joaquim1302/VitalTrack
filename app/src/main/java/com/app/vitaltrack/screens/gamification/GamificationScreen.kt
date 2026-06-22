package com.app.vitaltrack.screens.gamification

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import com.app.vitaltrack.data.gamification.GamificationRules
import com.app.vitaltrack.ui.theme.*
import com.app.vitaltrack.ui.widgets.AchievementBadge
import com.app.vitaltrack.ui.widgets.GamificationProgressCard

@Composable
fun GamificationScreen(
    onBackClick: () -> Unit,
    viewModel: GamificationViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

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
                GamificationHeader(onBackClick)
            }
        ) { innerPadding ->
            uiState?.let { state ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    item {
                        GamificationProgressCard(state = state)
                    }

                    item {
                        Text(
                            text = "Conquistas",
                            color = TextPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    item {
                        val achievements = GamificationRules.basicAchievements
                        
                        // We use a custom layout for achievements since LazyVerticalGrid 
                        // doesn't work well inside LazyColumn without a fixed height
                        Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                            achievements.chunked(3).forEach { rowItems ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    rowItems.forEach { achievement ->
                                        AchievementBadge(
                                            achievement = achievement,
                                            isUnlocked = state.unlockedAchievements.contains(achievement.id),
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    // Fill remaining space if row is not full
                                    if (rowItems.size < 3) {
                                        repeat(3 - rowItems.size) {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } ?: run {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = TealLight)
                }
            }
        }
    }
}

@Composable
fun GamificationHeader(onBackClick: () -> Unit) {
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
                text = "Meu Progresso",
                color = TextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Acompanhe seu nível e conquistas.",
                color = TextSecondary,
                fontSize = 12.sp
            )
        }
    }
}
