# Vocab Trainer

Vocab Trainer is an Android vocabulary learning app built with Native Kotlin and XML. It helps users learn, review, and test English words through a structured daily routine with progress tracking, streaks, reminders, and lightweight personalization.

## 5W1H Overview

### What
Vocab Trainer is a vocabulary training app that combines word learning, review, and quiz flows in one place.

### Why
The app is designed to make vocabulary practice consistent and measurable. It reduces friction by combining learning, repetition, and feedback in a simple daily workflow.

### Who
It is built for learners who want to improve their English vocabulary on a regular basis, especially users who prefer short sessions instead of long study blocks.

### Where
The app runs on Android devices and uses local storage, Firebase services, and a remote dictionary API to support learning and authentication.

### When
Users can use the app every day for a short learning session, review weak words, and take a quiz to reinforce retention. Notifications and streak tracking encourage regular practice.

### How
The app follows an MVVM-style structure with ViewModel, Repository, Room, Retrofit, WorkManager, and Firebase integration. Users can learn words, mark them as learned, review weak words, search the word list, and complete quizzes that update their progress.

## Key Features

- Daily word learning flow
- Review mode for all words, weak words, and daily words
- Multiple-choice quiz mode with score tracking
- Word list with search
- Streak tracking
- Notification reminders
- Audio pronunciation using TextToSpeech
- Local persistence with Room
- Remote dictionary lookup with Retrofit
- Firebase authentication and Firestore support
- Background sync and scheduled work with WorkManager

## Tech Stack

- Kotlin
- XML layouts
- AndroidX
- ViewModel and LiveData
- Room
- Retrofit
- WorkManager
- Firebase Auth
- Firestore
- TextToSpeech

## Project Structure

- `app/src/main/java/com/example/vocabtrainer/` contains the application code
- `data/` contains local and remote data layers
- `ui/` contains the screen logic and ViewModels
- `notification/` contains reminder and notification helpers
- `sync/` contains sync-related workers and schedulers
- `res/` contains layouts, drawables, and values

## Setup

### Requirements

- Android Studio
- JDK 17
- An Android device or emulator with API 26 or higher
- Internet access for remote dictionary and Firebase features

### Run the app

1. Open the project in Android Studio.
2. Make sure `google-services.json` is present in the `app/` directory.
3. Sync Gradle dependencies.
4. Run the app on a device or emulator.

## Best Practice

Use the app in short daily sessions and start with the review flow before taking quizzes. This keeps practice focused on weak words and improves long-term retention without overwhelming the learner.

## Notes

- The launcher activity is the login screen.
- Notification permission is required on supported Android versions.
- The app uses a daily word limit and streak logic to encourage consistent practice.
