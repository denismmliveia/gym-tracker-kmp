package com.gymtracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Tarjeta de lista Bold Sport: borde naranja izquierdo de 3dp + fondo surface.
 * Usar en lugar de Card() para filas de listas (ejercicios, sesiones, progreso).
 */
@Composable
fun SportCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Row(modifier = modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
        Box(
            Modifier
                .width(3.dp)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.primary)
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = MaterialTheme.shapes.medium
                )
                .padding(horizontal = 12.dp, vertical = 10.dp),
            content = content
        )
    }
}
