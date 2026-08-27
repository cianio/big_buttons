# BigButtons v0.2

BigButtons is a configurable Android control surface designed around large, low-distraction buttons.

It works especially well as a front end for LlamaLab Automate, but the broadcast receiver is configurable.

## v0.2 highlights

### Modes

Three independent modes are included by default:

- Solo Driving
- Family
- Work

Each mode has:

- its own name
- six button labels
- six command strings
- its own broadcast action
- its own target package
- its own startup command
- its own target-app wake setting
- its own startup delay
- an option to return to BigButtons after waking the target

### Startup automation

A mode can automatically run this sequence when BigButtons opens in that mode or when you switch to it:

```text
Open target app
    ->
wait
    ->
send startup broadcast
    ->
return to BigButtons
```

For example, Solo Driving can open Automate, wait 900 ms and send:

```text
command = solo_start
```

Automate can then perform the real car setup.

### Interface refresh

v0.2 has a cleaner dark interface with rounded surfaces, a visible active-mode header and a dedicated mode picker.

The default UI uses text labels only. No emoji labels are used.

### v0.1 migration

If v0.2 is installed over v0.1 using the same package/signing key, existing v0.1 button labels, commands and receiver settings are migrated into the Solo Driving mode.

For existing v0.1 users, Solo Driving startup is enabled during migration so the new wake-and-start workflow can be tested immediately.

## GitHub APK build

Push the project to GitHub. The included workflow builds:

```text
app/build/outputs/apk/debug/app-debug.apk
```

Open:

Actions -> Build Android APK -> latest successful run -> Artifacts

Download `BigButtons-debug-apk`, unzip it and install the APK.

## Current limits

v0.2 intentionally does not yet include:

- custom button images
- custom button colours
- arbitrary numbers of modes
- arbitrary startup action sequences
- signed release builds
- profile import/export
- Material 3 / Jetpack Compose dependencies

Those are better candidates for later versions after the modes architecture has been tested in real use.
