package com.app.vitaltrack.screens.refeicoes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.vitaltrack.ui.theme.*

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
    var quantityText by remember { mutableStateOf(if (initialQuantity == 0.0) "" else initialQuantity.toInt().toString()) }
    val quantity = quantityText.toDoubleOrNull() ?: 0.0
    
    val factor = quantity / baseQuantity
    val calories = baseCalories * factor
    val prot = baseProt * factor
    val carb = baseCarb * factor
    val gord = baseGord * factor

    AlertDialog(
        onDismissRequest = onCancelar,
        containerColor = BackgroundDark,
        titleContentColor = TextPrimary,
        textContentColor = TextSecondary,
        title = {
            Text(
                text = alimentoNome,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Info Nutricional calculada
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    NutriItem("Calorias", "${calories.toInt()} kcal")
                    NutriItem("Prot", "${prot.toInt()}g")
                    NutriItem("Carb", "${carb.toInt()}g")
                    NutriItem("Gord", "${gord.toInt()}g")
                }

                OutlinedTextField(
                    value = quantityText,
                    onValueChange = { if (it.all { char -> char.isDigit() }) quantityText = it },
                    label = { Text("Quantidade") },
                    suffix = { Text(initialUnit) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TealLight,
                        unfocusedBorderColor = CardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = TealLight
                    )
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSalvar(quantity, initialUnit) },
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
private fun NutriItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 11.sp, color = TextSecondary)
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
    }
}
