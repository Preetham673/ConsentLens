const fs = require("fs");
const path = require("path");

const consentPath = path.join(__dirname, "../consent-engine/consents.json");
const auditPath = path.join(__dirname, "../audit-logs/auditLogs.json");

const { verifyLogsIntegrity } =
  require("../audit-logs/auditService");

const { calculateAverageRating } =
  require("../feedback/feedbackService");

// 🔐 NEW: Encryption Support
const { decryptData } =
  require("../security/encryptionService");

/* ======================================================
   SAFE JSON READER (SUPPORTS ENCRYPTED FILES)
====================================================== */
function readJSON(filePath) {
  try {

    if (!fs.existsSync(filePath)) return [];

    const raw = fs.readFileSync(filePath, "utf8");

    if (!raw) return [];

    const parsed = JSON.parse(raw);

    // 🔐 If encrypted object → decrypt
    if (parsed.iv && parsed.data) {
      try {
        return decryptData(parsed);
      } catch (err) {
        console.error("KPI Decryption Error:", err.message);
        return [];
      }
    }

    // normal JSON array
    return parsed;

  } catch (err) {

    console.error("KPI Read Error:", err.message);
    return [];

  }
}

/* ======================================================
   KPI CALCULATION ENGINE
====================================================== */

function calculateKPIs() {

  const consents = readJSON(consentPath);
  const audits = readJSON(auditPath);

  const totalConsents = consents.length;

  const grantedConsents =
    consents.filter(c => c.decision === "GRANTED").length;

  const revokedConsents =
    consents.filter(c => c.revoked).length;

  const highRiskConsents =
    consents.filter(c => c.riskLevel === "HIGH").length;

  const mediumRiskConsents =
    consents.filter(c => c.riskLevel === "MEDIUM").length;

  const lowRiskConsents =
    consents.filter(c => c.riskLevel === "LOW").length;

  const sensitiveDataApps =
    consents.filter(c =>
      Array.isArray(c.dataCategories) &&
      c.dataCategories.length > 0
    ).length;

  const totalRiskAnalyses =
    audits.filter(a => a.action === "RISK_ANALYZED").length;

  /* ===============================
     Revocation Error Rate
  =============================== */

  const revokeAttempts =
    audits.filter(a =>
      a.action === "CONSENT_REVOKED_PURPOSE" ||
      a.action === "CONSENT_REVOKE_FAILED"
    ).length;

  const revokeFailures =
    audits.filter(a =>
      a.action === "CONSENT_REVOKE_FAILED"
    ).length;

  const revocationErrorRate =
    revokeAttempts > 0
      ? Number(((revokeFailures / revokeAttempts) * 100).toFixed(2))
      : 0;

  /* ===============================
     Average Risk Score
  =============================== */

  const averageRiskScore =
    totalConsents > 0
      ? Math.round(
          (highRiskConsents * 80 +
           mediumRiskConsents * 50 +
           lowRiskConsents * 20) / totalConsents
        )
      : 0;

  /* ===============================
     Audit Integrity
  =============================== */

  const integrityResult = verifyLogsIntegrity();
  const auditIntegrity =
    integrityResult.valid ? 100 : 0;

  /* ===============================
     Compliance Readiness Score
  =============================== */

  const complianceReadinessScore =
    Math.round(
      (auditIntegrity +
       (100 - revocationErrorRate) +
       (totalConsents > 0 ? 100 : 0)) / 3
    );

  /* ===============================
     User Satisfaction Score
  =============================== */

  const userSatisfactionScore = calculateAverageRating();

  /* ===============================
     FINAL RESPONSE
  =============================== */

  return {
    totalConsents,
    grantedConsents,
    revokedConsents,
    highRiskConsents,
    mediumRiskConsents,
    lowRiskConsents,
    sensitiveDataApps,
    totalRiskAnalyses,
    averageRiskScore,
    revocationErrorRate,
    complianceReadinessScore,
    userSatisfactionScore,
    datasetReference: "OPP-115, APP-350"
  };
}

module.exports = {
  calculateKPIs
};