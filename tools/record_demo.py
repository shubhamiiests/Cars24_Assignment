#!/usr/bin/env python3
"""Drives the demo on-device while adb screenrecord captures the screen.

Beats, in the order the brief lists them:
  1. cold open, page rendered from JSON
  2. chip selection changes content (SDUI actions, no client code)
  3. tenure selector rewrites the EMI; the CTA opens a sheet that is itself SDUI
  4. unknown-component fallback and the version-gated fallback
  5. payload-driven navigation, wishlist, city picker
  6. live JSON edit: push a file, pull to refresh, page changes with no rebuild

Coordinates are hardcoded from a calibration pass rather than looked up at runtime.
`uiautomator dump` needs an idle window and cannot get one while screenrecord is running -
it fails, leaves the previous ui.xml on disk, and every subsequent tap silently lands on
wherever the stale dump said the target was.
"""
import os
import subprocess
import time

PKG = "com.cars24.sdui.debug"
ACT = PKG + "/com.cars24.sdui.MainActivity"
OVERRIDE = "/sdcard/Android/data/%s/files/sdui/home.json" % PKG
HERE = os.path.dirname(os.path.abspath(__file__))
ADB = os.path.expanduser("~/Library/Android/sdk/platform-tools/adb")

# One "page" of scroll. 400ms over 1100px, which does not fling.
DOWN = (540, 1800, 540, 700, 400)
UP = (540, 800, 540, 1900, 400)

# Calibrated per scroll position: POS0 = top, POS1 = one DOWN from top, and so on.
POS0 = {"city": (173, 238), "buy_car": (166, 856),
        "all": (106, 1720), "diesel": (615, 1720), "cng": (856, 1720)}
POS1 = {"t36": (159, 1227), "t48": (413, 1227), "t72": (921, 1227),
        "breakdown": (540, 1879)}
POS2 = {"breakdown": (540, 877), "saved_cars": (289, 1578)}
CITY_SHEET = {"mumbai": (540, 1690)}


def sh(*args):
    return subprocess.run([ADB] + list(args), capture_output=True, text=True).stdout


def swipe(spec, wait=1.3):
    sh("shell", "input", "swipe", *[str(v) for v in spec])
    time.sleep(wait)


def tap(xy, wait=2.0, label=""):
    sh("shell", "input", "tap", str(xy[0]), str(xy[1]))
    if label:
        print("    tap %s" % label)
    time.sleep(wait)


def back(wait=1.8):
    sh("shell", "input", "keyevent", "KEYCODE_BACK")
    time.sleep(wait)


