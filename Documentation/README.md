AI Smart Countdown

Intelligent To-Do & Deadline Reminder Desktop Application

1. Project Overview

AI Smart Countdown is a Windows desktop application developed using Java and JavaFX. It helps users manage tasks, monitor upcoming deadlines, and organize their daily activities.

The application integrates Google Gemini AI to provide intelligent task suggestions, priority analysis, and task breakdown recommendations.

2. Key Features

Task Management

- Add new tasks.
- Edit existing tasks.
- Delete tasks.
- Mark tasks as completed.
- Reopen completed tasks.

Countdown Management

- Live countdown for upcoming deadlines.
- Automatic overdue calculation.
- Countdown updates every second.
- Persistent task storage after application restart.

AI-Powered Features

- AI Task Suggestions: Provides practical suggestions for organizing pending tasks.
- AI Priority Analysis: Analyzes pending tasks and recommends their priority order.
- AI Task Breakdown: Breaks an individual task into smaller, actionable steps.

Dashboard

- Total task summary.
- Pending task summary.
- Overdue task summary.
- Visual highlighting of completed and overdue tasks.

Database

- SQLite database integration.
- Persistent storage of task information.

Error Handling

- Handles Gemini API connection and service errors.
- Displays user-friendly error messages.
- Handles missing task selection for AI Task Breakdown.

3. Technologies Used

- Programming Language: Java
- User Interface: JavaFX
- Build Tool: Gradle
- Database: SQLite
- AI Integration: Google Gemini API
- Development Environment: Android Studio
- JDK: Java 21

4. Project Structure

The application follows a modular Java package structure to separate the user interface, data model, database operations, and AI services.

Project Directory

AISmartCountdown/
│
├── src/
│   └── main/
│       └── java/
│           └── com/
│               └── smartcountdown/
│
├── build.gradle
├── settings.gradle
├── gradlew
├── gradlew.bat
└── README.md

Java Package Structure

com.smartcountdown
│
├── SmartCountdownApp.java
│
├── model/
│   └── Task.java
│
├── database/
│   ├── DatabaseManager.java
│   └── TaskRepository.java
│
└── service/
├── CountdownService.java
└── GeminiService.java

Package Responsibilities

Package / Class| Responsibility
"com.smartcountdown"| Main JavaFX application and dashboard
"model"| Task data model
"database"| SQLite database initialization and task persistence
"service"| Countdown calculations and Gemini AI integration

This modular structure improves code organization, maintainability, and separation of responsibilities.

5. Prerequisites

- Windows operating system
- Java Development Kit (JDK) 21
- Android Studio or another suitable Java IDE
- Internet connection for Gemini AI features
- Google Gemini API key

6. Configuration

Set the Windows environment variable:

Variable name: GOOGLE_API_KEY
Variable value: Your Gemini API key

Restart the IDE or terminal after configuring the environment variable.

Never commit your actual API key to source control or include it in screenshots or submission documents.

7. Build and Run

Open the project directory in Android Studio.

Run the following commands in the terminal:

Build the project

.\gradlew.bat clean build

Run the application

.\gradlew.bat run

8. Database Storage

The application stores its SQLite database in:

%USERPROFILE%\.aismartcountdown\smartcountdown.db

Task information is retained when the application is closed and reopened.

9. AI Integration

The application uses Google's Gemini API through the Google Gen AI Java SDK.

The AI assistant provides:

- Task organization suggestions.
- Task priority analysis.
- Individual task breakdown recommendations.

An active internet connection and valid API key are required for AI functionality.

10. Project Objective

The objective of AI Smart Countdown is to combine task management, real-time deadline monitoring, and AI-powered planning in a single desktop application to improve productivity and time management.