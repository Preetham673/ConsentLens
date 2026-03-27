class ConsentLensSDK {

  constructor(baseURL) {
    this.baseURL = baseURL;
  }

  async request(endpoint, method = "GET", body = null) {

    const options = {
      method,
      headers: {
        "Content-Type": "application/json"
      }
    };

    if (body) {
      options.body = JSON.stringify(body);
    }

    try {
      const response = await fetch(`${this.baseURL}${endpoint}`, options);

      if (!response.ok) {
        throw new Error(`HTTP Error: ${response.status}`);
      }

      return await response.json();

    } catch (err) {
      console.error("SDK Request Error:", err.message);
      return null;
    }
  }

  // ===============================
  // RISK ANALYSIS
  // ===============================
  analyzeApp(packageName) {
    return this.request(
      "/api/risk/analyze",
      "POST",
      { packageName }
    );
  }

  // ===============================
  // GRANT CONSENT
  // ===============================
  grantConsent(data) {
    return this.request(
      "/api/consent/grant",
      "POST",
      data
    );
  }

  // ===============================
  // REVOKE PURPOSE (Granular)
  // ===============================
  revokePurpose(consentId, purpose) {
    return this.request(
      "/api/consent/revoke-purpose",
      "POST",
      { consentId, purpose }
    );
  }

  // ===============================
  // CONSENT HISTORY
  // ===============================
  getConsentHistory(packageName) {
    return this.request(
      "/api/consent/history",
      "POST",
      { packageName }
    );
  }

  // ===============================
  // RECEIPT
  // ===============================
  getReceipt(consentId) {
    return this.request(
      `/api/consent/receipt/${consentId}`
    );
  }

  // ===============================
  // KPIs
  // ===============================
  getKPIs() {
    return this.request("/api/kpis");
  }
}

module.exports = ConsentLensSDK;
