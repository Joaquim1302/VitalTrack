package com.app.vitaltrack.screens.perfil

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.vitaltrack.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilClienteScreen(
    onBackClick: () -> Unit,
    viewModel: PerfilClienteViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var metaText by remember(uiState.metaCalorias) { mutableStateOf(uiState.metaCalorias.toInt().toString()) }
    var expanded by remember { mutableStateOf(false) }

    val clienteSelecionado = uiState.clientes.find { it.cdCliente == uiState.clienteAtivoId }

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
                PerfilHeader(onBackClick)
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
                    Text(
                        text = "Cliente ativo",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                item {
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = clienteSelecionado?.dsNome ?: "Selecione um cliente",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Selecione o cliente ativo") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedBorderColor = TealLight,
                                unfocusedBorderColor = CardBorder,
                                cursorColor = TealLight
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                                .fillMaxWidth()
                        )

                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.background(CardBackground)
                        ) {
                            uiState.clientes.forEach { cliente ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = cliente.dsNome,
                                            color = TextPrimary
                                        )
                                    },
                                    onClick = {
                                        viewModel.selecionarCliente(cliente.cdCliente, cliente.dsNome)
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = "Meta diária de calorias",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                item {
                    OutlinedTextField(
                        value = metaText,
                        onValueChange = { 
                            metaText = it
                            it.toDoubleOrNull()?.let { meta -> viewModel.atualizarMeta(meta) }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        label = { Text("Calorias (kcal)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = TealLight,
                            unfocusedBorderColor = CardBorder,
                            cursorColor = TealLight
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
                
                if (uiState.erro != null) {
                    item {
                        Text(uiState.erro!!, color = Color.Red)
                    }
                }
            }
        }
    }
}

@Composable
fun PerfilHeader(onBackClick: () -> Unit) {
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
                text = "Perfil do Cliente",
                color = TextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Selecione o cliente ativo do aplicativo",
                color = TextSecondary,
                fontSize = 12.sp
            )
        }
    }
}