def main():
    sh("shell", "am", "force-stop", PKG)
    sh("shell", "rm", "-f", OVERRIDE)
    sh("shell", "pm", "clear", PKG)
    time.sleep(1)

    rec = subprocess.Popen(
        [ADB, "shell", "screenrecord", "--bit-rate", "8000000",
         "--time-limit", "180", "/sdcard/demo.mp4"])
    time.sleep(2)
    t0 = time.time()

    def beat(label):
        print("[%5.1fs] %s" % (time.time() - t0, label))

    # --- 1. cold open: skeleton, then 43 sections rendered from JSON ------------
    beat("cold open, page renders from JSON")
    sh("shell", "am", "start", "-n", ACT)
    time.sleep(6.5)
    swipe(DOWN, 1.6)
    swipe(DOWN, 1.6)
    swipe(UP, 1.4)
    swipe(UP, 2.2)

    # --- 2. chips swap which cars are listed, driven only by JSON ---------------
    beat("fuel chips change content")
    tap(POS0["diesel"], 2.8, "Diesel")
    tap(POS0["cng"], 2.8, "CNG")
    tap(POS0["all"], 2.2, "All")

    # --- 3. tenure selector rewrites the EMI, CTA opens an SDUI sheet -----------
    beat("tenure selector + bottom sheet")
    swipe(DOWN, 1.8)
    tap(POS1["t36"], 2.6, "36 mo")
    tap(POS1["t72"], 2.6, "72 mo")
    tap(POS1["t48"], 2.2, "48 mo")
    tap(POS1["breakdown"], 3.4, "See full breakdown")
    swipe((540, 1400, 540, 950, 400), 2.2)
    back(2.2)

    # --- 4. both degradation paths, live on the happy path ---------------------
    beat("unknown component + version fallback")
    swipe(DOWN, 1.1)
    swipe(DOWN, 1.1)
    swipe(DOWN, 1.4)
    time.sleep(5.0)

    # --- 5. wishlist page: gated entirely on shared state ----------------------
    beat("wishlist page")
    swipe(UP, 1.1)
    swipe(UP, 1.8)
    tap(POS2["saved_cars"], 3.4, "Saved cars")
    back(2.2)

    # --- 6. navigate to another SDUI page ---------------------------------------
    beat("navigate to a destination page")
    for _ in range(4):
        swipe(UP, 0.6)
    time.sleep(1.2)
    tap(POS0["buy_car"], 3.6, "Buy car")
    back(2.2)

    # --- 7. city picker writes app-wide state ----------------------------------
    beat("city picker")
    tap(POS0["city"], 2.6, "Gurgaon")
    tap(CITY_SHEET["mumbai"], 3.0, "Mumbai")

    # --- 8. live edit: push new JSON, pull to refresh, no rebuild --------------
    beat("push edited JSON to the device")
    subprocess.run([ADB, "push", os.path.join(HERE, "home_live_edit.json"), OVERRIDE],
                   capture_output=True)
    time.sleep(1.2)
    beat("pull to refresh - page changes, same APK")
    swipe((540, 1000, 540, 1950, 500), 5.5)
    time.sleep(4.0)
    subprocess.run([ADB, "exec-out", "screencap", "-p"],
                   stdout=open(os.path.join(HERE, "proof_live_edit.png"), "wb"))
    swipe(DOWN, 3.0)
    swipe(DOWN, 3.0)
    swipe(UP, 2.5)

    # --- 9. the same payload in dark mode: colours are tokens, not hex ----------
    beat("dark mode - style tokens resolve per theme")
    sh("shell", "cmd", "uimode", "night", "yes")
    time.sleep(4.0)
    swipe(DOWN, 2.5)
    swipe(UP, 2.5)
    sh("shell", "cmd", "uimode", "night", "no")
    time.sleep(2.5)

    # --- 10. first launch with no connection ------------------------------------
    beat("offline: no connection, nothing cached")
    sh("shell", "rm", "-f", OVERRIDE)
    sh("shell", "svc", "wifi", "disable")
    sh("shell", "svc", "data", "disable")
    sh("shell", "cmd", "connectivity", "airplane-mode", "enable")
    time.sleep(2.0)
    sh("shell", "pm", "clear", PKG)
    sh("shell", "am", "start", "-n", ACT)
    time.sleep(6.0)
    subprocess.run([ADB, "exec-out", "screencap", "-p"],
                   stdout=open(os.path.join(HERE, "proof_offline.png"), "wb"))
    time.sleep(2.0)

    beat("back online, retry loads the page")
    sh("shell", "cmd", "connectivity", "airplane-mode", "disable")
    sh("shell", "svc", "wifi", "enable")
    sh("shell", "svc", "data", "enable")
    time.sleep(5.0)
    tap((540, 1560), 5.0, "Try again")
    time.sleep(3.0)

    beat("stopping the recorder")
    # SIGINT so screenrecord finalises the container. Killing it outright, or letting the
    # 180s limit expire, leaves either a corrupt file or a long static tail.
    sh("shell", "pkill", "-SIGINT", "screenrecord")
    time.sleep(4)
    rec.wait()
    out = os.path.join(HERE, "cars24_sdui_demo.mp4")
    subprocess.run([ADB, "pull", "/sdcard/demo.mp4", out], capture_output=True)
    sh("shell", "rm", "-f", OVERRIDE)
    print("wrote %s (%.1f MB)" % (out, os.path.getsize(out) / 1e6))


if __name__ == "__main__":
    main()
