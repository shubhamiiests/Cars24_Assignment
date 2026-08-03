package com.cars24.sdui.components

import com.cars24.sdui.schema.SduiAction
import com.cars24.sdui.schema.SduiJson
import com.cars24.sdui.schema.SduiNode
import com.cars24.sdui.schema.SduiPage
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PayloadContractTest {

    private val payloadDir = locatePayloads()
    private val registry = Cars24Components.registry()

    private val pages: List<SduiPage> = payloadDir.listFiles()
        .orEmpty()
        .filter { it.extension == "json" }
        .sortedBy { it.name }
        .map { SduiJson.format.decodeFromString<SduiPage>(it.readText()) }

    @Test
    fun `every payload parses`() {
        assertTrue("No payloads found in $payloadDir", pages.isNotEmpty())
        pages.forEach { page ->
            assertTrue("Page ${page.pageId} has no sections", page.sections.isNotEmpty())
        }
    }

    @Test
    fun `page id matches the file name`() {
        val ids = pages.map { it.pageId }.toSet()
        val files = payloadDir.listFiles().orEmpty()
            .filter { it.extension == "json" }
            .map { it.nameWithoutExtension }
            .toSet()

        assertEquals(files, ids)
    }

    @Test
    fun `only the two deliberate landmines are unregistered types`() {
        val unknown = pages.flatMap { page ->
            page.allNodes().map { page.pageId to it.type }
        }.filter { (_, type) -> registry.find(type) == null }.toSet()

        assertEquals(
            "Unexpected unregistered component types: $unknown",
            setOf(
                "home" to "ar_showroom_360",
                "home" to "loyalty_tier_card",
            ),
            unknown,
        )
    }

    @Test
    fun `every navigate route has a payload`() {
        val available = pages.map { it.pageId }.toSet()

        val dangling = pages.flatMap { page ->
            page.allActions()
                .filter { it.type == "navigate" }
                .mapNotNull { it.params["route"] }
                .filterNot { it in available }
                .map { page.pageId to it }
        }.toSet()

        assertEquals("Routes with no payload: $dangling", emptySet<Pair<String, String>>(), dangling)
    }

    @Test
    fun `every node id is unique within its page`() {
        pages.forEach { page ->
            val ids = page.allNodes().map { it.id }
            val duplicates = ids.groupingBy { it }.eachCount().filterValues { it > 1 }.keys

            assertEquals(
                "Duplicate node ids in ${page.pageId}: $duplicates",
                emptySet<String>(),
                duplicates,
            )
        }
    }

    @Test
    fun `every state key a condition reads is written by something`() {
        pages.forEach { page ->
            val readKeys = page.allNodes().mapNotNull { it.visibleWhen?.key }.toSet()

            val writtenKeys = page.initialState.keys +
                page.sharedStateKeys +
                page.allActions()
                    .filter { it.type == "set_state" || it.type == "toggle_state" }
                    .mapNotNull { it.params["key"] } +
                page.allNodes().mapNotNull { node ->
                    node.props["stateKey"]?.toString()?.trim('"')
                } +
                page.allNodes().mapNotNull { node ->
                    node.props["wishKey"]?.toString()?.trim('"')
                }

            val orphans = readKeys - writtenKeys
            assertEquals(
                "${page.pageId} gates sections on keys nothing sets: $orphans",
                emptySet<String>(),
                orphans,
            )
        }
    }

    @Test
    fun `every template placeholder refers to a key the page can supply`() {
        val placeholder = Regex("""\{\{\s*state\.([A-Za-z0-9_.-]+)\s*(\|[^}]*)?\}\}""")

        pages.forEach { page ->
            val supplied = page.initialState.keys +
                page.sharedStateKeys +
                page.allActions()
                    .filter { it.type == "set_state" || it.type == "toggle_state" }
                    .mapNotNull { it.params["key"] } +
                ROUTE_SUPPLIED_KEYS

            val referenced = placeholder.findAll(page.rawText())
                .filter { it.groupValues[2].isEmpty() }
                .map { it.groupValues[1] }
                .toSet()

            val unresolvable = referenced - supplied
            assertEquals(
                "${page.pageId} interpolates keys with no source and no default: $unresolvable",
                emptySet<String>(),
                unresolvable,
            )
        }
    }

    private fun SduiPage.rawText(): String = SduiJson.format.encodeToString(this)

    private fun SduiPage.allNodes(): List<SduiNode> = sections.flatMap { it.selfAndDescendants() }

    private fun SduiNode.selfAndDescendants(): List<SduiNode> =
        listOf(this) +
            children.flatMap { it.selfAndDescendants() } +
            (fallback?.selfAndDescendants() ?: emptyList()) +
            actions.values.flatMap { it.contentNodes() }

    private fun SduiAction.contentNodes(): List<SduiNode> =
        content.flatMap { it.selfAndDescendants() } + then.flatMap { it.contentNodes() }

    private fun SduiPage.allActions(): List<SduiAction> =
        allNodes().flatMap { node -> node.actions.values.flatMap { it.selfAndChained() } } +
            allNodes().flatMap { node -> node.propActions() }

    private fun SduiAction.selfAndChained(): List<SduiAction> =
        listOf(this) + then.flatMap { it.selfAndChained() }

    private fun SduiNode.propActions(): List<SduiAction> {
        val text = props.toString()
        if (!text.contains("\"action\"")) return emptyList()
        return ACTION_KEYS.flatMap { key ->
            Regex("\"$key\"\\s*:\\s*(\\{)").findAll(text).mapNotNull { match ->
                val json = text.substring(match.range.last).balancedObject() ?: return@mapNotNull null
                runCatching { SduiJson.format.decodeFromString<SduiAction>(json) }.getOrNull()
            }.toList()
        }.flatMap { it.selfAndChained() }
    }

    private fun String.balancedObject(): String? {
        var depth = 0
        var inString = false
        var escaped = false
        forEachIndexed { index, ch ->
            when {
                escaped -> escaped = false
                ch == '\\' -> escaped = true
                ch == '"' -> inString = !inString
                inString -> Unit
                ch == '{' -> depth++
                ch == '}' -> {
                    depth--
                    if (depth == 0) return substring(0, index + 1)
                }
            }
        }
        return null
    }

    private companion object {
        val ROUTE_SUPPLIED_KEYS = setOf("carId", "name", "price", "emi", "specs", "image")

        val ACTION_KEYS = listOf("action")

        fun locatePayloads(): File {
            var dir: File? = File("").absoluteFile
            while (dir != null) {
                val candidate = File(dir, "data/src/main/assets/sdui")
                if (candidate.isDirectory) return candidate
                dir = dir.parentFile
            }
            error("Could not locate data/src/main/assets/sdui from ${File("").absolutePath}")
        }
    }
}
