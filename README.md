# KlachtenregistratieApp

An Android school project developed in Kotlin for the Associate Degree Informatica programme at Avans University of Applied Sciences.

## About the project

This application was developed for **Block 3 (Mobile & Innovation)** alongside a **Block 2 resit focused on object-oriented programming**. The assignment used a consumer-complaints scenario as a way to translate analysis and requirements into an Android application. The main learning goal was not to create a production-ready complaints service, but to apply software-development concepts and demonstrate the connection between requirements, design, implementation and validation.

The app lets users register consumers, sellers and purchased products; record complaints and their status or resolution; and capture photographic evidence with the phone camera. The data is stored locally on the device.

## Technical approach

- **Kotlin and Android Studio** for the Android application and UI.
- **Room / SQLite** for local relational data storage. Room DAO interfaces contain SQL queries for reading, inserting, updating and deleting records.
- **Object-oriented programming** through an abstract `Partij` superclass and specialised consumer/seller classes, including method overriding and encapsulation.
- **Separated packages** for `activity`, `dao`, `database`, `domain` and `model`, plus XML screen layouts.
- **Camera integration** using Android's Activity Result API to capture a photo for evidence registration.

The project followed the course's assessment-oriented development process: problem analysis, requirements, data and class modelling (ERD/UML), implementation and validation. The case and data were primarily educational examples; this is not a large-scale production database or a live external service.

## Running the project

1. Install **Android Studio** and the required Android SDK components.
2. Clone this repository or download its ZIP, then open the folder containing `settings.gradle.kts` in Android Studio.
3. Allow Android Studio to complete Gradle Sync. An internet connection may be needed to download dependencies.
4. Select an Android emulator or connect an Android phone with **Developer options → USB debugging** enabled.
5. Press **Run** to build, install and launch the application.

No separate database server or API key is required: the application uses a local Room/SQLite database.

## Testing and limitations

The original Android project was successfully launched on a physical OPPO Android phone in October 2026. The main screen, product registration flow and camera-based evidence capture were manually checked. This is **not** a claim of complete end-to-end test coverage.

Known limitations include manually entered sample data, limited automated tests, and Room's `fallbackToDestructiveMigration()` configuration, which can discard local data during an incompatible schema migration. The application is intended as an educational prototype.

## Development context

I developed this project as part of my coursework and the Block 2 resit, working against the assessment criteria and iterating on the design and implementation. I used AI-assisted development tools as support while making implementation decisions, testing functionality and refining the project. The repository was created **retrospectively in October 2026 for my internship portfolio**; its GitHub creation date does not represent the original development period.

This repository is a cleaned-up copy of the original school project. Generated build outputs, local machine settings and caches have been excluded. The application source code and Gradle project structure have otherwise been retained.
