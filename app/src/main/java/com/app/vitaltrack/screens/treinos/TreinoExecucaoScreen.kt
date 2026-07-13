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
import androidx.compose.ui.text.style.TextAlign
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
import com.app.vitaltrack.ui.widgets.TreinoActionsBottomNavigation

@Composable
fun TreinoExecucaoScreen(
    cdSessao: Long,
    onFinish: () -> Unit,
    onBack: () -> Unit,
    viewModel: TreinoExecucaoViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

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
                is TreinoExecucaoEvent.NotificarGamificacao -> {
                    event.result.snackbarMessage?.let { msg ->
                        snackbarHostState.showSnackbar(msg)
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
            snackbarHost = {
                SnackbarHost(snackbarHostState) { data ->
                    Snackbar(
                        containerColor = TealLight,
                        contentColor = TextPrimary,
                        snackbarData = data
                    )
                }
            },
            topBar = {
                TreinoExecucaoHeader(
                    diaNome = uiState.dia?.dsDia ?: "Treino",
                    diaGrupo = uiState.dia?.dsGrupoMuscular,
                    onClose = { 
                        android.widget.Toast.makeText(context, "Treino mantido em andamento.", android.widget.Toast.LENGTH_SHORT).show()
                        onBack() 
                    }
                )
            },
            bottomBar = {
                TreinoActionsBottomNavigation(
                    onCancelar = { viewModel.onCancelarClick() },
                    onConcluir = { viewModel.onConcluirClick() },
                    iniciarEnabled = false,
                    importarEnabled = false,
                    cancelarEnabled = !uiState.isLoading,
                    concluirEnabled = !uiState.isLoading
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
                        seriesStatus = "${uiState.seriesConcluidas} de ${uiState.totalSeries} séries",
                        intervaloProgresso = if (uiState.restTotalSeconds > 0) 
                            (uiState.restTotalSeconds - uiState.restRemainingSeconds) / uiState.restTotalSeconds.toFloat() 
                            else 0f,
                        exibirIntervalo = uiState.isRestTimerRunning
                    )

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                            .padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
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

        // Overlay do Cronômetro de Descanso
        if (uiState.isRestTimerVisible) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .clickable { viewModel.hideRestTimer() }, // Permite fechar ao tocar fora se o timer acabou
                contentAlignment = Alignment.Center
            ) {
                /*
                com.app.vitaltrack.screens.treinos.components.RestTimerComponent(
                    remainingSeconds = uiState.restRemainingSeconds,
                    isRunning = uiState.isRestTimerRunning,
                    onSkip = { viewModel.skipRestTimer() },
                    onAddThirtySeconds = { viewModel.addThirtySecondsToRestTimer() }
                )
                */

                // Mensagem informativa do descanso iniciado
                Box(
                    modifier = Modifier
                        .padding(horizontal = 40.dp)
                        .background(BackgroundDark.copy(alpha = 0.9f), RoundedCornerShape(16.dp))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Descanso de ${uiState.restTotalSeconds} segundos iniciado.",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center
                    )
                }

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

    if (uiState.showCancelarDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissCancelarDialog() },
            title = { Text("Cancelar Treino") },
            text = { Text("Deseja cancelar este treino? O treino ficará registrado como CANCELADO.") },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmarCancelamento() }) {
                    Text("Cancelar Treino", color = TealLight)
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
    // Horário de início da sessão de treino
    inicio: String,

    // Duração atual do treino, geralmente calculada em tempo real
    duracao: String,

    // Valor do progresso da sessão.
    // Deve estar entre 0f e 1f.
    // Exemplo: 0.5f representa 50% de progresso.
    progresso: Float,

    // Texto exibido no card indicando o status das séries.
    // Exemplo: "0 de 27 séries"
    seriesStatus: String,

    // Progresso do intervalo de descanso (0f a 1f)
    intervaloProgresso: Float = 0f,

    // Define se a barra de intervalo deve ser exibida
    exibirIntervalo: Boolean = false
) {
    // Card principal que agrupa as informações da sessão
    Card(
        modifier = Modifier
            // Ocupa toda a largura disponível
            .fillMaxWidth()

            // Aplica margem horizontal externa ao card
            .padding(horizontal = 20.dp),

        // Define a cor de fundo do card
        colors = CardDefaults.cardColors(containerColor = CardBackground),

        // Define cantos arredondados
        shape = RoundedCornerShape(16.dp),

        // Define a borda do card
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = CardBorder
        )
    ) {
        // Coluna principal interna do card.
        // Organiza o conteúdo verticalmente:
        // 1. linha com início, duração e progresso
        // 2. barra de progresso
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Linha horizontal com os três blocos de informação
            Row(
                modifier = Modifier.fillMaxWidth(),

                // Distribui os elementos com espaço entre eles
                horizontalArrangement = Arrangement.SpaceBetween,

                // Centraliza verticalmente os itens da linha
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Bloco 1: horário de início do treino
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Título do campo
                    Text(
                        text = "Início",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )

                    // Valor do horário de início
                    Text(
                        text = inicio,
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Divisor vertical entre "Início" e "Duração"
                VerticalDivider(
                    modifier = Modifier.height(30.dp),
                    thickness = 1.dp,
                    color = CardBorder
                )

                // Bloco 2: duração do treino
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Linha com ícone de cronômetro e texto "Duração"
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Ícone de cronômetro
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = TealLight,
                            modifier = Modifier.size(14.dp)
                        )

                        // Espaço entre o ícone e o texto
                        Spacer(
                            modifier = Modifier.width(4.dp)
                        )

                        // Título do campo
                        Text(
                            text = "Duração",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }

                    // Valor da duração do treino
                    Text(
                        text = duracao,
                        color = TealLight,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Divisor vertical entre "Duração" e "Progresso"
                VerticalDivider(
                    modifier = Modifier.height(30.dp),
                    thickness = 1.dp,
                    color = CardBorder
                )

                // Bloco 3: progresso das séries
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Título do campo
                    Text(
                        text = "Progresso",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )

                    // Texto com o número de séries concluídas em relação ao total
                    Text(
                        text = seriesStatus,
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Espaço entre a linha de informações e a barra de progresso
            Spacer(
                modifier = Modifier.height(16.dp)
            )

            // Barra de progresso inferior do card
            LinearProgressIndicator(
                // Progresso atual.
                // O valor esperado é de 0f a 1f.
                progress = { progresso },

                modifier = Modifier
                    // Ocupa toda a largura do card
                    .fillMaxWidth()

                    // Define a altura da barra
                    .height(8.dp)

                    // Deixa a barra com extremidades arredondadas
                    .clip(CircleShape),

                // Cor da parte preenchida da barra
                color = TealLight,

                // Cor do trilho/fundo da barra
                trackColor = CardBorder
            )

            // Exibe a barra de intervalo apenas quando o descanso está ativo
            if (exibirIntervalo) {
                // Espaço entre a barra de progresso e a barra de intervalo
                Spacer(
                    modifier = Modifier.height(16.dp)
                )

                // Barra de intervalo inferior do card (LinearIntervalIndicator)
                LinearProgressIndicator(
                    // Intervalo atual.
                    // O valor esperado é de 0f a 1f.
                    progress = { intervaloProgresso },

                    modifier = Modifier
                        // Ocupa toda a largura do card
                        .fillMaxWidth()

                        // Define a altura da barra
                        .height(8.dp)

                        // Deixa a barra com extremidades arredondadas
                        .clip(CircleShape),

                    // Cor da parte preenchida da barra (usando GreenLight para diferenciar)
                    color = GreenLight,

                    // Cor do trilho/fundo da barra
                    trackColor = CardBorder
                )
            }


        }
    }
}
