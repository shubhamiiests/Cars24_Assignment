package com.cars24.sdui.schema

import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class SduiSchemaTest {

    private val json = SduiJson.format

    @Test
    fun `unknown top level fields do not fail the parse`() {
        val payload = """
            {
              "pageId": "home",
              "schemaVersion": 2,
              "experimentBucket": "b",
              "sections": [
                { "id": "t1", "type": "text", "props": { "value": "hi" }, "trackingSlot": 4 }
              ]
            }
        """.trimIndent()

        val page = json.decodeFromString<SduiPage>(payload)

        assertEquals("home", page.pageId)
        assertEquals(1, page.sections.size)
        assertEquals("hi", page.sections[0].props["value"]?.jsonPrimitive?.content)
    }

    @Test
    fun `props of an unknown component type survive parsing`() {
        val payload = """
            {
              "pageId": "home",
              "sections": [
                { "id": "x", "type": "hologram_viewer", "props": { "model": "swift" } }
              ]
            }
        """.trimIndent()

        val page = json.decodeFromString<SduiPage>(payload)

        assertEquals("hologram_viewer", page.sections[0].type)
        assertEquals("swift", page.sections[0].props["model"]?.jsonPrimitive?.content)
    }

    @Test
    fun `a node can carry a fallback for clients that are too old`() {
        val payload = """
            {
              "pageId": "home",
              "sections": [
                {
                  "id": "new", "type": "ar_showroom", "minSchemaVersion": 9,
                  "fallback": { "id": "new_fb", "type": "text", "props": { "value": "Update the app" } }
                }
              ]
            }
        """.trimIndent()

        val node = json.decodeFromString<SduiPage>(payload).sections.single()

        assertEquals(9, node.minSchemaVersion)
        assertNotNull(node.fallback)
        assertEquals("text", node.fallback?.type)
    }

    @Test
    fun `edge shorthand is overridden by specific sides`() {
        val edges = json.decodeFromString<SduiEdges>("""{ "all": 16, "top": 0 }""")

        assertEquals(16, edges.resolvedStart)
        assertEquals(16, edges.resolvedEnd)
        assertEquals(16, edges.resolvedBottom)
        assertEquals(0, edges.resolvedTop)
    }

    @Test
    fun `conditions gate on the page state map`() {
        val onlyPetrol = SduiCondition(key = "fuel", equals = "petrol")

        assertTrue(onlyPetrol.evaluate(mapOf("fuel" to "petrol")))
        assertTrue(!onlyPetrol.evaluate(mapOf("fuel" to "diesel")))
        assertTrue(!onlyPetrol.evaluate(emptyMap()))

        val anyOf = SduiCondition(key = "fuel", oneOf = listOf("petrol", "cng"))
        assertTrue(anyOf.evaluate(mapOf("fuel" to "cng")))
        assertTrue(!anyOf.evaluate(mapOf("fuel" to "diesel")))

        val absent = SduiCondition(key = "coupon", exists = false)
        assertTrue(absent.evaluate(emptyMap()))
        assertTrue(!absent.evaluate(mapOf("coupon" to "SAVE10")))
    }

    @Test
    fun `templates substitute state and honour fallbacks`() {
        val state = mapOf("tenure" to "48", "city" to "Noida")

        assertEquals("48 months", SduiTemplate.resolve("{{state.tenure}} months", state))
        assertEquals("Noida", SduiTemplate.resolve("{{state.city|Gurgaon}}", state))
        assertEquals("Gurgaon", SduiTemplate.resolve("{{state.city|Gurgaon}}", emptyMap()))
    }

    @Test
    fun `an unresolvable placeholder stays visible instead of blanking`() {
        assertEquals("{{state.typo}}", SduiTemplate.resolve("{{state.typo}}", emptyMap()))
    }

    @Test
    fun `resolving props without placeholders returns the same instance`() {
        val props = json.parseToJsonElement("""{ "value": "Browse cars", "count": 12 }""")

        assertSame(props, SduiTemplate.resolve(props, mapOf("tenure" to "48")))
    }

    @Test
    fun `resolving props with placeholders rewrites only the matching leaves`() {
        val props = json.parseToJsonElement(
            """{ "title": "EMI for {{state.tenure}} months", "subtitle": "fixed", "rows": ["{{state.tenure}}"] }""",
        )

        val resolved = SduiTemplate.resolve(props, mapOf("tenure" to "36"))

        assertEquals(
            """{"title":"EMI for 36 months","subtitle":"fixed","rows":["36"]}""",
            resolved.toString(),
        )
    }

    @Test
    fun `actions keep an unknown type instead of refusing to parse`() {
        val payload = """
            {
              "id": "cta", "type": "button",
              "actions": {
                "onClick": { "type": "teleport", "params": { "to": "mars" } }
              }
            }
        """.trimIndent()

        val action = json.decodeFromString<SduiNode>(payload).actions["onClick"]

        assertEquals("teleport", action?.type)
        assertEquals("mars", action?.params?.get("to"))
    }

    @Test
    fun `actions can chain and carry sheet content`() {
        val payload = """
            {
              "id": "cta", "type": "button",
              "actions": {
                "onClick": {
                  "type": "open_bottom_sheet",
                  "params": { "sheetId": "emi" },
                  "content": [ { "id": "s1", "type": "text", "props": { "value": "EMI details" } } ],
                  "then": [ { "type": "track_event", "params": { "name": "emi_sheet_open" } } ]
                }
              }
            }
        """.trimIndent()

        val action = json.decodeFromString<SduiNode>(payload).actions.getValue("onClick")

        assertEquals("emi", action.params["sheetId"])
        assertEquals("text", action.content.single().type)
        assertEquals("track_event", action.then.single().type)
    }

    @Test
    fun `absent optional blocks stay null rather than becoming empty objects`() {
        val node = json.decodeFromString<SduiNode>("""{ "id": "a", "type": "spacer" }""")

        assertNull(node.style)
        assertNull(node.visibleWhen)
        assertTrue(node.children.isEmpty())
        assertEquals(1, node.minSchemaVersion)
    }
}
