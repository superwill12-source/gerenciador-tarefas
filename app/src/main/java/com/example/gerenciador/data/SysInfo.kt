package com.example.gerenciador.data

import android.app.ActivityManager
import android.content.Context
import java.io.File

object SysInfo {
    /** Retorna (total, usada) em bytes. */
    fun memory(ctx: Context): Pair<Long, Long> {
        val am = ctx.getSystemService(ActivityManager::class.java)
        val mi = ActivityManager.MemoryInfo()
        am.getMemoryInfo(mi)
        return mi.totalMem to (mi.totalMem - mi.availMem)
    }

    /** Frequência atual/máxima de cada núcleo (lida de /sys, funciona na maioria dos aparelhos). */
    fun cores(): List<Core> {
        val out = mutableListOf<Core>()
        for (i in 0 until Runtime.getRuntime().availableProcessors()) {
            val dir = File("/sys/devices/system/cpu/cpu$i/cpufreq")
            val cur = readLong(File(dir, "scaling_cur_freq"))
            val max = readLong(File(dir, "cpuinfo_max_freq"))
            if (cur != null && max != null) out += Core((cur / 1000).toInt(), (max / 1000).toInt())
        }
        return out
    }

    private fun readLong(f: File): Long? =
        try { f.readText().trim().toLongOrNull() } catch (e: Exception) { null }
}
