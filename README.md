# CarChecker

CarChecker is a single-screen Android application that retrieves vehicle information from the Israeli DataGov API based on a license plate number.

The application starts searching automatically after the user enters a valid 7 or 8 digit license number. No search button is required.

---

## Features

- Automatic search while typing
- Parallel API requests using Kotlin Coroutines
- Vehicle manufacturer
- Vehicle color
- Vehicle type (P/C)
- Disabled parking badge status
- Loading, Success, Not Found and Error states
- Israeli license plate styled input
- Unit tests

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
DataGovApiService
        ↓
DataGov API
```

The repository performs both API requests in parallel using `async` and `await`.

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
- JUnit
- kotlinx-coroutines-test

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
- License number formatting
- Repository success scenario
- Repository "vehicle not found" scenario
- Repository disabled badge detection

Run all tests with:

```bash
./gradlew testDebugUnitTest
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

## Future Improvements

- Offline-first architecture
- Room database
- Daily synchronization
- Foreground service
- Additional UI tests
- Dependency Injection (Hilt/Koin)

---

## Example

A valid vehicle number for testing:

```
28367902
```