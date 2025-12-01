# SOLVEXIS Metro Route Planner

A Java console application for planning metro routes in Hyderabad Metro system with integrated last-mile connectivity options.

## Project Structure

```
SOLVEXIS/
├── solvexis/
│   ├── app/                          # Application entry points
│   │   ├── MetroApp.java            # Main application class
│   │   └── ListStations.java        # Utility to list all stations
│   │
│   ├── model/                        # Station model classes
│   │   ├── Station.java             # Abstract base class for stations
│   │   ├── NormalStation.java       # Regular metro stations
│   │   ├── TerminalStation.java     # Terminal/end stations
│   │   └── JunctionStation.java     # Interchange stations
│   │
│   ├── service/                      # Core business logic services
│   │   ├── MetroRoutePlanner.java   # Route planning and fare calculation
│   │   ├── AlertsService.java       # Journey alerts during travel
│   │   ├── PlatformCrowdDensityService.java  # Crowd density indicator
│   │   ├── CarbonFootprintService.java       # CO2 savings calculator
│   │   ├── UserStatsService.java    # Travel statistics and gamification
│   │   ├── RoutePlanner.java        # Route planner interface
│   │   └── transport/               # Last-mile transport services
│   │       ├── LastMileService.java # Last-mile connectivity options
│   │       └── TransportMode.java   # Transport mode class (RTC, Ola, Uber, Rapido)
│   │
│   ├── data/                         # Data persistence
│   │   └── TripHistory.java         # Trip history management
│   │
│   └── exceptions/                   # Custom exceptions
│       ├── StationNotFoundException.java
│       └── InvalidRouteException.java
│
├── trip_history.txt                  # Generated trip history file
├── user_stats.txt                    # Generated user statistics file
└── README.md                         # This file
```

## Features

- **Route Planning**: Find optimal routes between any two metro stations
- **Fare Calculation**: Automatic fare calculation based on distance
- **Last-Mile Connectivity**: Options for RTC Bus, Ola, Uber, and Rapido
- **Platform Crowd Density**: Real-time crowd level indicators (PCDI)
- **Carbon Footprint Tracker**: Calculate CO2 savings vs car travel
- **Travel Statistics**: Track trips, distance, points, and achievements
- **Journey Alerts**: Real-time alerts during metro journey with lane/corridor change notifications
- **Lane Change Alerts**: Clear notifications when transferring between different metro corridors (Red, Blue, Green lines)
- **Trip History**: Save and view complete trip history

## Requirements

- Java 8 or higher (JDK)
- No external libraries required

## Compilation and Execution

### Windows (PowerShell) - UI Version (Recommended)
```bash
mkdir out
javac -d out solvexis/app/MetroUILauncher.java
java -cp out solvexis.app.MetroUILauncher
```

### Windows (PowerShell) - Console Version
```bash
mkdir out
javac -d out solvexis/app/MetroApp.java
java -cp out solvexis.app.MetroApp
```

### Linux/Mac - UI Version (Recommended)
```bash
mkdir out
javac -d out solvexis/app/MetroUILauncher.java
java -cp out solvexis.app.MetroUILauncher
```

### Linux/Mac - Console Version
```bash
mkdir out
javac -d out solvexis/app/MetroApp.java
java -cp out solvexis.app.MetroApp
```

## User Interface (UI)

The SOLVEXIS Metro Planner features a modern graphical user interface with multiple tabs for easy navigation:

### Metro Ticket Tab
- **Select Route**: Choose start and end stations from dropdown menus
- **Find Route**: Click to calculate the shortest route with fare and estimated time
- **Start Ride**: Simulates your journey with real-time alerts:
  - ✓ Departure notifications
  - → Next station alerts with corridor information
  - ⚠️ **Lane Change Alerts**: Clear notifications when transferring between different metro corridors (Red → Blue, Blue → Green, etc.)
  - ✓ Arrival notifications
- **Save Metro Trip**: Records the metro journey to your trip history and updates statistics

### Last Mile Tab
- **Destination Details**: Enter destination address and distance
- **Show Options**: Displays available transport modes with fares and estimated times:
  - TSRTC Bus
  - Ola / Uber  
  - Rapido
- **Start Last Mile Ride**: Simulates the last-mile journey with alerts
- **Save Trip**: Saves the complete journey (metro + last mile) with combined statistics

