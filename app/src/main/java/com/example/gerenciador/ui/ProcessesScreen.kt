package com.example.gerenciador.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun ProcessesScreen(state: UiState, onKill: (String) -> Unit) {
    var sort by remember(state.full) { mutableStateOf(if (state.full) SortBy.RAM else SortBy.NOME) }
    var query by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf<ProcInfoRef?>(null) }

    val list = remember(state.procs, sort, query) {
        val filtered = state.procs.filter {
            query.isBlank() || it.label.contains(query, ignoreCase = true) || it.packageName.contains(query, ignoreCase = true)
        }
        when (sort) {
            SortBy.RAM -> filtered.sortedByDescending { it.ramBytes ?: -1L }
            SortBy.CPU -> filtered.sortedByDescending { it.cpuPercent ?: -1f }
            SortBy.NOME -> filtered.sortedBy { it.label.lowercase() }
        }
    }
    val maxRam = remember(state.procs) { state.procs.maxOfOrNull { it.ramBytes ?: 0L } ?: 0L }

    Column(Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text("Buscar aplicativo…") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 8.dp),
        )
        Row(
            Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Ordenar:", style = MaterialTheme.typography.labelMedium)
            SortBy.values().forEach {
                FilterChip(selected = sort == it, onClick = { sort = it }, label = { Text(it.name) })
            }
        }
        if (!state.full) {
            Text(
                "Modo limitado: RAM e CPU por app aparecem só com o Shizuku. Encerrar afeta apenas apps em segundo plano.",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            )
        }
        Text(
            "${list.size} aplicativos",
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        )
        LazyColumn(Modifier.weight(1f)) {
            items(list, key = { it.packageName }) { p ->
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            p.label.firstOrNull()?.uppercase() ?: "?",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                    Column(Modifier.weight(1f)) {
                        Text(p.label, style = MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(p.packageName, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        if (p.ramBytes != null) {
                            Text("RAM ${fmtBytes(p.ramBytes)} · CPU ${fmtPct(p.cpuPercent)}", style = MaterialTheme.typography.labelSmall)
                            if (maxRam > 0) {
                                LinearProgressIndicator(
                                    progress = { (p.ramBytes.toFloat() / maxRam).coerceIn(0f, 1f) },
                                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                )
                            }
                        }
                    }
                    TextButton(onClick = { confirm = ProcInfoRef(p.packageName, p.label) }) { Text("Encerrar") }
                }
                HorizontalDivider()
            }
        }
    }

    confirm?.let { ref ->
        AlertDialog(
            onDismissRequest = { confirm = null },
            title = { Text("Encerrar ${ref.label}?") },
            text = { Text(ref.pkg) },
            confirmButton = { TextButton(onClick = { onKill(ref.pkg); confirm = null }) { Text("Encerrar") } },
            dismissButton = { TextButton(onClick = { confirm = null }) { Text("Cancelar") } },
        )
    }
}

private data class ProcInfoRef(val pkg: String, val label: String)
