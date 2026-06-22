package com.app.vitaltrack.screens.transferencia

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.vitaltrack.screens.configuracoes.ConfiguracoesViewModel
import com.app.vitaltrack.screens.configuracoes.ExportCard
import com.app.vitaltrack.screens.configuracoes.ExportState
import com.app.vitaltrack.screens.configuracoes.ImportCard
import com.app.vitaltrack.screens.configuracoes.ImportState
import com.app.vitaltrack.ui.theme.*
import com.app.vitaltrack.ui.widgets.VitalTrackBottomNavigation

/**
 * Tela de Transferência de Dados.
 * Permite ao usuário realizar a importação e exportação de dados via JSON.
 */
@Composable
fun TransferirDadosScreen(
    onBackClick: () -> Unit,
    onNavigateToConfig: () -> Unit = {},
    onNavigateToProgresso: () -> Unit = {},
    viewModel: ConfiguracoesViewModel = viewModel()
) {
    val context = LocalContext.current
    val importState by viewModel.importState.collectAsState()
    val exportState by viewModel.exportState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Lançador para o seletor de arquivos (Importação)
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri: Uri? ->
            uri?.let { viewModel.importJson(context, it) }
        }
    )

    // Lançador para criar um novo arquivo (Exportação via SAF)
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json"),
        onResult = { uri: Uri? ->
            uri?.let { viewModel.exportJson(context, it) }
        }
    )

    // Efeito para exibir mensagens (Snackbars) baseadas nas mudanças de estado
    LaunchedEffect(importState, exportState) {
        when (val state = importState) {
            is ImportState.Success -> {
                snackbarHostState.showSnackbar(state.message)
                viewModel.resetState()
            }
            is ImportState.Error -> {
                snackbarHostState.showSnackbar(state.message)
                viewModel.resetState()
            }
            else -> {}
        }

        when (val state = exportState) {
            is ExportState.Success -> {
                snackbarHostState.showSnackbar(state.message)
                viewModel.resetState()
            }
            is ExportState.Error -> {
                snackbarHostState.showSnackbar(state.message)
                viewModel.resetState()
            }
            else -> {}
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
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TransferirDadosHeader(onBackClick)
            },
            bottomBar = {
                VitalTrackBottomNavigation(
                    selectedItem = 1,
                    onItemClick = { 
                        when (it) {
                            0 -> onBackClick()
                            1 -> {} // Já está em transferência
                            2 -> onNavigateToProgresso()
                            3 -> onNavigateToConfig()
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
                // Item de Importação
                item {
                    ImportCard(
                        isLoading = importState is ImportState.Loading,
                        onImportClick = {
                            importLauncher.launch(arrayOf("application/json", "text/*"))
                        }
                    )
                }

                // Item de Exportação
                item {
                    ExportCard(
                        isLoading = exportState is ExportState.Loading,
                        onExportClick = {
                            exportLauncher.launch("vitaltrack_export.json")
                        }
                    )
                }
            }
        }
    }
}

/**
 * Cabeçalho da tela de transferência de dados com botão de voltar.
 */
@Composable
fun TransferirDadosHeader(onBackClick: () -> Unit) {
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
                text = "Transferir Dados",
                color = TextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Importe ou exporte seus registros via JSON.",
                color = TextSecondary,
                fontSize = 12.sp
            )
        }
    }
}
