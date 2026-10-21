package com.example.gerenciador.ui

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gerenciador.data.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import rikka.shizuku.Shizuku

enum class SortBy { RAM, CPU, NOME }

data class UiState(
    val full: Boolean = false,
    val stats: SystemStats? = null,
    val procs: List<ProcInfo> = emptyList(),
    val ramHist: List<Float> = emptyList(),
    val cpuHist: List<Float> = emptyList(),
    val gpuHist: List<Float> = emptyList(),
)

class MainViewModel(app: Application) : AndroidViewModel(app) {
    private val normal = NormalProvider(app)
    private val shizuku = ShizukuProvider(app)
    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state

    init {
        viewModelScope.launch(Dispatchers.IO) {
            while (isActive) {
                val provider: StatsProvider = if (shizuku.isReady()) shizuku else normal
                val snap = try { provider.snapshot() } catch (e: Exception) { null }
                if (snap != null) {
                    val s = snap.stats
                    val cpu = s.cpuPercent ?: s.cores.takeIf { it.isNotEmpty() }?.map { it.percent }?.average()?.toFloat()
                    _state.update {
                        it.copy(
                            full = provider.isFull,
                            stats = s,
                            procs = snap.procs,
                            ramHist = (it.ramHist + s.ramPercent).takeLast(60),
                            cpuHist = if (cpu != null) (it.cpuHist + cpu).takeLast(60) else it.cpuHist,
                            gpuHist = if (s.gpuPercent != null) (it.gpuHist + s.gpuPercent).takeLast(60) else it.gpuHist,
                        )
                    }
                }
                delay(2000)
            }
        }
    }

    fun kill(pkg: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val provider: StatsProvider = if (shizuku.isReady()) shizuku else normal
            val ok = try { provider.kill(pkg) } catch (e: Exception) { false }
            toast(if (ok) "Encerrado: $pkg" else "Falha ao encerrar")
        }
    }

    fun requestShizuku() {
        try {
            if (Shizuku.pingBinder()) Shizuku.requestPermission(1001)
            else toast("Abra o app Shizuku e inicie o serviço primeiro")
        } catch (e: Throwable) {
            toast("Shizuku não encontrado. Instale o app Shizuku.")
        }
    }

        private fun toast(msg: String) {
        android.os.Handler(android.os.Looper.getMainLooper()).post {
            Toast.makeText(getApplication<Application>(), msg, Toast.LENGTH_SHORT).show()
        }
    }
}
