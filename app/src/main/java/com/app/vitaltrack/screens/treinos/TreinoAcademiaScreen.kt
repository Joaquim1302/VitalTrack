package com.app.vitaltrack.screens.treinos

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.vitaltrack.ui.theme.*

@Composable
fun TreinoAcademiaScreen(
    onBackClick: () -> Unit,
    viewModel: TreinoAcademiaViewModel = viewModel()
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
                    onBackClick = onBackClick
                )
            }
        ) { innerPadding ->
            if (uiState.isLoading && uiState.divisoes.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = TealLight)
                }
            } else if (uiState.isFichaVazia) {
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    // Divisões (Treino A, B, C...)
                        divisoes = uiState.divisoes,
                        selecionada = uiState.divisaoSelecionada,
                        onSelect = { viewModel.selecionarDivisao(it) }
                    )

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        items(uiState.exercicios) { exercicio ->
                        }

                        item {
                        }
                    }
                }
            }
        }

        // Botão Iniciar Treino (Fixo no Rodapé)
        if (!uiState.isFichaVazia && uiState.divisaoSelecionada != null) {
            )
        }
    }
}
