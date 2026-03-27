const express = require("express");
const router  = express.Router();
const axios   = require("axios");

const { getAppPrivacyProfile }      = require("../privacy-knowledge-hub/service");
const { calculateRisk }             = require("../risk-engine/riskService");
const { submitFeedback }            = require("../feedback/feedbackService");

const { generateToken }             = require("../auth/jwtService");
const { authenticate }              = require("../auth/authMiddleware");
const { requireRole }               = require("../auth/roleMiddleware");
const { sendOTP, verifyOTP }        = require("../security/emailOTPService");
const { generateRecommendation }    = require("../ai-engine/recommendationEngine");

// ✅ NEW — ISO 27001 certification checker
const { checkCertification }        = require("../certification/certificationChecker");

// ✅ WhatsApp OTP — replaces SMS OTP
const {
    sendWhatsAppOTP,
    verifyWhatsAppOTP
} = require("../security/whatsappOTPService");

const {
    getAllLogs,
    verifyLogsIntegrity,
    logAction
} = require("../audit-logs/auditService");

const {
    grantConsent,
    grantConsentAdvanced,
    revokePurpose,
    getConsentHistory,
    generateReceipt,
    eraseConsent
} = require("../consent-engine/consentService");

const { calculateKPIs }             = require("../kpis/kpiService");
const { generateComplianceReport }  = require("../compliance/reportService");


/* ======================================================
   AUTH LOGIN
====================================================== */

router.post("/auth/login", (req, res) => {

    const { username, role } = req.body || {};

    if (!username) {
        return res.status(400).json({ error: "username required" });
    }

    const token = generateToken({
        username,
        role: role || "USER"
    });

    res.json({ token });
});


/* ======================================================
   RISK ANALYSIS
   ✅ NOW INCLUDES ISO 27001 CERTIFICATION CHECK
====================================================== */

router.post("/risk/analyze", async (req, res) => {

    try {

        const { packageName } = req.body;

        if (!packageName)
            return res.status(400).json({ error: "packageName required" });

        const start = Date.now();

        // Step 1 — get privacy profile
        const privacyProfile = await getAppPrivacyProfile(packageName);

        // Step 2 — calculate base risk
        let risk = calculateRisk(packageName, privacyProfile, []);

        // =============================================
        // Step 3 — ISO 27001 CERTIFICATION CHECK ✅
        // =============================================

        const certification = checkCertification(packageName);

        // If ISO 27001 certified, reduce the risk score
        if (certification.certified && certification.isISO27001) {

            const originalScore = risk.riskScore;

            // Apply risk reduction (never go below 0)
            risk.riskScore = Math.max(
                0,
                risk.riskScore - certification.riskReduction
            );

            console.log(
                `[CERT] ${packageName} is ISO 27001 certified. ` +
                `Risk reduced: ${originalScore} → ${risk.riskScore}`
            );

        } else if (certification.certified) {

            // Has some other cert (SOC2 etc.) — smaller reduction
            risk.riskScore = Math.max(
                0,
                risk.riskScore - certification.riskReduction
            );

            console.log(
                `[CERT] ${packageName} has ${certification.standard} cert. ` +
                `Risk reduced by ${certification.riskReduction}`
            );

        } else {

            console.log(`[CERT] ${packageName} — no certification found.`);
        }

        // Attach certification result to risk object
        // so Android app receives it in response.risk.certification
        risk.certification = certification;

        // =============================================
        // Step 4 — AI Policy Analysis
        // =============================================

        let aiPolicyRisk = null;

        try {

            const aiResponse = await axios.post(
                "http://localhost:8001/analyze-policy",
                {
                    policy:
                        privacyProfile?.privacyPolicy ||
                        "This app collects user data."
                }
            );

            aiPolicyRisk = aiResponse.data;

        } catch (err) {
            console.log("AI engine not reachable");
        }

        // Step 5 — AI recommendations
        const recommendations = generateRecommendation(
            risk,
            aiPolicyRisk,
            privacyProfile?.dataCategories
        );

        const latency = Date.now() - start;

        logAction("RISK_ANALYZED", null, packageName);

        // =============================================
        // Step 6 — Send full response to Android
        // =============================================

        res.json({
            packageName,
            privacyProfile,
            risk,                   // ← includes risk.certification now
            aiPolicyRisk,
            recommendations,
            processingLatencyMs: latency
        });

    } catch (err) {
        console.error(err);
        res.status(500).json({ error: "Internal server error" });
    }
});


/* ======================================================
   BASIC CONSENT
====================================================== */

router.post("/consent/grant", (req, res) => {

    try {

        const {
            packageName,
            riskLevel,
            purpose,
            dataCategories,
            expiryDate,
            age
        } = req.body;

        if (!packageName)
            return res.status(400).json({ error: "packageName required" });

        const consent = grantConsent({
            packageName,
            riskLevel,
            purpose,
            dataCategories,
            expiryDate,
            age
        });

        res.json({
            message: "Consent granted successfully",
            consent
        });

    } catch (err) {
        console.error(err);
        res.status(500).json({ error: "Internal server error" });
    }
});


