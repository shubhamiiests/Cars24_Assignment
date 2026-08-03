package com.cars24.benchmark

import android.content.Intent
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeScrollBenchmark {

    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun sduiScrollJank() = measureScroll(static = false)

    @Test
    fun staticScrollJank() = measureScroll(static = true)

    private fun measureScroll(static: Boolean) {
        benchmarkRule.measureRepeated(
            packageName = TARGET_PACKAGE,
            metrics = listOf(FrameTimingMetric()),
            iterations = ITERATIONS,
            startupMode = StartupMode.WARM,
            setupBlock = {
                startActivityAndWait(
                    Intent().apply {
                        setClassName(TARGET_PACKAGE, TARGET_ACTIVITY)
                        putExtra(EXTRA_STATIC_BASELINE, static)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    },
                )
                device.wait(Until.hasObject(By.textContains("Find your next car")), CONTENT_TIMEOUT_MS)
            },
        ) {
            fun fling(direction: Direction) {
                val list = device.findObject(By.scrollable(true))
                    ?: error("No scrollable page found - the payload failed to render")

                list.setGestureMargin(device.displayWidth / 5)
                list.fling(direction, FLING_SPEED)
                device.waitForIdle()
            }

            repeat(FLINGS_EACH_WAY) { fling(Direction.DOWN) }
            repeat(FLINGS_EACH_WAY) { fling(Direction.UP) }
        }
    }

    private companion object {
        const val TARGET_PACKAGE = "com.cars24.sdui"
        const val TARGET_ACTIVITY = "com.cars24.sdui.MainActivity"
        const val EXTRA_STATIC_BASELINE = "com.cars24.sdui.STATIC_BASELINE"
        const val ITERATIONS = 6
        const val FLINGS_EACH_WAY = 4
        const val FLING_SPEED = 8_000
        const val CONTENT_TIMEOUT_MS = 5_000L
    }
}
