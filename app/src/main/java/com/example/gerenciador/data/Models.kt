package com.example.gerenciador.data

data class Core(val curMhz: Int, val maxMhz: Int) {
    val percent: Float get() = if (maxMhz > 0) curMhz * 100f / maxMhz else 0f
}

data class SystemStats(
    val ramTotal: Long,
    val ramUsed: Long,
    /** null quando o Android bloqueia a leitura (modo normal) */
    val cpuPercent: Float?,
    val cores: List<Core>,
    /** null quando a GPU não é legível neste aparelho */
    val gpuPercent: Float?,
) {
    val ramPercent: Float get() = if (ramTotal > 0) ramUsed * 100f / ramTotal else 0f
}

data class ProcInfo(
    val packageName: String,
    val label: String,
    val ramBytes: Long?,
    val cpuPercent: Float?,
)

data class Snapshot(val stats: SystemStats, val procs: List<ProcInfo>)

interface StatsProvider {
    val isFull: Boolean
    suspend fun snapshot(): Snapshot
    suspend fun kill(packageName: String): Boolean
}
