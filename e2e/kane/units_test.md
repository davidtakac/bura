---
mode: testing
target: emulator
app: ./app.apk
max_steps: 40
tags: [settings, units]
---

# Switch the temperature unit to Fahrenheit

## Open the settings
Wait for the app to finish loading.
Tap the "Select place" button.
Tap the settings (gear) icon in the search bar.
Verify a "Settings" screen is shown with a "Units" section and a "Temperature" option.

## Choose Fahrenheit
Tap the "Temperature" option and choose "Degrees Fahrenheit".
Verify the "Temperature" option now shows "Degrees Fahrenheit".

## Check the setting is kept
Go back to the previous screen, then tap the settings (gear) icon in the search bar again.
Verify the "Temperature" option still shows "Degrees Fahrenheit".