/* ======================================================
   ADVANCED CONSENT
====================================================== */

router.post("/consent/grant-advanced", (req, res) => {

    try {

        const consent = grantConsentAdvanced(req.body);

        res.json({
            message: "Advanced consent granted successfully",
            consent
        });

    } catch (err) {
        console.error(err);
        res.status(500).json({ error: "Internal server error" });
    }
});


/* ======================================================
   REVOKE PURPOSE
====================================================== */

router.post("/consent/revoke-purpose", (req, res) => {

    try {

        const { consentId, purpose } = req.body;

        if (!consentId || !purpose)
            return res.status(400).json({
                error: "consentId and purpose required"
            });

        const updated = revokePurpose(consentId, purpose);

        if (!updated) {

            logAction("CONSENT_REVOKE_FAILED", consentId, "UNKNOWN");

            return res.status(404).json({
                error: "Consent not found or expired"
            });
        }

        res.json({
            message: "Purpose revoked successfully",
            consent: updated
        });

    } catch (err) {
        console.error(err);
        res.status(500).json({ error: "Internal server error" });
    }
});


/* ======================================================
   CONSENT HISTORY
====================================================== */

router.post("/consent/history", (req, res) => {

    try {

        const { packageName } = req.body;

        res.json({
            history: getConsentHistory(packageName)
        });

    } catch (err) {
        console.error(err);
        res.status(500).json({ error: "Internal server error" });
    }
});


router.get("/consent/all", (req, res) => {

    try {

        res.json({
            history: getConsentHistory("ALL")
        });

    } catch (err) {
        console.error(err);
        res.status(500).json({ error: "Internal server error" });
    }
});


/* ======================================================
   CONSENT RECEIPT
====================================================== */

router.get("/consent/receipt/:id", (req, res) => {

    try {

        const all = getConsentHistory("ALL");

        const consent = all.find(c => c.consentId === req.params.id);

        if (!consent)
            return res.status(404).json({ error: "Consent not found" });

        const receipt = generateReceipt(consent);

        res.json({ receipt });

    } catch (err) {
        console.error(err);
        res.status(500).json({ error: "Internal server error" });
    }
});


/* ======================================================
   KPIs
====================================================== */

router.get("/kpis", authenticate, (req, res) => {

    try {

        res.json({ kpis: calculateKPIs() });

    } catch (err) {
        console.error(err);
        res.status(500).json({ error: "Internal server error" });
    }
});


/* ======================================================
   ADD THIS ROUTE TO routes.js
   Place it after the /kpis route (around line 360)
====================================================== */

