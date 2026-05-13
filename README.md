# Personal Contribution

This repository is a public snapshot cloned from the team's original private repository, so the commit history here does not accurately reflect each member's contribution.

This README summarizes my actual role in the project based on the final source code, the team report, and the integration work I handled during development.

## Project Context

Android application for project and human resource management, built with:

- Java
- Android Studio
- Firebase Authentication
- Cloud Firestore
- Cloudinary

Core modules in the app include authentication, employee management, project management, task management, attendance, salary, cost tracking, reminders, notifications, and settings.

## My Main Role

My officially assigned area in the report was **authentication**. In practice, I also contributed significantly to **integration work**, especially where UI behavior had to match real data from Firebase/Firestore.

My contribution was strongest in these areas:

- building the login and signup flow;
- connecting authentication with user profile data;
- improving account-related screens and user flows;
- helping merge, align, and stabilize screens that depended on shared database fields;
- making UI and stored data more consistent across modules.

## Key Contributions

### 1. Authentication Flow

I was responsible for the main account flow of the app:

- login;
- signup;
- logout;
- forgot password;
- change password;
- delete account;
- basic validation for account forms.

Relevant files:

- [`app/src/main/java/com/example/group13/activity/MainActivity.java`](app/src/main/java/com/example/group13/activity/MainActivity.java)
- [`app/src/main/java/com/example/group13/activity/SignupActivity.java`](app/src/main/java/com/example/group13/activity/SignupActivity.java)
- [`app/src/main/java/com/example/group13/activity/SettingActivity.java`](app/src/main/java/com/example/group13/activity/SettingActivity.java)
- [`app/src/main/java/com/example/group13/activity/SettingChangepasswordActivity.java`](app/src/main/java/com/example/group13/activity/SettingChangepasswordActivity.java)

### 2. Linking Auth With User Profile Data

Beyond sign-in/sign-up screens, I also helped shape the flow between Firebase Authentication and Firestore user data:

- creating the initial `users` document after signup;
- using `profileCompleted` to drive onboarding and profile completion;
- connecting account data to Home and Settings screens;
- aligning displayed user information with Firestore fields such as `employeeName`, `employeeId`, `position`, `department`, and `avatarUrl`.

Relevant files:

- [`app/src/main/java/com/example/group13/activity/MainActivity.java`](app/src/main/java/com/example/group13/activity/MainActivity.java)
- [`app/src/main/java/com/example/group13/activity/SignupActivity.java`](app/src/main/java/com/example/group13/activity/SignupActivity.java)
- [`app/src/main/java/com/example/group13/activity/EmployeeAddEditActivity.java`](app/src/main/java/com/example/group13/activity/EmployeeAddEditActivity.java)
- [`app/src/main/java/com/example/group13/activity/HomeActivity.java`](app/src/main/java/com/example/group13/activity/HomeActivity.java)
- [`app/src/main/java/com/example/group13/activity/SettingActivity.java`](app/src/main/java/com/example/group13/activity/SettingActivity.java)

### 3. UI and Database Consistency

One important part of my real contribution, which is not fully reflected in the report, was helping keep the interface and data model consistent.

This included work such as:

- checking that form input matched Firestore structure;
- reviewing shared field usage across multiple screens;
- reducing mismatches between UI labels, model fields, and stored documents;
- supporting smoother cross-screen data display for employee, project, salary, cost, and notification flows.

Relevant areas:

- [`app/src/main/java/com/example/group13/activity/EmployeeAddEditActivity.java`](app/src/main/java/com/example/group13/activity/EmployeeAddEditActivity.java)
- [`app/src/main/java/com/example/group13/activity/ProjectAddEditActivity.java`](app/src/main/java/com/example/group13/activity/ProjectAddEditActivity.java)
- [`app/src/main/java/com/example/group13/activity/SalaryListActivity.java`](app/src/main/java/com/example/group13/activity/SalaryListActivity.java)
- [`app/src/main/java/com/example/group13/activity/SalaryAddEditActivity.java`](app/src/main/java/com/example/group13/activity/SalaryAddEditActivity.java)
- [`app/src/main/java/com/example/group13/activity/CostProjectListActivity.java`](app/src/main/java/com/example/group13/activity/CostProjectListActivity.java)
- [`app/src/main/java/com/example/group13/activity/CostActivity.java`](app/src/main/java/com/example/group13/activity/CostActivity.java)
- [`app/src/main/java/com/example/group13/activity/SettingNotificationActivity.java`](app/src/main/java/com/example/group13/activity/SettingNotificationActivity.java)

### 4. Integration and Final Stabilization

During later stages of the project, I also contributed to integration and cleanup work:

- merge support;
- fixing cross-module issues;
- improving shared user flows;
- helping align screens after multiple modules were combined;
- supporting the overall consistency of the app before final delivery.

Relevant shared-flow files:

- [`app/src/main/java/com/example/group13/base/BaseActivity.java`](app/src/main/java/com/example/group13/base/BaseActivity.java)
- [`app/src/main/java/com/example/group13/activity/HomeActivity.java`](app/src/main/java/com/example/group13/activity/HomeActivity.java)
- [`app/src/main/java/com/example/group13/activity/SettingActivity.java`](app/src/main/java/com/example/group13/activity/SettingActivity.java)
- [`app/src/main/java/com/example/group13/activity/SettingNotificationActivity.java`](app/src/main/java/com/example/group13/activity/SettingNotificationActivity.java)

## What This README Clarifies

If someone only reads the team report, my role may look limited to login and signup. In reality, I also contributed to:

- account and profile flow integration;
- UI/data alignment with Firebase and Firestore;
- consistency between screens sharing the same database structure;
- final-stage fixing and stabilization across modules.

In short, my contribution was not only **authentication**, but also a meaningful part of the app's **integration** and **data consistency** work.
