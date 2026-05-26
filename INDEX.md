# MetroAtBKK Project Index

This file is an AI handoff guide for future work on the MetroAtBKK Android app. It summarizes the repo structure, runtime flow, important files, data contracts, and build/test notes so a future agent can get oriented quickly.

## Project Overview

- App name: `MetroAtBKK`
- Type: native Android app
- Language: Kotlin
- UI: Jetpack Compose with Material 3
- Build system: Gradle Kotlin DSL
- Root project: `MetroAtBKK`
- Main module: `:app`
- Package / namespace: `com.ottertondev.metroatbkk`
- Main activity: `app/src/main/java/com/ottertondev/metroatbkk/MainActivity.kt`
- Primary screen: `app/src/main/java/com/ottertondev/metroatbkk/ui/MetroExplorerRoute.kt`

The current app is a Compose prototype for browsing Bangkok metro stations. The visible app shell has three bottom-navigation tabs: Metro, SRT, and Preferences. Only Metro has real content; SRT and Preferences are placeholders.

## Build Environment

- Gradle wrapper: Gradle `9.4.1`
- Android Gradle Plugin: `9.2.1`
- Kotlin Compose plugin: `2.2.10`
- Compose BOM: `2026.02.01`
- `compileSdk`: Android `36.1`
- `targetSdk`: `36`
- `minSdk`: `24`
- Java compatibility: Java `11`
- Gradle daemon toolchain target: Java `21`

Useful commands:

```sh
JAVA_HOME=/home/otterton/.gradle/jdks/eclipse_adoptium-21-amd64-linux.2 sh gradlew assembleDebug
JAVA_HOME=/home/otterton/.gradle/jdks/eclipse_adoptium-21-amd64-linux.2 sh gradlew testDebugUnitTest
```

AGENT.md says to build a debug APK every time code or project files change. In this environment, plain `sh gradlew assembleDebug` may fail if `JAVA_HOME` is not set, so use the Gradle-managed JDK path above when needed.

Debug APK output:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Repository Layout

```text
.
├── AGENT.md
├── README.md
├── INDEX.md
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── gradlew / gradlew.bat
├── gradle/
│   ├── libs.versions.toml
│   ├── gradle-daemon-jvm.properties
│   └── wrapper/
└── app/
    ├── build.gradle.kts
    ├── proguard-rules.pro
    └── src/
        ├── main/
        │   ├── AndroidManifest.xml
        │   ├── assets/
        │   ├── java/com/ottertondev/metroatbkk/
        │   └── res/
        ├── androidTest/
        └── test/
```

Generated and local-tooling directories exist but should not be treated as source: `.git/`, `.gradle/`, `.idea/`, `.kotlin/`, and `app/build/`.

## Gradle Files

- `settings.gradle.kts` configures plugin repositories, dependency repositories, the Foojay toolchain resolver, root name, and includes `:app`.
- Root `build.gradle.kts` only declares Android application and Kotlin Compose plugins via aliases.
- `gradle/libs.versions.toml` owns dependency and plugin versions.
- `app/build.gradle.kts` configures the Android app module, Compose, SDK versions, release minify settings, Java 11 compile options, and dependencies.
- `gradle/gradle-daemon-jvm.properties` pins Gradle daemon toolchains to Java 21 via Foojay URLs.
- `gradle.properties` sets Gradle JVM args and Kotlin official code style.

## Runtime Flow

1. `MainActivity` calls `enableEdgeToEdge()`.
2. `MainActivity` sets Compose content with `MetroAtBKKTheme`.
3. `MetroAtBKKTheme` applies Material 3 colors, typography, and shapes.
4. `MetroExplorerRoute()` renders the app shell with a bottom navigation bar.
5. The Metro tab calls `rememberStationListState()`, which loads `schematic_stations.json` through `SchematicMapRepository`.
6. Loading, error, and success UI states are represented by the private `StationListState` sealed interface.
7. On success, the app displays a greeting header, optional selected-station route cards, and a lazy list of stations.
8. Tapping a station adds it to `selectedStations` if the same `lineId-key` pair is not already selected.
9. Tapping the close button on a selected card removes that selected station.

## Main UI File

