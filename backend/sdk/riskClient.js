const fetch = require("node-fetch");

class RiskClient {

  constructor(baseURL) {
    this.baseURL = baseURL;
  }

  async analyze(packageName) {
    const res = await fetch(
      this.baseURL + "/api/risk/analyze",
      {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ packageName })
      }
    );

    return res.json();
  }
}

module.exports = RiskClient;
