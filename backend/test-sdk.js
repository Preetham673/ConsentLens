const ConsentLensSDK = require("./consentlens-sdk");

const sdk = new ConsentLensSDK("http://localhost:5000");

// Get package name from command line
const packageName = process.argv[2];

if (!packageName) {
  console.log("❌ Please provide package name.");
  console.log("Example:");
  console.log("node test-sdk.js com.android.chrome");
  process.exit();
}

async function run() {

  console.log("\n🔍 Analyzing:", packageName);

  const risk = await sdk.analyzeApp(packageName);
  console.log("\n📊 Risk Result:");
  console.log(JSON.stringify(risk, null, 2));

  console.log("\n📝 Granting Consent...");

  const consent = await sdk.grantConsent({
    packageName,
    riskLevel: risk?.risk?.riskLevel || "LOW",
    purpose: ["General Processing"],
    dataCategories: ["Location", "Contacts"]
  });

  console.log("\n✅ Consent Result:");
  console.log(JSON.stringify(consent, null, 2));

  console.log("\n📜 Fetching History...");

  const history = await sdk.getConsentHistory(packageName);
  console.log(JSON.stringify(history, null, 2));

  if (history?.history?.length > 0) {

    const consentId = history.history[0].consentId;

    console.log("\n🧾 Fetching Receipt for:", consentId);

    const receipt = await sdk.getReceipt(consentId);
    console.log(JSON.stringify(receipt, null, 2));
  }

  console.log("\n📈 Fetching KPIs...");

  const kpis = await sdk.getKPIs();
  console.log(JSON.stringify(kpis, null, 2));
}

run();
