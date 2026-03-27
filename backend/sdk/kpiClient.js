const fetch = require("node-fetch");

class KPIClient {

  constructor(baseURL) {
    this.baseURL = baseURL;
  }

  async getKPIs() {
    const res = await fetch(
      this.baseURL + "/api/kpis"
    );
    return res.json();
  }
}

module.exports = KPIClient;
