package com.example.gerenciador.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private val BLUE = Color(0xFF3B82F6)
private val GREEN = Color(0xFF22C55E)
private val ORANGE = Color(0xFFF97316)

@Composable
fun PerformanceScreen(state: UiState, onRequestShizuku: () -> Unit, onClean: () -> Unit) {
    val s = state.stats
    var confirm by remember { mutableStateOf(false) }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (s == null) {
            Box(Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Column
        }

        val cpuNow = state.cpuHist.lastOrNull()

        Card(Modifier.fillMaxWidth()) {
            Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                Gauge(s.ramPercent, "RAM", BLUE)
                Gauge(cpuNow, "CPU", GREEN)
                Gauge(s.gpuPercent, "GPU", ORANGE)
            }
        }

        Button(
            onClick = { confirm = true },
            enabled = !state.cleaning,
            modifier = Modifier.fillMaxWidth().height(56.dp),
        ) {
            if (state.cleaning) {
                CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = LocalContentColor.current)
                Spacer(Modifier.width(10.dp))
                Text("Limpando…")
            } else {
                Text("🧹  Limpar apps em segundo plano", fontWeight = FontWeight.SemiBold)
            }
        }
        state.lastClean?.let {
            Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
        }

        Card(
            Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    if (state.full) "⚡ Modo completo (Shizuku)" else "Modo limitado",
                    style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold,
                )
                if (!state.full) {
                    Text(
                        "CPU/RAM por processo e GPU exigem o Shizuku.",
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(onClick = onRequestShizuku) { Text("Ativar Shizuku") }
                }
            }
        }

        MetricCard(
            "Memória RAM", fmtPct(s.ramPercent),
            "${fmtBytes(s.ramUsed)} de ${fmtBytes(s.ramTotal)} em uso", state.ramHist, BLUE,
        )
        MetricCard(
            "CPU", fmtPct(cpuNow),
            if (s.cpuPercent != null) "Uso real do sistema" else "Estimado pela frequência dos núcleos",
            state.cpuHist, GREEN,
        )
        MetricCard(
            "GPU", fmtPct(s.gpuPercent),
            if (s.gpuPercent != null) "Uso da GPU" else "Indisponível neste aparelho/modo",
            state.gpuHist, ORANGE,
        )

        if (s.cores.isNotEmpty()) {
            Card(
                Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Núcleos da CPU", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    s.cores.forEachIndexed { i, c ->
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("CPU $i", Modifier.width(52.dp), style = MaterialTheme.typography.bodySmall)
                            LinearProgressIndicator(progress = { (c.percent / 100f).coerceIn(0f, 1f) }, modifier = Modifier.weight(1f))
                            Text("${c.curMhz} MHz", Modifier.width(76.dp), style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
    }

    if (confirm) {
        AlertDialog(
            onDismissRequest = { confirm = false },
            title = { Text("Limpar apps em segundo plano?") },
            text = {
                Text(
                    "Isso encerra os apps que não estão em uso. A memória liberada é temporária: " +
                        "o Android pode reabrir alguns apps e eles abrirão mais devagar na próxima vez."
                )
            },
            confirmButton = { TextButton(onClick = { confirm = false; onClean() }) { Text("Limpar") } },
            dismissButton = { TextButton(onClick = { confirm = false }) { Text("Cancelar") } },
        )
    }
}
