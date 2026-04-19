# SecretVault-Android
Android-based vault for data protection. The app operates behind a functional dialer interface and uses the label "System Service" for obfuscation.
​Core Functionality
​Encryption: Uses EncryptedSharedPreferences (AES-256 GCM) for all stored credentials.
​Security: Includes FLAG_SECURE to block screenshots and a lockout system after 5 failed attempts.
​Stealth: Disguised as a system utility. Redirects to the native dialer if the secret code isn't entered.
​Privacy: Triggers a "Panic Mode" (mutes audio and clears notifications) upon vault access.
​Technical Specs
​Language: Kotlin.
​Min SDK: 21.
​Libraries: androidx.security:security-crypto, androidx.activity-ktx.
​Implementation
​User defines a 6+ digit PIN on first boot.
​Entry is validated through the Dialer activity.
​Successful validation grants access to the internal vault activity.
​License
​MIT. See LICENSE for more info.
