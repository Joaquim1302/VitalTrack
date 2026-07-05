package com.app.vitaltrack.screens.refeicoes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.util.Locale
import com.app.vitaltrack.data.dao.AlimentoDisponivel
import com.app.vitaltrack.data.dao.MostUsedFood
import com.app.vitaltrack.data.dao.RecentFood
import com.app.vitaltrack.data.dao.RefeicaoItemComDescricao
import com.app.vitaltrack.data.entity.AlimentoEntity
import com.app.vitaltrack.data.entity.RefeicaoFavoritaEntity
import com.app.vitaltrack.data.entity.RefeicaoItemEntity
import com.app.vitaltrack.ui.theme.*
import com.app.vitaltrack.utils.normalizeSearch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RefeicaoCadastroScreen(
    date: String,
    typeId: Int,
    onBackClick: () -> Unit,
    viewModel: RefeicaoCadastroViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showCopyConfirmDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val listStateSelecionados = rememberLazyListState()
    val listStateAlimentos = rememberLazyListState()
    val listStateRecentes = rememberLazyListState()
    val listStateMaisConsumidos = rememberLazyListState()
    val listStateRefeicoesSalvas = rememberLazyListState()

    val snackbarHostState = remember { SnackbarHostState() }

    val filteredAvailableFoods by remember(searchQuery, uiState.allAvailableFoods) {
        derivedStateOf {
            val query = searchQuery.normalizeSearch()
            if (query.isEmpty()) {
                uiState.allAvailableFoods
            } else {
                uiState.allAvailableFoods.filter {
                    it.dsNormalized.contains(query)
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.init(date, typeId)
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is RefeicaoCadastroEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(GradientTop, GradientBottom)))
    ) {
        val totalCalories = uiState.alimentosSelecionados.sumOf {
            ((it.nmQnt ?: 0.0) / (it.nmQntBase?.toDouble() ?: 1.0)) * (it.nmCal ?: 0.0)
        }

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
                HeaderSection(
                    title = "${uiState.mealEmoji} ${uiState.mealName}",
                    totalCalories = totalCalories.toInt(),
                    onBackClick = onBackClick
                )
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
                            AbaAdicionarAlimento.SELECIONADOS -> {
                                val count = uiState.alimentosSelecionados.size
                                if (count > 0) "Selecionados ($count)" else "Selecionados"
                            }
                            AbaAdicionarAlimento.ALIMENTOS -> "Disponíveis"
                            AbaAdicionarAlimento.RECENTES -> "Recentes"
                            AbaAdicionarAlimento.MAIS_CONSUMIDOS -> "+ Consumidos"
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
                                EmptyState(
                                    icon = Icons.Default.Info,
                                    message = "Nenhum alimento selecionado.\nAdicione alimentos pela aba Alimentos."
                                )
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = CardBackground),
                                        shape = RoundedCornerShape(12.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                                    ) {
                                        LazyColumn(
                                            state = listStateSelecionados,
                                            modifier = Modifier.padding(vertical = 4.dp)
                                        ) {
                                            items(uiState.alimentosSelecionados) { item ->
                                                CurrentItemRow(
                                                    item = item,
                                                    onDelete = { viewModel.solicitarRemocaoAlimento(item) },
                                                    onEdit = { viewModel.selecionarAlimentoParaEditar(item) }
                                                )
                                            }
                                        }
                                    }

                                    Button(
                                        onClick = { viewModel.abrirDialogNovaRefeicaoSalva() },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = TealLight.copy(alpha = 0.8f),
                                            contentColor = TextPrimary
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(Icons.Default.Save, contentDescription = null)
                                        Spacer(Modifier.width(8.dp))
                                        Text("Nova Refeição Salva", fontWeight = FontWeight.Bold)
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

                                if (filteredAvailableFoods.isEmpty()) {
                                    EmptyState(
                                        icon = Icons.Default.Search,
                                        message = "Nenhum alimento encontrado"
                                    )
                                } else {
                                    LazyColumn(
                                        state = listStateAlimentos,
                                        verticalArrangement = Arrangement.spacedBy(12.dp),
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        items(filteredAvailableFoods) { alimento ->
                                            val isSelected = viewModel.isAlimentoSelecionado(alimento.cdAlimento)
                                            AlimentoDisponivelCard(
                                                alimento = alimento,
                                                isSelected = isSelected,
                                                onSelect = {
                                                    if (isSelected) {
                                                        // Se já está selecionado, busca o item na lista para editar
                                                        val item = uiState.alimentosSelecionados.find { it.cdAlimento == alimento.cdAlimento }
                                                        item?.let { viewModel.selecionarAlimentoParaEditar(item) }
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
                        AbaAdicionarAlimento.RECENTES -> {
                            if (uiState.consumidosRecentemente.isEmpty()) {
                                EmptyState(
                                    icon = Icons.Default.History,
                                    message = "Nenhum alimento consumido recentemente"
                                )
                            } else {
                                val agrupados = uiState.consumidosRecentemente.groupBy { it.dsRefeicaoTp ?: "OUTROS" }
                                LazyColumn(
                                    state = listStateRecentes,
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    agrupados.forEach { (refeicao, lista) ->
                                        item {
                                            Text(
                                                text = refeicao.uppercase(),
                                                color = TealLight,
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 12.sp,
                                                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                                            )
                                        }
                                        items(lista) { alimento ->
                                            AlimentoRecentCard(
                                                alimento = alimento,
                                                onSelect = { viewModel.selecionarAlimentoRecentParaAdicionar(alimento) }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        AbaAdicionarAlimento.MAIS_CONSUMIDOS -> {
                            if (uiState.maisConsumidos.isEmpty()) {
                                EmptyState(
                                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                                    message = "Nenhum alimento frequente encontrado"
                                )
                            } else {
                                LazyColumn(
                                    state = listStateMaisConsumidos,
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    items(uiState.maisConsumidos) { food ->
                                        MostUsedFoodCard(
                                            food = food,
                                            onSelect = { viewModel.selecionarAlimentoMaisConsumidoParaAdicionar(food) }
                                        )
                                    }
                                }
                            }
                        }
                        AbaAdicionarAlimento.REFEICOES_SALVAS -> {
                            if (uiState.refeicoesSalvas.isEmpty()) {
                                EmptyState(
                                    icon = Icons.Default.Save,
                                    message = "Nenhuma refeição salva"
                                )
                            } else {
                                LazyColumn(
                                    state = listStateRefeicoesSalvas,
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    items(uiState.refeicoesSalvas) { template ->
                                        RefeicaoSalvaCard(
                                            template = template,
                                            isSelected = uiState.refeicaoSalvaSelecionadaId == template.id,
                                            onClick = { viewModel.selecionarRefeicaoSalva(template.id) },
                                            onAdd = { viewModel.adicionarRefeicaoSalva(template.id) },
                                            onDelete = { viewModel.excluirRefeicaoSalva(template.id) }
                                        )
                                    }
                                }
                            }
                        }
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

        if (uiState.exibindoDialogNovaRefeicaoSalva) {
            SaveMealDialog(
                name = uiState.nomeNovaRefeicaoSalva,
                onNameChange = { viewModel.atualizarNomeNovaRefeicaoSalva(it) },
                error = uiState.erroNovaRefeicaoSalva,
                isSaving = uiState.salvandoNovaRefeicaoSalva,
                onDismiss = { viewModel.fecharDialogNovaRefeicaoSalva() },
                onConfirm = { viewModel.salvarNovaRefeicaoSalva() }
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
                initialQuantity = alimento.nmUltimaQnt ?: alimento.nmQntBase?.toDouble() ?: 100.0,
                onSalvar = { qty, unit -> viewModel.addItem(alimento.cdAlimento, qty, unit) },
                onCancelar = { viewModel.cancelarAdicaoAlimento() }
            )
        }

        if (uiState.alimentoRecentSelecionado != null) {
            val alimento = uiState.alimentoRecentSelecionado!!
            FoodQuantityDialog(
                alimentoNome = alimento.dsAlimento ?: "",
                baseCalories = alimento.nmCal ?: 0.0,
                baseProt = alimento.nmProt ?: 0.0,
                baseCarb = alimento.nmCarb ?: 0.0,
                baseGord = alimento.nmGord ?: 0.0,
                baseQuantity = alimento.nmQntBase?.toDouble() ?: 100.0,
                initialQuantity = alimento.nmQnt ?: 100.0,
                initialUnit = alimento.dsUnidade ?: "g",
                onSalvar = { qty, unit -> viewModel.addItem(alimento.cdAlimento, qty, unit) },
                onCancelar = { viewModel.cancelarAdicaoAlimento() }
            )
        }

        if (uiState.alimentoMaisConsumidoSelecionado != null) {
            val food = uiState.alimentoMaisConsumidoSelecionado!!
            FoodQuantityDialog(
                alimentoNome = food.dsAlimento ?: "",
                baseCalories = food.nmCal ?: 0.0,
                baseProt = food.nmProt ?: 0.0,
                baseCarb = food.nmCarb ?: 0.0,
                baseGord = food.nmGord ?: 0.0,
                baseQuantity = food.nmQntBase?.toDouble() ?: 100.0,
                initialQuantity = food.nmQntBase?.toDouble() ?: 100.0,
                initialUnit = food.dsUnidade ?: "g",
                onSalvar = { qty, unit -> viewModel.addItem(food.cdAlimento, qty, unit) },
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
fun HeaderSection(title: String, totalCalories: Int, onBackClick: () -> Unit) {
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
        Column(Modifier.weight(1f)) {
            Text(title, color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text("Adicionar Alimentos", color = TextSecondary, fontSize = 12.sp)
        }

        if (totalCalories > 0) {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$totalCalories",
                    color = TealLight,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "kcal total",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
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
fun EmptyState(
    icon: ImageVector,
    message: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = TextSecondary.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            color = TextSecondary,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}

@Composable
fun CurrentItemRow(
    item: RefeicaoItemComDescricao,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    val qnt = item.nmQnt ?: 0.0
    val calorias = (qnt / (item.nmQntBase?.toDouble() ?: 1.0)) * (item.nmCal ?: 0.0)

    val formattedQnt = if (qnt % 1.0 == 0.0) {
        qnt.toInt().toString()
    } else {
        String.format(Locale.forLanguageTag("pt-BR"), "%.2f", qnt)
    }

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
                text = buildAnnotatedString {
                    append(item.dsAlimento ?: "Alimento desconhecido")
                    append(" ")
                    withStyle(style = SpanStyle(color = TextSecondary, fontWeight = FontWeight.Bold)) {
                        append(" • [$formattedQnt] • ${calorias.toInt()} kcal")
                    }
                },
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
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
fun MostUsedFoodCard(
    food: MostUsedFood,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, CardBorder)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = food.dsAlimento ?: "",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                Text(
                    text = "${food.nmCal?.toInt() ?: 0} kcal / ${food.nmQntBase ?: 100} ${food.dsUnidade ?: ""}",
                    color = TextSecondary,
                    fontSize = 13.sp
                )

                Text(
                    text = "Usado ${food.totalUsos} vezes",
                    color = TealLight,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                contentDescription = null,
                tint = TealLight,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun AlimentoRecentCard(
    alimento: RecentFood,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, CardBorder)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = alimento.dsAlimento ?: "",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${alimento.nmQnt?.toInt() ?: 0} ${alimento.dsUnidade ?: ""}  •  ${alimento.nmCal?.toInt() ?: 0} kcal",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                }

                val dataFormatada = try {
                    val dateString = if (alimento.dtConsumo.contains("T")) {
                        alimento.dtConsumo.split("T")[0]
                    } else {
                        alimento.dtConsumo
                    }
                    val data = java.time.LocalDate.parse(dateString)
                    val formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MMM/yy", Locale.forLanguageTag("pt-BR"))
                    data.format(formatter).replace(".", "").lowercase()
                } catch (e: Exception) {
                    alimento.dtConsumo
                }

                Text(
                    text = "Consumido em $dataFormatada",
                    color = TextSecondary.copy(alpha = 0.6f),
                    fontSize = 11.sp
                )
            }

            Icon(
                imageVector = Icons.Default.History,
                contentDescription = null,
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
                        alimento.nmCal?.let { "${it.toInt()} kcal" }
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
fun MostUsedCard(food: MostUsedFood, onAdd: () -> Unit) {
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
fun RefeicaoSalvaCard(
    template: RefeicaoSalvaUi,
    isSelected: Boolean,
    onClick: () -> Unit,
    onAdd: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) TealLight.copy(alpha = 0.15f) else CardBackground
        ),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(
            if (isSelected) 1.dp else 0.5.dp,
            if (isSelected) TealLight else CardBorder
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = template.nome,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                Text(
                    text = "${template.calorias.toInt()} kcal • ${template.quantidadeItens} alimentos",
                    color = TealLight,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            }

            Row {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Excluir", tint = Color.Red.copy(alpha = 0.6f))
                }

                FilledIconButton(
                    onClick = onAdd,
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = TealLight)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Adicionar", tint = TextPrimary)
                }
            }
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
fun SaveMealDialog(
    name: String,
    onNameChange: (String) -> Unit,
    error: String?,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nova Refeição Salva") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChange,
                    label = { Text("Nome da refeição") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = error != null,
                    enabled = !isSaving,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
                if (error != null) {
                    Text(
                        text = error,
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = !isSaving
            ) {
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = TealLight)
                } else {
                    Text("Salvar", color = TealLight)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSaving) {
                Text("Cancelar", color = TextSecondary)
            }
        },
        containerColor = BackgroundDark,
        titleContentColor = TextPrimary,
        textContentColor = TextSecondary
    )
}
