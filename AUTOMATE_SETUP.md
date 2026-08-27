# Automate setup for BigButtons v0.2

BigButtons sends Android broadcasts to Automate.

By default every mode uses:

- Broadcast action: `com.bigbuttons.COMMAND`
- Target package: `com.llamalab.automate`
- Command extra key: `command`

## Receiver flow

1. Create a flow in Automate.
2. Add **Broadcast receive**.
3. Set **Action** to `com.bigbuttons.COMMAND`.
4. Set **Broadcast extras** output variable to `extras`.
5. Start the flow and leave it waiting at Broadcast receive.

The main command is:

`extras["command"]`

BigButtons also sends:

- `extras["source"]` = `bigbuttons`
- `extras["event_type"]` = `startup` or `button`
- `extras["mode_index"]`
- `extras["mode_name"]`
- `extras["button_index"]` for button events
- `extras["button_label"]` for button events

## Mode startup examples

Default startup command names are:

- Solo Driving: `solo_start`
- Family: `family_start`
- Work: `work_start`

A mode can optionally:

1. Open Automate first.
2. Wait a configurable delay.
3. Send its startup command.
4. Return to BigButtons.

This is intended to help when Android has stopped or suspended the receiver app.

Opening Automate does not itself start a stopped Automate flow. Keep the receiver flow running and configure Automate/Android background settings appropriately.

## Example branching

Your Automate flow can branch on:

`extras["command"] = "solo_start"`

or:

`extras["command"] = "driving_playlist"`

Then loop back to Broadcast receive after handling the command.
