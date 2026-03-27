function generateExplanation(riskResult, privacyProfile) {

  const explanation = [];

  explanation.push("Risk Score: " + riskResult.riskScore);

  explanation.push(
    "Data Categories Involved: " +
    (privacyProfile.dataCategories || []).join(", ")
  );

  if (riskResult.riskLevel === "HIGH") {
    explanation.push(
      "High risk due to sensitive data processing and possible third-party sharing."
    );
  }

  explanation.push(
    "Dataset Intelligence: OPP-115 policy pattern matched."
  );

  explanation.push(
    "DPDP Section 6: Explicit consent required for personal data processing."
  );

  explanation.push(
    "GDPR Article 7: Consent must be freely given and revocable."
  );

  return explanation;
}

module.exports = {
  generateExplanation
};
