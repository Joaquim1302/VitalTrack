package com.app.vitaltrack.screens.treinos.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.vitaltrack.data.entity.treinos.TreinoFichaExercicioEntity
import com.app.vitaltrack.ui.theme.*

@Composable
fun TreinoExercicioCard(
    exercicio: TreinoFichaExercicioEntity
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(TealDark.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${exercicio.nrOrdem}",
                    color = TealLight,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = exercicio.dsObs ?: "Exercício", // Atualmente usando dsObs para o nome, TODO vincular com tabela exercícios
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                val repsText = if (exercicio.nrRepeticoesPlanejadas > 0) {
                    "${exercicio.nrRepeticoesPlanejadas} repetições"
                } else {
                    exercicio.dsObs ?: "repetições"
                }

                Text(
                    text = "${exercicio.nrSeriesPlanejadas} séries x $repsText",
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Carga sugerida: ",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    Text(
                        text = exercicio.nmCargaRecomendada?.let { "${it.toInt()} kg" } ?: "não informada",
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Descanso: ",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    Text(
                        text = exercicio.nrDescansoSegundos?.let { "${it}s" } ?: "padrão",
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                // Futuramente, se houver observação específica (além do nome temporário)
                // exercicio.dsObsReal?.let { ... }
            }
            
            Icon(
                imageVector = Icons.Default.FitnessCenter,
                contentDescription = null,
                tint = TealLight.copy(alpha = 0.5f),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
