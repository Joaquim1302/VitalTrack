package com.app.vitaltrack.model

import androidx.compose.ui.graphics.vector.ImageVector

data class RecommendedFeature(
    val title: String,
    val icon: ImageVector,
    val description: String = ""
)
