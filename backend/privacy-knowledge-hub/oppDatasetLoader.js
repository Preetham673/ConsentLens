const fs = require("fs");
const path = require("path");

const datasetPath = path.join(__dirname, "datasets/opp115.json");

let oppData = [];

try {
  if (fs.existsSync(datasetPath)) {
    const raw = fs.readFileSync(datasetPath, "utf8");
    oppData = JSON.parse(raw);

    console.log("✅ OPP-115 dataset loaded:", oppData.length, "policies");
  } else {
    console.log("⚠️ opp115.json not found.");
  }
} catch (err) {
  console.error("❌ Failed to load OPP dataset:", err.message);
}

function searchPolicyByAppName(appName) {
  if (!appName || !Array.isArray(oppData)) return null;

  return oppData.find(policy =>
    policy.policy_text &&
    policy.policy_text.toLowerCase().includes(appName.toLowerCase())
  );
}

module.exports = {
  searchPolicyByAppName
};
