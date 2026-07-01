package com.app.vitaltrack.screens.treinos.components

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.vitaltrack.ui.theme.TealLight
import com.app.vitaltrack.ui.theme.TextPrimary

@Composable
fun TreinoIniciarButton(
    cdFichaDia: Long?,
    onIniciarTreino: (Long) -> Unit
) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Button(
            onClick = {
                if (cdFichaDia != null) {
                    onIniciarTreino(cdFichaDia)
                    Toast.makeText(context, "Execução do treino será implementada na próxima fase.", Toast.LENGTH_SHORT).show()
                }
            },
            enabled = cdFichaDia != null,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = TealLight,
                disabledContainerColor = TealLight.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null,
                tint = TextPrimary
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Iniciar Treino",
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}
