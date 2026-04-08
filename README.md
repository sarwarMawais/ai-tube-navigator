# AI Tube Navigator 🚇

Smart AI-powered navigation for the London Underground. Get carriage recommendations, crowd predictions, fastest exit guidance, and live line status — all with offline support.

## Features

### Core AI Features
- **AI Carriage Recommendation** — Board the right carriage for the fastest exit at your destination
- **Crowd Prediction** — AI predicts busy times so you can avoid peak crowds
- **Fastest Exit Guidance** — Station exit maps with walking times and nearby landmarks
- **Smart Route Planning** — Plan journeys with AI-enhanced time predictions

### Live Data
- **Live Line Status** — Real-time status for all Tube lines via TfL API
- **Pull-to-Refresh** — Always up-to-date information
- **Offline Cache** — Line status cached locally via Room DB for offline use

### Station Intelligence
- **15+ detailed stations** with exit data, carriage positions, and landmarks
- **Step-free access indicators** for accessibility
- **Zone information** and interchange details

### User Experience
- **Material 3 Design** with dynamic color support
- **Dark mode** support (system theme)
- **Bottom navigation** with smooth transitions
- **Saved journeys** and recent history

## Tech Stack

| Layer | Technology |
|---|---|
| **Language** | Kotlin 2.0 |
| **UI** | Jetpack Compose + Material 3 |
| **Architecture** | MVVM + Clean Architecture |
| **DI** | Hilt (with KSP) |
| **Local DB** | Room |
| **Network** | Retrofit + OkHttp |
| **Navigation** | Navigation Compose |
| **Async** | Kotlin Coroutines + Flow |
| **Image** | Coil Compose |
| **Build** | Gradle Version Catalog |

## Project Structure

```
app/src/main/java/com/aitube/navigator/
├── AiTubeApp.kt                    # Hilt Application
├── MainActivity.kt                  # Single Activity + Compose
├── navigation/
│   ├── Screen.kt                    # Sealed class route definitions
│   └── AppNavigation.kt            # NavHost + Scaffold
├── ui/
│   ├── theme/                       # Material 3 theme (Color, Type, Theme)
│   ├── components/
│   │   ├── BottomNavBar.kt         # Bottom navigation
│   │   ├── TubeLineCard.kt         # Line status card + chip
│   │   └── CarriageVisualizer.kt   # Animated carriage diagram
│   └── screens/
│       ├── home/                    # Dashboard with hero, quick actions, AI insights
│       ├── route/                   # Journey planner with AI recommendations
│       ├── station/                 # Station list + detail with exit guide
│       ├── status/                  # Live line status with filters
│       └── settings/               # User preferences
├── data/
│   ├── model/
│   │   ├── TubeModels.kt          # Domain models
│   │   └── TubeData.kt            # Embedded station/line data (offline)
│   ├── remote/
│   │   ├── TflApiService.kt       # Retrofit interface for TfL API
│   │   └── TflApiModels.kt        # API response models
│   ├── local/
│   │   ├── AppDatabase.kt         # Room database
│   │   ├── dao/TubeDao.kt         # Data access object
│   │   └── entity/StationEntity.kt # Room entities
│   └── repository/
│       └── TubeRepository.kt      # Single source of truth
└── di/
    └── AppModule.kt                # Hilt dependency injection
```

## Setup

1. Open the project in **Android Studio Ladybug** (2024.2+) or newer
2. Let Gradle sync complete
3. Run on an emulator or physical device (API 26+)

No API keys required — the TfL API is free and open.

## Data Sources

- **TfL Unified API** — Free, open data for live line status, arrivals, journey planning
- **Embedded station data** — 15+ major stations with exit layouts and carriage recommendations
- **AI predictions** — On-device crowd and delay predictions based on time-of-day patterns

## Screens

| Screen | Description |
|---|---|
| **Home** | Dashboard with hero banner, quick actions, AI insights, line status overview |
| **Route** | Journey planner with station search, carriage visualizer, crowd prediction |
| **Status** | Full live line status with filter chips (All/Good/Disrupted) |
| **Stations** | Searchable station list with zone, lines, step-free indicators |
| **Station Detail** | Exit guide, carriage recommendations per exit, crowd info |
| **Settings** | Route preferences (fastest/less crowds/step-free/less walking) |

## Roadmap

- [ ] TfL Journey Planner API integration for real route calculation
- [ ] Live crowd data from TfL Crowding API
- [ ] Delay compensation auto-claim feature
- [ ] Event-aware routing (football matches, concerts)
- [ ] Offline voice assistant with Whisper
- [ ] AR station navigation
- [ ] Widget for home screen commute summary

## License

MIT
