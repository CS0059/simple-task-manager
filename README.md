# Simple Task Manager

A modern and user-friendly Android task management application built with **Jetpack Compose** and adhering to **Clean Architecture** principles.

## Installation

Follow these steps to get a local copy up and running.

**1. Clone the repository**

```bash
git clone https://github.com/CS0059/simple-task-manager.git
cd simple-task-manager
```
**2. Open in Android Studio**

 - Open Android Studio.

 - Select File > Open and navigate to the cloned directory.

 - Let Gradle sync the project dependencies.

**3. Run the App**

 - Connect an Android device or start an emulator.

 - Click the Run button (Shift+F10).


## Features

* **Task Management:** Easily add, edit, and delete tasks.
* **Progress Tracking:** Mark tasks as complete or incomplete.
* **Time Management:** Set specific start and end dates/times for every task.
* **Smart Notifications:** Receive timely alerts for task start and completion times.
* **Audio Feedback:** Custom sound effects for creating, completing, and deleting tasks.
* **Theme Support:** Full support for both Dark and Light modes.
* **Localization:** Multi-language support including English, Turkish, Spanish, French, German, Italian, Japanese, and Chinese.
* **Sorting Options:** Sort tasks by Date or ID for better organization.
* **Modern UI:** Designed with Material Design 3 guidelines.
* **Offline Capability:** Fully functional offline using Room Database.


## Tech Stack

### Language & Framework
* **Kotlin**
* **Jetpack Compose**

### Architecture & Design Patterns
The project follows **Clean Architecture** to ensure separation of concerns and testability:
* **Layers:** Domain, Data, Presentation
* **MVVM Pattern** (Model-View-ViewModel)
* **Repository Pattern**
* **Use Case Pattern**

### Key Libraries
* **Jetpack Compose:** Modern native UI toolkit.
* **Room Database:** Local data persistence and management.
* **Koin:** Pragmatic dependency injection.
* **Kotlin Coroutines & Flow:** Asynchronous programming and reactive streams.
* **Material 3:** The latest Android UI design components.
* **Lifecycle & ViewModel:** Robust lifecycle and state management.


## Requirements

To build and run this project, you will need:

* **Android Studio:** Hedgehog or newer
* **Java Development Kit (JDK):** Version 17
* **Minimum SDK:** 24 (Android 7.0)
* **Target SDK:** 34 (Android 14)
* **Gradle:** Version 8.13

