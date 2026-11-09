# MindForge 101 — v1 Design

## Intent (agreed)
- Offline-first Android brain-training app, **Play Store release ASAP**.
- v1: **free, no ads, no purchases, no INTERNET permission**. General audience (teen/adult/senior); no under-13 marketing, so no Families-program obligations. Premium and Kids mode are later updates.
- v1 content: **~60 games** in 10 categories, built from a small set of reusable game templates (not 60 bespoke games).
- Success: signed release AAB that installs and plays on a phone/emulator, passes basic tests, and has store listing assets/privacy policy text drafted.

## Non-goals (v1)
Accounts, cloud sync, billing, ads, voice, localization beyond English, kids mode, parent controls, tablet-specific layouts.

## Stack
Kotlin 2.2, Jetpack Compose (Material 3), Room (KSP), DataStore, Navigation Compose, single-activity, MVVM. minSdk 24, targetSdk 35. No network libraries.

## Architecture
```
ui/ (Compose screens, ViewModels)
domain/ (pure Kotlin: engine, difficulty, scoring, xp, achievements, recommend)
games/ (templates + catalog)
data/ (Room, DataStore, repository)
```
Domain and games are pure Kotlin so they are unit-testable on the JVM.

### Game engine
- `GameTemplate`: `fun generate(seed: Long, params: DifficultyParams): Round`. A `Round` is a prompt plus answer-type (`Choice`, `Sequence`, `Grid`, `Number`) and the correct answer. Generators are seeded, so rounds are reproducible.
- `GameDefinition(id, name, category, template, config)`: a catalog entry. ~60 definitions map onto ~14 templates.
- `GameSession`: runs N rounds (default 8), records per-round correctness and time, produces `Performance`.
- UI renders by round kind (a handful of composables), not per game.

### Templates (v1)
1. GridMemory (recall lit cells) 2. SequenceMemory (Simon-style) 3. NumberMemory 4. OddOneOut 5. TargetCount 6. QuickMath 7. MissingOperand/equation 8. NumberSeries 9. WordScramble 10. Stroop/rule choice 11. ReactionTap/Go-NoGo 12. PatternMatrix (shape/rotation/odd-pattern choice) 13. PairsMatch 14. Compare/Estimate.
Each game config varies parameters (symbols, operations, sizes, wording), not code.

### Difficulty
10 levels per game. `DifficultyParams` derived from level (object count, distractors, time limit, memory time, rule count, value range). `AdaptiveDifficultyEngine` takes `Performance(accuracy, avgTimeMs, mistakes, streak)` and returns next level as a float (up on strong, down on weak), clamped 1–10.

### Scoring / XP / achievements
Score = correct-weighted with speed bonus. XP = base + accuracy + speed + streak + difficulty. Rank ladder (Beginner → Mind Forge) from total XP. ~12 achievements, evaluated after each session from stats. Per-session sanity cap on score.

### Data (Room)
`profile`, `game_progress(profileId, gameId, level float, bestScore, plays, lastPlayed)`, `session(profileId, gameId, score, accuracy, xp, time)`, `achievement(profileId, id, unlockedAt)`. DataStore: active profile, sound/haptics/reduce-motion/large-text. Multiple local profiles supported (simple).

### UI
Bottom nav: Home, Games, Progress, Profile. Home: Today's Brain Mix (5 games, rotating by date), streak, category scores. Games: category chips + list. Gameplay: clean round UI with timer and progress. Result: score, accuracy, XP, new best, next level. Achievements inside Progress. Accessibility: large text toggle, shape+color (never color alone), content descriptions, reduce motion.

### Release
Package `com.mindforge101.app`; release build with R8; signed AAB (keystore created locally, **never committed**); privacy policy text stating no data leaves the device; Data safety: no data collected. Wording avoids medical claims ("brain training", not diagnosis).

## Testing
JVM unit tests for generators (answer correct, seeded determinism), difficulty engine, scoring/XP, achievements, daily mix. Instrumented/emulator smoke run of the app.

## Risks
- 60 games from 14 templates may feel repetitive → mitigate with varied configs and seeded variety; improve post-launch.
- Build toolchain: JDK 25 default is unsupported by Gradle 8.x → build with JDK 21.
