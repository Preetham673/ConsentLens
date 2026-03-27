function analyzePolicyText(policyText) {
  const lowerText = policyText.toLowerCase();

  let findings = {
    collectsSensitiveData: false,
    mentionsThirdPartySharing: false,
    transparencyLevel: "HIGH",
    detectedPurposes: []
  };

  if (lowerText.includes("location") || lowerText.includes("contacts")) {
    findings.collectsSensitiveData = true;
  }

  if (lowerText.includes("third party") || lowerText.includes("share")) {
    findings.mentionsThirdPartySharing = true;
  }

  if (!lowerText.includes("purpose") || lowerText.length < 100) {
    findings.transparencyLevel = "LOW";
  }

  if (lowerText.includes("advertising")) {
    findings.detectedPurposes.push("advertising");
  }

  if (lowerText.includes("analytics")) {
    findings.detectedPurposes.push("analytics");
  }

  return findings;
}

module.exports = {
  analyzePolicyText
};
