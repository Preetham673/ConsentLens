function mapToLaws(riskResult, privacyProfile = {}) {

  const { riskLevel } = riskResult;

  let gdprArticles = [];
  let dpdpPrinciples = [];

  // ================================
  // HIGH RISK
  // ================================
  if (riskLevel === "HIGH") {

    gdprArticles = [
      "GDPR Article 5 - Transparency and Accountability",
      "GDPR Article 6 - Lawful Basis for Processing",
      "GDPR Article 7 - Conditions for Consent",
      "GDPR Article 9 - Processing of Special Category Data"
    ];

    dpdpPrinciples = [
      "DPDP Section 6 - Explicit Consent Required",
      "DPDP Section 7 - Purpose Limitation",
      "DPDP Section 8 - Data Fiduciary Obligations"
    ];
  }

  // ================================
  // MEDIUM RISK
  // ================================
  else if (riskLevel === "MEDIUM") {

    gdprArticles = [
      "GDPR Article 5 - Transparency",
      "GDPR Article 6 - Lawful Processing"
    ];

    dpdpPrinciples = [
      "DPDP - Notice and Transparency",
      "DPDP - Data Minimization"
    ];
  }

  // ================================
  // LOW RISK
  // ================================
  else {

    gdprArticles = [
      "GDPR Article 5 - Basic Principles of Processing"
    ];

    dpdpPrinciples = [
      "DPDP - Fair and Lawful Processing"
    ];
  }

  return {
    gdprArticles,
    dpdpPrinciples
  };
}

module.exports = { mapToLaws };