`app/src/main/java/com/ottertondev/metroatbkk/ui/MetroExplorerRoute.kt` owns almost all visible app behavior.

Important composables and helpers:

- `MetroExplorerRoute`: top-level route and tab state.
- `rememberStationListState`: loads schematic station data from assets in a Compose `produceState`.
- `MetroHomeStateContent`: branches loading/error/success.
- `LoadingMetroHome`: centered loading spinner and label.
- `ErrorMetroHome`: error surface with fallback message.
- `MetroHomeContent`: `LazyColumn` with header, selected route cards, and station list.
- `MetroGreetingHeader`: greeting text and search button.
- `StarSearchButton`: primary colored search icon button; click handler is currently empty.
- `SelectedRoutesRow`: horizontally scrollable selected-station cards.
- `SelectedRouteCard`: card for a selected station with BTS/MRT icon, line label, station label, and remove action.
- `StationSelectionSegment`: list row for one station.
- `StationCodeBadge`: circular colored station-code badge.
- `MetroNavigationBar`: bottom nav for Metro, SRT, Preferences.
- `PlaceholderScreen`: placeholder for SRT and Preferences tabs.

UI behavior notes:

- Station uniqueness is based on `SchematicStation.selectionId`, which is `"$lineId-$key"`.
- `isMrtLine` is currently `lineId >= 4`, so Yellow and Pink use MRT card styling.
- Locale-sensitive station names use Thai headline text when the current locale language is `th`; otherwise English is headline and Thai is supporting text.
- `lineNameResource` maps line IDs `1..5` to strings.
- `lineColor` maps line IDs to hardcoded transit colors:
  - `1`: BTS Sukhumvit green `0xFF65B724`
  - `2`: BTS Silom teal `0xFF008C83`
  - `3`: BTS Gold `0xFFB28B18`
  - `4`: MRT Yellow `0xFFFFD400`
  - `5`: MRT Pink `0xFFCD4692`
- `contentColorForLine` chooses dark or white text based on luminance.

## Data Layer

`app/src/main/java/com/ottertondev/metroatbkk/data/SchematicMapRepository.kt` contains the current data models and parser:

- `SchematicMapData`
- `SchematicBounds`
- `SchematicStation`
- `SchematicMapRepository`

The repository loads `schematic_stations.json` from app assets using `AssetManager`, reads it as UTF-8, and parses it with `org.json.JSONObject` on `Dispatchers.IO`.

The parser expects this JSON shape:

```json
{
  "v": 1,
  "canvas": [1380, 1380],
  "bounds": [287, 60, 1279, 1318],
  "cols": ["line", "id", "key", "en", "th", "x", "y"],
  "lines": {
    "1": ["bts_sukhumvit", "BTS Sukhumvit"]
  },
  "stations": [
    [1, 79, "n24", "Khu Khot", "คูคต", 896, 60]
  ]
}
```

Only `canvas`, `bounds`, and `stations` are parsed today. `v`, `cols`, and `lines` are present in the asset but ignored by the Kotlin parser.

## Data Asset

`app/src/main/assets/schematic_stations.json` is the main visible data source.

- Size: about 9.8 KB
- Version field: `1`
- Canvas: `1380 x 1380`
- Bounds: `[287, 60, 1279, 1318]`
- Station row columns: `line`, `id`, `key`, `en`, `th`, `x`, `y`
- Lines:
  - `1`: BTS Sukhumvit
  - `2`: BTS Silom
  - `3`: BTS Gold
  - `4`: MRT Yellow
  - `5`: MRT Pink
- Current station count: 118

Known data quality notes visible in the asset:

- Some English transliterations may need cleanup, for example `Ladprao`, `Sri Kretta`, `Huamark`, `Si lam`, and `MLake Muang Thong Thani`.
- One Silom station entry appears as `Wongwian Yai่` in English and `วงเวียนใหญ` in Thai, which likely needs spelling/diacritic cleanup.

## Theme And Design

Theme files live in `app/src/main/java/com/ottertondev/metroatbkk/ui/theme/`.

- `Theme.kt`: Material 3 light/dark color schemes, dynamic color support on Android 12+, and custom rounded shapes.
- `Color.kt`: base transit and Bangkok surface colors.
- `Type.kt`: Google Sans Flex typography, including variable font variation settings.