### Crowd Indicator Tab
- **Real-time Crowd Levels**: Shows platform density for each station on your route
- **PCDI Ratings**: Visual indicators with emojis and descriptions:
  - 🟢 Level 1: Comfortable - Plenty of Space
  - 🟡 Level 2: Moderate Crowding
  - 🟠 Level 3: Busy but Manageable
  - 🔴 Level 4: Uncomfortable - Movement Restricted
  - ⛔ Level 5: Critical Level - Unsafe Conditions

### Carbon Indicator Tab
- **Eco-friendly Tracking**: Displays CO₂ saved by using metro vs car
- **Distance Metrics**: Shows route distance in kilometers
- **Environmental Impact**: Quantifies your green contribution per journey

### Stations Tab
- **All Metro Stations**: Browse all 59 stations organized by corridor:
  - 🔴 Red Line: 27 stations (Miyapur ↔ LB Nagar)
  - 🔵 Blue Line: 23 stations (Nagole ↔ Raidurg)
  - 🟢 Green Line: 9 stations (JBS Parade Ground ↔ MGBS)

### Trip History Tab
- **Complete Journey Records**: View all saved trips with:
  - Date and time
  - Route (start → end stations)
  - Fare breakdown
  - CO₂ savings

### Stats Tab
- **Personal Statistics**: Track your metro usage:
  - Total trips completed
  - Total kilometers traveled
  - CO₂ emissions saved
  - Points earned through gamification
  - Longest route taken

## How It Works

### Metro Network
The application includes all 59 stations across 3 corridors:
- **Corridor I (Red Line)**: Miyapur ↔ LB Nagar (27 stations)
- **Corridor II (Blue Line)**: Nagole ↔ Raidurg (23 stations)
- **Corridor III (Green Line)**: JBS Parade Ground ↔ MGBS (9 stations)

### Route Finding
Uses Breadth-First Search (BFS) algorithm to find shortest path between stations.

### Fare Structure
- 0-2 stations: ₹10
- 3-5 stations: ₹15
- 6-9 stations: ₹20
- 10-16 stations: ₹30
- 17+ stations: ₹40

### Last-Mile Options
- **RTC Bus**: ₹10/km (min 5km)
- **Ola/Uber**: ₹20/km + ₹25 booking fee (min 3km)
- **Rapido**: ₹12/km (min 2km)

### Platform Crowd Density (PCDI)
- Level 1 (🟢): < 1 person/m² - Comfortable
- Level 2 (🟡): 1-2 persons/m² - Moderate
- Level 3 (🟠): 2-3 persons/m² - Busy
- Level 4 (🔴): 3-4 persons/m² - Uncomfortable
- Level 5 (⛔): > 4 persons/m² - Critical

### Carbon Footprint
Calculates CO2 saved by using metro instead of car:
- Formula: Distance × 0.12 kg/km

### Gamification
- Points: 10 points per km + 5 bonus per trip
- Tracks: Total trips, distance traveled, CO2 saved, longest route

### Journey Alerts & Lane Change Notifications
When journey alerts are enabled, the system provides:
- **Station Notifications**: Alerts for departure, next station, and arrival
- **Lane Change Alerts**: Automatic notifications when transferring between different metro corridors
  - Example: "⚠️ Lane changed from Red Line (Corridor I) to Blue Line (Corridor II) at Ameerpet"
  - Applies to transfers between Red, Blue, and Green lines
  - Helps passengers track corridor/line changes during multi-line journeys

## Console Menu Options (Legacy Command-Line Version)

1. **Book Metro Ticket & Last Mile** - Plan route, book ticket, start journey, select last-mile transport
2. **List All Stations** - View all stations organized by corridor
3. **View Recent Trips** - Display trip history from file
4. **Check Platform Crowd Density** - Check crowd levels for stations
5. **View Travel Stats & Leaderboard** - View personal statistics and rankings
6. **Toggle Journey Alerts** - Enable/disable journey alerts (with lane change notifications)
7. **Exit** - Close application

## File Outputs

- `trip_history.txt`: Complete trip details with dates, routes, fares, and CO2 savings
- `user_stats.txt`: Persistent user statistics including trips, distance, points

## Notes

- All metro network data is hardcoded for Hyderabad Metro system
- Last-mile distances are entered by user
- Crowd density values are randomly generated for demonstration
- Statistics persist across application sessions
