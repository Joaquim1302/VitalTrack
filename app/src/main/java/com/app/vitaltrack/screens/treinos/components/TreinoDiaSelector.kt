package com.app.vitaltrack.screens.treinos.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.vitaltrack.data.entity.treinos.TreinoFichaDiaEntity
import com.app.vitaltrack.ui.theme.*

@Composable
fun TreinoDiaSelector(
    divisoes: List<TreinoFichaDiaEntity>,
    selecionada: TreinoFichaDiaEntity?,
    onSelect: (TreinoFichaDiaEntity) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(divisoes) { item ->
            val isSelected = selecionada?.cdFichaDia == item.cdFichaDia
            
            Column(
                modifier = Modifier
                    .width(IntrinsicSize.Min)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) TealLight else CardBackground)
                    .border(
                        width = 1.dp,
                        color = if (isSelected) TealLight else CardBorder,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable { onSelect(item) }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = item.dsDia,
                    color = if (isSelected) TextPrimary else TextPrimary,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 14.sp,
                    maxLines = 1
                )
                if (item.dsGrupoMuscular != null && item.dsGrupoMuscular.isNotBlank()) {
                    Text(
                        text = item.dsGrupoMuscular,
                        color = if (isSelected) TextPrimary.copy(alpha = 0.8f) else TextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
