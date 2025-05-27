package com.Optometry.Library.Models

import androidx.annotation.DrawableRes
import com.Optometry.Library.enums.ToolsType

data class ToolsModel(
    val title: String,
    @DrawableRes
    val image: Int,
    val type: ToolsType
)
