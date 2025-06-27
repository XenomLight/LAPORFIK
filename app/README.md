# LAPORFIK Android App Structure

## Overview
This project uses a feature-based structure for clarity, scalability, and maintainability. Each feature or domain area has its own folder, and common patterns are followed for modern Android development.

## Structure

```
app/
  ├── src/
  │   └── main/
  │       ├── java/com/example/applaporfik/
  │       │   ├── data/                # Data models, repositories, API
  │       │   │   ├── model/
  │       │   │   └── repository/
  │       │   ├── ui/                  # All UI (fragments, adapters, viewmodels)
  │       │   │   ├── admin/
  │       │   │   ├── user/
  │       │   │   ├── form/
  │       │   │   ├── home/
  │       │   │   └── profil/
  │       │   ├── util/                # Utility classes/helpers (if any)
  │       │   └── MainActivity.kt, SplashActivity.kt, LoginActivity.kt, etc.
  │       ├── res/                     # Resources (layouts, drawables, etc.)
  │       └── AndroidManifest.xml
  ├── build.gradle.kts
  └── ... (other gradle/config files)
```

- **data/model/**: Data classes (e.g., FeedbackItem)
- **data/repository/**: Data access, repositories
- **ui/admin/**: Admin dashboard, admin features
- **ui/user/**: User dashboard, user features
- **ui/form/**, **ui/home/**, **ui/profil/**: Feature-specific UI
- **util/**: Utility classes (if needed)

## Benefits
- Easier to maintain and scale
- Clear separation of concerns
- Feature-based navigation

---
Feel free to add more features or layers as your app grows! 