# House of Cards

A cross-platform (Android + iOS) hub for a growing set of card games, written in Kotlin with [Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html) and [Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/).

The first game is **Jack's Lines** — a variant of *Sequence* played on a permanent, fixed board.

## Games

### Jack's Lines (Sequence variant)

Standard Sequence with two house-rule changes:

- **Black Joker** — place your chip on any empty (non-corner) cell.
- **Red Joker** — remove an opponent's chip from any cell **except** one that is part of an already-completed sequence.

Everything else is classic Sequence:

- Sequences are **5 chips in a row** (horizontal, vertical, or diagonal). The four corner cells are free spaces that count for either player.
- Once a sequence is completed, its chips are **locked** — they can no longer be removed.
- Win the game by completing **2 sequences**.

## App structure

A main menu leads into each game, then into one of three modes:

| Mode | Behavior |
|------|----------|
| **Online** | Matchmaking against other users. When no live player is found, the game falls back to a human-named AI so you can't tell the difference. Ends with Rematch / Add-as-friend / Back to menu. |
| **Friends** | Challenge a friend from your friends list. |
| **Offline (AI)** | Play the built-in AI. |

Five bottom tabs are always available:

- **Profile** — username, level, wins/losses/streak, coins, an Identity box (username / status / region), achievements, and purchased store items.
- **Friends** — friends list, add-friend, incoming/outgoing requests, and game invites.
- **Home** — game selection, with level / coins / streak up top.
- **Store** — custom chips purchasable with coins (real-money items are marked "coming soon").
- **Settings** — audio / vibration / chat toggles, status & region, sign out, and delete account.

### Custom chips

Chips bought in the Store represent you in-game and are shown to your opponent. If both players equip the **same** custom chip, the lower-level player is switched to the classic chip for that game and a notice is shown, auto-dismissing after ~20 seconds.

### Match records

Every finished game is saved as a snapshot (chip positions, locked cells, winner, opponent, duration, move count) and stored in the local match history.

### Turn timer

Each player has a **60-second** turn. On timeout, the game auto-plays the *worst legal move* from their hand.

### Sign-in & accounts

A **first-run login / sign-up screen** appears before the app shell. Auth is **email/password via Firebase Auth**; when no real Firebase project is configured yet (the placeholder `google-services.json` is still in place), it falls back to a local demo auth so the app stays fully usable. The display name is **editable** from the Profile tab.

The Android application id is **`com.lomork.house_of_cards`**. Drop your real `google-services.json` into `composeApp/` (and `GoogleService-Info.plist` into the iOS app) to switch on live Firebase; the app detects this automatically at runtime.

## Project layout

```
composeApp/src/
  commonMain/kotlin/com/lomork/house_of_cards/
    data/          Models, seed data, JSON persistence (expect/actual), AppStore
    games/         Game registry + the sequence engine and AI
    nav/           Routes, Navigator, and the app shell with the bottom bar
    screens/       Profile, Friends, Home, Store, Settings, mode select, and the game screen
    theme/         Palette + typography ("modern but classy")
    ui/            Shared components (poker chip, buttons, cards, tiles)
    App.kt         Root composable (AppStore + theme + shell)
  androidMain/     Activity entry point + SharedPreferences storage
  iosMain/         UIViewController entry point + NSUserDefaults storage
```

- **State**: a single `AppStore` holds the whole `SavedState` in Compose snapshot state and persists it as JSON on every mutation (`SharedPreferences` on Android, `NSUserDefaults` on iOS).
- **Level** is derived (`1 + (wins*2 + losses) / 5`) so it can never drift from the stats.
- The board layout is **deterministic** — a seeded shuffle means every install gets the identical permanent board.

## Building

### Android (Windows / macOS / Linux)

```bash
./gradlew :composeApp:assembleDebug
```

The debug APK lands in `composeApp/build/outputs/apk/debug/composeApp-debug.apk`.

Requires:
- JDK 17+ (e.g. Android Studio's bundled JBR)
- Android SDK, with `local.properties` pointing at it (`sdk.dir=...`)
- Android Studio optionally, to open the project directly

### iOS (macOS only)

```bash
./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64
```

Or open the project in Android Studio / IntelliJ with the iOS targets enabled and run on a simulator from Xcode. The iOS targets are guarded behind a macOS-host check, so Android-only machines build cleanly without Kotlin/Native.

## Scoped / stubbed for now

These are intentionally simulated and ready to be wired to real services:

- **Firebase authentication** — email/password sign-up/sign-in is wired, with a local fallback until the real project config is added.
- **Online multiplayer & friends via Firestore** — matchmaking and the friends graph still resolve locally (a named AI stand-in / seeded friend requests). The `GameOpponent` model and match-recording flow are in place; adding `firebase-firestore` and syncing these collections is the next step.
- **Real-money purchases** — items show a USD price labelled "soon"; only coin purchases are active.

## Status

- App builds, installs, and launches on a Pixel 9 Pro without crashing.
- All tabs, the mode-select flow, and the full game screen (board, hand, jokers, AI turns, 60s timer, chip-collision notice, and the post-game overlay) are implemented.