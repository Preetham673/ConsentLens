const ConsentLensSDK = require("./index");

const sdk = new ConsentLensSDK("http://localhost:5000");

async function test() {

  console.log("🔍 Analyzing...");
  const risk = await sdk.risk.analyze("com.android.chrome");
  console.log(risk);

  console.log("📝 Granting Consent...");
  const consent = await sdk.consent.grantConsent({
    packageName: "com.android.chrome",
    riskLevel: "MEDIUM",
    purpose: ["Analytics"],
    dataCategories: ["Location"]
  });
  console.log(consent);

  console.log("📜 Fetching History...");
  const history = await sdk.consent.getHistory("com.android.chrome");
  console.log(history);

  console.log("📈 Fetching KPIs...");
  const kpis = await sdk.kpis.getKPIs();
  console.log(kpis);

}

test();
