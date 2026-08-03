package com.cars24.core.common.perf

import android.os.Process
import android.os.SystemClock
import android.util.Log
import androidx.tracing.Trace

object StartupTrace {

    const val LOG_TAG = "Cars24Perf"

    const val MARK_FIRST_SECTION_DRAWN = "first_section_drawn"
    const val MARK_INTERACTIVE = "interactive"

    const val MARK_FULL_PAGE = "full_page_rendered"

    private val marks = linkedMapOf<String, Long>()

    private val processStartUptimeMs: Long by lazy { Process.getStartUptimeMillis() }

    fun sinceProcessStart(): Long = SystemClock.uptimeMillis() - processStartUptimeMs

    fun mark(name: String) {
        if (marks.containsKey(name)) return
        val elapsed = sinceProcessStart()
        marks[name] = elapsed
        Trace.setCounter(name, elapsed.toInt())
        Log.i(LOG_TAG, "$name=${elapsed}ms")
    }

    fun snapshot(): Map<String, Long> = marks.toMap()

    fun reset() = marks.clear()

    inline fun <T> measured(section: String, block: () -> T): Pair<T, Long> {
        Trace.beginSection(section)
        val start = SystemClock.elapsedRealtimeNanos()
        try {
            val result = block()
            val millis = (SystemClock.elapsedRealtimeNanos() - start) / 1_000_000
            return result to millis
        } finally {
            Trace.endSection()
        }
    }
}
