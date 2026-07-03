package com.app.vitaltrack.screens.treinos.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.vitaltrack.screens.treinos.TreinoSerieUiModel
import com.app.vitaltrack.ui.theme.CardBorder
import com.app.vitaltrack.ui.theme.TealLight
import com.app.vitaltrack.ui.theme.TextPrimary
import com.app.vitaltrack.ui.theme.TextSecondary

@Composable
fun SerieExecutionRow(
    serie: TreinoSerieUiModel,
    recomendacaoCarga: String? = null,
    onCargaChange: (String) -> Unit,
    onRepsChange: (String) -> Unit,
    onAdjustCarga: (Float) -> Unit,
    onAdjustReps: (Int) -> Unit,
    onConcluir: () -> Unit
) {
    val backgroundColor = if (serie.concluida) TealLight.copy(alpha = 0.1f) else Color.Transparent

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor, RoundedCornerShape(8.dp))
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Número da Série
            Surface(
                modifier = Modifier.size(24.dp),
                shape = RoundedCornerShape(4.dp),
                color = if (serie.concluida) TealLight else CardBorder
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "${serie.nrSerie}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (serie.concluida) TextPrimary else TextSecondary
                    )
                }
            }

            // Input Carga
            OutlinedTextField(
                value = serie.carga,
                onValueChange = onCargaChange,
                modifier = Modifier.width(80.dp),
                label = { Text("Carga", fontSize = 10.sp) },
                placeholder = { recomendacaoCarga?.let { Text(it, fontSize = 10.sp) } },
                suffix = { Text("kg", fontSize = 10.sp) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                enabled = !serie.concluida,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TealLight,
                    unfocusedBorderColor = CardBorder
                )
            )

            // Input Repetições
            OutlinedTextField(
                value = serie.repeticoes,
                onValueChange = onRepsChange,
                modifier = Modifier.width(80.dp),
                label = { Text("Reps", fontSize = 10.sp) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                enabled = !serie.concluida,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TealLight,
                    unfocusedBorderColor = CardBorder
                )
            )

            if (serie.sugeridoDoTreinoAnterior && !serie.concluida) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = "Valores vindos do treino anterior",
                    modifier = Modifier.size(16.dp),
                    tint = TealLight.copy(alpha = 0.6f)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Botão Concluir
            if (serie.isSaving) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp, color = TealLight)
            } else {
                IconButton(
                    onClick = onConcluir,
                    enabled = !serie.concluida && serie.carga.isNotEmpty() && serie.repeticoes.isNotEmpty(),
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = if (serie.concluida) TealLight else TextSecondary
                    )
                ) {
                    Icon(Icons.Default.Check, contentDescription = "Concluir série")
                }
            }
        }

        // Botões de Ajuste Rápido
        if (!serie.concluida) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                QuickAdjustButton("-5", onClick = { onAdjustCarga(-5f) })
                QuickAdjustButton("-1", onClick = { onAdjustCarga(-1f) })
                QuickAdjustButton("+1", onClick = { onAdjustCarga(1f) })
                QuickAdjustButton("+5", onClick = { onAdjustCarga(5f) })
                
                Spacer(modifier = Modifier.width(8.dp))
                
                QuickAdjustButton("-1 rep", onClick = { onAdjustReps(-1) })
                QuickAdjustButton("+1 rep", onClick = { onAdjustReps(1) })
            }
        }
    }
}

@Composable
fun QuickAdjustButton(label: String, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.height(32.dp),
        contentPadding = PaddingValues(horizontal = 4.dp),
        colors = ButtonDefaults.textButtonColors(contentColor = TealLight)
    ) {
        Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}
