# Punch-In Application

A modern Android application built with Jetpack Compose for tracking employee punch-ins with location tracking, route plotting, data visualization, and an interactive coach mark system for user guidance.

## 🚀 Features

### Authentication
- **User Registration & Login**: Secure local authentication with username/password
- **Session Management**: Persistent login state using SharedPreferences
- **Auto-login**: Remembers user session across app restarts

### Home Screen
- **Weekly Statistics Dashboard**: 
  - Interactive donut charts showing punch-in distribution by day of week
  - Visual representation of weekly punch-in patterns
- **Recent Activity**: 
  - Displays last 5 punch-in records with timestamps and coordinates
  - Quick access to recent location data
- **Quick Actions**: 
  - Direct navigation to Punch-In screen
  - Access to Route Plotting (when unlocked)
- **Coach Mark System**: 
  - Interactive tutorials highlighting key features
  - Step-by-step guidance for new users
  - Customizable widget highlighting

### Punch-In Screen
- **Location Tracking**: 
  - Real-time GPS location capture (latitude/longitude)
  - Automatic location updates
  - Permission handling with runtime requests
- **Timer System**: 
  - **10-minute interval** between required punch-ins
  - **1-minute warning** before next required punch-in
  - Visual countdown timer display
  - Color-coded status indicators (normal/warning/overdue)
- **Screen Locking**: 
  - Automatic lock when punch-in is overdue
  - Prevents access to other screens until punch-in is completed
  - Visual overlay with warning message
- **Success/Error Feedback**: 
  - Success message displayed for 3 seconds
  - Error messages displayed for 4 seconds
  - Clear visual feedback for all operations
- **Coach Mark Integration**: 
  - Interactive tutorials for timer, location, and punch-in button
  - Contextual help for each component

### Route Plotting Screen
- **Google Maps Integration**: 
  - Full-screen interactive map
  - Real-time location display
  - Custom markers for each punch-in location
- **Route Visualization**: 
  - Select multiple punch-ins to plot
  - Polyline connections showing travel path
  - Chronological route ordering
- **Interactive Features**: 
  - Bottom sheet for punch-in selection
  - Select All / Clear options
  - Auto-zoom to show all selected points
  - Individual marker information (time, coordinates)
- **Location Services**: 
  - Current location marker when no data available
  - Permission handling for location access
  - Automatic camera positioning

### Coach Mark System
- **Generic Coach Mark Framework**: 
  - Reusable `withCoachMark()` wrapper function
  - Works with any composable (Text, Button, Card, FAB, etc.)
  - Customizable reveal effects and styles
- **Pre-built Components**: 
  - `CoachMarkFloatingActionButton()` - For floating action buttons
  - `CoachMarkTextView()` - For text widgets
  - `CoachMarkActionButton()` - For action buttons
  - `CoachMarkIconButton()` - For icon buttons (supports both ImageVector and drawable resources)
- **Features**: 
  - Multiple reveal effects (Circle, Default, Custom)
  - Customizable background styles
  - Position-based targeting
  - Interactive overlay with images and descriptions

## 🏗️ Architecture

The app follows **MVVM (Model-View-ViewModel)** architecture pattern:

- **Data Layer**: 
  - Room database with entities (User, PunchIn)
  - DAOs for database operations
  - Repositories for data management
- **Domain Layer**: 
  - Business logic in repositories
  - Timer utilities
  - Session management
- **Presentation Layer**: 
  - ViewModels for state management
  - Compose UI screens
  - Reusable components
- **Dependency Injection**: 
  - Koin for dependency management
  - Modular architecture

## 📱 Screens

1. **Login Screen**: User authentication and registration
2. **Home Screen**: Dashboard with statistics and quick actions
3. **Punch-In Screen**: Location tracking and timer management
4. **Route Screen**: Google Maps with route visualization

## 🛠️ Setup Instructions

### 1. Google Maps API Key

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select an existing one
3. Enable the **Maps SDK for Android**
4. Create credentials (API Key)
5. Open `app/src/main/AndroidManifest.xml`
6. Replace the API key in the meta-data tag:

```xml
<meta-data
    android:name="com.google.android.geo.API_KEY"
    android:value="YOUR_ACTUAL_API_KEY_HERE" />
```

### 2. Permissions

The app requires the following permissions (already configured in AndroidManifest.xml):
- `INTERNET` - For Google Maps and network operations
- `ACCESS_FINE_LOCATION` - For precise location tracking
- `ACCESS_COARSE_LOCATION` - For approximate location tracking

**Note**: Location permissions are requested at runtime for Android 6.0+ devices.

### 3. Build and Run

1. Open the project in Android Studio (Arctic Fox or later recommended)
2. Sync Gradle files
3. Ensure you have a valid Google Maps API key
4. Build the project
5. Run on an emulator or physical device (location features work best on physical devices)

## 📂 Project Structure

