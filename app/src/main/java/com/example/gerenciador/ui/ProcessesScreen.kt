package com.example.gerenciador.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.util.Locale

@Composable
fun ProcessesScreen(state: UiState, onKill: (String) -> Unit) {
    var sort by remember { mutableStateOf(SortBy.RAM) }
    var confirm by remember { mutableStateOf<String?>(null) }

    val list = remember(state.procs, sort) {
        when (sort) {
            SortBy.RAM -> state.procs.sortedByDescending { it.ramBytes ?: -1 }
            SortBy.CPU -> state.procs.sortedByDescending { it.cpuPercent ?: -1f }
            SortBy.NOME -> state.procs.sortedBy { it.label.lowercase() }
        }
    }

    Column(Modifier.fillMaxSize()) {
        Row(Modifier.padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SortBy.values().forEach {
                FilterChip(selected = sort == it, onClick = { sort = it }, label = { Text(it.name) })
            }
        }
        LazyColumn(Modifier.weight(1f)) {
            items(list, key = { it.packageName }) { p ->
                Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(p.label, style = MaterialTheme.typography.bodyLarge, maxLines = 1)
                        Text(p.packageName, style = MaterialTheme.typography.bodySmall, maxLines = 1)
                        val ram = p.ramBytes?.let { fmtBytes(it) } ?: "—"
                        val cpu = p.cpuPercent?.let { "%.1f%%".format(Locale.US, it) } ?: "—"
                        Text("RAM $ram · CPU $cpu", style = MaterialTheme.typography.bodySmall)
                    }
                    TextButton(onClick = { confirm = p.packageName }) { Text("Encerrar") }
                }
                HorizontalDivider()
            }
        }
    }

    confirm?.let { pkg ->
        AlertDialog(
            onDismissRequest = { confirm = null },
            title = { Text("Encerrar aplicativo?") },
            text = { Text(pkg) },
            confirmButton = { TextButton(onClick = { onKill(pkg); confirm = null }) { Text("Encerrar") } },
            dismissButton = { TextButton(onClick = { confirm = null }) { Text("Cancelar") } },
        )
    }
}
