# Coin Hunter V1 — Android starter project

This is an Android Studio starter project for a one-account TikTok LIVE reward assistant.

## Important current limitations
- This project is a UI prototype only.
- It does not capture or inspect TikTok's screen.
- It does not detect coin/reward buttons.
- It does not click or claim rewards.
- It cannot guarantee that any automation would be permitted by TikTok or avoid account enforcement.

The next development stage would be a user-consented screen-observation flow using Android MediaProjection, with on-device detection and an immediate user notification. Any interaction/auto-click functionality must first be checked against TikTok's current rules and Android permission requirements.

## Build an APK
1. Install Android Studio on a computer.
2. Open this `CoinHunterV1` folder.
3. Allow Gradle sync and install Android SDK Platform 35 if prompted.
4. Select **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
5. Android Studio will show the generated APK location (usually `app/build/outputs/apk/debug/app-debug.apk`).

The debug APK is for testing. A release APK should be signed before distribution.
