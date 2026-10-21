package com.example.gerenciador.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.util.Locale

@Composable
fun PerformanceScreen(state: UiState, onRequestShizuku: () -> Unit) {
    val s = state.stats
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text(if (state.full) "Modo completo (Shizuku)" else "Modo limitado", style = MaterialTheme.typography.titleMedium)
                if (!state.full) {
                    Text("CPU por processo, RAM por processo e GPU exigem o Shizuku.", style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = onRequestShizuku) { Text("Ativar Shizuku") }
                }
            }
        }
        if (s == null) { Text("Carregando…", Modifier.padding(16.dp)); return@Column }

        MetricCard("RAM", "%.0f%%".format(Locale.US, s.ramPercent),
            "${fmtBytes(s.ramUsed)} / ${fmtBytes(s.ramTotal)}", state.ramHist, Color(0xFF1E88E5))

        val cpuNow = state.cpuHist.lastOrNull()
        MetricCard("CPU", cpuNow?.let { "%.0f%%".format(Locale.US, it) } ?: "—",
            if (s.cpuPercent != null) "Uso real do sistema" else "Estimado pela frequência dos núcleos",
            state.cpuHist, Color(0xFF43A047))

        MetricCard("GPU", s.gpuPercent?.let { "%.0f%%".format(Locale.US, it) } ?: "—",
            if (s.gpuPercent != null) "Uso da GPU" else "Indisponível neste aparelho/modo",
            state.gpuHist, Color(0xFFE53935))

        if (s.cores.isNotEmpty()) {
            Text("Núcleos", Modifier.padding(top = 12.dp, bottom = 4.dp), style = MaterialTheme.typography.titleMedium)
            s.cores.forEachIndexed { i, c ->
                Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("CPU $i", Modifier.width(56.dp), style = MaterialTheme.typography.bodySmall)
                    LinearProgressIndicator(progress = { c.percent / 100f }, Modifier.weight(1f).align(androidx.compose.ui.Alignment.CenterVertically))
                    Text("${c.curMhz} MHz", Modifier.width(80.dp), style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
