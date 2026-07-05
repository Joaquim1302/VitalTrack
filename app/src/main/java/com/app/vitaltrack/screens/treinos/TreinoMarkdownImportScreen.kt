package com.app.vitaltrack.screens.treinos

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Refresh
import android.net.Uri
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
import com.app.vitaltrack.data.markdown.TreinoExercicioMarkdownImportado
import com.app.vitaltrack.ui.theme.*

@Composable
fun TreinoMarkdownImportScreen(
    uri: Uri?,
    onBackClick: () -> Unit,
    onImportSuccess: () -> Unit,
    viewModel: TreinoMarkdownImportViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uri) {
        if (uri != null) {
            viewModel.carregarDeUri(uri)
        } else {
            viewModel.carregarTreinos()
        }
    }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.resetSnackbarMessage()
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
                ImportHeader(onBackClick = onBackClick, onRefresh = { viewModel.carregarTreinos() })
            }
        ) { innerPadding ->
            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = TealLight)
                }
            } else if (uiState.errorMessage != null) {
                ImportErrorState(message = uiState.errorMessage!!, onRetry = { viewModel.carregarTreinos() })
            } else if (uiState.treinos.isEmpty()) {
                ImportEmptyState()
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    // Seletor de Treinos (Treino A, B, C...)
                    TreinoMarkdownSelector(
                        treinos = uiState.treinos.map { it.dsDia },
                        selecionadoIndex = uiState.treinoSelecionadoIndex,
                        onSelect = { viewModel.selecionarTreino(it) }
                    )

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp)
                    ) {
                        uiState.treinoSelecionado?.let { treino ->
                            item {
                                treino.dsGrupoMuscular?.let {
                                    Text(
                                        text = it,
                                        color = TealLight,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                }
                            }

                            items(treino.exercicios) { exercicio ->
                                MarkdownExercicioCard(exercicio)
                            }
                        }
                    }
                }
            }
        }

        // Botão Usar como Ficha (Fixo no Rodapé)
        if (uiState.treinos.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 20.dp, end = 20.dp, bottom = 55.dp, top = 20.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Button(
                    onClick = { 
                        viewModel.usarComoFichaDigital()
                        onImportSuccess()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TealLight),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Description, contentDescription = null, tint = TextPrimary)
                    Spacer(Modifier.width(8.dp))
                    Text("Usar como ficha digital", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
fun ImportHeader(onBackClick: () -> Unit, onRefresh: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
            .statusBarsPadding(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier.size(40.dp).clip(CircleShape).background(CardBackground)
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = TextPrimary)
        }
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text("Importar Markdown", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text("Revisão de plano de treino", color = TextSecondary, fontSize = 12.sp)
        }
        IconButton(
            onClick = onRefresh,
            modifier = Modifier.size(40.dp).clip(CircleShape).background(CardBackground)
        ) {
            Icon(Icons.Default.Refresh, contentDescription = "Recarregar", tint = TextPrimary)
        }
    }
}

@Composable
fun TreinoMarkdownSelector(
    treinos: List<String>,
    selecionadoIndex: Int,
    onSelect: (Int) -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        itemsIndexed(treinos) { index, nome ->
            val isSelected = selecionadoIndex == index
            Surface(
                modifier = Modifier.clip(RoundedCornerShape(12.dp)).clickable { onSelect(index) },
                color = if (isSelected) TealLight else CardBackground,
                shape = RoundedCornerShape(12.dp),
                border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
            ) {
                Text(
                    text = nome,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    color = if (isSelected) TextPrimary else TextSecondary,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
fun MarkdownExercicioCard(exercicio: TreinoExercicioMarkdownImportado) {
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
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)).background(TealDark.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${exercicio.ordem}",
                    color = TealLight,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp
                )
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(exercicio.nome, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Séries: ${exercicio.series ?: "?"}", color = TextSecondary, fontSize = 13.sp)
                    Text(" • ", color = TextSecondary)
                    Text("Reps: ${exercicio.repeticoes}", color = TextSecondary, fontSize = 13.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Carga: ${exercicio.carga ?: "não definida"}", color = TextSecondary, fontSize = 13.sp)
                    Text(" • ", color = TextSecondary)
                    Text("Intervalo: ${exercicio.intervaloSegundos ?: 60}s", color = TextSecondary, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun ImportErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(40.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.Red.copy(alpha = 0.5f))
        Spacer(Modifier.height(16.dp))
        Text("Erro na leitura", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(message, color = TextSecondary, fontSize = 14.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 8.dp))
        Spacer(Modifier.height(32.dp))
        Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = TealLight)) {
            Text("Tentar novamente", color = TextPrimary)
        }
    }
}

@Composable
fun ImportEmptyState() {
    Column(
        modifier = Modifier.fillMaxSize().padding(40.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(64.dp), tint = TextSecondary.copy(alpha = 0.3f))
        Spacer(Modifier.height(16.dp))
        Text("Nenhum treino encontrado", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text("O arquivo treinamento.md parece estar vazio.", color = TextSecondary, fontSize = 14.sp, textAlign = TextAlign.Center)
    }
}
