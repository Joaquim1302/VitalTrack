package com.app.vitaltrack.ui.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.vitaltrack.data.gamification.GamificationRules
import com.app.vitaltrack.data.gamification.GamificationState
import com.app.vitaltrack.ui.theme.*

@Composable
fun GamificationProgressCard(
    state: GamificationState,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val levelName = GamificationRules.getLevelName(state.level)
    val progress = GamificationRules.calculateLevelProgress(state.totalPoints)
    val nextLevelPoints = GamificationRules.getNextLevelPoints(state.level)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(CardBackground)
            .border(1.dp, CardBorder, RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "Nível ${state.level} — $levelName",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${state.totalPoints} / $nextLevelPoints pontos para o próximo nível",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
                
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = TealLight,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = TealLight,
                trackColor = Color.White.copy(alpha = 0.1f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                GamificationStatItem(
                    icon = Icons.Default.LocalFireDepartment,
                    label = "Sequência",
                    value = "${state.currentStreak} dias",
                    iconColor = Color(0xFFFF9800)
                )
                
                GamificationStatItem(
                    icon = Icons.Default.EmojiEvents,
                    label = "Melhor",
                    value = "${state.bestStreak} dias",
                    iconColor = Color(0xFFFFD700)
                )
                
                GamificationStatItem(
                    icon = Icons.Default.Star,
                    label = "Conquistas",
                    value = "${state.unlockedAchievements.size}",
                    iconColor = TealLight
                )
            }
        }
    }
}

@Composable
private fun GamificationStatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    iconColor: Color
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Column {
            Text(
                text = value,
                color = TextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                color = TextSecondary,
                fontSize = 10.sp
            )
        }
    }
}
