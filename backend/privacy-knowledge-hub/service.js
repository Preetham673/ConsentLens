const fs = require("fs");
const path = require("path");

const { fetchPlayStoreData } = require("../policy-intelligence/playStoreService");
const { analyzePrivacyPolicy } = require("../policy-intelligence/policyAnalyzer");

const dataPath = path.join(__dirname, "data.json");

function loadKnowledgeBase() {
  if (!fs.existsSync(dataPath)) return {};
  const rawData = fs.readFileSync(dataPath);
  return JSON.parse(rawData || "{}");
}

function saveKnowledgeBase(data) {
  fs.writeFileSync(dataPath, JSON.stringify(data, null, 2));
}

/* ==========================================================
   🔥 SAFE FALLBACK INTELLIGENCE (NO DATASET DAMAGE)
========================================================== */
function inferCategoriesFromPackage(packageName) {

  const name = packageName.toLowerCase();

  if (name.includes("camera"))
    return ["Media", "Location"];

  if (name.includes("contact"))
    return ["Contacts"];

  if (name.includes("chat") || name.includes("messenger"))
    return ["Contacts", "Messages"];

  if (name.includes("video"))
    return ["Media"];

  if (name.includes("map") || name.includes("location"))
    return ["Location"];

  if (name.includes("browser") || name.includes("chrome"))
    return ["Browsing History", "Location"];

  if (name.includes("store"))
    return ["Device Information", "Usage Data"];

  // Default minimal assumption
  return ["Location"];
}

/* ==========================================================
   MAIN FUNCTION
========================================================== */
async function getAppPrivacyProfile(packageName) {

  const privacyData = loadKnowledgeBase();

  // ✅ If already cached → return immediately
  if (privacyData[packageName]) {
    return privacyData[packageName];
  }

  console.log("🔍 Unknown app detected. Attempting Play Store fetch...");

  try {

    const playStoreResult = await fetchPlayStoreData(packageName);

    if (playStoreResult.success && playStoreResult.privacyPolicyUrl) {

      const analysis = await analyzePrivacyPolicy(
        playStoreResult.privacyPolicyUrl
      );

      if (analysis.success) {

        const generatedProfile = {
          appName: packageName,
          dataCategories:
            analysis.detectedCategories.length > 0
              ? analysis.detectedCategories
              : inferCategoriesFromPackage(packageName),

          thirdPartySharing: analysis.riskScore > 40,
          policyClarity: analysis.riskScore > 70 ? "LOW" : "MEDIUM",
          regulatoryFlags: [],
          dynamicallyGenerated: true,
        };

        privacyData[packageName] = generatedProfile;
        saveKnowledgeBase(privacyData);

        console.log("✅ Profile generated from policy analysis");

        return generatedProfile;
      }
    }

  } catch (error) {
    console.log("⚠ Play Store fetch failed. Using intelligent fallback.");
  }

  /* ==========================================================
     SAFE FALLBACK (REPLACES 'unknown')
  ========================================================== */

  const fallbackProfile = {
    appName: packageName,
    dataCategories: inferCategoriesFromPackage(packageName),
    thirdPartySharing: false,
    policyClarity: "MEDIUM",
    regulatoryFlags: [],
    dynamicallyGenerated: true,
  };

  privacyData[packageName] = fallbackProfile;
  saveKnowledgeBase(privacyData);

  console.log("✅ Fallback profile generated");

  return fallbackProfile;
}

module.exports = {
  getAppPrivacyProfile,
};
