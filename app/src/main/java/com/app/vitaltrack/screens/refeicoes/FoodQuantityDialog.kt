package com.app.vitaltrack.screens.refeicoes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale
import com.app.vitaltrack.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodQuantityDialog(
    alimentoNome: String,
    baseCalories: Double,
    baseProt: Double,
    baseCarb: Double,
    baseGord: Double,
    baseQuantity: Double,
    initialQuantity: Double = 100.0,
    initialUnit: String = "g",
    onSalvar: (Double, String) -> Unit,
    onCancelar: () -> Unit
) {
    var quantityStr by remember { mutableStateOf(initialQuantity.toString()) }
    var unit by remember { mutableStateOf(initialUnit) }
    var expanded by remember { mutableStateOf(false) }
    val units = listOf("g", "ml", "fatia", "porção")

    val quantity = quantityStr.toDoubleOrNull() ?: 0.0

    // Cálculos nutricionais
    val factor = if (baseQuantity > 0) quantity / baseQuantity else 0.0
    val calcCal = baseCalories * factor
    val calcProt = baseProt * factor
    val calcCarb = baseCarb * factor
    val calcGord = baseGord * factor

    AlertDialog(
        onDismissRequest = onCancelar,
        containerColor = BackgroundDark,
        titleContentColor = TextPrimary,
        textContentColor = TextSecondary,
        title = {
            Text(
                text = alimentoNome,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = quantityStr,
                        onValueChange = { quantityStr = it },
                        label = { Text("Quantidade") },
                        modifier = Modifier.fillMaxWidth(), // Alterado de weight(1f) para fillMaxWidth()
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = TealLight,
                            unfocusedBorderColor = CardBorder
                        )
                    )

                    /* SELETOR DE UNIDADE COMENTADO PARA VERSÃO POSTERIOR
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = unit,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Unidade") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedBorderColor = TealLight,
                                unfocusedBorderColor = CardBorder
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.background(BackgroundDark)
                        ) {
                            units.forEach { selectionOption ->
                                DropdownMenuItem(
                                    text = { Text(selectionOption, color = TextPrimary) },
                                    onClick = {
                                        unit = selectionOption
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                    */
                }

                // Painel Nutricional
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Informação Nutricional",
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                        
                        NutritionalItem("Calorias", String.format(Locale.getDefault(), "%.1f kcal", calcCal))
                        NutritionalItem("Proteínas", String.format(Locale.getDefault(), "%.1f g", calcProt))
                        NutritionalItem("Carboidratos", String.format(Locale.getDefault(), "%.1f g", calcCarb))
                        NutritionalItem("Gorduras", String.format(Locale.getDefault(), "%.1f g", calcGord))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (quantity > 0) {
                        onSalvar(quantity, unit)
                    }
                },
                enabled = quantity > 0
            ) {
                Text("Salvar", color = TealLight, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) {
                Text("Cancelar", color = TextSecondary)
            }
        }
    )
}

@Composable
fun NutritionalItem(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = TextSecondary, fontSize = 12.sp)
        Text(text = value, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}
