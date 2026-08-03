package com.cars24.benchmark

import android.content.Intent
import androidx.benchmark.macro.ExperimentalMetricApi
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.TraceSectionMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeStartupBenchmark {

    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun sduiColdStartup() = measureColdStartup(static = false)

    @Test
    fun staticColdStartup() = measureColdStartup(static = true)

    @OptIn(ExperimentalMetricApi::class)
    private fun measureColdStartup(static: Boolean) {
        val metrics = buildList {
            add(StartupTimingMetric())
            if (!static) {
                add(TraceSectionMetric("sdui_json_parse", mode = TraceSectionMetric.Mode.Sum))
            }
        }

        benchmarkRule.measureRepeated(
            packageName = TARGET_PACKAGE,
            metrics = metrics,
            iterations = ITERATIONS,
            startupMode = StartupMode.COLD,
            setupBlock = { pressHome() },
        ) {
            startActivityAndWait(
                Intent().apply {
                    setClassName(TARGET_PACKAGE, TARGET_ACTIVITY)
                    putExtra(EXTRA_STATIC_BASELINE, static)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                },
            )

            device.wait(Until.hasObject(By.textContains("Find your next car")), CONTENT_TIMEOUT_MS)
        }
    }

    private companion object {
        const val TARGET_PACKAGE = "com.cars24.sdui"
        const val TARGET_ACTIVITY = "com.cars24.sdui.MainActivity"
        const val EXTRA_STATIC_BASELINE = "com.cars24.sdui.STATIC_BASELINE"
        const val ITERATIONS = 10
        const val CONTENT_TIMEOUT_MS = 5_000L
    }
}
