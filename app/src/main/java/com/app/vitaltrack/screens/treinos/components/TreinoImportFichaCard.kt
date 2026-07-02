package com.app.vitaltrack.screens.treinos.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.vitaltrack.ui.theme.CardBorder
import com.app.vitaltrack.ui.theme.TextSecondary

@Composable
fun TreinoImportFichaCard(
    onImportMarkdown: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.UploadFile,
                contentDescription = null,
                tint = TextSecondary.copy(alpha = 0.5f),
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Importar ficha de papel",
                color = TextSecondary,
                fontSize = 16.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
            /*
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Em breve você poderá importar uma foto, PDF ou texto da ficha e revisar antes de salvar.",
                color = TextSecondary.copy(alpha = 0.7f),
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp
            )
            */
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onImportMarkdown,
                colors = ButtonDefaults.buttonColors(
                    containerColor = com.app.vitaltrack.ui.theme.TealLight.copy(alpha = 0.2f),
                    contentColor = com.app.vitaltrack.ui.theme.TealLight
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Ler treino do Markdown", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
            }
        }
    }
}
