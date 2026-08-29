# REVENEX SCHOOL ERP — FIREBASE INTEGRATION GUIDE

## 1. Prerequisites
- A Google Cloud & Firebase project created on the [Firebase Console](https://console.firebase.google.com).
- Package name matching: `com.aistudio.revenexerp.vqpxtz` (or your configured applicationId).

## 2. Configuration Steps
1. Place the downloaded `google-services.json` file inside the `/app/` root directory.
2. Ensure the Google Services Gradle plugin is applied in `app/build.gradle.kts`:
   ```kotlin
   plugins {
     alias(libs.plugins.google.services)
   }
   ```
3. Enable the required Firebase products in your Firebase Console:
   - **Firebase Authentication**: Enable Email/Password and Google Sign-In providers.
   - **Cloud Firestore**: Create the database in production mode with appropriate security rules.
   - **Firebase Cloud Messaging (FCM)**: For push notifications on fee dues, circulars, and attendance alerts.
   - **Firebase Cloud Storage**: For student avatars, study materials, and report card PDFs.

## 3. Firestore Security Rules Blueprint
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Helper to check user role
    function getUserRole() {
      return request.auth.token.role;
    }
    
    // Principal has global administrative write access
    match /schools/{schoolId}/{document=**} {
      allow read, write: if request.auth != null && getUserRole() == 'PRINCIPAL';
    }
    
    // Teachers can update attendance and homework
    match /schools/{schoolId}/attendance/{recordId} {
      allow read, write: if request.auth != null && (getUserRole() == 'TEACHER' || getUserRole() == 'PRINCIPAL');
    }
    
    // Parents and students can read their own records
    match /schools/{schoolId}/students/{studentId} {
      allow read: if request.auth != null && (
        request.auth.uid == studentId || 
        getUserRole() == 'TEACHER' || 
        getUserRole() == 'PRINCIPAL'
      );
    }
  }
}
```
