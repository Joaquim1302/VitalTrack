package com.app.vitaltrack.screens.treinos.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.vitaltrack.screens.treinos.TreinoExercicioExecucao
import com.app.vitaltrack.ui.theme.*

@Composable
fun ExerciseExecutionCard(
    execucao: TreinoExercicioExecucao,
    onCargaChange: (Int, String) -> Unit,
    onRepsChange: (Int, String) -> Unit,
    onAdjustCarga: (Int, Float) -> Unit,
    onAdjustReps: (Int, Int) -> Unit,
    onConcluirSerie: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(TealDark.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = TealLight)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    val repsText = if (execucao.exercicio.nrRepeticoesPlanejadas > 0) {
                        execucao.exercicio.nrRepeticoesPlanejadas.toString()
                    } else {
                        execucao.exercicio.dsObs ?: "0"
                    }
                    
                    Text(
                        text = execucao.exercicio.dsObs ?: "Exercício",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    val cargaPlanejada = execucao.exercicio.nmCargaRecomendada?.let { " • Carga: ${it.toInt()}kg" } ?: ""
                    
                    Text(
                        text = "Planejado: ${execucao.exercicio.nrSeriesPlanejadas} x $repsText$cargaPlanejada • Descanso: ${execucao.exercicio.nrDescansoSegundos ?: "padrão"}s",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            execucao.series.forEach { serie ->
                SerieExecutionRow(
                    serie = serie,
                    recomendacaoCarga = execucao.exercicio.nmCargaRecomendada?.toInt()?.toString(),
                    onCargaChange = { onCargaChange(serie.nrSerie, it) },
                    onRepsChange = { onRepsChange(serie.nrSerie, it) },
                    onAdjustCarga = { onAdjustCarga(serie.nrSerie, it) },
                    onAdjustReps = { onAdjustReps(serie.nrSerie, it) },
                    onConcluir = { onConcluirSerie(serie.nrSerie) }
                )
                if (serie.nrSerie < execucao.series.size) {
                    HorizontalDivider(color = CardBorder.copy(alpha = 0.5f), thickness = 0.5.dp, modifier = Modifier.padding(vertical = 4.dp))
                }
            }
        }
    }
}
