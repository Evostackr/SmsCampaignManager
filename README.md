# Native Android SMS Campaign Manager

A production-grade, native Android application built with **Kotlin**, **Jetpack Compose**, **MVVM Architecture**, **Room Database (SQLCipher Encrypted)**, **WorkManager**, and a **Foreground Service** for user-controlled SMS campaign management directly from the device's own SIM card.

---

## 📱 Features

- **Dashboard**:
  - Remaining monthly SMS counter (Default: 100 limit).
  - Sent Today & Sent Month metrics.
  - Failed Messages & Pending Queue counters.
  - Active Campaign Live Monitor card with quick **Pause**, **Resume**, and **Cancel** controls.
  - Real-time SIM card & Carrier info status banner (`SubscriptionManager`).

- **Campaign Creation**:
  - Import phone numbers from **CSV** files (`.csv`).
  - Import contacts via native device Contact Picker (`READ_CONTACTS`).
  - Paste numbers manually with multi-delimiter support (commas, newlines, semicolons).
  - Automatic duplicate phone number detection & removal toggle.
  - Live Phone Number Sanitization & Format Validation (E.164 / local 7-15 digits).
  - SMS Message Composer with dynamic tag variables `{name}` and `{number}`.
  - Live character & SMS segment counter (160 GSM / 70 Unicode).
  - Cost & time estimation calculator before starting.
  - **Explicit User Confirmation Dialog** before launching campaigns.

- **SMS Sending & Dispatching**:
  - Native `SmsManager` integration.
  - Single-recipient sequential dispatching.
  - Configurable delay (30 seconds to 10 minutes).
  - Automatic retry mechanism for failed SMS (up to 3 retries).
  - Automatically pauses when monthly SMS cap is reached.
  - Unfinished campaign state restoration after device reboot or service interruption.

- **Background Execution & Resilience**:
  - Android **Foreground Service** with ongoing status notification.
  - Notification controls: **Pause**, **Resume**, **Cancel**.
  - `WorkManager` & `BOOT_COMPLETED` receiver for seamless reboot resumption without duplicate sends.
  - Room DB state persistence after every single SMS attempt.

- **Database & Security**:
  - Encrypted SQLite Database powered by **SQLCipher** (`SupportOpenHelperFactory`).
  - Key generated & securely stored in `EncryptedSharedPreferences` / Android KeyStore.
  - **100% Local Execution**: Zero external network calls, zero tracking, total user privacy.

- **Templates & Reports**:
  - Saved SMS templates with Favorite toggle and Recently Used sorting.
  - Comprehensive Campaign Reports & Success Rate metrics.
  - Export Campaign Logs & Recipient lists to `.csv` format.

---

## 🛠️ Technology Stack

- **Language**: Kotlin 2.3
- **UI Framework**: Jetpack Compose + Material 3 (Light & Dark Theme)
- **Architecture**: MVVM + Repository Pattern
- **Database**: Room Database 2.6.1 + SQLCipher 4.5.4
- **Background Tasks**: WorkManager 2.10.0 + Foreground Service
- **Navigation**: Jetpack Compose Navigation

---

## 🚀 Build Instructions

1. Open **Android Studio** (Ladybug or higher).
2. Select **Open Project** and navigate to `C:\Users\ujwal\.gemini\antigravity-ide\scratch\SmsCampaignManager`.
3. Allow Gradle to sync dependencies.
4. Run the application on an Android device or emulator with SIM support:
   ```bash
   ./gradlew.bat assembleDebug
   ```
5. Run Unit Tests:
   ```bash
   ./gradlew.bat test
   ```

---

## 🛡️ Permissions Required

- `SEND_SMS`: Required to send campaign text messages from your SIM card.
- `READ_PHONE_STATE`: Required to inspect SIM carrier and signal status.
- `READ_CONTACTS`: Optional, required for importing contacts into campaigns.
- `POST_NOTIFICATIONS`: Required for Android 13+ foreground service progress notifications.
- `RECEIVE_BOOT_COMPLETED`: Required to restore pending campaigns after device restart.
