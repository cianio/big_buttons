# BigButtons v0.3

BigButtons is a configurable Android control surface built around large, low-distraction buttons, especially useful as a front end for LlamaLab Automate.

## What's new in 0.3

- **Pages inside modes**. Every mode has a Main page and can have additional pages such as Playlists or Navigation.
- A button can **Send command**, **Open page**, or **Go back**.
- Visual 2 x 3 page/button editor.
- Cleaner driving header: mode name is the picker, settings is a cog, no `ACTIVE MODE`, no successful `Sent:` text.
- **Use Automate integration** preset. Automate package/action/key are filled automatically and the raw receiver fields become read-only.
- Built-in Automate setup instructions and a `bigbuttons_test` command.
- Clearer startup labels such as `Startup delay` + `milliseconds` and `Startup command` + example.
- New BigButtons launcher icon.
- Persistent **development-only** signing key for GitHub debug APKs from v0.3 onward.

## Pages example

```text
Solo Driving
+-- Main
|   +-- Playlists -> opens Playlists
|   +-- Navigation
|   +-- Call
|   +-- Home
|   +-- Pause
|
+-- Playlists
    +-- Driving Mix
    +-- Favourites
    +-- Chill
    +-- Podcasts
    +-- Back
```

## Automate preset

When enabled, BigButtons uses:

- target package: `com.llamalab.automate`
- broadcast action: `com.bigbuttons.COMMAND`
- command key: `command`

In Automate, use **Broadcast receive**, set the Action to `com.bigbuttons.COMMAND`, and set Broadcast extras output to `extras`. The command is `extras["command"]`.

## Updating from v0.2

v0.3 has the same package id but a new persistent development signing key. Because v0.2 GitHub builds used an ephemeral debug key, Android may refuse to update it. **Uninstall v0.2 once, then install v0.3.** Future v0.3+ debug builds using this repository key can update one another.

The included key is only for development. A private production key should be used for a public release.

## Build on GitHub

Push the repo. The included Action builds `app-debug.apk`. Download it from the successful workflow run's `BigButtons-debug-apk` artifact.

## Deferred

Button colours/icons, installed-app picker, gestures/double-tap/hold, production signing, profile import/export.
