# Automate setup

In BigButtons, enable **Use Automate integration**.

In Automate:

1. Add **Broadcast receive**.
2. Set Action to `com.bigbuttons.COMMAND`.
3. Set Broadcast extras output to `extras`.
4. Start the flow.

The command is `extras["command"]`.

BigButtons also sends `source`, `event_type`, `mode_index`, `mode_name`, and for button events `button_index` and `button_label`.

To test, connect Broadcast receive to Toast show with `extras["command"]`, then use BigButtons Settings -> Automate setup instructions -> **SEND TEST**. You should see `bigbuttons_test`.
