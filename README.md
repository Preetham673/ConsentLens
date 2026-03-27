# ConsentLens

AI-powered privacy consent management for GDPR and India's DPDP Act 2023.
Supported by iSEA · Powered by DSCI.

---

## Repository structure
```
ConsentLens/
├── android/     Android app — Java, Retrofit, BroadcastReceiver
├── backend/     Node.js + Express + MongoDB
├── nlp/         Python FastAPI — AI policy risk analysis
└── README.md
```

---

## How to run — start in this exact order: NLP → Backend → Android

---

### Step 1 — Start the NLP / AI service (Python)

Open a new terminal window.
```bash
cd nlp
pip install fastapi uvicorn
python ai_service.py
```

You should see:
```
INFO:     Uvicorn running on http://0.0.0.0:8001
```

Keep this terminal open. The backend calls this on port 8001.

---

### Step 2 — Start the Backend (Node.js)

Open a second terminal window.
```bash
cd backend
npm install
cp .env.example .env
```

Open the `.env` file and fill in:
```
MONGO_URI            = your_mongodb_connection_string
TWILIO_ACCOUNT_SID   = your_twilio_sid
TWILIO_AUTH_TOKEN    = your_twilio_auth_token
TWILIO_WHATSAPP_FROM = whatsapp:+14155238886
EMAIL_USER           = your_gmail_address
EMAIL_PASS           = your_gmail_app_password
PORT                 = 5000
```

Start the server:
```bash
node server.js
```

You should see:
```
ConsentLens backend running on port 5000
MongoDB connected successfully
```

Find your machine's local IP address — you need this for the Android app:
- Windows: open Command Prompt → run `ipconfig` → look for IPv4 Address
- Mac/Linux: run `ifconfig` → look for inet
- Example: `192.168.1.105`

---

### Step 3 — Run the Android app

1. Open Android Studio → File → Open → select the `android/` folder
2. Wait for Gradle sync to complete (2–3 minutes)
3. Open this file:
   `app/src/main/java/com/example/consentlens/network/RetrofitClient.java`
4. Change the BASE_URL to your backend IP from Step 2:
```java
private static final String BASE_URL = "http://192.168.X.X:5000/api/";
// If using Android emulator instead of real phone, use:
// private static final String BASE_URL = "http://10.0.2.2:5000/api/";
```

5. Connect your Android phone via USB cable
6. Enable USB Debugging on your phone:
   Settings → Developer Options → USB Debugging → ON
7. Click the green Run button in Android Studio
8. App installs and launches automatically

---

### Step 4 — WhatsApp OTP sandbox setup (one time per phone number)

The parent's phone must do this once before receiving OTPs:

1. Open WhatsApp on the parent's phone
2. Send a message to: **+1 415 523 8886**
3. Message text must be exactly: **join using-five-house**
4. Wait for the Twilio confirmation reply
5. Done — OTP messages will now arrive on that number

---

## Test walkthrough for the jury

### Test 1 — New app install notification (star feature)
1. Make sure ConsentLens is running in the background
2. Install any new app on the device (from Play Store or via `adb install app.apk`)
3. A notification appears within 2 seconds: "New app installed — Tap to see privacy risk report"
4. Tap the notification
5. Full AI risk analysis opens automatically for the new app

### Test 2 — Manual privacy scan
1. Tap Scan My Apps on the dashboard
2. All installed apps load in the list
3. Tap any app
4. 5-section AI risk report appears:
   - Section 1: Safety Overview — risk score, meter, sensitive data alert, purpose drift
   - Section 2: Your Data — data categories, third-party sharing, DPIA
   - Section 3: Your Legal Rights — GDPR articles, DPDP principles, AI policy analysis
   - Section 4: Trust and Certification — ISO 27001 check, consent expiry, Privacy Maturity Index
   - Section 5: Your Decision — Grant Consent or Uninstall

