package com.app.vitaltrack.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
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
import com.app.vitaltrack.ui.theme.*
import com.app.vitaltrack.ui.widgets.*

@Composable
fun DashboardScreen(
    onNavigateToConfig: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToMealRegistration: (String, Int) -> Unit,
    onNavigateToExport: () -> Unit = {},
    viewModel: DashboardViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }

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
                DashboardHeader(onProfileClick = onNavigateToProfile)
            },
            bottomBar = {
                VitalTrackBottomNavigation(
                    selectedItem = selectedTab,
                    onItemClick = { 
                        selectedTab = it 
                        if (it == 3) onNavigateToConfig()
                        if (it == 1) onNavigateToExport()
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
                    DateNavigationCard(
                        dateText = viewModel.getFormattedDate(),
                        onPreviousDay = { viewModel.previousDay() },
                        onNextDay = { viewModel.nextDay() }
                    )
                }

                item {
                    CalorieProgressCard(
                        consumed = uiState.totalConsumed,
                        goal = uiState.calorieGoal
                    )
                }

                item {
                    Text(
                        text = "Refeições",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(uiState.meals) { meal ->
                    MealCard(
                        emoji = meal.emoji,
                        name = meal.name,
                        calories = meal.calories,
                        onAddClick = { onNavigateToMealRegistration(viewModel.getDbDate(), meal.id.toInt()) }
                    )
                }

/*
                item {
                    Text(
                        text = "Funcionalidades recomendadas",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                item {
                    // Grid 2x2 para funcionalidades recomendadas
                    // Usando um Row com duas colunas para simular o grid dentro da LazyColumn
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            RecommendedFeatureCard(
                                title = uiState.recommendedFeatures[0].title,
                                icon = uiState.recommendedFeatures[0].icon,
                                modifier = Modifier.weight(1f)
                            )
                            RecommendedFeatureCard(
                                title = uiState.recommendedFeatures[1].title,
                                icon = uiState.recommendedFeatures[1].icon,
                                modifier = Modifier.weight(1f),
                                onClick = onNavigateToExport
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            RecommendedFeatureCard(
                                title = uiState.recommendedFeatures[2].title,
                                icon = uiState.recommendedFeatures[2].icon,
                                modifier = Modifier.weight(1f)
                            )
                            RecommendedFeatureCard(
                                title = uiState.recommendedFeatures[3].title,
                                icon = uiState.recommendedFeatures[3].icon,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
*/
            }
        }
    }
}

@Composable
fun DashboardHeader(onProfileClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
            .statusBarsPadding(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "VitalTrack",
                color = TextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Transformando hábitos em resultados.",
                color = TextSecondary,
                fontSize = 12.sp
            )
        }

        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(TealLight)
                .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                .clickable { onProfileClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Usuário",
                tint = TextPrimary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
