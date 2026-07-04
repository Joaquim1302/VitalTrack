package com.app.vitaltrack.screens.treinos.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.vitaltrack.data.entity.treinos.TreinoFichaEntity
import com.app.vitaltrack.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TreinoFichaHeader(
    ficha: TreinoFichaEntity?,
    onBackClick: () -> Unit,
    onExportClick: () -> Unit = {}
) {
    val locale = LocalConfiguration.current.locales[0]
    val dateFormatter = remember(locale) { SimpleDateFormat("dd/MM/yyyy", locale) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
            .statusBarsPadding()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(CardBackground)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Voltar",
                        tint = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Musculação",
                        color = TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (ficha != null) {
                        Text(
                            text = "Ficha: ${ficha.dsFicha}",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            if (ficha != null) {
                IconButton(
                    onClick = onExportClick,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(CardBackground)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Exportar Ficha (Markdown)",
                        tint = TealLight
                    )
                }
            }
        }

        if (ficha != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Badge(
                    containerColor = TealLight.copy(alpha = 0.2f),
                    contentColor = TealLight
                ) {
                    Text(
                        text = "ATIVA",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Desde: ${dateFormatter.format(ficha.dtInicio)}",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
            
            ficha.dsObs?.let { obs ->
                if (obs.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = obs,
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}