router.get("/compliance/dashboard", authenticate, (req, res) => {
    try {
        const allConsents = getConsentHistory ? getConsentHistory("ALL") : [];
        const allLogs     = getAllLogs ? getAllLogs() : [];
        const kpis        = calculateKPIs ? calculateKPIs() : {};

        const consents = Array.isArray(allConsents) ? allConsents : [];
        const logs     = Array.isArray(allLogs)     ? allLogs     : [];

        // Active vs Expired (expired = revoked or older than 30 days)
        const now       = Date.now();
        const thirtyDays = 30 * 24 * 60 * 60 * 1000;

        let active  = 0;
        let expired = 0;
        let highRisk = 0;
        let gdprConsentCount = 0;
        let dpdpConsentCount = 0;

        for (const c of consents) {
            const isExpired = c.revoked ||
                (c.timestamp && now - new Date(c.timestamp).getTime() > thirtyDays);

            if (isExpired) expired++;
            else active++;

            if (c.riskLevel && c.riskLevel.toLowerCase() === "high") highRisk++;

            if (c.jurisdiction && c.jurisdiction.toLowerCase().includes("eu")) gdprConsentCount++;
            if (c.jurisdiction && c.jurisdiction.toLowerCase().includes("in")) dpdpConsentCount++;
        }

        const total = consents.length || 1; // avoid divide by zero

        // GDPR coverage = consents that have lawful basis mapped
        const gdprCoverage = Math.min(100, Math.round((gdprConsentCount / total) * 100));
        const dpdpCoverage = Math.min(100, Math.round((dpdpConsentCount / total) * 100));

        // Recent audit events (last 5)
        const recentEvents = logs.slice(-5).reverse();

        res.json({
            dashboard: {
                gdprCoverage,
                dpdpCoverage,
                activeConsents: active,
                expiredConsents: expired,
                highRiskApps: highRisk,
                totalConsents: total,
                complianceScore: kpis.complianceReadinessScore || 0,
                revocationErrorRate: kpis.revocationErrorRate || 0,
                userSatisfactionScore: kpis.userSatisfactionScore || 0,
                recentAuditEvents: recentEvents
            }
        });
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});


/* ======================================================
   AUDIT LOGS (ADMIN ONLY)
====================================================== */

router.get("/audit/logs", authenticate, requireRole("ADMIN"), (req, res) => {

    try {

        res.json({ logs: getAllLogs() });

    } catch (err) {
        console.error(err);
        res.status(500).json({ error: "Internal server error" });
    }
});


/* ======================================================
   VERIFY AUDIT LEDGER
====================================================== */

router.get("/audit/verify", authenticate, requireRole("ADMIN"), (req, res) => {

    try {

        const result = verifyLogsIntegrity();

        res.json(result);

    } catch (err) {
        console.error(err);
        res.status(500).json({ error: "Verification failed" });
    }
});


/* ======================================================
   ERASE CONSENT
====================================================== */

router.delete("/consent/erase/:id", (req, res) => {

    try {

        const result = eraseConsent(req.params.id);

        if (!result)
            return res.status(404).json({ error: "Consent not found" });

        res.json({
            message: "Consent permanently erased",
            result
        });

    } catch (err) {
        console.error(err);
        res.status(500).json({ error: "Internal server error" });
    }
});


/* ======================================================
   USER FEEDBACK
====================================================== */

router.post("/feedback", (req, res) => {

    try {

        const { packageName, rating } = req.body;

        if (!packageName || !rating)
            return res.status(400).json({
                error: "packageName and rating required"
            });

        const result = submitFeedback({ packageName, rating });

        res.json({
            message: "Feedback submitted",
            result
        });

    } catch (err) {
        console.error(err);
        res.status(500).json({ error: "Internal server error" });
    }
});


/* ======================================================
   COMPLIANCE REPORT (REGULATOR ONLY)
====================================================== */

router.post("/compliance/report",
    authenticate,
    requireRole(["ADMIN", "REGULATOR"]),
    async (req, res) => {

        try {

            const { packageName } = req.body;

            if (!packageName)
                return res.status(400).json({ error: "packageName required" });

            const report = await generateComplianceReport(packageName);

            res.json(report);

        } catch (err) {
            console.error(err);
            res.status(500).json({ error: "Internal server error" });
        }
    }
);


/* ======================================================
   WEBHOOK — DATA DELETION CONFIRMATION
====================================================== */

router.post("/webhook/deletion-confirmation", (req, res) => {

    try {

        const { consentId, packageName, status } = req.body;

        if (!consentId || !status) {
            return res.status(400).json({
                error: "consentId and status required"
            });
        }

        logAction(
            "DELETION_CONFIRMATION_RECEIVED",
            consentId,
            packageName,
            { status }
        );

        res.json({
            message: "Deletion confirmation received",
            consentId,
            status
        });

    } catch (err) {
        console.error(err);
        res.status(500).json({ error: "Webhook processing failed" });
    }
});


/* ======================================================
   PARENT EMAIL OTP — SEND
====================================================== */

router.post("/parent/send-email-otp", async (req, res) => {

    const { parentEmail } = req.body;

    if (!parentEmail) {
        return res.status(400).json({ error: "parentEmail required" });
    }

    try {

        await sendOTP(parentEmail);

        res.json({ message: "OTP sent to parent email" });

    } catch (err) {

        console.error("EMAIL OTP ERROR:", err.message);
        console.error(err);

        res.status(500).json({ error: "Email OTP failed" });
    }
});


/* ======================================================
   PARENT EMAIL OTP — VERIFY
====================================================== */

router.post("/parent/verify-email-otp", (req, res) => {

    const { parentEmail, otp } = req.body;

    if (!parentEmail || !otp) {
        return res.status(400).json({ error: "Missing fields" });
    }

    const approved = verifyOTP(parentEmail, otp);

    res.json({ approved });
});


/* ======================================================
   PARENT WHATSAPP OTP — SEND
   phone must include country code e.g. 919876543210
====================================================== */

router.post("/parent/send-sms-otp", async (req, res) => {

    const { phone } = req.body;

    if (!phone) {
        return res.status(400).json({ error: "phone required" });
    }

    try {

        const result = await sendWhatsAppOTP(phone);

        res.json({
            message: "OTP sent via WhatsApp",
            messageId: result.messageId
        });

    } catch (err) {

        console.error("[WhatsApp OTP Send Error]", err.message);

        res.status(500).json({
            error: "Failed to send WhatsApp OTP",
            detail: err.message
        });
    }
});


/* ======================================================
   PARENT WHATSAPP OTP — VERIFY
====================================================== */

router.post("/parent/verify-sms-otp", (req, res) => {

    const { phone, otp } = req.body;

    if (!phone || !otp) {
        return res.status(400).json({ error: "phone and otp required" });
    }

    const approved = verifyWhatsAppOTP(phone, otp);

    res.json({ approved });
});


/* ======================================================
   ✅ NEW — CHECK CERTIFICATION DIRECTLY (optional endpoint)
   GET /api/certification/check?packageName=com.whatsapp
====================================================== */

router.get("/certification/check", (req, res) => {

    try {

        const { packageName } = req.query;

        if (!packageName)
            return res.status(400).json({ error: "packageName required" });

        const result = checkCertification(packageName);

        res.json({
            packageName,
            certification: result
        });

    } catch (err) {
        console.error(err);
        res.status(500).json({ error: "Internal server error" });
    }
});


module.exports = router;