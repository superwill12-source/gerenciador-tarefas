package com.example.gerenciador.data

import android.app.ActivityManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager

/**
 * Modo sem privilégios: RAM do sistema, frequência dos núcleos e lista de apps instalados.
 * Só consegue encerrar processos em segundo plano. Não vê CPU/RAM por processo nem GPU.
 */
class NormalProvider(private val ctx: Context) : StatsProvider {
    override val isFull = false

    override suspend fun snapshot(): Snapshot {
        val (total, used) = SysInfo.memory(ctx)
        val stats = SystemStats(total, used, cpuPercent = null, cores = SysInfo.cores(), gpuPercent = null)

        val pm = ctx.packageManager
        val procs = pm.getInstalledApplications(PackageManager.GET_META_DATA)
            .filter { it.flags and ApplicationInfo.FLAG_SYSTEM == 0 && it.packageName != ctx.packageName }
            .map { ProcInfo(it.packageName, pm.getApplicationLabel(it).toString(), null, null) }
            .sortedBy { it.label.lowercase() }
        return Snapshot(stats, procs)
    }

    override suspend fun kill(packageName: String): Boolean {
        ctx.getSystemService(ActivityManager::class.java).killBackgroundProcesses(packageName)
        return true
    }

    override suspend fun cleanAll(): Boolean {
        val am = ctx.getSystemService(ActivityManager::class.java)
        ctx.packageManager.getInstalledApplications(0)
            .filter { it.flags and ApplicationInfo.FLAG_SYSTEM == 0 && it.packageName != ctx.packageName }
            .forEach { am.killBackgroundProcesses(it.packageName) }
        return true
    }
}
