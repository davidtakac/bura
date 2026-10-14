---
mode: testing
target: emulator
app: ./app.apk
max_steps: 30
tags: [smoke, places]
---

# Search for a place and see its forecast

## Start without a place
Wait for the app to finish loading.
Verify the screen says "No selected place." and shows a "Select place" button.

## Search for a place
Tap the "Select place" button.
Type "Zagreb" into the "Search places…" field and submit the search.
Verify the search results list a place named "Zagreb" in Croatia.

## Open the forecast
Tap the "Zagreb" result in Croatia.
Wait for the forecast to load.
Verify the screen shows the current temperature with a degree sign (°) and no "Failed to download forecast" error.
