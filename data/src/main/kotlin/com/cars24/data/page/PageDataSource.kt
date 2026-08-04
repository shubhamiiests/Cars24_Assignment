package com.cars24.data.page

import android.content.Context
import java.io.File
import java.io.IOException

interface PageDataSource {
    suspend fun fetch(pageId: String): String
    suspend fun availablePages(): Set<String>
}

class AssetPageDataSource(private val context: Context) : PageDataSource {

    override suspend fun fetch(pageId: String): String {
        val path = "sdui/$pageId.json"
        return try {
            context.assets.open(path).bufferedReader().use { it.readText() }
        } catch (cause: IOException) {
            throw IOException("Mock server has no payload at $path", cause)
        }
    }

    override suspend fun availablePages(): Set<String> =
        context.assets.list(ASSET_DIR)
            .orEmpty()
            .filter { it.endsWith(JSON_SUFFIX) }
            .map { it.removeSuffix(JSON_SUFFIX) }
            .toSet()

    private companion object {
        const val ASSET_DIR = "sdui"
        const val JSON_SUFFIX = ".json"
    }
}

class FileOverridePageDataSource(
    private val context: Context,
    private val delegate: PageDataSource,
) : PageDataSource {

    override suspend fun fetch(pageId: String): String {
        val override = overrideFile(pageId)
        return if (override?.canRead() == true) override.readText() else delegate.fetch(pageId)
    }

    fun hasOverride(pageId: String): Boolean = overrideFile(pageId)?.canRead() == true

    override suspend fun availablePages(): Set<String> {
        val pushed = overrideDir()
            ?.listFiles()
            .orEmpty()
            .filter { it.isFile && it.name.endsWith(".json") }
            .map { it.name.removeSuffix(".json") }
        return delegate.availablePages() + pushed
    }

    private fun overrideFile(pageId: String): File? =
        overrideDir()?.let { File(it, "$pageId.json") }

    private fun overrideDir(): File? =
        context.getExternalFilesDir(null)?.let { base ->
            File(base, OVERRIDE_DIR).also { if (!it.exists()) it.mkdirs() }
        }

    private companion object {
        const val OVERRIDE_DIR = "sdui"
    }
}
