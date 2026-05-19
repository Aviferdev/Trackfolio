import java.io.File

val verifyTranslations by tasks.registering {
    group = "verification"
    description = "Verifica que todos los idiomas tengan traducción completa respecto al locale base"

    val composeResourcesDir = layout.projectDirectory.dir("src/commonMain/composeResources")

    inputs.dir(composeResourcesDir)
    outputs.upToDateWhen { false }

    doLast {
        val supportedLocales = linkedMapOf(
            "es" to "Español", "en" to "English", "de" to "Deutsch",
            "fr" to "Français", "it" to "Italiano",
            "ja" to "日本語", "ko" to "한국어", "pt" to "Português",
            "ru" to "Русский", "zh" to "中文"
        )
        val baseCode = supportedLocales.keys.first()

        // ── Escanear disco ──
        val onDisk = mutableMapOf<String, File>()
        composeResourcesDir.asFile.listFiles()?.forEach { dir ->
            if (dir.isDirectory && dir.name.startsWith("values")) {
                val code = if (dir.name == "values") baseCode else dir.name.removePrefix("values-")
                val file = File(dir, "strings.xml")
                if (file.exists()) onDisk[code] = file
            }
        }
        if (baseCode !in onDisk) return@doLast

        val missingLocales = supportedLocales.keys - onDisk.keys

        // ── Parsear ──
        val localeKeys = mutableMapOf<String, Set<String>>()
        val keyPattern = Regex("""name\s*=\s*"(?<key>[^"]*)"""")
        val transPattern = Regex("""translatable\s*=\s*"(?<val>[^"]*)"""")

        for ((code, file) in onDisk) {
            val keys = mutableSetOf<String>()
            var pos = 0
            val text = file.readText()
            while (true) {
                val start = text.indexOf("<string ", pos)
                if (start == -1) break
                val end = text.indexOf('>', start)
                if (end == -1) break
                val tag = text.substring(start + 8, end)
                val name = keyPattern.find(tag)?.groups?.get("key")?.value ?: ""
                if (name.isNotEmpty()) {
                    val trans = transPattern.find(tag)?.groups?.get("val")?.value ?: ""
                    if (!trans.equals("false", true)) keys.add(name)
                }
                pos = end + 1
            }
            localeKeys[code] = keys
        }

        val baseKeys = localeKeys[baseCode] ?: emptySet()

        // ── Tabla ──
        println("\n" + "=".repeat(60))
        println("   TRANSLATION COVERAGE REPORT")
        println("=".repeat(60))
        println("   Base: $baseCode • ${baseKeys.size} keys • ${supportedLocales.size} locales\n")
        println("   ${"Locale".padEnd(7)} ${"Keys".padEnd(6)} ${"Miss".padEnd(5)} Coverage")
        println("   ${"------".padEnd(7)} ${"----".padEnd(6)} ${"----".padEnd(5)} -------")

        for (code in supportedLocales.keys) {
            val name = supportedLocales[code] ?: ""
            if (code == baseCode) {
                println("   ${code.padEnd(7)} ${baseKeys.size.toString().padEnd(6)} ${"—".padEnd(5)} 100.0% ★")
            } else if (code in missingLocales) {
                println("   ${code.padEnd(7)} ${"—".padEnd(6)} ${"—".padEnd(5)} 0.0%   ❌ $name")
            } else {
                val keys = localeKeys[code] ?: emptySet()
                val missing = (baseKeys - keys).size
                val cov = "%.1f".format(
                    if (baseKeys.isEmpty()) 100.0
                    else ((baseKeys.size - missing).toDouble() / baseKeys.size) * 100.0
                )
                val icon = if (missing == 0) "✅" else "⚠️"
                println("   ${code.padEnd(7)} ${keys.size.toString().padEnd(6)} ${missing.toString().padEnd(5)} ${cov}% $icon $name")
            }
        }

        // ── Detalle ──
        println("\n" + "─".repeat(60))

        val allMissing = supportedLocales.keys
            .filter { it != baseCode && it in onDisk }
            .flatMap { code ->
                val missing = (baseKeys - (localeKeys[code] ?: emptySet())).sorted()
                missing.map { key -> code to key }
            }
            .groupBy({ it.first }, { it.second })

        if (allMissing.isEmpty() && missingLocales.isEmpty()) {
            println("\n   ✅ All translations complete!\n")
            return@doLast
        }

        if (missingLocales.isNotEmpty()) {
            println("\n   ❌ Missing entirely:")
            missingLocales.sorted().forEach { println("      • $it  (${supportedLocales[it]})") }
        }

        allMissing.forEach { (code, keys) ->
            println("\n   [$code] (${supportedLocales[code]}) — ${keys.size} missing:")
            keys.forEach { println("      • $it") }
        }

        println("\n   ─── How to fix ───")
        println("   Add the missing keys to the corresponding strings.xml file(s).")
        println("   Use translatable=\"false\" to exclude a key from checks.")
        println()
    }
}
