# WeatherSnap Setup Guide

WeatherSnap is a modern Android application that provides live weather reports with integrated camera evidence, utilizing local storage (Room) and real-time weather APIs.

## 🛠 Tech Stack

- **UI:** Jetpack Compose with Material 3
- **Architecture:** MVVM (Model-View-ViewModel)
- **Dependency Injection:** Hilt
- **Local Database:** Room (with KSP support)
- **Networking:** Retrofit & OkHttp
- **Camera:** CameraX
- **Image Loading:** Coil
- **Concurrency:** Kotlin Coroutines & Flow

## 📋 Prerequisites

- **Android Studio:** Ladybug (2024.2.1) or newer recommended.
- **JDK:** Version 17
- **Kotlin:** 2.0.21
- **Gradle:** 8.11.0-rc02 or compatible.

## 🚀 Installation & Configuration

### 1. Clone the repository
```bash
git clone https://github.com/pankajkumar53/WeatherSnap.git
cd WeatherSnap
```

### 2. Configure API Base URLs
The project uses `local.properties` to manage API endpoints safely. Create a `local.properties` file in the root directory (if it doesn't exist) and add the following keys:

```properties
GEO_BASE_URL=https://geocoding-api.open-meteo.com/v1/
WEATHER_BASE_URL=https://api.open-meteo.com/v1/
```
*Note: These URLs point to the Open-Meteo API, which provides free geocoding and weather data.*

### 3. Setup Permissions
The application requires the following permissions, which are already defined in `AndroidManifest.xml`:
- `android.permission.INTERNET`
- `android.permission.ACCESS_NETWORK_STATE`
- `android.permission.CAMERA`

### 4. Build the Project
1. Open the project in **Android Studio**.
2. Wait for the **Gradle Sync** to finish.
3. Select the `app` configuration and click **Run**.

## 🛠 Troubleshooting

### ANR or Performance Issues
If you encounter UI freezes while capturing or viewing photos, ensure:
- Bitmaps are decoded using `Dispatchers.IO`.
- Image compression is offloaded from the main thread.
- `SavedStateHandle` is only storing essential small primitives (like coordinates) rather than large Parcelable objects.

### Room Compilation Error
If you see a `kapt` error related to `Continuation` parameters in `Dao`, ensure you are using Room version `2.6.1` or higher and have the `google-devtools-ksp` plugin configured correctly for Kotlin 2.0.

## 📱 Features
- **Search:** Search for cities globally using Geocoding API.
- **Weather Details:** View temperature, humidity, wind speed, and pressure.
- **Camera Report:** Capture a photo of the current conditions, which is automatically compressed to save local space.
- **Saved Reports:** Access a local history of your weather captures with storage metrics.
