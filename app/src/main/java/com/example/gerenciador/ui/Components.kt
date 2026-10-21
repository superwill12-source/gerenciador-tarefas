package com.example.gerenciador.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import java.util.Locale

fun fmtBytes(b: Long): String = when {
    b >= 1L shl 30 -> String.format(Locale.US, "%.1f GB", b / (1024.0 * 1024 * 1024))
    b >= 1L shl 20 -> String.format(Locale.US, "%d MB", b / (1024 * 1024))
    else -> "${b / 1024} KB"
}

@Composable
fun LineChart(values: List<Float>, color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier.fillMaxWidth().height(60.dp)) {
        if (values.size < 2) return@Canvas
        val step = size.width / 59f
        val path = Path()
        values.forEachIndexed { i, v ->
            val x = size.width - (values.size - 1 - i) * step
            val y = size.height - (v.coerceIn(0f, 100f) / 100f) * size.height
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(path, color, style = Stroke(width = 3f))
    }
}

@Composable
fun MetricCard(title: String, valueText: String, detail: String, hist: List<Float>, color: Color) {
    Card(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(valueText, style = MaterialTheme.typography.titleMedium)
            }
            Text(detail, style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(8.dp))
            LineChart(hist, color)
        }
    }
}
