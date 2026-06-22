package com.app.vitaltrack.screens.configuracoes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FileUpload
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
import com.app.vitaltrack.ui.theme.*
import com.app.vitaltrack.ui.widgets.VitalTrackBottomNavigation

/**
 * Tela de Configurações do aplicativo.
 */
@Composable
fun ConfiguracoesScreen(
    onBackClick: () -> Unit,
    onNavigateToExport: () -> Unit = {},
    onNavigateToProgresso: () -> Unit = {}
) {
    val snackbarHostState = remember { SnackbarHostState() }

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
                ConfiguracoesHeader(onBackClick)
            },
            bottomBar = {
                VitalTrackBottomNavigation(
                    selectedItem = 3,
                    onItemClick = { 
                        when (it) {
                            0 -> onBackClick()
                            1 -> onNavigateToExport()
                            2 -> onNavigateToProgresso()
                            3 -> {} // Já está em configurações
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
                // Conteúdo de configurações removido conforme solicitado
            }
        }
    }
}

/**
 * Cabeçalho da tela de configurações com botão de voltar.
 */
@Composable
fun ConfiguracoesHeader(onBackClick: () -> Unit) {
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
                text = "Configurações",
                color = TextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Gerencie seus dados e preferências.",
                color = TextSecondary,
                fontSize = 12.sp
            )
        }
    }
}

/**
 * Card visual para a funcionalidade de Importação de Dados.
 */
@Composable
fun ImportCard(
    isLoading: Boolean,
    onImportClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(CardBackground)
            .border(1.dp, CardBorder, RoundedCornerShape(20.dp))
            .padding(20.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Importação de dados",
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Importe os alimentos, unidades, tipos de refeição e refeições registradas a partir de um arquivo JSON exportado do Access.",
                color = TextSecondary,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Button(
                onClick = onImportClick,
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = TealLight,
                    contentColor = TextPrimary,
                    disabledContainerColor = TealLight.copy(alpha = 0.5f),
                    disabledContentColor = TextPrimary.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = TextPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Importando dados...", fontWeight = FontWeight.Bold)
                } else {
                    Icon(Icons.Default.FileUpload, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Importar JSON", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/**
 * Card visual para a funcionalidade de Exportação de Dados.
 */
@Composable
fun ExportCard(
    isLoading: Boolean,
    onExportClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(CardBackground)
            .border(1.dp, CardBorder, RoundedCornerShape(20.dp))
            .padding(20.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Exportação de dados",
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Exporte todos os seus registros de refeições em formato JSON para backup ou uso externo.",
                color = TextSecondary,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Button(
                onClick = onExportClick,
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = TealLight,
                    contentColor = TextPrimary,
                    disabledContainerColor = TealLight.copy(alpha = 0.5f),
                    disabledContentColor = TextPrimary.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = TextPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Exportando dados...", fontWeight = FontWeight.Bold)
                } else {
                    Icon(Icons.Default.FileUpload, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Exportar JSON", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
