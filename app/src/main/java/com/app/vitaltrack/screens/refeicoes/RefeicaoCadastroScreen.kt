package com.app.vitaltrack.screens.refeicoes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
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
import com.app.vitaltrack.data.dao.AlimentoDisponivel
import com.app.vitaltrack.data.dao.RefeicaoItemComDescricao
import com.app.vitaltrack.data.entity.AlimentoEntity
import com.app.vitaltrack.data.entity.RefeicaoFavoritaEntity
import com.app.vitaltrack.data.entity.RefeicaoItemEntity
import com.app.vitaltrack.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RefeicaoCadastroScreen(
    date: String,
    typeId: Int,
    onBackClick: () -> Unit,
    viewModel: RefeicaoCadastroViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showFavoriteDialog by remember { mutableStateOf(false) }
    var showCopyConfirmDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.init(date, typeId)
    }

    LaunchedEffect(uiState.successMessage, uiState.errorMessage) {
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(GradientTop, GradientBottom)))
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                HeaderSection("${uiState.mealEmoji} ${uiState.mealName}", onBackClick)
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                val abas = AbaAdicionarAlimento.entries
                SecondaryScrollableTabRow(
                    selectedTabIndex = abas.indexOf(uiState.abaSelecionada),
                    containerColor = Color.Transparent,
                    contentColor = TealLight,
                    edgePadding = 20.dp,
                    divider = {},
                    indicator = {
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(selectedTabIndex = abas.indexOf(uiState.abaSelecionada)),
                            color = TealLight
                        )
                    }
                ) {
                    abas.forEach { aba ->
                        val titulo = when (aba) {
                            AbaAdicionarAlimento.SELECIONADOS -> "Selecionados"
                            AbaAdicionarAlimento.ALIMENTOS -> "Alimentos"
                            AbaAdicionarAlimento.RECENTES -> "Consumidos Recentemente"
                            AbaAdicionarAlimento.MAIS_CONSUMIDOS -> "Mais Consumidos"
                            AbaAdicionarAlimento.REFEICOES_SALVAS -> "Refeições Salvas"
                        }
                        Tab(
                            selected = uiState.abaSelecionada == aba,
                            onClick = { viewModel.selecionarAba(aba) },
                            text = {
                                Text(
                                    text = titulo,
                                    fontSize = 14.sp,
                                    fontWeight = if (uiState.abaSelecionada == aba) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            selectedContentColor = TealLight,
                            unselectedContentColor = TextSecondary
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    when (uiState.abaSelecionada) {
                        AbaAdicionarAlimento.SELECIONADOS -> {
                            if (uiState.alimentosSelecionados.isEmpty()) {
                                EmptyMessage("Nenhum alimento selecionado.\nAdicione alimentos pela aba Alimentos.")
                            } else {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                                    shape = RoundedCornerShape(12.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                                ) {
                                    LazyColumn(modifier = Modifier.padding(vertical = 4.dp)) {
                                        items(uiState.alimentosSelecionados) { item ->
                                            CurrentItemRow(
                                                item = item,
                                                onDelete = { viewModel.solicitarRemocaoAlimento(item) },
                                                onEdit = { viewModel.selecionarAlimentoParaEditar(item) }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        AbaAdicionarAlimento.ALIMENTOS -> {
                            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                OutlinedTextField(
                                    value = searchQuery,
                                    onValueChange = { 
                                        searchQuery = it
                                        viewModel.onSearchQueryChange(it)
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    placeholder = { Text("Pesquisar alimento", color = TextSecondary) },
                                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TealLight) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = TealLight,
                                        unfocusedBorderColor = CardBorder,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary,
                                        cursorColor = TealLight
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    if (uiState.availableFoods.isEmpty()) {
                                        item { 
                                            Box(
                                                modifier = Modifier.fillParentMaxSize(),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "Nenhum alimento encontrado",
                                                    color = TextSecondary,
                                                    fontSize = 16.sp
                                                )
                                            }
                                        }
                                    } else {
                                        items(uiState.availableFoods) { alimento ->
                                            val isSelected = viewModel.isAlimentoSelecionado(alimento.cdAlimento)
                                            AlimentoDisponivelCard(
                                                alimento = alimento,
                                                isSelected = isSelected,
                                                onSelect = { 
                                                    if (isSelected) {
                                                        // Se já está selecionado, busca o item na lista para editar
                                                        val item = uiState.alimentosSelecionados.find { it.cdAlimento == alimento.cdAlimento }
                                                        item?.let { viewModel.selecionarAlimentoParaEditar(it) }
                                                    } else {
                                                        viewModel.selecionarAlimentoParaAdicionar(alimento)
                                                    }
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        AbaAdicionarAlimento.RECENTES -> Text("Aba Recentes", color = TextPrimary)
                        AbaAdicionarAlimento.MAIS_CONSUMIDOS -> Text("Aba Mais Consumidos", color = TextPrimary)
                        AbaAdicionarAlimento.REFEICOES_SALVAS -> Text("Aba Refeições Salvas", color = TextPrimary)
                    }
                }
            }
        }

        if (uiState.alimentoParaRemover != null) {
            AlertDialog(
                onDismissRequest = { viewModel.cancelarRemocaoAlimento() },
                title = { Text("Remover alimento?") },
                text = { 
                    Text("Deseja remover \"${uiState.alimentoParaRemover?.dsAlimento}\" da refeição?") 
                },
                confirmButton = {
                    TextButton(onClick = { viewModel.confirmarRemocaoAlimento() }) {
                        Text("Remover", color = Color.Red)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.cancelarRemocaoAlimento() }) {
                        Text("Cancelar", color = TextSecondary)
                    }
                },
                containerColor = BackgroundDark,
                titleContentColor = TextPrimary,
                textContentColor = TextSecondary
            )
        }

        if (showFavoriteDialog) {
            SaveFavoriteDialog(
                onDismiss = { showFavoriteDialog = false },
                onConfirm = { name ->
                    viewModel.saveAsFavorite(name)
                    showFavoriteDialog = false
                }
            )
        }

        if (uiState.alimentoDisponivelSelecionado != null) {
            val alimento = uiState.alimentoDisponivelSelecionado!!
            FoodQuantityDialog(
                alimentoNome = alimento.dsAlimento ?: "",
                baseCalories = alimento.nmCal ?: 0.0,
                baseProt = alimento.nmProt ?: 0.0,
                baseCarb = alimento.nmCarb ?: 0.0,
                baseGord = alimento.nmGord ?: 0.0,
                baseQuantity = alimento.nmQntBase?.toDouble() ?: 100.0,
                onSalvar = { qty, unit -> viewModel.addItem(alimento.cdAlimento, qty, unit) },
                onCancelar = { viewModel.cancelarAdicaoAlimento() }
            )
        }

        if (uiState.alimentoParaEditar != null) {
            val item = uiState.alimentoParaEditar!!
            FoodQuantityDialog(
                alimentoNome = item.dsAlimento ?: "",
                baseCalories = item.nmCal ?: 0.0,
                baseProt = item.nmProt ?: 0.0,
                baseCarb = item.nmCarb ?: 0.0,
                baseGord = item.nmGord ?: 0.0,
                baseQuantity = item.nmQntBase?.toDouble() ?: 100.0,
                initialQuantity = item.nmQnt ?: 100.0,
                initialUnit = item.dsUnidade ?: "g",
                onSalvar = { qty, unit -> viewModel.updateItem(item.cdAlimento, qty, unit) },
                onCancelar = { viewModel.cancelarEdicaoAlimento() }
            )
        }

        if (showCopyConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showCopyConfirmDialog = false },
                title = { Text("Copiar refeição anterior") },
                text = { Text("Deseja copiar os alimentos da última refeição deste tipo?") },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.copyPreviousMeal()
                        showCopyConfirmDialog = false
                    }) {
                        Text("Copiar", color = TealLight)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCopyConfirmDialog = false }) {
                        Text("Cancelar", color = TextSecondary)
                    }
                },
                containerColor = BackgroundDark,
                titleContentColor = TextPrimary,
                textContentColor = TextSecondary
            )
        }
    }
}

@Composable
fun HeaderSection(title: String, onBackClick: () -> Unit) {
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
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = TextPrimary)
        }
        Spacer(Modifier.width(16.dp))
        Column {
            Text(title, color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text("Adicionar Alimentos", color = TextSecondary, fontSize = 12.sp)
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        color = TextPrimary,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Composable
fun EmptyMessage(message: String) {
    Text(message, color = TextSecondary, fontSize = 14.sp, modifier = Modifier.padding(vertical = 8.dp))
}

@Composable
fun CurrentItemRow(
    item: RefeicaoItemComDescricao,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    val calorias = ((item.nmQnt ?: 0.0) / (item.nmQntBase?.toDouble() ?: 1.0)) * (item.nmCal ?: 0.0)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() }
            .padding(vertical = 8.dp)
            .padding(start = 12.dp, end = 0.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = item.dsAlimento ?: "Alimento desconhecido",
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Row {
                Text(
                    text = "${item.nmQnt?.toInt() ?: 0} ${item.dsUnidade ?: ""}",
                    color = TextSecondary,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "•  ${calorias.toInt()} kcal",
                    color = TextSecondary,
                    fontSize = 14.sp
                )
            }
        }

        IconButton(
            onClick = onDelete,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Remover",
                tint = TealLight,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun AlimentoDisponivelCard(
    alimento: AlimentoDisponivel,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) TealLight.copy(alpha = 0.15f) else CardBackground
        ),
        shape = RoundedCornerShape(12.dp),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, TealLight.copy(alpha = 0.5f)) else null
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onSelect() },
                colors = CheckboxDefaults.colors(
                    checkedColor = TealLight,
                    uncheckedColor = CardBorder,
                    checkmarkColor = TextPrimary
                )
            )
            
            Spacer(Modifier.width(8.dp))
            
            Column(Modifier.weight(1f)) {
                Text(
                    text = alimento.dsAlimento ?: "",
                    color = TextPrimary,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val infoNutricional = listOfNotNull(
                        alimento.nmCal?.let { "${it.toInt()} kcal" },
                        alimento.nmProt?.let { "P: ${it.toInt()}g" },
                        alimento.nmCarb?.let { "C: ${it.toInt()}g" },
                        alimento.nmGord?.let { "G: ${it.toInt()}g" }
                    ).joinToString("  •  ")
                    
                    Text(
                        text = infoNutricional,
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }
            
            Icon(
                imageVector = if (isSelected) Icons.Default.Edit else Icons.Default.Add,
                contentDescription = null,
                tint = TealLight
            )
        }
    }
}

@Composable
fun MostUsedCard(food: com.app.vitaltrack.data.dao.MostUsedFood, onAdd: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onAdd() },
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(food.dsAlimento ?: "Alimento", color = TextPrimary)
                Text("${food.totalUsos} usos", color = TextSecondary, fontSize = 12.sp)
            }
            Icon(Icons.Default.AddCircle, contentDescription = null, tint = TealLight)
        }
    }
}

@Composable
fun FavoriteMealCard(favorite: RefeicaoFavoritaEntity, onImport: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onImport() },
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFD700))
            Spacer(Modifier.width(12.dp))
            Text(favorite.dsFavorita, color = TextPrimary, modifier = Modifier.weight(1f))
            Icon(Icons.Default.FileDownload, contentDescription = null, tint = TealLight)
        }
    }
}

@Composable
fun SaveFavoriteDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Salvar Favorita") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nome da refeição") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                )
            )
        },
        confirmButton = {
            TextButton(onClick = { if (name.isNotBlank()) onConfirm(name) }) {
                Text("Salvar", color = TealLight)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = TextSecondary)
            }
        },
        containerColor = BackgroundDark,
        titleContentColor = TextPrimary,
        textContentColor = TextSecondary
    )
}