### Test 3 — Adult consent
1. On any risk report, tap Grant Consent
2. Enter age 18 or above
3. Consent saves immediately
4. Rating dialog appears
5. Verify consent appears in Consent History

### Test 4 — Minor guardian consent via WhatsApp OTP
1. On any risk report, tap Grant Consent
2. Enter age under 18 (example: 15)
3. Guardian Consent Required dialog appears
4. Tap WhatsApp OTP
5. Enter parent's number with country code (India example: 919876543210)
6. Parent receives OTP on WhatsApp
7. Enter the 6-digit OTP in the app
8. Expected: consent saved with guardian approval

### Test 5 — ISO 27001 certification check
1. Scan YouTube (com.google.android.youtube) or WhatsApp (com.whatsapp)
2. Open Section 4 — Trust and Certification
3. Expected: ISO 27001 — VERIFIED badge in green

### Test 6 — Audit logs
1. Tap Audit Logs on the dashboard
2. Blockchain integrity banner shows: All records VALID
3. Events visible: RISK_ANALYZED, CONSENT_GRANTED, RECEIPT_GENERATED, CONSENT_REVOKED_PURPOSE
4. Each event has a Consent ID hash and timestamp

### Test 7 — Compliance dashboard
1. Tap Compliance Dashboard
2. Shows live: GDPR coverage, DPDP coverage, active consents, expired consents,
   high risk count, revocation error rate, user satisfaction score
3. Recent audit events appear at the bottom

### Test 8 — Compliance report
1. Tap Compliance Report → Generate
2. Full report shows: all apps, risk scores, legal articles, consent history
3. Tap Export as PDF

### Test 9 — Simulation lab
1. Tap App Simulation
2. Three synthetic apps: Fintech Wallet, Health Tracker, E-Commerce Shop
3. Tap Scan and Analyse on any
4. Real AI analysis runs and results appear live

### Test 10 — Language switching
1. Tap the language button on the dashboard
2. Switch to Hindi, Tamil, or Telugu
3. All buttons and labels change language immediately

---

## Performance benchmarks

| Metric | Value |
|--------|-------|
| API response time | 16–19 ms average |
| AI policy analysis latency | under 100 ms |
| New install detection | under 2 seconds |
| Total consents recorded | 46 |
| Compliance score | 90% |
| DPDP coverage | 100% |
| User satisfaction | 4.67 / 5 |
| Blockchain records valid | 100% |

---

## API endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /api/risk/analyze | AI privacy risk analysis |
| POST | /api/consent/grant | Save consent record |
| POST | /api/parent/send-sms-otp | Send WhatsApp OTP via Twilio |
| POST | /api/parent/verify-sms-otp | Verify WhatsApp OTP |
| POST | /api/parent/send-email-otp | Send email OTP |
| POST | /api/parent/verify-email-otp | Verify email OTP |
| GET | /api/consent/history | All consent records |
| GET | /api/audit/logs | Tamper-proof audit trail |
| GET | /api/compliance/dashboard | Live GDPR and DPDP stats |
| GET | /api/compliance/report | Full compliance report |
| GET | /api/kpis | Dashboard KPI metrics |
| GET | /api/certification/check | ISO 27001 check by package |

---

## Tech stack

| Layer | Technology |
|-------|-----------|
| Android | Java, Retrofit, Material Components, BroadcastReceiver, ViewModel, LiveData |
| Backend | Node.js, Express, MongoDB Atlas |
| NLP / AI | Python, FastAPI, Uvicorn, rule-based keyword privacy risk scoring |
| OTP | Twilio WhatsApp sandbox, Nodemailer (email) |
| Compliance | GDPR Articles 5/7/8/9 · DPDP Act 2023 Sections 6/7 |
| Security | JWT authentication, SHA-256 blockchain hash per consent event, ISO 27001 certification check |
| Languages | English, Hindi, Tamil, Telugu |
