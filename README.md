# Big Buttons

A deliberately simple Android "driving remote": six enormous configurable buttons that send command strings to **Automate** (or another Android broadcast receiver).

The point is not to duplicate Automate. Big Buttons is the low-distraction front end; Automate does the powerful automation behind it.

## V1 features

- Six huge buttons
- Portrait layout: 2 × 3
- Landscape layout: 3 × 2
- Custom label for every button
- Custom command string for every button
- Configurable broadcast action
- Configurable broadcast extra key
- Configurable target package
- Defaults configured for LlamaLab Automate
- Short vibration acknowledgement
- Optional keep-screen-awake mode
- Separate edit screen
- No internet permission
- No account
- No analytics
- No Android Studio required to build with GitHub Actions

## Default broadcast

When the HOME button is tapped, the app sends approximately:

```text
Action:  com.bigbuttons.COMMAND
Package: com.llamalab.automate

Extras:
command      = "home"
source       = "bigbuttons"
button_index = 0
button_label = "🏠 HOME"
```

See `AUTOMATE_SETUP.md` for the Automate side.

## Build the APK on GitHub

1. Create a new GitHub repository.
2. Upload/push all the files in this project.
3. Open the repository's **Actions** tab.
4. Open **Build Android APK**.
5. Wait for the build to finish.
6. Open the completed workflow run.
7. Under **Artifacts**, download `BigButtons-debug-apk`.
8. Unzip it and install `app-debug.apk` on your Android phone.

Android may ask you to allow installation from your browser/file manager because this is a sideloaded debug build.

The workflow can also be started manually using **Actions → Build Android APK → Run workflow**.

## Toolchain

The GitHub workflow uses:

- Java 17
- Android SDK / compileSdk 36
- Android Build Tools 36.0.0
- Android Gradle Plugin 9.3.0
- Gradle 9.5.0

The app has `minSdk = 26` (Android 8.0+) and `targetSdk = 36`.

## Local build (optional)

You do not need this for the GitHub workflow.

If you later install the Android SDK and Gradle 9.5 locally:

```bash
gradle :app:assembleDebug
```

The APK will be at:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Safety / driving note

The UI is intentionally designed around large touch targets and minimal interaction. Configure the buttons while parked. Do not use actions that require reading, typing, or complex interaction while driving.

## Possible V2 features

- More/fewer buttons
- Per-button colours
- Multiple pages
- Long-press actions
- Button icons
- Spoken acknowledgement
- Intent/URI modes in addition to broadcasts
- "Driving lock" to hide all editing controls
- Import/export configuration
# big_buttons
