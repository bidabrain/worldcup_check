# WC 2026 — FIFA World Cup Android App

A native Android app for following the FIFA World Cup schedule, results, standings, and knockout bracket. Supports the 2026, 2022, and 2018 tournaments.

## Screenshots

| Schedule | Standings | Knockout | Match Detail |
|----------|-----------|----------|--------------|
| Monthly calendar with match-day indicators, live match auto-refresh | Group stage standings with GD colour coding | Horizontal bracket with clickable matches | Events, stats with bar chart, formation lineup |

## Features

- **Schedule** — Monthly calendar view; tap any tournament day to see that day's matches. Soccer-ball dots mark days with matches, populated in the background as data is fetched.
- **Live mode** — When matches are in progress the app polls for updates every 30 s and shows a pulsing LIVE badge.
- **Standings** — Group stage table for any supported tournament, cached offline.
- **Knockout bracket** — Horizontal scrollable bracket; finished matches are clickable.
- **Match detail**
  - *Events* tab — Goals, cards, substitutions, penalty shootout
  - *Stats* tab — 30+ statistics with relative bar charts (Ball Possession, xG, Shots on Target, …)
  - *Lineup* tab — Starting XI plotted on a football pitch using SofaScore coordinates with formation fallback
- **Offline cache** — Room database; last-fetched data shown when offline.
- **i18n** — UI adapts automatically to system locale: 🇺🇸 English / 🇨🇳 Chinese (zh) / 🇰🇷 Korean (ko).
- **Support screen** — PayMe QR code with one-tap download.

## Tech Stack

| Layer | Library |
|-------|---------|
| UI | Jetpack Compose + Material3 |
| Navigation | Navigation Compose |
| Async | Kotlin Coroutines + StateFlow |
| Local DB | Room 2 |
| Network | Retrofit + OkHttp |
| Images | Coil |
| DI | Manual (ViewModel Factory) |
| Build | AGP 8.2.2 · Kotlin 1.9.24 · compileSdk 34 |

Data is sourced from the public SofaScore API (for personal, non-commercial use).

## Supported Tournaments

| Tournament | Dates |
|------------|-------|
| FIFA World Cup 2026 | June 11 – July 19, 2026 |
| FIFA World Cup 2022 | Nov 20 – Dec 18, 2022 |
| FIFA World Cup 2018 | June 14 – July 15, 2018 |

## Requirements

- Android 8.0+ (minSdk 26)
- Internet connection for initial data load; works offline with cached data

## Build

```bash
# Clone
git clone git@github.com:bidabrain/worldcup_check.git
cd worldcup_check

# Debug build
./gradlew assembleDebug

# Install on connected device
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## Project Structure

```
app/src/main/java/com/worldcup2026/
├── data/
│   ├── local/          # Room DB — MatchEntity, StandingEntity, IncidentEntity
│   ├── remote/         # Retrofit API + DTOs (SofaScore)
│   └── repository/     # MatchRepository + Mappers
├── domain/model/       # Domain models, TournamentConfig
└── ui/
    ├── schedule/        # ScheduleScreen + ViewModel (calendar, live polling)
    ├── standings/       # StandingsScreen + ViewModel
    ├── knockout/        # KnockoutScreen + ViewModel (background sync)
    ├── matchdetail/     # MatchDetailScreen + ViewModel (events, stats, lineup)
    ├── support/         # SupportScreen (PayMe QR)
    ├── components/      # MatchCard, FlagEmoji
    ├── theme/           # Colors, Typography
    └── util/            # StatNameTranslator (API name → locale string)
```

## License

Personal / non-commercial use only. Data © SofaScore.
