# Implementation Plan - Migration to Compose Multiplatform (iOS & Web)

This plan outlines the steps to migrate the existing Android-only `eventRADAR` app to **Compose Multiplatform**. This will allow the app to run on Android, iOS, and Web (WasmJS) with a shared codebase for UI and logic.

## User Review Required

> [!IMPORTANT]
> **Map Support**: Google Maps (AndroidX) is Android-specific. For iOS and Web, we will need to use a multiplatform map library (like `maps-compose-multiplatform`) or abstract the map implementation.
> **Ads**: AdMob is platform-specific. Ads will be disabled or abstracted for iOS/Web in the initial migration.
> **Project Structure**: The current `app` module will be converted into a Multiplatform module (renamed to `composeApp` or similar in standard templates, but we can keep the name if preferred).

## Proposed Changes

### 1. Build Configuration & Dependencies
We need to add the Kotlin Multiplatform and Compose Multiplatform plugins and update dependencies to their KMP versions.

#### [libs.versions.toml](file:///C:/Users/johan/AndroidStudioProjects/eventRADAR2/gradle/libs.versions.toml)
- Add `compose-multiplatform = "1.7.3"` (or latest stable)
- Add `kotlin = "2.2.10"` (already there)
- Update `room`, `coil`, `navigation`, and `lifecycle` to KMP-compatible versions.

#### [build.gradle.kts (root)](file:///C:/Users/johan/AndroidStudioProjects/eventRADAR2/build.gradle.kts)
- Add `id("org.jetbrains.compose") version libs.versions.compose.multiplatform`
- Add `kotlin("multiplatform") version libs.versions.kotlin` apply false

#### [app/build.gradle.kts](file:///C:/Users/johan/AndroidStudioProjects/eventRADAR2/app/build.gradle.kts)
- Convert `plugins` block to use `kotlin("multiplatform")` and `id("org.jetbrains.compose")`.
- Configure targets: `androidTarget()`, `iosX64()`, `iosArm64()`, `iosSimulatorArm64()`, `wasmJs()`.
- Define source sets: `commonMain`, `androidMain`, `iosMain`, `wasmJsMain`.
- Move dependencies into `commonMain.dependencies`.

---

### 2. Core Logic & Data Migration
Move models, repositories, and database code to `commonMain`.

#### [NEW] [LatLng.kt](file:///C:/Users/johan/AndroidStudioProjects/eventRADAR2/app/src/commonMain/kotlin/com/example/eventradar/model/LatLng.kt)
- Create a shared `LatLng` data class to replace `com.google.android.gms.maps.model.LatLng`.

#### [FestivalViewModel.kt](file:///C:/Users/johan/AndroidStudioProjects/eventRADAR2/app/src/main/java/com/example/eventradar/ui/FestivalViewModel.kt)
- Change `AndroidViewModel` to `ViewModel`.
- Remove `Application` dependency; use a factory or dependency injection for the database.
- Replace Android-specific distance calculation with a common implementation.

#### [EventRadarDatabase.kt](file:///C:/Users/johan/AndroidStudioProjects/eventRADAR2/app/src/main/java/com/example/eventradar/data/database/EventRadarDatabase.kt)
- Update to use KMP Room configuration (`RoomDatabase.Builder`).

---

### 3. UI Migration
Move Compose screens and components to `commonMain`.

#### [MainActivity.kt](file:///C:/Users/johan/AndroidStudioProjects/eventRADAR2/app/src/main/java/com/example/eventradar/MainActivity.kt)
- Move all screen composables to `commonMain`.
- `MainActivity` will only contain the `androidMain` entry point.

#### [NEW] [App.kt](file:///C:/Users/johan/AndroidStudioProjects/eventRADAR2/app/src/commonMain/kotlin/com/example/eventradar/App.kt)
- Create a common entry point for the UI that will be called by all platforms.

---

### 4. Platform Abstractions (expect/actual)
Create abstractions for features that are platform-specific.

- **Maps**: `expect @Composable fun MapView(...)`
- **Location**: `expect fun getCurrentLocation(...)`
- **Ads**: `expect @Composable fun AdBanner(...)`

## Verification Plan

### Automated Tests
- Run `./gradlew check` to ensure common code compiles for all targets.
- Run `./gradlew connectedAndroidTest` to ensure Android functionality remains intact.

### Manual Verification
- **Android**: Run the app on an emulator/device.
- **iOS**: Run the app on an iOS simulator (requires macOS).
- **Web**: Run `./gradlew wasmJsBrowserRun` and verify in the browser.
