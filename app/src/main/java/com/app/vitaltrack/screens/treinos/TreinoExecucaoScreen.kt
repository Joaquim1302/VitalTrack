package com.app.vitaltrack.screens.treinos

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.vitaltrack.screens.treinos.components.TreinoExercicioCard
import com.app.vitaltrack.ui.theme.*

@Composable
fun TreinoExecucaoScreen(
    cdSessao: Long,
    onFinish: () -> Unit,
    viewModel: TreinoExecucaoViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(cdSessao) {
        viewModel.init(cdSessao)
    }

    LaunchedEffect(uiState.treinoConcluido) {
        if (uiState.treinoConcluido) {
            onFinish()
        }
    }

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
                TreinoExecucaoHeader(
                    diaNome = uiState.dia?.dsDia ?: "Treino",
                    diaGrupo = uiState.dia?.dsGrupoMuscular,
                    onClose = onFinish
                )
            }
        ) { innerPadding ->
            if (uiState.isLoading && uiState.sessao == null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = TealLight)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    TreinoSessaoStatusCard(
                        inicio = uiState.horarioInicioFormatado,
                        duracao = uiState.duracaoFormatada
                    )

                    Text(
                        text = "Exercícios Planejados",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
                    )

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                            .padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 100.dp)
                    ) {
                        items(uiState.exercicios) { exercicio ->
                            TreinoExercicioCard(exercicio)
                        }
                    }
                }
            }
        }

        // Botão Concluir fixo no rodapé
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Button(
                onClick = { viewModel.onConcluirClick() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TealLight),
                shape = RoundedCornerShape(16.dp),
                enabled = !uiState.isLoading
            ) {
                Icon(Icons.Default.Done, contentDescription = null, tint = TextPrimary)
                Spacer(Modifier.width(8.dp))
                Text("Concluir Treino", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }

    if (uiState.showConcluirDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissConcluirDialog() },
            title = { Text("Concluir Treino") },
            text = { Text("Deseja concluir este treino?") },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmarConclusao() }) {
                    Text("Concluir", color = TealLight)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissConcluirDialog() }) {
                    Text("Cancelar", color = TextSecondary)
                }
            },
            containerColor = BackgroundDark,
            titleContentColor = TextPrimary,
            textContentColor = TextSecondary
        )
    }

    if (uiState.showCancelarDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissCancelarDialog() },
            title = { Text("Cancelar Treino") },
            text = { Text("Deseja cancelar este treino? O treino ficará registrado como CANCELADO.") },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmarCancelamento() }) {
                    Text("Cancelar Treino", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissCancelarDialog() }) {
                    Text("Manter Treino", color = TextSecondary)
                }
            },
            containerColor = BackgroundDark,
            titleContentColor = TextPrimary,
            textContentColor = TextSecondary
        )
    }
}

@Composable
fun TreinoExecucaoHeader(
    diaNome: String,
    diaGrupo: String?,
    onClose: () -> Unit
) {
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
                text = "Executando $diaNome",
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            diaGrupo?.let {
                Text(text = it, color = TealLight, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
        }
        
        IconButton(
            onClick = onClose,
            modifier = Modifier.background(CardBackground, CircleShape)
        ) {
            Icon(Icons.Default.Close, contentDescription = "Sair", tint = TextPrimary)
        }
    }
}

@Composable
fun TreinoSessaoStatusCard(
    inicio: String,
    duracao: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Início", color = TextSecondary, fontSize = 12.sp)
                Text(inicio, color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            
            VerticalDivider(
                modifier = Modifier.height(40.dp),
                thickness = 1.dp,
                color = CardBorder
            )
            
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Timer, contentDescription = null, tint = TealLight, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Duração", color = TextSecondary, fontSize = 12.sp)
                }
                Text(duracao, color = TealLight, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
