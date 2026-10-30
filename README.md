# ⏰ uthoSajib (ওঠো সজীব) — Smart Math Wake-Up Alarm Clock

[![Android CI / Beta Release](https://github.com/hisajib/uthoSajib/actions/workflows/release.yml/badge.svg)](https://github.com/hisajib/uthoSajib/actions/workflows/release.yml)
[![Platform](https://img.shields.io/badge/Platform-Android-green.svg?logo=android)](https://www.android.com/)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0+-purple.svg?logo=kotlin)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20M3-blue.svg?logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![Room DB](https://img.shields.io/badge/Database-Room%20SQLite-orange.svg)](https://developer.android.com/training/data-storage/room)
[![License](https://img.shields.io/badge/License-MIT-lightgrey.svg)](LICENSE)

> **"ঘুম থেকে ওঠার গ্যারান্টি!"**
> **uthoSajib** is an un-snoozeable, persistent morning wake-up smart alarm clock for Android. Designed specifically for heavy sleepers, it guarantees you wake up by requiring you to solve 3 arithmetic math problems before the alarm will ever turn off.

---

## 🌟 Key Features

- 🧠 **Math Challenge Lock (গণিত সমাধান চ্যালেঞ্জ)**:
  - You cannot simply swipe, mute, or hit snooze!
  - Must solve **3 consecutive math problems** to stop the alarm ringing.
  - Generates clear, balanced equations with intuitive numeric keypad input.

- 🔒 **Bulletproof Persistence (Never Stops Until Solved)**:
  - **Survives App Kill**: Even if you swipe away the app from recent apps (task switcher), the alarm keeps ringing uninterrupted in an isolated foreground service.
  - **Lock Screen Bypassing**: Ringing screen shows automatically over locked devices (`showWhenLocked`, `turnScreenOn`, and full-screen intent).
  - **Reboot Resilient**: Alarms are automatically rescheduled on device boot via `BOOT_COMPLETED` broadcast receiver.

- ⏰ **Intuitive Alarm Management**:
  - Scrollable hour/minute picker with AM/PM toggle for rapid setup.
  - Day-of-week repeat selector (e.g., Weekdays, Weekends, Everyday, or One-time).
  - Custom alarm labels, vibration toggles, and sound preferences.
  - One-click individual delete and "Delete All Alarms" with confirmation safeguards.
  - Real-time digital clock display with live countdown to your next active alarm.

- 🎨 **Modern Material 3 Design**:
  - Sleek midnight dark theme (Midnight Navy, Alert Crimson, Dawn Orange, Electric Cyan).
  - High-contrast visuals and fluid animations designed for groggy early-morning eyes.
  - Edge-to-edge system bar integration with dynamic WindowInsets.

---

## 🛠️ Tech Stack & Architecture

- **Language:** [Kotlin 2.0+](https://kotlinlang.org/)
- **UI Toolkit:** [Jetpack Compose](https://developer.android.com/jetpack/compose) with Material Design 3 (M3)
- **Local Persistence:** [Room Database](https://developer.android.com/training/data-storage/room) with Coroutine Flow & StateFlow
- **Background & Alarm Engine:**
  - Android `AlarmManager` with `setExactAndAllowWhileIdle`
  - High-priority `ForegroundService` with `mediaPlayback` type running in an isolated process (`:alarm_service`)
  - `WakeLock` & `MediaPlayer` loop for continuous high-volume sound
- **Architecture Pattern:** MVVM (Model-View-ViewModel) + Repository Pattern
- **Build System:** Gradle (Kotlin DSL `.gradle.kts`) with Version Catalog (`libs.versions.toml`)

---

## 📂 Project Structure

```text
app/src/main/java/com/example/
├── MainActivity.kt               # Main entry point & lock-screen intent handler
├── alarm/
│   ├── AlarmAudioPlayer.kt       # Continuous ringtone & vibration playback engine
│   ├── AlarmLockManager.kt       # WakeLock acquisition & screen management
│   ├── AlarmReceiver.kt          # Exact alarm receiver & BOOT_COMPLETED scheduler
│   ├── AlarmRingingService.kt    # Isolated background ringing service
│   ├── AlarmScheduler.kt         # AlarmManager scheduling logic
│   └── AlarmStateHolder.kt       # State coordinator for active ringing session
├── data/
│   ├── AlarmDao.kt               # Room database DAO with Flow queries
│   ├── AlarmEntity.kt            # Alarm data entity definition
│   ├── AlarmRepository.kt        # Room data repository
│   └── AppDatabase.kt            # Room database instance
├── math/
│   └── MathEngine.kt             # Randomized arithmetic equation generator
└── ui/
    ├── AlarmViewModel.kt         # State holder & business logic
    ├── components/               # Keypad, Math cards, Time selectors
    ├── dialogs/                  # Alarm creation & edit dialogs
    ├── screens/
    │   ├── AlarmListScreen.kt    # Main alarms list & countdown dashboard
    │   └── AlarmRingingScreen.kt # Fullscreen math challenge solve screen
    └── theme/                    # Color schemes, typography, and shapes
```

---

## 🚀 Getting Started

### Prerequisites
- **Android Studio Ladybug (2024.2+)** or newer recommended.
- **JDK 21** (bundled with recent Android Studio versions).
- **Android SDK:**
  - `compileSdk`: **36**
  - `targetSdk`: **36**
  - `minSdk`: **26** (Android 8.0 Oreo+)

### 1. Clone the Repository
```bash
git clone https://github.com/hisajib/uthoSajib.git
cd uthoSajib
```

### 2. Build Debug APK Locally
```bash
# On Linux / macOS:
./gradlew :app:assembleDebug

# On Windows:
gradlew.bat :app:assembleDebug
```
The output APK will be generated at:
`app/build/outputs/apk/debug/app-debug.apk`

### 3. Run Unit Tests
```bash
./gradlew :app:testDebugUnitTest
```

---

## 🤖 Automated CI/CD & GitHub Releases

This project includes a fully automated **GitHub Actions Workflow** located at [`.github/workflows/release.yml`](.github/workflows/release.yml) to automatically compile, sign, and publish Beta APK releases directly to GitHub Releases.

### How to Trigger a New Beta Release:

#### Option A: Push a Git Tag (Recommended)
```bash
# Create a release tag (must start with 'v')
git tag v1.0.0-beta1

# Push the tag to GitHub
git push origin v1.0.0-beta1
```
The GitHub Action will automatically:
1. Check out code and setup JDK 21.
2. Build the signed Release APK.
3. Name it `UthoSajib-v1.0.0-beta1.apk`.
4. Upload it to the GitHub Releases section under tag `v1.0.0-beta1`.

#### Option B: Manual Run (workflow_dispatch)
1. Go to your GitHub repository -> **Actions** tab.
2. Select **Release Beta APK** on the left sidebar.
3. Click **Run workflow**, enter your desired tag (e.g. `v1.0.1-beta`), and click **Run workflow**.

---

## 🔐 Configuring GitHub Secrets (Optional but Recommended)

By default, the release workflow automatically generates a fallback keystore so builds succeed immediately out-of-the-box. For official distribution, you can configure your own production keystore:

1. Navigate to your GitHub repository -> **Settings** -> **Secrets and variables** -> **Actions**.
2. Click **New repository secret** and add:

| Secret Name | Description | Example |
|---|---|---|
| `KEYSTORE_BASE64` | Base64-encoded string of your `.jks` file | `base64 -w 0 my-release-key.jks` |
| `STORE_PASSWORD` | Password for your keystore | `YourStrongKeystorePassword` |
| `KEY_PASSWORD` | Password for your key alias | `YourStrongKeyPassword` |

> 💡 **How to generate the Base64 string from your terminal:**
> ```bash
> base64 -w 0 my-release-key.jks > keystore_base64.txt
> # Copy the content inside keystore_base64.txt to GitHub Secrets!
> ```

---

## 📱 Permissions Used

| Permission | Reason |
|---|---|
| `SCHEDULE_EXACT_ALARM` / `USE_EXACT_ALARM` | Precise millisecond-level alarm triggering via AlarmManager. |
| `POST_NOTIFICATIONS` | Ongoing ringing notifications for Android 13+. |
| `FOREGROUND_SERVICE_MEDIA_PLAYBACK` | Reliable, un-killable audio ringing service. |
| `USE_FULL_SCREEN_INTENT` / `DISABLE_KEYGUARD` | Shows math challenge over the lock screen immediately. |
| `WAKE_LOCK` | Wakes up CPU and screen when the alarm goes off. |
| `VIBRATE` | Device vibration synchronization with alarm sound. |
| `RECEIVE_BOOT_COMPLETED` | Automatically reschedule all active alarms after phone restart. |

---

## 👤 Author & Support

- **Author:** Sajib ([@hisajib](https://github.com/hisajib))
- **Email:** [hisajib.net@gmail.com](mailto:hisajib.net@gmail.com)
- **Feedback & Issues:** If you encounter any bugs or have feature suggestions, please feel free to open an [Issue](https://github.com/hisajib/uthoSajib/issues).

---

## 📄 License

This project is licensed under the [MIT License](LICENSE) — see the LICENSE file for details.
