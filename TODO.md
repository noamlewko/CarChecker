# CarChecker

## UI
- [x] Create screen layout
- [x] Add license input
- [x] Display car information
- [x] Show loading
- [x] Show error state
- [x] Retry action on error
- [x] License plate-style auto-formatting while typing

## Architecture
- [x] Create ViewModel
- [x] Create Repository

## Network
- [x] Add Retrofit
- [x] Vehicle API
- [x] Disabled Badge API

## Logic
- [x] Search on input
- [x] Parallel API requests
- [x] Handle errors

## Tests
- [x] ViewModel tests
- [x] Repository tests
- [x] Compose UI tests

## Polish
- [x] README
- [x] Final UI improvements

## Bonus: offline-first architecture
- [x] Room database for cached vehicles
- [x] Repository serves cached results first, falls back to network
- [x] Daily refresh via WorkManager + foreground service
- [x] Verified working with the network disabled on-device