```
app/src/main/java/com/app/punchinapplication/
├── coachmark/                    # Coach mark system
│   ├── Coach.kt                  # Coach overlay component
│   ├── CoachMarkHost.kt          # Coach mark container
│   ├── CoachMarkScope.kt         # Scope interface
│   ├── CoachMarkState.kt         # State management
│   ├── CoachStyle.kt             # Style interfaces
│   └── RevealEffect.kt          # Reveal effect interfaces
├── data/
│   ├── local/
│   │   ├── entity/              # Room database entities
│   │   │   ├── PunchInEntity.kt
│   │   │   └── UserEntity.kt
│   │   ├── dao/                 # Data Access Objects
│   │   │   ├── PunchInDao.kt
│   │   │   └── UserDao.kt
│   │   └── database/            # Database class
│   │       └── AppDatabase.kt
│   └── repository/              # Repository layer
│       ├── PunchInRepository.kt
│       └── UserRepository.kt
├── di/                          # Koin dependency injection
│   └── AppModule.kt
├── presentation/
│   ├── components/              # Reusable UI components
│   │   ├── DonutChart.kt       # Chart visualization
│   │   └── WarningAlertOverlay.kt
│   ├── navigation/             # Navigation and screen locking
│   │   ├── NavGraph.kt
│   │   └── ScreenLockManager.kt
│   ├── screen/                 # Compose screens
│   │   ├── HomeScreen.kt
│   │   ├── LoginScreen.kt
│   │   ├── PunchInScreen.kt
│   │   └── RouteScreen.kt
│   └── viewmodel/              # ViewModels
│       ├── HomeViewModel.kt
│       ├── LoginViewModel.kt
│       ├── PunchInViewModel.kt
│       └── RouteViewModel.kt
├── util/                       # Utility classes
│   ├── PunchInTimer.kt         # Timer logic
│   └── SessionManager.kt       # Session management
├── ui/theme/                   # App theme
│   ├── Color.kt
│   ├── Theme.kt
│   └── Type.kt
└── MainActivity.kt             # Main activity with coach mark helpers
```

## 🔑 Key Components

### Screen Locking Logic

The app implements an intelligent screen locking mechanism:
- **Timer-based**: 10-minute interval between required punch-ins
- **Warning System**: 1-minute warning before next required punch-in
- **Auto-lock**: When time exceeds 10 minutes, all screens except Punch-In are locked
- **Visual Feedback**: Overlay warning with clear instructions
- **Auto-unlock**: Lock is automatically released after successful punch-in

### Timer System

- **Interval**: 10 minutes between required punch-ins
- **Warning Threshold**: 1 minute before next required punch-in (9 minutes elapsed)
- **Overdue State**: When time exceeds 10 minutes, screens are locked
- **Visual Indicators**: 
  - Normal state: Primary color
  - Warning state: Tertiary/warning color
  - Overdue state: Error color
- **Real-time Updates**: Timer updates every second

### Data Storage

- **Room Database**: Local SQLite database for data persistence
- **Entities**: 
  - User credentials (username, password)
  - Punch-in records (timestamp, latitude, longitude, username)
- **Features**: 
  - Weekly data aggregation for chart visualization
  - Chronological sorting for route plotting
  - User-specific data isolation

### Coach Mark System

- **Generic Wrapper**: `withCoachMark()` function works with any composable
- **Customizable**: 
  - Position-based targeting
  - Custom reveal effects
  - Custom background styles
  - Title and description support
- **Pre-built Components**: Ready-to-use wrappers for common UI elements
- **Interactive**: Overlay with images and instructional content

## 🎨 Technologies Used

- **Jetpack Compose** - Modern declarative UI toolkit
- **Material Design 3** - Latest Material Design components
- **Room Database** - Local data persistence
- **Koin** - Lightweight dependency injection framework
- **Navigation Compose** - Type-safe navigation
- **Google Maps Compose** - Maps SDK integration
- **Coroutines & Flow** - Asynchronous programming and reactive streams
- **Fused Location Provider** - Google Play Services location API
- **WindowInsets** - Edge-to-edge display support
- **Activity Result API** - Modern permission handling

## 📋 Key Features Details

### Location Tracking
- Uses Google Play Services Fused Location Provider
- Requests fine/coarse location permissions at runtime
- Handles permission denial gracefully
- Updates location automatically when permissions granted

### Route Plotting
- Interactive Google Maps integration
- Multiple punch-in selection
- Automatic bounds calculation
- Chronological route ordering
- Custom markers with timestamps
- Polyline visualization

### Data Visualization
- Donut charts for weekly statistics
- Day-wise punch-in distribution
- Recent activity list
- Real-time data updates

### User Experience
- Edge-to-edge display support
- System bar padding (status bar and navigation bar)
- Smooth animations and transitions
- Clear visual feedback
- Error handling with user-friendly messages
- Auto-dismissing success/error messages with delays

## 📝 Notes

- The app uses local authentication (username/password stored in Room database)
- Location permissions must be granted for punch-in functionality
- Google Maps requires a valid API key to function
- All punch-in data is stored locally in the Room database
- Timer interval is set to 10 minutes (configurable in `PunchInTimer.kt`)
- Coach mark system is fully customizable and reusable
- Success messages display for 3 seconds, error messages for 4 seconds

  

https://github.com/user-attachments/assets/b05cdcff-c763-46dc-b46e-e2e86d5df051



## 🔄 Future Enhancements

Potential improvements for future versions:
- Cloud sync for punch-in data
- Export functionality (CSV, PDF)
- Advanced analytics and reporting
- Biometric authentication
- Offline map caching
- Multiple user profiles
- Customizable timer intervals
- Push notifications for reminders

## 📄 License

This project is created for educational/assignment purposes.

---

**Built with ❤️ using Jetpack Compose**
