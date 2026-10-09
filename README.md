# Battery Pet

A cute native Android app: a live kawaii pet that sits on your phone's battery icon
in the status bar, reacting to your real battery level in real time.

## Features

- **Status-bar pet overlay** — draws a custom battery icon exactly over your
  phone's real battery icon, with your pet sitting on top of it. The overlay is
  non-touchable, so taps pass straight through to the status bar underneath.
- **Live battery moods** — the pet reacts to your real battery:
  charging = happy, 20% or less = sleepy, otherwise content.
- **4 pets** — Bunny, Cat, Bear, Dino, each with normal / happy / sleepy art,
  all hand-drawn kawaii vector stickers.
- **3 themes** — Pastel Pink, Coquette Cream, Lavender Dream: each recolors the
  battery icon outline and fill.
- **Position calibration** — drag the icon once so it covers your real battery
  icon perfectly on any phone.
- **Home-screen widget** — your pet + live battery % on the home screen.
- **Auto-restart** — the overlay comes back after a reboot if left enabled.

## How the build works (no computer needed)

Every push to the `main` branch triggers GitHub Actions
(`.github/workflows/build.yml`):

1. Checks out the code on a free Ubuntu runner.
2. Installs JDK 17, the Android SDK (platform 36), and Gradle 8.10.
3. Runs `gradle assembleDebug bundleDebug` — pure Android SDK, zero
   third-party dependencies.
4. Uploads two artifacts you can download from the Actions tab:
   - `app-debug-apk` — install directly on your phone to test.
   - `app-debug-aab` — the Play Store bundle format for release.

## Install on your phone

1. Open the latest successful workflow run under the **Actions** tab.
2. Download the `app-debug-apk` artifact and unzip it.
3. Copy `app-debug.apk` to your phone, open it, and allow
   **"Install unknown apps"** when asked.
4. Open **Battery Pet**, pick a pet and theme, then turn on
   **"Show pet on status bar"** and grant **"Display over other apps"**.
5. Tap **"Position the icon"** and drag the pet over your real battery icon.

## Permissions explained

| Permission | Why the app needs it |
|---|---|
| Display over other apps (`SYSTEM_ALERT_WINDOW`) | Draws the pet + battery icon over the status bar. You grant this manually via a system screen. |
| Foreground service (`specialUse`) | Keeps the overlay alive with a low-priority persistent notification ("Your pet is on duty"). |
| Run at startup | Restarts the overlay after a reboot (only if you left it enabled). |
| Notifications (Android 13+) | Required to show the foreground-service notification. |

## Roadmap

- More pets (fox, panda, axolotl…) and gamer/hacker theme packs.
- Tap-the-pet interactions on the overlay.
- Battery-full and low-battery celebration alerts.
- Release signing + Play Store closed testing track.
