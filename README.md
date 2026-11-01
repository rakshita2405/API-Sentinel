# API Sentinel

A Kotlin-based API automation testing project built using Ktor Client, JUnit 5, and Gradle.

## Overview

API Sentinel automates tests for REST API endpoints. It sends HTTP requests, validates response status codes and JSON data, and measures API response time.

The project uses JSONPlaceholder, a public demonstration REST API, to practise API automation testing.

## Features

- GET requests to retrieve user information
- POST requests to create a user
- PUT requests to update user information
- PATCH requests to partially update user information
- DELETE requests to delete a user
- HTTP status-code validation
- JSON response validation
- Response-time measurement
- Reusable API request methods through `ApiClient`

## Tech Stack

- **Language:** Kotlin
- **HTTP Client:** Ktor Client
- **Testing Framework:** JUnit 5
- **Build Tool:** Gradle
- **API:** JSONPlaceholder
- **IDE:** IntelliJ IDEA

## Project Structure

```text
API-Sentinel/
├── src/
│   └── test/
│       └── kotlin/
│           └── com/
│               └── apisentinel/
│                   ├── ApiClient.kt
│                   └── ApiSentinelTest.kt
├── build.gradle.kts
├── settings.gradle.kts
├── gradlew
├── gradlew.bat
├── gradle/
└── README.md
```

## Test Coverage

The project currently contains seven automated tests covering:

| HTTP Method | Test Scenario | Expected Status |
|---|---|---|
| GET | Retrieve an existing user | 200 |
| GET | Retrieve another existing user | 200 |
| GET | Request a nonexistent user | 404 |
| POST | Create a user | 201 |
| PUT | Update a user's information | 200 |
| PATCH | Partially update a user's information | 200 |
| DELETE | Delete a user | 200 |

## How to Run

### Prerequisites

- JDK 21
- IntelliJ IDEA or a compatible IDE
- Internet connection

### Run the tests

**Windows:**

Open a terminal in the project root and execute:

```powershell
.\gradlew.bat test
```

**macOS/Linux:**

```bash
./gradlew test
```

The test results will be generated in the Gradle build reports directory.

## What I Learned

Through this project, I practised:

- Writing automated API tests using Kotlin
- Sending HTTP requests using Ktor Client
- Validating HTTP status codes and JSON responses
- Organizing request logic into a reusable API client
- Running tests using JUnit 5 and Gradle

## Future Improvements

- Add more positive and negative test scenarios
- Improve test data management
- Generate more detailed test reports
- Automate test execution through GitHub Actions

## Disclaimer

JSONPlaceholder is a demonstration API. Its write operations simulate changes rather than persistently modifying a production database.

---

**Project:** API Sentinel  
**Purpose:** Learning and practising Kotlin-based API automation testing.