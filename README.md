# CarChecker

CarChecker is a single-screen Android application that retrieves vehicle information from the Israeli DataGov API based on a license plate number.

The application starts searching automatically after the user enters a valid 7 or 8 digit license number. No search button is required.

The application is offline-first: once a vehicle has been searched, its details are cached locally and remain available even without a network connection.

---

## Features

- Automatic search while typing
- Parallel API requests using Kotlin Coroutines
- License plate-style input that auto-formats digits with dashes as you type
- Vehicle manufacturer
- Vehicle color
- Vehicle type (P/C)
- Disabled parking badge status
- Loading, Success, Not Found and Error states, with a retry action on error
- Offline-first caching: previously searched vehicles remain available without a network connection
- Daily background refresh of cached vehicles via a foreground service
- Israeli license plate styled input
- Unit tests and a Compose UI test

---

## Architecture

The project follows the MVVM architecture.

```
UI (Jetpack Compose)
        ↓
CarCheckerViewModel
        ↓
CarRepository
        ↓
    ┌───┴────┐
    ↓        ↓
DataGovApiService   Room database (CarDao)
    ↓
DataGov API
```

The repository performs both API requests in parallel using `async` and `await`. When a local database is available, it is checked first: a cache hit returns instantly without a network call, and a cache miss falls back to the network and stores the result for next time.

A daily `WorkManager` job, promoted to a foreground service, refreshes every previously searched vehicle in the background.

---

## Technologies

- Kotlin
- Jetpack Compose
- Material 3
- MVVM
- StateFlow
- Kotlin Coroutines
- Retrofit
- OkHttp
- Gson
- Room
- WorkManager
- JUnit
- kotlinx-coroutines-test
- Compose UI testing

---

## Data Sources

The application uses two official Israeli DataGov resources:

- Private and Commercial Vehicles
- Disabled Parking Badge Registry

---

## Running the Project

1. Clone the repository.
2. Open the project in Android Studio.
3. Wait for Gradle Sync.
4. Run the **app** configuration.

Example license number:

```
28367902
```

---

## Unit Tests

The project includes unit tests for:

- License number validation
- License number formatting (auto-inserted dashes)
- Repository success scenario
- Repository "vehicle not found" scenario
- Repository disabled badge detection
- Offline-first caching: cache hit skips the network, cache miss fetches and stores
- Daily refresh updating every cached vehicle
- Debounced search: rapid typing collapses into a single network search
- A newer search cancels a still-loading previous one, so a stale result can never overwrite a fresh one

It also includes a Compose UI test verifying the error state and its retry button.

Run unit tests with:

```bash
./gradlew testDebugUnitTest
```

Run the Compose UI test with:

```bash
./gradlew connectedDebugAndroidTest
```

---

## Error Handling

The application handles:

- Vehicle not found
- Network failures
- HTTP errors
- Unexpected exceptions
- Coroutine cancellation during fast typing

---

## Offline-First Bonus

Implemented in full:

- `CarDetailsEntity` / `CarDao` / `CarDatabase`: a Room database that caches every vehicle that has been searched, keyed by license number.
- `CarRepository`: serves cached results instantly when available, and falls back to the network otherwise, caching the result afterward.
- `CarSyncWorker` / `CarSyncScheduler`: a daily `WorkManager` job, promoted to a foreground service via `setForeground`, that refreshes every cached vehicle from the network once a day.

This was verified on-device by searching a vehicle with the network on, then disabling all connectivity and confirming the same vehicle still returns its cached result while a never-searched vehicle correctly reports it cannot reach the server.

## Possible Next Steps

Further improvements worth considering beyond the scope of this assignment:

- Dependency injection (Hilt/Koin) instead of the manual `ViewModelProvider.Factory`.
- Broader Compose UI test coverage for the other search states.
