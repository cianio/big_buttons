# Automate setup

Big Buttons sends an Android broadcast when a button is tapped.

Default values:

- Broadcast action: `com.bigbuttons.COMMAND`
- Target package: `com.llamalab.automate`
- Extra key: `command`
- Example command: `home`

## Minimal Automate flow

1. Create a new flow in Automate.
2. Add a **Broadcast receive** block.
3. Set **Action** to:

   `com.bigbuttons.COMMAND`

4. Set **Broadcast extras** output variable to:

   `extras`

5. The command sent by Big Buttons is then available in Automate as:

   `extras["command"]`

6. Branch on that value. For example:

   - `extras["command"] = "home"`
   - `extras["command"] = "music"`
   - `extras["command"] = "navigate"`
   - `extras["command"] = "call"`
   - `extras["command"] = "gate"`
   - `extras["command"] = "custom"`

7. Loop your flow back to **Broadcast receive** so it waits for the next button press.

Big Buttons also sends these optional extras:

- `source` = `bigbuttons`
- `button_index` = 0 through 5
- `button_label` = the visible button label

## Troubleshooting

- The Automate flow must be running and waiting at **Broadcast receive**.
- Leave the default target package as `com.llamalab.automate`.
- If you intentionally want another receiver app to hear the broadcast, clear the target package field in Big Buttons settings.
- Make sure the action text matches exactly on both sides.
