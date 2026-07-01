package com.app.vitaltrack.screens.treinos

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.UploadFile
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
import com.app.vitaltrack.data.entity.treinos.TreinoFichaDiaEntity
import com.app.vitaltrack.data.entity.treinos.TreinoFichaExercicioEntity
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
                TreinoHeader(
                    title = uiState.fichaAtiva?.dsFicha ?: "Treino de Academia",
                    onBackClick = onBackClick
                )
            }
        ) { innerPadding ->
            if (uiState.isLoading && uiState.divisoes.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = TealLight)
                }
            } else if (uiState.isFichaVazia) {
                EmptyFichaState(onCriarExemplo = { viewModel.criarDadosExemplo() })
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    // Divisões (Treino A, B, C...)
                    DivisoesTabRow(
                        divisoes = uiState.divisoes,
                        selecionada = uiState.divisaoSelecionada,
                        onSelect = { viewModel.selecionarDivisao(it) }
                    )

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp)
                    ) {
                        item {
                            uiState.divisaoSelecionada?.dsGrupoMuscular?.let {
                                Text(
                                    text = it,
                                    color = TealLight,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                            }
                        }

                        items(uiState.exercicios) { exercicio ->
                            ExercicioFichaCard(exercicio)
                        }

                        item {
                            ImportFichaPlaceholder()
                        }
                    }
                }
            }
        }

        // Botão Iniciar Treino (Fixo no Rodapé)
        if (!uiState.isFichaVazia && uiState.divisaoSelecionada != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Button(
                    onClick = { /* TODO: Iniciar Sessão na Fase 3 */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TealLight),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = TextPrimary)
                    Spacer(Modifier.width(8.dp))
                    Text("Iniciar Treino", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
fun TreinoHeader(title: String, onBackClick: () -> Unit) {
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
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = TextPrimary)
        }
        Spacer(Modifier.width(16.dp))
        Column {
            Text(title, color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text("Ficha Digital", color = TextSecondary, fontSize = 12.sp)
        }
    }
}

@Composable
fun DivisoesTabRow(
    divisoes: List<TreinoFichaDiaEntity>,
    selecionada: TreinoFichaDiaEntity?,
    onSelect: (TreinoFichaDiaEntity) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(divisoes) { item ->
            val isSelected = selecionada?.cdFichaDia == item.cdFichaDia
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onSelect(item) },
                color = if (isSelected) TealLight else CardBackground,
                shape = RoundedCornerShape(12.dp),
                border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
            ) {
                Text(
                    text = item.dsDia,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    color = if (isSelected) TextPrimary else TextSecondary,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
fun ExercicioFichaCard(exercicio: TreinoFichaExercicioEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(TealDark.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = TealLight)
            }
            
            Spacer(Modifier.width(16.dp))
            
            Column(Modifier.weight(1f)) {
                Text(
                    text = exercicio.dsObs ?: "Exercício",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${exercicio.nrSeriesPlanejadas} x ${exercicio.nrRepeticoesPlanejadas}",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                    
                    exercicio.nmCargaRecomendada?.let {
                        Text(" • ", color = TextSecondary)
                        Text("${it.toInt()} kg", color = TextSecondary, fontSize = 13.sp)
                    }

                    exercicio.nrDescansoSegundos?.let {
                        Text(" • ", color = TextSecondary)
                        Text("${it}s descanso", color = TextSecondary, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyFichaState(onCriarExemplo: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(40.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(64.dp), tint = TextSecondary.copy(alpha = 0.3f))
        Spacer(Modifier.height(16.dp))
        Text("Nenhuma ficha ativa", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(
            "Você ainda não possui uma ficha de treino cadastrada para este perfil.",
            color = TextSecondary,
            fontSize = 14.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
        Spacer(Modifier.height(32.dp))
        Button(
            onClick = onCriarExemplo,
            colors = ButtonDefaults.buttonColors(containerColor = TealLight),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null, tint = TextPrimary)
            Spacer(Modifier.width(8.dp))
            Text("Criar Ficha de Exemplo", color = TextPrimary, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ImportFichaPlaceholder() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Default.UploadFile, contentDescription = null, tint = TextSecondary.copy(alpha = 0.5f))
            Spacer(Modifier.width(12.dp))
            Text("Importar ficha de papel (Futuro)", color = TextSecondary.copy(alpha = 0.5f), fontSize = 14.sp)
        }
    }
}
