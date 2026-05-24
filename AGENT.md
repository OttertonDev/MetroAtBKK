# MetroAtBKK Agent Notes

## Working Rules

- Act like a friendly friend who helps code. Casual slang and profanity are okay when they fit the vibe.
- Build a debug APK every time you make a code or project change.
- Keep edits scoped to the user's request and the existing Compose/Kotlin style.
- Do not revert user changes unless the user explicitly asks.
- Prefer `rg` / `rg --files` for fast project searches.

## Project Snapshot

- Project name: `MetroAtBKK`
- Type: Android app
- Main module: `:app`
- Package / namespace: `com.ottertondev.metroatbkk`
- Main activity: `app/src/main/java/com/ottertondev/metroatbkk/MainActivity.kt`
- Primary screen: `app/src/main/java/com/ottertondev/metroatbkk/ui/MetroExplorerRoute.kt`
- UI stack: Jetpack Compose with Material 3
- Language: Kotlin
- Build system: Gradle Kotlin DSL
- Java compatibility: Java 11
- `compileSdk`: Android 36.1
- `targetSdk`: 36
- `minSdk`: 24
- App version: `1.0` / version code `1`

## Build And Verify

- Debug APK: `./gradlew.bat assembleDebug`
- Unit tests: `./gradlew.bat testDebugUnitTest`
- Instrumented tests exist under `app/src/androidTest`, but require a device or emulator.
- Because this repo uses Git LFS-backed assets, Git commands can touch large transit files and may need full repo permissions.

## App Structure

- `MainActivity.kt` enables edge-to-edge and launches `MetroAtBKKTheme` with `MetroExplorerRoute`.
- `MetroExplorerRoute.kt` owns the visible app shell: Metro tab, SRT placeholder, Preferences placeholder, bottom navigation, station loading state, selected-station chips, and the station list.
- `ui/theme/` defines the Metro color scheme, custom shapes, Google Sans Flex typography, and dynamic Material colors on Android 12+.
- `data/` contains transit models and parsers for GTFS-style rail data, schematic station JSON, CSV rows, and legacy DB enrichment.

## Data And Assets

- Main visible station-list data comes from `app/src/main/assets/schematic_stations.json`.
- Current schematic data: 118 stations, 1380x1380 canvas, bounds `287,60,1279,1318`, line IDs `1..5`.
- Large bundled transit assets include `routes.txt`, `trips.txt`, `stop_times.txt`, `stops.txt`, `shapes.txt`, fare files, `DB.txt`, and agency/calendar/frequency files.
- SVG assets include BTS and MRT logos in the root asset folder and `assets/icons/`.
- App resources include launcher icons, `ic_train_rounded`, `ic_mrt`, `ic_bts_lg`, Google Sans Flex font, and Thai/English strings.

## Transit Domain Notes

- `TransitLineKind` models BTS Sukhumvit, BTS Silom, BTS Gold, MRT Blue, MRT Purple, MRT Yellow, MRT Pink, Airport Rail Link, BRT, SRT Dark Red, and SRT Light Red.
- `GtfsRailMapParser` currently filters configured rail route IDs: `1`, `2`, `3`, `4`, `5`, `179`, `2025`, `2026`, `2027`, `2224`, `2436`.
- The current home list maps schematic line IDs to labels as:
  - `1`: BTS Sukhumvit
  - `2`: BTS Silom
  - `3`: BTS Gold
  - `4`: MRT Yellow
  - `5`: MRT Pink
- In the UI, `lineId >= 4` is treated as MRT styling for selected-route cards.

## Tests

- `TransitStationParserTest` checks quoted DB parsing, official station-code overrides, BTS/MRT line splitting, bus/out-of-bounds filtering, map Y-axis flipping, and malformed rows.
- `GtfsRailMapParserTest` checks quoted CSV parsing, rail-route filtering, trip/shape collection, stop-time route linking, GTFS stop route membership, and shape point ordering.
