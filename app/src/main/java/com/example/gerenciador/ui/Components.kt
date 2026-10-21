package com.example.gerenciador.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.util.Locale

fun fmtBytes(b: Long): String = when {
    b >= 1L shl 30 -> String.format(Locale.US, "%.1f GB", b / (1024.0 * 1024 * 1024))
    b >= 1L shl 20 -> String.format(Locale.US, "%d MB", b / (1024 * 1024))
    else -> "${b / 1024} KB"
}

fun fmtPct(v: Float?): String = if (v == null) "—" else String.format(Locale.US, "%.0f%%", v)

@Composable
fun LineChart(values: List<Float>, color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier.fillMaxWidth().height(64.dp)) {
        if (values.size < 2) return@Canvas
        val step = size.width / 59f
        val line = Path()
        val area = Path()
        values.forEachIndexed { i, v ->
            val x = size.width - (values.size - 1 - i) * step
            val y = size.height - (v.coerceIn(0f, 100f) / 100f) * size.height
            if (i == 0) {
                line.moveTo(x, y)
                area.moveTo(x, size.height)
                area.lineTo(x, y)
            } else {
                line.lineTo(x, y)
                area.lineTo(x, y)
            }
        }
        area.lineTo(size.width, size.height)
        area.close()
        drawPath(area, color.copy(alpha = 0.15f))
        drawPath(line, color, style = Stroke(width = 4f, cap = StrokeCap.Round))
    }
}

@Composable
fun Gauge(percent: Float?, label: String, color: Color, modifier: Modifier = Modifier) {
    val target = (percent ?: 0f).coerceIn(0f, 100f) / 100f
    val anim by animateFloatAsState(target, tween(600), label = "gauge")
    val track = MaterialTheme.colorScheme.surfaceVariant
    Box(modifier.size(96.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize().padding(8.dp)) {
            val stroke = Stroke(width = 14f, cap = StrokeCap.Round)
            drawArc(color = track, startAngle = 135f, sweepAngle = 270f, useCenter = false, style = stroke)
            drawArc(color = color, startAngle = 135f, sweepAngle = 270f * anim, useCenter = false, style = stroke)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(fmtPct(percent), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
fun MetricCard(title: String, valueText: String, detail: String, hist: List<Float>, color: Color) {
    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(valueText, style = MaterialTheme.typography.titleMedium, color = color)
            }
            Text(detail, style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(8.dp))
            LineChart(hist, color)
        }
    }
}
