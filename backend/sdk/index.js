const ConsentClient = require("./consentClient");
const RiskClient = require("./riskClient");
const KPIClient = require("./kpiClient");
const ComplianceClient = require("./complianceClient");

class ConsentLensSDK {

  constructor(baseURL) {
    this.consent = new ConsentClient(baseURL);
    this.risk = new RiskClient(baseURL);
    this.kpis = new KPIClient(baseURL);
    this.compliance = new ComplianceClient(baseURL);
  }

}

module.exports = ConsentLensSDK;
