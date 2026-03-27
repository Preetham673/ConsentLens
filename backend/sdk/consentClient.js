const fetch = require("node-fetch");

class ConsentClient {

  constructor(baseURL) {
    this.baseURL = baseURL;
  }

  async grantConsent(data) {
    return this._post("/api/consent/grant", data);
  }

  async revokePurpose(data) {
    return this._post("/api/consent/revoke-purpose", data);
  }

  async getHistory(packageName) {
    return this._post("/api/consent/history", { packageName });
  }

  async getReceipt(consentId) {
    return this._get(`/api/consent/receipt/${consentId}`);
  }

  async _post(endpoint, body) {
    const res = await fetch(this.baseURL + endpoint, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(body)
    });

    return res.json();
  }

  async _get(endpoint) {
    const res = await fetch(this.baseURL + endpoint);
    return res.json();
  }
}

module.exports = ConsentClient;
