package com.example.gerenciador

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.gerenciador.ui.*

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    private val vm: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GerenciadorTheme {
                val state by vm.state.collectAsState()
                var tab by remember { mutableIntStateOf(0) }
                Scaffold(
                    topBar = {
                        CenterAlignedTopAppBar(title = { Text("Gerenciador de Tarefas") })
                    },
                    bottomBar = {
                        NavigationBar {
                            NavigationBarItem(
                                selected = tab == 0, onClick = { tab = 0 },
                                icon = { Text("📊") }, label = { Text("Desempenho") },
                            )
                            NavigationBarItem(
                                selected = tab == 1, onClick = { tab = 1 },
                                icon = { Text("📋") }, label = { Text("Processos") },
                            )
                        }
                    },
                ) { pad ->
                    Surface(Modifier.padding(pad).fillMaxSize()) {
                        if (tab == 0) PerformanceScreen(state, vm::requestShizuku, vm::cleanAll)
                        else ProcessesScreen(state, vm::kill)
                    }
                }
            }
        }
    }
}
