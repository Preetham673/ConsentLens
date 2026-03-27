const axios = require("axios");

// Intelligent keyword-based analyzer
async function analyzePrivacyPolicy(policyUrl) {
  try {
    if (!policyUrl) {
      return {
        success: false,
        reason: "No privacy policy URL found",
      };
    }

    const response = await axios.get(policyUrl, {
      headers: {
        "User-Agent":
          "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/115 Safari/537.36",
      },
    });

    const policyText = response.data.toLowerCase();

    let score = 0;
    let detectedCategories = [];

    // 🔎 Keyword Intelligence Rules
    if (policyText.includes("location")) {
      score += 30;
      detectedCategories.push("location");
    }

    if (policyText.includes("contact")) {
      score += 30;
      detectedCategories.push("contacts");
    }

    if (policyText.includes("camera") || policyText.includes("microphone")) {
      score += 25;
      detectedCategories.push("media access");
    }

    if (policyText.includes("third party")) {
      score += 20;
    }

    if (policyText.includes("advertising")) {
      score += 20;
    }

    if (policyText.includes("biometric")) {
      score += 40;
      detectedCategories.push("biometric");
    }

    if (policyText.includes("analytics")) {
      score += 15;
    }

    if (score > 100) score = 100;

    let riskLevel = "LOW";
    if (score > 30 && score <= 70) riskLevel = "MEDIUM";
    if (score > 70) riskLevel = "HIGH";

    return {
      success: true,
      riskScore: score,
      riskLevel,
      detectedCategories,
    };
  } catch (error) {
    return {
      success: false,
      error: error.message,
    };
  }
}

module.exports = {
  analyzePrivacyPolicy,
};
