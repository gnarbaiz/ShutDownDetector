# Power Outage Monitor

Android app (Kotlin) that detects power outages using Wi‑Fi and battery state. Keeps a local log of events, notifies on outages and restorations, and can export to CSV or open a pre-filled email.

## Features

- Background monitoring via Foreground Service (survives app close and reboot)
- Local event log (Room) with battery level and estimated outage duration
- Notifications for outage and restoration
- Send events: opens default email app with report pre-filled
- Export to CSV, events-by-month chart
- UI: Jetpack Compose. English and Spanish (follows system language)

## Stack

Clean Architecture + MVVM. Compose, Hilt, Room, Coroutines/Flow, Timber. BroadcastReceivers for boot and power events.

## Project layout

```
app/
├── core/           # Device state, CSV export
├── data/           # Room, repositories, email intent
├── domain/         # Models, use cases, repository contracts
├── ui/             # Compose screens, ViewModels
├── service/        # PowerMonitoringService (foreground)
├── receiver/       # Boot, connectivity, power
└── di/
```

## Config

**Permissions** (in manifest): `ACCESS_NETWORK_STATE`, `INTERNET`, `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_DATA_SYNC`, `RECEIVE_BOOT_COMPLETED`, `POST_NOTIFICATIONS`.

**Email recipient**: set `toEmail` in `EmailSenderImpl.kt`. The app only opens the mail client with subject/body; the user sends from their own account.

## Build & run

- JDK 11+, Android SDK 24+, Android Studio Hedgehog+
- `./gradlew build` / `./gradlew installDebug`

## License

MIT
