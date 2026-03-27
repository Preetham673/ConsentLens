const { getAppPrivacyProfile } = require("../privacy-knowledge-hub/service");
const { calculateRisk } = require("../risk-engine/riskService");
const { getConsentHistory } = require("../consent-engine/consentService");
const { getAllLogs } = require("../audit-logs/auditService");

async function generateComplianceReport(packageName) {

  try {

    /* ===============================
       FETCH DATA SAFELY
    =============================== */

    const privacyProfile =
      await getAppPrivacyProfile(packageName) || {
        appName: "Unknown App",
        dataCategories: []
      };

    const riskResult =
      calculateRisk(packageName, privacyProfile) || {};

    const consentHistory =
      getConsentHistory(packageName) || [];

    const auditLogs =
      (getAllLogs() || []).filter(
        log => log.packageName === packageName
      );

    const riskLevel = riskResult.riskLevel || "LOW";

    const hasRevocation =
      consentHistory.some(c => c.revoked === true);

    const auditTrailExists =
      auditLogs.length > 0;

    /* ===============================
       COMPLIANCE SCORING LOGIC
    =============================== */

    let dpdpCompliance = "COMPLIANT";
    let gdprCompliance = "COMPLIANT";
    let complianceScore = 80;

    if (riskLevel === "HIGH") {
      dpdpCompliance = "PARTIAL";
      gdprCompliance = "PARTIAL";
      complianceScore -= 20;
    }

    if (hasRevocation) {
      complianceScore += 5;
    }

    if (!auditTrailExists) {
      complianceScore -= 15;
    }

    if (complianceScore > 100) complianceScore = 100;
    if (complianceScore < 0) complianceScore = 0;

    /* ===============================
       ALWAYS RETURN SAME STRUCTURE
    =============================== */

    return {
      complianceReport: {

        app: {
          packageName,
          appName: privacyProfile.appName
        },

        privacyProfile,

        riskAssessment: riskResult,

        consentHistory,

        relatedAuditEvents: auditLogs,

        dpdpCompliance,
        gdprCompliance,

        complianceScore,

        datasetInfluence: "OPP-115, APP-350",

        recommendation:
          riskLevel === "HIGH"
            ? "Review third-party sharing and sensitive data usage."
            : "Maintain transparency and monitor data usage.",

        generatedAt: new Date().toISOString()
      }
    };

  } catch (err) {

    console.error("Compliance Report Error:", err);

    return {
      complianceReport: null
    };
  }
}

module.exports = {
  generateComplianceReport
};
