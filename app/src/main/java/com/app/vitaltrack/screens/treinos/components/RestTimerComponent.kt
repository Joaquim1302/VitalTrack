package com.app.vitaltrack.screens.treinos.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.vitaltrack.ui.theme.*
import java.util.Locale

/**
 * Componente visual para o cronômetro de descanso entre séries.
 */
@Composable
fun RestTimerComponent(
    remainingSeconds: Int,
    isRunning: Boolean,
    onSkip: () -> Unit,
    onAddThirtySeconds: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Se o cronômetro não estiver rodando, o componente não deve ser exibido.
    if (!isRunning) return

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        colors = CardDefaults.cardColors(containerColor = BackgroundDark.copy(alpha = 0.98f)),
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(2.dp, TealLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = null,
                    tint = TealLight,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Descanso",
                    color = TextSecondary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Text(
                text = formatRestTime(remainingSeconds),
                color = TealLight,
                fontSize = 56.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Botão Pular Descanso
                OutlinedButton(
                    onClick = onSkip,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White.copy(alpha = 0.7f)
                    ),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext, 
                        contentDescription = null, 
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Pular", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
                
                // Botão +30s
                Button(
                    onClick = onAddThirtySeconds,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TealLight),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = "+30s", 
                        color = TextPrimary, 
                        fontSize = 18.sp, 
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Formata os segundos em mm:ss
 */
fun formatRestTime(seconds: Int): String {
    val minutes = seconds / 60
    val secs = seconds % 60
    return String.format(Locale.getDefault(), "%02d:%02d", minutes, secs)
}
