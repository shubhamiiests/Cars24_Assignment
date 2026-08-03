package com.cars24.data.page

import android.content.Context
import java.io.File
import java.io.IOException

interface PageDataSource {
    suspend fun fetch(pageId: String): String
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

    private fun overrideFile(pageId: String): File? =
        context.getExternalFilesDir(null)?.let { File(it, "sdui/$pageId.json") }
}
