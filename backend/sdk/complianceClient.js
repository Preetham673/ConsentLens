const fetch = require("node-fetch");

class ComplianceClient {

  constructor(baseURL) {
    this.baseURL = baseURL;
  }

  async generateReport(packageName) {
    const res = await fetch(
      this.baseURL + "/api/compliance/report",
      {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ packageName })
      }
    );

    return res.json();
  }
}

module.exports = ComplianceClient;
