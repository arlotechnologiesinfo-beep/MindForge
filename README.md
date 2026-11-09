# MindForge

Offline brain-training app for Android. 62 short games across 10 cognitive categories, with adaptive difficulty, XP and ranks, daily streaks, achievements, and multiple local player profiles.

- No account, no ads, no analytics, no network access (the app declares no `INTERNET` permission).
- All data stays on the device.
- Kotlin, Jetpack Compose, Room, DataStore.

> MindForge is a game and not a medical or diagnostic tool. Scores are not an IQ measure.

## Features

| Area | What it does |
|------|--------------|
| Games | 62 games in 10 categories (Memory, Focus, Logic, Math, Words, Spatial, Speed, Strategy, Problem Solving, Visual), 8 rounds each |
| Adaptive difficulty | Level adjusts between sessions based on accuracy and speed |
| Progress | XP, ranks, per-category scores, achievements |
| Daily Brain Mix | A 5-game daily workout and a focus card for your weakest category |
| Profiles | Several players on one device, each with their own progress and streak |
| Accessibility | Large text option, Reduce motion option, content descriptions on interactive elements |
| Motion | Short animations (Compose and bundled Lottie), all disabled or simplified under Reduce motion |

A per-game and per-screen description lives in [docs/app-guide.md](docs/app-guide.md).

## Requirements

- Android Studio (current stable) or the Android command line tools
- JDK 21 (JDK 25 is not supported by the Gradle/AGP version in use)
- Android SDK platform 36

Min SDK 24 (Android 7.0), target/compile SDK 36. Java time APIs are used on API 24-25 through core library desugaring.

## Build and run

```bash
# Debug build installed on a connected device or emulator
./gradlew installDebug

# Unit tests
./gradlew testDebugUnitTest
```

On Windows with Git Bash, point `JAVA_HOME` at a JDK 21 first:

```bash
export JAVA_HOME="/c/Program Files/Eclipse Adoptium/jdk-21.0.12.101-hotspot"
```

Create `local.properties` with your SDK path if Android Studio has not already:

```
sdk.dir=/path/to/Android/Sdk
```

## Release build

Release builds are minified (R8) and resource-shrunk. Signing is read from `keystore.properties` in the project root when it exists; without it the release build is unsigned.

`keystore.properties` (never commit this file or the keystore):

```
storeFile=mindforge-release.jks
storePassword=...
keyAlias=...
keyPassword=...
```

```bash
./gradlew assembleRelease   # APK  -> app/build/outputs/apk/release/
./gradlew bundleRelease     # AAB  -> app/build/outputs/bundle/release/ (upload this to Google Play)
```

`*.jks`, `*.keystore`, `keystore.properties` and `local.properties` are gitignored. Back the keystore up somewhere safe: losing it means you cannot update the published app.

Version is set in `app/build.gradle.kts` (`versionCode`, `versionName`); bump `versionCode` for every upload.

## Project structure

```
app/src/main/java/com/mindforge101/app/
  MainActivity.kt        Entry point, navigation, splash, theme and settings wiring
  data/                  Room database, DataStore settings, Repository
  domain/                Models, adaptive difficulty engine, ranks, achievements, daily mix
  games/                 Game catalog and the 62 game generators, grouped by category
  ui/
    DesignSystem.kt      Colors, spacing, shapes, motion tokens, press feedback
    Theme.kt             Material theme, large text and reduce-motion locals
    BottomNav.kt         Floating bottom bar
    HomeScreen / GamesScreen / ProgressScreen / ProfileScreen / ResultScreen
    Gameplay.kt          Session runner and the game view types
    GameplayComponents.kt  Answer options, keypad, play cells, progress bars
    GameIcons.kt         Code-drawn vector icon for every game
    GameInstructions.kt  Short how-to-play text for every game
    GameIntro.kt         Pre-game intro screen
    MfLottie.kt          Offline Lottie player (respects Reduce motion)
app/src/main/res/raw/    Bundled Lottie animations (ring_pulse.json, burst.json)
app/src/test/            Unit tests (engine, games, icon and instruction coverage)
docs/                    App guide, privacy policy, store listing
```

## Architecture notes

- **Games are data plus a generator.** Each game in `games/` produces `Round` objects; `Gameplay.kt` picks a view type (choice, number, grid, sequence, glyph grid, phased, countdown) and renders it. Scoring, XP, adaptive level and achievements live in `domain/` and `data/`, independent of the UI.
- **Single light theme** built from `MfColors`; Large text scales font size, Reduce motion is exposed through `LocalReduceMotion` and honored by every animation.
- **Icons are code.** `GameIcons` draws each game's icon on a 24-unit grid; a unit test fails if a game is added without an icon or instructions.
- **Offline by design.** No network libraries, no remote assets, fonts and animations are bundled.

## Adding a game

1. Add its generator in the matching file under `games/` and register it in `Catalog.kt`.
2. Add its icon drawer to `GameIcons.kt` and its text to `GameInstructions.kt`.
3. Run `./gradlew testDebugUnitTest`; `GameIconCoverageTest` checks the icon and instructions exist.

## Releasing to Google Play

1. Bump `versionCode` / `versionName`.
2. `./gradlew bundleRelease` and upload the `.aab`.
3. Use [docs/release/store-listing.md](docs/release/store-listing.md) for listing text and [docs/release/privacy-policy.md](docs/release/privacy-policy.md) as the privacy policy (host it at a public URL).
4. Provide the store icon, feature graphic and screenshots.

## Privacy

The app collects no personal data and makes no network requests. See [docs/release/privacy-policy.md](docs/release/privacy-policy.md).

## License

No license has been chosen yet. Until one is added, all rights are reserved by the author.
