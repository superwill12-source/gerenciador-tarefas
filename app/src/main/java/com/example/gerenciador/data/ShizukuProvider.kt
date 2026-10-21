package com.example.gerenciador.data

import android.content.Context
import android.content.pm.PackageManager
import rikka.shizuku.Shizuku

/**
 * Modo completo: executa comandos de shell com privilégio ADB via Shizuku.
 * O usuário precisa instalar o app Shizuku, iniciá-lo (depuração sem fio ou root)
 * e conceder permissão a este app.
 */
class ShizukuProvider(private val ctx: Context) : StatsProvider {
    override val isFull = true

    fun isReady(): Boolean = try {
        Shizuku.pingBinder() && Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
    } catch (e: Throwable) { false }

    /** Em Shizuku 13.x, newProcess é privado; usamos reflexão (método comum na comunidade). */
    private fun sh(cmd: String): String {
        val m = Shizuku::class.java.getDeclaredMethod(
            "newProcess", Array<String>::class.java, Array<String>::class.java, String::class.java
        )
        m.isAccessible = true
        val p = m.invoke(null, arrayOf("sh", "-c", cmd), null, null) as Process
        val out = p.inputStream.bufferedReader().readText()
        p.waitFor()
        return out
    }

    override suspend fun snapshot(): Snapshot {
        val top = sh("top -b -n 1 -m 120 -o PID,RES,%CPU,ARGS 2>&1")
        val (total, used) = SysInfo.memory(ctx)

        // Cabeçalho do toybox: "800%cpu  35%user ... 700%idle ..."
        val totalCpu = Regex("(\\d+)%cpu").find(top)?.groupValues?.get(1)?.toFloatOrNull()
        val idle = Regex("(\\d+)%idle").find(top)?.groupValues?.get(1)?.toFloatOrNull()
        val cpu = if (totalCpu != null && idle != null && totalCpu > 0) (totalCpu - idle) * 100f / totalCpu else null

        val gpu = sh("cat /sys/class/kgsl/kgsl-3d0/gpu_busy_percentage 2>/dev/null")
            .trim().removeSuffix("%").trim().toFloatOrNull()

        val pm = ctx.packageManager
        val map = linkedMapOf<String, ProcInfo>()
        for (line in top.lines()) {
            val t = line.trim().split(Regex("\\s+"))
            if (t.size < 4 || t[0].toIntOrNull() == null) continue
            val name = t[3]
            if (!name.contains('.') || name.startsWith("/") || name.startsWith("[")) continue
            val pkg = name.substringBefore(':')
            val ram = parseSize(t[1])
            val c = t[2].toFloatOrNull() ?: 0f
            val old = map[pkg]
            map[pkg] = if (old == null) ProcInfo(pkg, labelOf(pm, pkg), ram, c)
            else old.copy(ramBytes = (old.ramBytes ?: 0) + (ram ?: 0), cpuPercent = (old.cpuPercent ?: 0f) + c)
        }
        return Snapshot(SystemStats(total, used, cpu, SysInfo.cores(), gpu), map.values.toList())
    }

    override suspend fun kill(packageName: String): Boolean {
        if (!Regex("[A-Za-z0-9._]+").matches(packageName)) return false
        sh("am force-stop $packageName")
        return true
    }

    private fun labelOf(pm: PackageManager, pkg: String): String = try {
        pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString()
    } catch (e: Exception) { pkg }

    /** Aceita "123456" (KB), "120M", "1.2G", "800K". */
    private fun parseSize(s: String): Long? {
        val num = s.dropLastWhile { it.isLetter() }.toDoubleOrNull() ?: return null
        return when (s.last().uppercaseChar()) {
            'G' -> (num * 1024 * 1024 * 1024).toLong()
            'M' -> (num * 1024 * 1024).toLong()
            'K' -> (num * 1024).toLong()
            else -> (num * 1024).toLong()
        }
    }
}
