package com.app.vitaltrack.screens.refeicoes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
    var showQuantityDialog by remember { mutableStateOf(false) }
    var selectedAlimento by remember { mutableStateOf<AlimentoDisponivel?>(null) }
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                // 1. Alimentos já adicionados
                if (uiState.currentItems.isNotEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = CardBackground),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                        ) {
                            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                uiState.currentItems.forEachIndexed { index, item ->
                                    CurrentItemRow(
                                        item = item,
                                        onDelete = { viewModel.deleteItem(item.cdAlimento) }
                                    )
                                }
                            }
                        }
                    }
                }

                // 2. Busca e Alimentos disponíveis
                item {
                    SectionTitle("Buscar alimento")
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { 
                            searchQuery = it
                            viewModel.onSearchQueryChange(it)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Nome do alimento...", color = TextSecondary) },
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
                }

                item { SectionTitle("Alimentos disponíveis") }
                if (uiState.availableFoods.isEmpty()) {
                    item { EmptyMessage("Nenhum alimento cadastrado.") }
                } else {
                    items(uiState.availableFoods) { alimento ->
                        AlimentoDisponivelCard(
                            alimento = alimento,
                            onSelect = { 
                                selectedAlimento = alimento
                                showQuantityDialog = true
                            }
                        )
                    }
                }

                // 3. Mais usados
                item { SectionTitle("Mais usados") }
                if (uiState.mostUsed.isEmpty()) {
                    item { EmptyMessage("Nenhum alimento usado recentemente.") }
                } else {
                    items(uiState.mostUsed) { food ->
                        MostUsedCard(food, onAdd = { 
                            selectedAlimento = AlimentoDisponivel(
                                cdAlimento = food.cdAlimento,
                                dsAlimento = food.dsAlimento,
                                cdUnidade = food.cdUnidade,
                                dsUnidade = food.dsUnidade,
                                nmQntBase = 100L, // Valor padrão se não tiver
                                nmCal = 0.0, nmProt = 0.0, nmCarb = 0.0, nmGord = 0.0
                            )
                            showQuantityDialog = true
                        })
                    }
                }

                // 4. Refeições favoritas
                item { SectionTitle("Refeições favoritas") }
                if (uiState.favorites.isEmpty()) {
                    item { EmptyMessage("Nenhuma refeição favorita salva.") }
                } else {
                    items(uiState.favorites) { favorite ->
                        FavoriteMealCard(favorite, onImport = { viewModel.importFavorite(favorite.cdFavorita) })
                    }
                }

                // 5. Copiar anterior
                item {
                    Button(
                        onClick = { showCopyConfirmDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = TealLight),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Copiar refeição anterior")
                    }
                }

                // 6. Botão salvar refeição atual como favorita
                if (uiState.currentItems.isNotEmpty()) {
                    item {
                        Button(
                            onClick = { showFavoriteDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = TealDark),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Favorite, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Salvar como favorita")
                        }
                    }
                }
            }
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

        if (showQuantityDialog && selectedAlimento != null) {
            QuantidadeAlimentoDialog(
                alimento = selectedAlimento!!,
                onDismiss = { showQuantityDialog = false },
                onConfirm = { quantity ->
                    viewModel.addItem(selectedAlimento!!.cdAlimento, quantity)
                    showQuantityDialog = false
                }
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
fun CurrentItemRow(item: RefeicaoItemComDescricao, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .padding(start = 12.dp, end = 0.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = item.dsAlimento ?: "Alimento desconhecido",
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            modifier = Modifier.weight(1f)
        )
        
        Text(
            text = "${item.nmQnt?.toInt() ?: 0} ${item.dsUnidade ?: ""}",
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            modifier = Modifier.padding(start = 8.dp)
        )

        IconButton(
            onClick = onDelete,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = null,
                tint = TealLight,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun AlimentoDisponivelCard(alimento: AlimentoDisponivel, onSelect: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onSelect() },
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Restaurant, contentDescription = null, tint = TealLight)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(alimento.dsAlimento ?: "", color = TextPrimary, fontWeight = FontWeight.Medium)
                if (alimento.dsUnidade != null) {
                    Text(alimento.dsUnidade, color = TextSecondary, fontSize = 12.sp)
                }
            }
            Icon(Icons.Default.Add, contentDescription = null, tint = TealLight)
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
fun QuantidadeAlimentoDialog(
    alimento: AlimentoDisponivel,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var quantity by remember { mutableStateOf(alimento.nmQntBase?.toString() ?: "1") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(alimento.dsAlimento ?: "Quantidade") },
        text = {
            Column {
                Text("Informe a quantidade (${alimento.dsUnidade ?: ""})", color = TextSecondary, fontSize = 14.sp)
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { 
                quantity.toDoubleOrNull()?.let { if (it > 0) onConfirm(it) }
            }) {
                Text("Adicionar", color = TealLight)
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
