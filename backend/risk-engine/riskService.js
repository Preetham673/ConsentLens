const { getViolationsByApp } = require("../research-violations/service");
const { mapToLaws } = require("./lawMapper");
const datasetPatterns = require("../privacy-knowledge-hub/datasets/datasetPatterns");
const { generateExplanation } = require("./explainService");
const { checkCertification } = require("./securityCertificationService");

function calculateRisk(packageName, privacyProfile = {}, permissions = []) {

  let score = 0;
  let reasons = [];
  let potentialRisks = [];
  let confidenceLevel = "MEDIUM";
  let confidenceReason = "";

  const appName = privacyProfile.appName || "Unknown App";

  const dataCategories = Array.isArray(privacyProfile.dataCategories)
    ? privacyProfile.dataCategories
    : [];

  const isUnknownApp =
    appName === "Unknown App" ||
    privacyProfile.policyClarity === "UNKNOWN";

  /* =====================================================
     1️⃣ DATASET-INSPIRED SENSITIVE DATA DETECTION
  ===================================================== */

  if (
    datasetPatterns &&
    Array.isArray(datasetPatterns.sensitiveDataCategories)
  ) {
    dataCategories.forEach(category => {

      if (
        datasetPatterns.sensitiveDataCategories.includes(
          category.toLowerCase()
        )
      ) {
        score += 20;

        reasons.push(
          `Sensitive data category detected: ${category}`
        );

        potentialRisks.push(
          `${category} data may be misused or exposed`
        );
      }
    });
  }

  /* =====================================================
     2️⃣ THIRD-PARTY SHARING
  ===================================================== */

  if (privacyProfile.thirdPartySharing === true) {
    score += 25;
    reasons.push("Third-party data sharing detected");
    potentialRisks.push("Personal data may be shared externally");
  }

  /* =====================================================
     3️⃣ POLICY CLARITY
  ===================================================== */

  if (privacyProfile.policyClarity === "LOW") {
    score += 15;
    reasons.push("Privacy policy lacks clarity");
  }

  /* =====================================================
     4️⃣ PERMISSIONS
  ===================================================== */

  const dangerousPermissions = [
    "READ_CONTACTS",
    "ACCESS_FINE_LOCATION",
    "READ_SMS",
    "RECORD_AUDIO",
    "CAMERA",
    "READ_CALL_LOG",
    "WRITE_EXTERNAL_STORAGE"
  ];

  if (Array.isArray(permissions)) {
    permissions.forEach(p => {
      dangerousPermissions.forEach(d => {
        if (p && p.includes(d)) {
          score += 10;
          reasons.push(`Uses sensitive permission: ${d}`);
        }
      });
    });

    if (permissions.length > 20) {
      score += 10;
      reasons.push("Requests unusually high number of permissions");
    }
  }

  /* =====================================================
     5️⃣ REGULATORY HISTORY
  ===================================================== */

  const violations = getViolationsByApp(packageName) || [];

  if (violations.length > 0) {
    score += 20;
    reasons.push("Past regulatory violations detected");
    potentialRisks.push("App has prior compliance issues");
    confidenceLevel = "HIGH";
    confidenceReason =
      "Confidence increased due to documented violations";
  }

  /* =====================================================
     6️⃣ SECURITY CERTIFICATION CHECK
  ===================================================== */

  const certification = checkCertification(packageName);

  if (certification && certification.certified) {

    score -= certification.riskReduction;

    reasons.push(
      `Security certification detected: ${certification.standard}`
    );

    potentialRisks.push(
      "Risk reduced due to verified security certification"
    );

  }



  /* =====================================================
     NORMALIZE
  ===================================================== */

  if (score > 100) score = 100;
if (score < 0) score = 0;

  let riskLevel = "LOW";
  if (score >= 80) riskLevel = "HIGH";
  else if (score >= 40) riskLevel = "MEDIUM";

  if (isUnknownApp) {
    confidenceLevel = "LOW";
    confidenceReason =
      "Limited privacy intelligence available";
  }

  const legalMapping = mapToLaws(
    { riskScore: score, riskLevel },
    privacyProfile
  );

  return {
    riskScore: score,
    riskLevel,
    reasons,
    potentialRisks,
    confidenceLevel,
    confidenceReason,
  certification,
    dataImpactSummary:
      "This app may access: " + dataCategories.join(", "),
    userRecommendation:
      riskLevel === "HIGH"
        ? "Proceed with caution."
        : "Review permissions and stay informed.",
    whyYouAreSeeingThis:
      "Assessment based on privacy profile, permissions, regulatory intelligence, and dataset-informed patterns.",
    datasetReference: "OPP-115, APP-350",
    regulatoryViolationsFound: violations.length,
    legalMapping,
    explanation: generateExplanation(
      { riskScore: score, riskLevel },
      privacyProfile
    )
  };
}

module.exports = { calculateRisk };
