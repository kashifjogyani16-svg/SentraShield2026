# SentraShield

Network monitoring & lightweight antivirus-style Android app with an
iPhone-style "Dynamic Island" water-wave alert overlay.

## Features
- Water-wave animated Dynamic Island overlay (auto-positions at notch)
- Foreground network monitoring service (per-app rx/tx tracking)
- Heuristic threat analyzer (dangerous permission combos, sideload/debug checks)
- VPN / TOR detection
- Optional VirusTotal file-hash lookup
- Optional Gemini AI analysis
- Jetpack Compose Material 3 dashboard
- Room database for usage + threat history
- Boot-persistent monitoring

## Requirements
- Android Studio Hedgehog+ or a JDK 17 + Android SDK (cmdline-tools) for
  building from Termux/CLI
- Min SDK 24, Target SDK 34

## Setup (free API keys)

Both AI/lookup integrations are **optional** — the app works fully without
them (threat scoring, monitoring, and the Dynamic Island all work offline).
To enable them, get free keys and put them in `local.properties`
(this file is git-ignored, never commit it):

```properties
sdk.dir=/path/to/Android/sdk
VIRUSTOTAL_API_KEY=your_free_virustotal_key
GEMINI_API_KEY=your_free_gemini_key
```

- VirusTotal free API key: https://www.virustotal.com/gui/join-us (Public API, free tier)
- Gemini free API key: https://aistudio.google.com/app/apikey (Google AI Studio, free tier)

If a key is left blank, that feature is simply skipped at runtime — no crash.

## Building from Termux (on-device, no PC)

```bash
pkg update && pkg upgrade -y
pkg install -y openjdk-17 git wget
# Install Android SDK cmdline-tools (one-time):
mkdir -p ~/android-sdk/cmdline-tools && cd ~/android-sdk/cmdline-tools
wget https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip
unzip commandlinetools-linux-*.zip && mv cmdline-tools latest
export ANDROID_SDK_ROOT=~/android-sdk
export PATH=$PATH:$ANDROID_SDK_ROOT/cmdline-tools/latest/bin:$ANDROID_SDK_ROOT/platform-tools
yes | sdkmanager --licenses
sdkmanager "platform-tools" "platforms;android-34" "build-tools;34.0.0"

cd ~/SentraShield
echo "sdk.dir=$ANDROID_SDK_ROOT" > local.properties
echo "VIRUSTOTAL_API_KEY=" >> local.properties
echo "GEMINI_API_KEY=" >> local.properties

chmod +x gradlew
./gradlew assembleDebug
# APK output: app/build/outputs/apk/debug/app-debug.apk
```

## Pushing updates to GitHub from Termux

```bash
cd ~/SentraShield
git init                                   # first time only
git remote add origin https://github.com/<your-user>/SentraShield.git   # first time only
git add -A
git commit -m "Update SentraShield"
git branch -M main
git push -u origin main
```

For subsequent updates just:

```bash
git add -A && git commit -m "message" && git push
```

Use a GitHub Personal Access Token as the password when prompted
(GitHub no longer accepts account passwords over HTTPS git push).

## Permissions
`SYSTEM_ALERT_WINDOW` (overlay), `PACKAGE_USAGE_STATS`, and
`QUERY_ALL_PACKAGES` must be granted manually in system settings — the
app links you there from the dashboard's "Grant overlay permission" and
first-run prompts.
