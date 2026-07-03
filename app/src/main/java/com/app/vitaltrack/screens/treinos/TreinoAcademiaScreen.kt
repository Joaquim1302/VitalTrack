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
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.vitaltrack.screens.treinos.components.*
import com.app.vitaltrack.ui.theme.*
import com.app.vitaltrack.ui.widgets.TreinoActionsBottomNavigation

@Composable
fun TreinoAcademiaScreen(
    onBackClick: () -> Unit,
    onNavigateToExecution: (Long) -> Unit,
    onNavigateToMarkdown: (Uri?) -> Unit,
    viewModel: TreinoAcademiaViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    var sessaoConflito by remember { mutableStateOf<com.app.vitaltrack.data.entity.treinos.TreinoSessaoEntity?>(null) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            if (uri != null) {
                viewModel.processarMarkdown(uri)
            }
        }
    )

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is TreinoAcademiaEvent.NavegarParaExecucao -> {
                    onNavigateToExecution(event.cdSessao)
                }
                is TreinoAcademiaEvent.NavegarParaImportacaoMarkdown -> {
                    onNavigateToMarkdown(null)
                }
                is TreinoAcademiaEvent.MostrarDialogoConflito -> {
                    sessaoConflito = event.sessao
                }
                is TreinoAcademiaEvent.MostrarErro -> {
                    android.widget.Toast.makeText(context, event.mensagem, android.widget.Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    if (sessaoConflito != null) {
        AlertDialog(
            onDismissRequest = { sessaoConflito = null },
            title = { Text("Treino em Andamento") },
            text = { Text("Você já possui um treino em andamento. Deseja finalizar o treino anterior e iniciar este novo?") },
            confirmButton = {
                Button(
                    onClick = {
                        val idDia = uiState.divisaoSelecionada?.cdFichaDia
                        if (idDia != null) {
                            viewModel.iniciarTreino(idDia, ignorarConflito = true)
                        }
                        sessaoConflito = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TealLight)
                ) {
                    Text("Finalizar e Iniciar", color = TextPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { sessaoConflito = null }) {
                    Text("Cancelar", color = TextSecondary)
                }
            },
            containerColor = CardBackground,
            titleContentColor = TextPrimary,
            textContentColor = TextSecondary
        )
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
                TreinoFichaHeader(
                    ficha = uiState.fichaAtiva,
                    onBackClick = onBackClick
                )
            },
            bottomBar = {
                TreinoActionsBottomNavigation(
                    onIniciar = {
                        uiState.divisaoSelecionada?.cdFichaDia?.let { viewModel.iniciarTreino(it) }
                    },
                    onImportar = {
                        filePickerLauncher.launch(
                            arrayOf(
                                "text/markdown",
                                "text/x-markdown",
                                "text/plain",
                                "application/octet-stream"
                            )
                        )
                    },
                    iniciarEnabled = !uiState.isFichaVazia && uiState.divisaoSelecionada != null && !uiState.isLoading,
                    importarEnabled = !uiState.isLoading,
                    cancelarEnabled = false,
                    concluirEnabled = false
                )
            }
        ) { innerPadding ->
            if (uiState.isLoading && uiState.divisoes.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = TealLight)
                }
            } else if (uiState.isFichaVazia) {
                TreinoEmptyState(onCriarExemplo = { viewModel.criarDadosExemplo() })
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    // Seletor de treinos/divisões (Treino A, B, C...)
                    TreinoDiaSelector(
                        divisoes = uiState.divisoes,
                        selecionada = uiState.divisaoSelecionada,
                        onSelect = { viewModel.selecionarDivisao(it) }
                    )

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
                    ) {
                        items(uiState.exercicios) { exercicio ->
                            TreinoExercicioCard(exercicio)
                        }

                        item {
                            TreinoImportFichaCard(onImportMarkdown = {
                                filePickerLauncher.launch(
                                    arrayOf(
                                        "text/markdown",
                                        "text/x-markdown",
                                        "text/plain",
                                        "application/octet-stream"
                                    )
                                )
                            })
                        }
                    }
                }
            }
        }
    }
}
