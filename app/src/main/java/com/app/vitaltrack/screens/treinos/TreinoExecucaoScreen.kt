package com.app.vitaltrack.screens.treinos

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import android.os.Vibrator
import android.os.VibrationEffect
import android.media.AudioManager
import android.media.ToneGenerator
import androidx.core.content.ContextCompat
import com.app.vitaltrack.screens.treinos.components.ExerciseExecutionCard
import com.app.vitaltrack.ui.theme.*

@Composable
fun TreinoExecucaoScreen(
    cdSessao: Long,
    onFinish: () -> Unit,
    onBack: () -> Unit,
    viewModel: TreinoExecucaoViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(cdSessao) {
        viewModel.init(cdSessao)
    }

    LaunchedEffect(uiState.treinoConcluido) {
        if (uiState.treinoConcluido) {
            onFinish()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is TreinoExecucaoEvent.DescansoConcluido -> {
                    // Vibração curta e discreta
                    val vibrator = ContextCompat.getSystemService(context, Vibrator::class.java)
                    vibrator?.vibrate(VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE))

                    // Som simples (Beep curto)
                    try {
                        val tg = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100)
                        tg.startTone(ToneGenerator.TONE_PROP_BEEP)
                    } catch (e: Exception) {
                        // Silencioso se der erro ou permissão
                    }
                }
            }
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
                    onClose = { 
                        android.widget.Toast.makeText(context, "Treino mantido em andamento.", android.widget.Toast.LENGTH_SHORT).show()
                        onBack() 
                    }
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
                        duracao = uiState.duracaoFormatada,
                        progresso = uiState.progresso,
                        seriesStatus = "${uiState.seriesConcluidas} de ${uiState.totalSeries} séries"
                    )

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                            .padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp)
                    ) {
                        items(uiState.exerciciosExecucao) { ex ->
                            ExerciseExecutionCard(
                                execucao = ex,
                                onCargaChange = { nr, valStr -> viewModel.updateCarga(ex.exercicio.cdFichaExercicio, nr, valStr) },
                                onRepsChange = { nr, valStr -> viewModel.updateRepeticoes(ex.exercicio.cdFichaExercicio, nr, valStr) },
                                onAdjustCarga = { nr, delta -> viewModel.ajustarCarga(ex.exercicio.cdFichaExercicio, nr, delta) },
                                onAdjustReps = { nr, delta -> viewModel.ajustarRepeticoes(ex.exercicio.cdFichaExercicio, nr, delta) },
                                onConcluirSerie = { nr -> viewModel.concluirSerie(ex.exercicio.cdFichaExercicio, nr) }
                            )
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

        // Overlay do Cronômetro de Descanso
        if (uiState.isRestTimerVisible) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .clickable { viewModel.hideRestTimer() }, // Permite fechar ao tocar fora se o timer acabou
                contentAlignment = Alignment.Center
            ) {
                com.app.vitaltrack.screens.treinos.components.RestTimerComponent(
                    remainingSeconds = uiState.restRemainingSeconds,
                    isRunning = uiState.isRestTimerRunning,
                    onSkip = { viewModel.skipRestTimer() },
                    onAddThirtySeconds = { viewModel.addThirtySecondsToRestTimer() }
                )

                if (uiState.restFinished) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 200.dp)
                            .background(TealLight, RoundedCornerShape(20.dp))
                            .padding(horizontal = 24.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = "Descanso Concluído! Próxima série.",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
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
    duracao: String,
    progresso: Float,
    seriesStatus: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Início", color = TextSecondary, fontSize = 12.sp)
                    Text(inicio, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                
                VerticalDivider(modifier = Modifier.height(30.dp), thickness = 1.dp, color = CardBorder)
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Timer, contentDescription = null, tint = TealLight, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Duração", color = TextSecondary, fontSize = 12.sp)
                    }
                    Text(duracao, color = TealLight, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                VerticalDivider(modifier = Modifier.height(30.dp), thickness = 1.dp, color = CardBorder)

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Progresso", color = TextSecondary, fontSize = 12.sp)
                    Text(seriesStatus, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LinearProgressIndicator(
                progress = { progresso },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = TealLight,
                trackColor = CardBorder
            )
        }
    }
}