Important details:

- `MetroAtBKKTheme` defaults to system dark theme and enables dynamic colors when available.
- Light background is `BangkokSurface` `0xFFF6F1E8`.
- Dark background is `BangkokSurfaceDark` `0xFF14130F`.
- Shapes use rounded corners from `8.dp` through `36.dp`.
- The app uses `GoogleSansFlex.ttf` from resources as `R.font.google_sans_flex`.
- `GoogleSansFlexBodyMain` uses weight `1000`, `ROND = 11f`, and width `25f`.

## Resources And Assets

Text resources:

- English strings: `app/src/main/res/values/strings.xml`
- Thai strings: `app/src/main/res/values-th/strings.xml`

Drawable/vector resources:

- `ic_train_rounded.xml`: bottom-nav train icon.
- `ic_mrt.xml`: MRT logo vector.
- `ic_bts_lg.xml`: BTS logo vector.
- `ic_launcher_background.xml` and `ic_launcher_foreground.xml`: launcher icon layers.

Mipmap resources:

- Launcher icons exist for mdpi, hdpi, xhdpi, xxhdpi, xxxhdpi, and adaptive icon XML under `mipmap-anydpi-v26`.

Asset folder:

- `schematic_stations.json`: parsed station-list data.
- `BTS_LG.svg` and `MRT.svg`: source SVG logos.
- `icons/BTS_LG.svg` and `icons/MRT.svg`: duplicate SVG logo copies.
- `GoogleSansFlex.ttf`: duplicate copy of the font also present in `res/font`.

The duplicate font and duplicate SVGs may be intentional source assets, but the running app uses the resource font and drawable XML logo resources, not the SVG files directly.

## Manifest And Android Resources

`app/src/main/AndroidManifest.xml` declares one exported launcher activity:

- `.MainActivity`
- Label: `@string/app_name`
- Theme: `@style/Theme.MetroAtBKK`
- Backup enabled with `backup_rules.xml` and `data_extraction_rules.xml`
- RTL supported

`values/themes.xml` and `values-night/themes.xml` define a no-action-bar platform theme. Compose handles the actual visible Material theme.

`values/colors.xml` still contains default Android template colors and is not part of the Compose color scheme.

Backup/data extraction XML files are still template-style placeholders.

## Tests

Current tests:

- `app/src/androidTest/java/com/ottertondev/metroatbkk/ExampleInstrumentedTest.kt`
  - Verifies app context package name.
  - Requires device/emulator for instrumented execution.

There are currently no unit test source files under `app/src/test`.

Useful test command once unit tests exist:

```sh
JAVA_HOME=/home/otterton/.gradle/jdks/eclipse_adoptium-21-amd64-linux.2 sh gradlew testDebugUnitTest
```

## Known Placeholders And Gaps

- Search button exists visually but has no behavior.
- SRT tab is a placeholder.
- Preferences tab is a placeholder.
- No station search/filtering yet.
- No route planning or map rendering yet.
- Schematic station coordinates are loaded but not rendered visually in the current UI.
- The parser ignores the `lines` metadata in `schematic_stations.json`; line labels/colors are hardcoded in UI.
- No local unit tests currently cover `SchematicMapRepository`.

## Editing Guidance For Future AI

- Read `AGENT.md` before making changes.
- Keep changes scoped to the Compose/Kotlin style already present.
- Prefer `rg` / `rg --files` for searches.
- Do not revert user changes.
- Build the debug APK after code or project edits.
- If this environment lacks Java on `PATH`, set `JAVA_HOME` to `/home/otterton/.gradle/jdks/eclipse_adoptium-21-amd64-linux.2`.
- Avoid generated directories: `.gradle/`, `.idea/`, `.kotlin/`, `app/build/`.
- For UI changes, start in `MetroExplorerRoute.kt`.
- For station data loading changes, start in `SchematicMapRepository.kt`.
- For colors/typography/shapes, use `ui/theme/`.
- For visible text, update both English and Thai string resources when relevant.

## Model Tone
- Do speak in friend style, this means profanity, slang, like-friends are all expected.