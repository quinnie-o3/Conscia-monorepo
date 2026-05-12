# Project Context: Conscia

## Overview
Conscia is a native Android productivity and digital wellbeing application built with **Java in Android Studio**.  
Its main purpose is to help users track smartphone/app usage time and understand **why** they use apps, not only **how long** they use them.

Unlike basic screen-time trackers, Conscia allows users to classify usage sessions by purpose, such as:
- Study
- Work
- Entertainment
- Social media
- Rest
- Mindless scrolling

The goal of the app is to improve users' **self-awareness**, **focus**, and **time management habits**.

---

## App Category
- Productivity
- Digital Wellbeing
- Time Tracking
- Self-awareness / Habit Monitoring

---

## Main Features
1. **Authentication**
   - User sign up, login, logout
   - Basic profile management

2. **App Usage Tracking**
   - Track how long users spend on different apps
   - Show top used apps
   - Support daily, weekly, and monthly views

3. **Session Purpose Tagging**
   - Let users assign a purpose/category to app usage sessions
   - Example tags: Study, Work, Entertainment, Social, Rest, Mindless Scrolling

4. **Dashboard and Statistics**
   - Total screen time
   - Most used apps
   - Usage distribution by purpose
   - Time trends and simple charts

5. **Goals and Limits**
   - Set daily/weekly screen-time goals
   - Set app-specific usage limits
   - Track progress

6. **Notifications and Reminders**
   - Notify users when app usage exceeds limits
   - Remind users to classify sessions or review habits

7. **History and Insights**
   - View usage history
   - Show insights such as most distracting apps, peak usage times, and common purposes

---

## User Types

### 1. Regular User
Main end-user of the application.

Permissions:
- Register and log in
- Update profile
- View personal usage data
- Tag usage sessions
- Set goals and limits
- View dashboard, statistics, and insights
- Receive notifications

### 2. Admin (optional)
Used for backend/system management or project demonstration.

Permissions:
- Manage users
- Manage purpose tags/categories
- View overall system statistics
- Manage app configurations

> For MVP, only the **Regular User** role is required.

---

## Core Data Entities

### User
Stores account and profile information.
- user_id
- full_name
- email
- password_hash
- avatar_url
- created_at
- updated_at

### Device
Stores user device information.
- device_id
- user_id
- device_name
- model
- os_version
- created_at

### AppInfo
Stores installed/tracked application information.
- app_id
- package_name
- app_name
- category
- icon_reference

### UsageSession
Stores each app usage session.
- session_id
- user_id
- device_id
- app_id
- start_time
- end_time
- duration
- date

### PurposeTag
Stores usage purpose categories.
- tag_id
- tag_name
- color_code
- description

### SessionTag
Links a usage session with one or more purpose tags.
- id
- session_id
- tag_id
- note
- classified_at

### Goal
Stores usage goals or limits.
- goal_id
- user_id
- goal_type
- target_value
- period_type
- app_id (nullable)
- status
- created_at

### Reminder
Stores notification/reminder rules.
- reminder_id
- user_id
- type
- condition_value
- message
- is_active

### InsightReport / DailySummary
Stores aggregated analytics data.
- report_id
- user_id
- date
- total_usage_time
- top_app
- most_common_purpose
- distraction_score

---

## Entity Relationships
- One **User** can have many **Devices**
- One **User** can have many **UsageSessions**
- One **User** can have many **Goals**
- One **User** can have many **Reminders**
- One **AppInfo** can appear in many **UsageSessions**
- One **Device** can have many **UsageSessions**
- One **UsageSession** can have one or many **PurposeTags** through **SessionTag**

---

## Non-Functional / Technical Requirements

### Platform
- Native Android app
- Built with **Java**
- Developed in **Android Studio**

### Database
Possible options:
- Local: SQLite / Room
- Remote: Firebase Realtime Database / Firestore / MySQL via custom backend API

### Backend / API
If backend is used:
- Authentication API
- User profile API
- Usage tracking API
- Goal/reminder API
- Dashboard/statistics API

### Real-time Features
- WebSocket is **not required**
- Push notifications or local notifications are recommended

### File Upload
- Avatar/profile image upload is needed
- No complex document upload required for MVP

### Search / Filter / Pagination
Needed for:
- Usage history
- App lists
- Statistics by date range
- Tag-based filtering

### Analytics / Reporting
Important module of the app:
- Daily/weekly/monthly usage reports
- Most used apps
- Purpose distribution
- Goal progress
- Distraction insights

---

## Development Goal
This project is intended as a student Android app project focusing on:
- mobile UI/UX
- local or remote data storage
- user behavior tracking
- statistics and insight visualization
- practical Java Android development
