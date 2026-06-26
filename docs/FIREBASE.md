# RoadRescue — Firebase & stack

## Tech stack

- **Android** — Kotlin, Jetpack Compose, Material 3  
- **Navigation** — Navigation Compose  
- **Auth** — Firebase Authentication (email/password)  
- **Database** — Cloud Firestore  
- **Maps** — Google Maps SDK for Android + Maps Compose + Play Services Location  
- **Images** — Coil  
- **Async** — Kotlin Coroutines + `kotlinx-coroutines-play-services` for Firebase `Task.await()`  

## Firestore data model

| Collection | Purpose |
|------------|---------|
| `users/{uid}` | Profile: `displayName`, `email`, `createdAt` |
| `breakdown_requests/{requestId}` | One document per rescue request (vehicle, location, status, `acceptedBid`, etc.) |
| `breakdown_requests/{requestId}/bids/{bidId}` | Simulated shop bids (sorted by `price` in the app) |

## Deploy rules & indexes

From the project root (with [Firebase CLI](https://firebase.google.com/docs/cli) logged in):

```bash
firebase deploy --only firestore:rules,firestore:indexes
```

Or copy `firestore.rules` and `firestore.indexes.json` into the Firebase Console under **Firestore → Rules** and **Indexes**.

## Enable in Firebase Console

1. **Firestore** — Create database (production mode), then deploy the rules above (or relax for testing).  
2. **Authentication** — Email/Password enabled.  
3. **Maps** — `MAPS_API_KEY` in `local.properties`; Maps SDK for Android enabled in Google Cloud.

## `CONFIGURATION_NOT_FOUND` when signing up / signing in

This Firebase Auth error almost always means the **Identity Toolkit API** is not enabled for the Google Cloud project linked to your Firebase app, or Auth was never fully turned on.

### Fix (do these in order)

1. **Firebase Console** → **Build** → **Authentication** → click **Get started** (if you have not already).  
2. **Authentication** → **Sign-in method** → enable **Email/Password** → **Save**.  
3. **Google Cloud Console** (same project as Firebase: **Project settings** → **Project ID**):  
   - **APIs & Services** → **Library** → search **Identity Toolkit API** → **Enable**.  
   - Also enable **Token Service API** if prompted (often auto with Firebase).  
4. **Firebase Console** → **Project settings** (gear) → **Your apps** → Android app:  
   - Add **SHA-1** (and SHA-256) for your **debug** keystore (`./gradlew signingReport`).  
   - Download a fresh **`google-services.json`** and replace `app/google-services.json`.  
5. **API key restrictions** (Google Cloud → Credentials): if your Android key is restricted, ensure **Identity Toolkit API** is allowed for that key (or use separate keys for Maps vs Firebase as recommended).  
6. Rebuild the app: **Build → Clean Project**, then run again.

After step 3, wait 1–2 minutes for Google’s backends to propagate.
