# 🚀 Service Booking Android App

A modern Android Service Booking Application built using **Jetpack Compose** and **Firebase**, designed to provide seamless service discovery, real-time appointment booking, automated reminders, and secure authentication with an elegant Material 3 UI.

---

---

# ✨ Features

- 🔎 Browse services by category
- ⭐ View ratings and service descriptions
- 📅 Real-time appointment booking
- 🔔 Automated booking reminders
- 👨‍💼 Admin dashboard for managing services
- 🌙 Dark mode support
- 🔐 Secure authentication with Firebase
- ⚡ Real-time database synchronization
- 📲 Modern Material 3 UI Design

---

# 🛠 Tech Stack

| Technology | Usage |
|---|---|
| Jetpack Compose | Declarative Android UI |
| Firebase Authentication | User Login & Registration |
| Firebase Firestore | Real-time Database |
| Firebase Storage | Image Storage |
| Kotlin Coroutines & Flow | Asynchronous Programming |
| Coil | Image Loading |
| DataStore | Local Preferences Storage |
| AlarmManager | Booking Reminders |
| JobScheduler | Background Sync |
| Material 3 | Modern UI Components |

---

# 🧠 Major Android Concepts Used

## 📌 Jetpack Compose Navigation
- Screen transitions
- Navigation graph management
- Deep linking support

---

## 🔥 Firebase Integration
- Real-time Firestore synchronization
- Secure user authentication
- Cloud image storage

---

## ⏰ Background Tasks

### AlarmManager
Used for:
- Appointment reminders
- Scheduled notifications

### JobScheduler
Used for:
- Data synchronization
- Background processing

---

## 💾 Local Data Persistence
Implemented using **Jetpack DataStore** for:
- Dark mode preference
- User settings
- Lightweight local storage

---

## 🔔 Notifications
Custom notification system using:
- NotificationChannel
- PendingIntent
- NotificationHelper

---

# 👥 Authentication & Roles

## User Features
- Login/Register
- Browse services
- Book appointments
- Receive reminders

## Admin Features
- Add/Edit/Delete services
- Manage bookings
- Monitor service listings

---

# 🎨 UI/UX Design

The application follows a:
- Clean
- Modern
- User-centric

design language with a professional:
- Blue
- Teal

color palette using **Material 3** guidelines.

---

# 📂 Project Structure

```bash
app/
├── ui/
├── navigation/
├── screens/
├── components/
├── data/
├── repository/
├── viewmodel/
├── utils/
└── firebase/
