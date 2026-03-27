const fs = require("fs");
const path = require("path");
const { v4: uuidv4 } = require("uuid");
const { logAction } = require("../audit-logs/auditService");

// 🔐 NEW: Encryption Import
const { encryptData, decryptData } =
require("../security/encryptionService");

const {
  checkChildConsent
} = require("../child-consent/childConsentService");


const dataPath = path.join(__dirname, "consents.json");

/* ======================================================
   READ CONSENTS (DECRYPTED STORAGE)
====================================================== */
function readConsents() {

  if (!fs.existsSync(dataPath)) return [];

  const raw = fs.readFileSync(dataPath, "utf8");

  if (!raw) return [];

  const parsed = JSON.parse(raw);

  // 🔐 decrypt if encrypted
  if (parsed.iv && parsed.data) {
    try {
      const decrypted = decryptData(parsed);

      if (Array.isArray(decrypted)) {
        return decrypted;
      }

      return [];
    } catch (err) {
      console.error("Consent decrypt error:", err);
      return [];
    }
  }

  if (Array.isArray(parsed)) {
    return parsed;
  }

  return [];
}
/* ======================================================
   WRITE CONSENTS (ENCRYPTED STORAGE)
====================================================== */
function writeConsents(data) {

  // 🔐 encrypt before saving
  const encrypted = encryptData(data);

  fs.writeFileSync(
    dataPath,
    JSON.stringify(encrypted, null, 2)
  );
}


/* ======================================================
   LAWFUL BASIS DECISION ENGINE
====================================================== */
function determineLawfulBasis(dataCategories = [], thirdPartySharing = false, isMinor = false) {

  const sensitiveCategories = [
    "health",
    "biometric",
    "financial",
    "children",
    "location"
  ];

  const containsSensitive = dataCategories.some(cat =>
    sensitiveCategories.includes(cat.toLowerCase())
  );

  /* ===== CHILD DATA ===== */
  if (isMinor) {
    return {
      lawfulBasisType: "EXPLICIT_CONSENT",
      gdprArticle: "Article 8",
      dpdpSection: "Section 9"
    };
  }

  /* ===== SENSITIVE DATA ===== */
  if (containsSensitive) {
    return {
      lawfulBasisType: "EXPLICIT_CONSENT",
      gdprArticle: "Article 9",
      dpdpSection: "Section 6"
    };
  }

  /* ===== THIRD PARTY SHARING ===== */
  if (thirdPartySharing) {
    return {
      lawfulBasisType: "CONSENT",
      gdprArticle: "Article 6(1)(a)",
      dpdpSection: "Section 6"
    };
  }

  /* ===== LEGITIMATE USE ===== */
  return {
    lawfulBasisType: "LEGITIMATE_USE",
    gdprArticle: "Article 6(1)(f)",
    dpdpSection: "Section 7"
  };
}


/* ======================================================
   GRANT CONSENT (WITH CUSTOM EXPIRY SUPPORT)
====================================================== */
function grantConsent(consentData) {

  const consents = readConsents();

  const expiryDate = consentData.expiryDate
    ? new Date(consentData.expiryDate).toISOString()
    : null;

  const lawfulBasis = determineLawfulBasis(
    consentData.dataCategories,
    false,
    false
  );

  /* ======================================================
     NEW: CHILD CONSENT CHECK (DPDP)
  ====================================================== */

  const childCheck = checkChildConsent(consentData.age);

  if (!childCheck.allowed) {

    return {
      blocked: true,
      reason: childCheck.reason,
      message: childCheck.message
    };

  }

  /* ======================================================
     EXISTING CONSENT CREATION (UNCHANGED)
  ====================================================== */

  const newConsent = {
    consentId: uuidv4(),
    dataPrincipalId: "anonymous",
    packageName: consentData.packageName,

    purpose: Array.isArray(consentData.purpose)
      ? consentData.purpose
      : ["General Processing"],

    dataCategories: consentData.dataCategories || [],

    lawfulBasisType: lawfulBasis.lawfulBasisType,
    gdprArticle: lawfulBasis.gdprArticle,
    dpdpSection: lawfulBasis.dpdpSection,

    jurisdiction: "DPDP-INDIA",
    decision: "GRANTED",
    riskLevel: consentData.riskLevel,

    timestamp: new Date().toISOString(),
    expiryDate,
    expired: false,

    revokedPurposes: [],
    revoked: false
  };

  consents.push(newConsent);
  writeConsents(consents);

  logAction("CONSENT_GRANTED", newConsent.consentId, newConsent.packageName);

  return newConsent;
}

/* ======================================================
   ADVANCED GRANT CONSENT (REGTECH VERSION)
====================================================== */
function grantConsentAdvanced(consentData) {

  const consents = readConsents();

  let expiryDate = null;

  if (consentData.retentionPeriodDays) {
    const now = new Date();
    now.setDate(now.getDate() + consentData.retentionPeriodDays);
    expiryDate = now.toISOString();
  }

  const lawfulBasis = determineLawfulBasis(
    consentData.dataCategories,
    consentData.thirdPartySharing,
    consentData.isMinor
  );

  const newConsent = {
    consentId: uuidv4(),
    dataPrincipalId: "anonymous",

    packageName: consentData.packageName,
    purpose: [consentData.purposeId || "GENERAL_PROCESSING"],
    dataCategories: consentData.dataCategories || [],

    lawfulBasisType: lawfulBasis.lawfulBasisType,
    gdprArticle: lawfulBasis.gdprArticle,
    dpdpSection: lawfulBasis.dpdpSection,

    requiresExplicitConsent: consentData.requiresExplicitConsent,
    sensitiveData: consentData.sensitiveData,
    isMinor: consentData.isMinor,
    thirdPartySharing: consentData.thirdPartySharing,
    jurisdiction: consentData.jurisdiction || "DPDP-INDIA",

    decision: "GRANTED",
    riskLevel: consentData.riskLevel,

    retentionPeriodDays: consentData.retentionPeriodDays,
    expiryDate,
    expired: false,

    revokedPurposes: [],
    revoked: false,

    timestamp: new Date().toISOString()
  };

  consents.push(newConsent);
  writeConsents(consents);

  logAction(
    "CONSENT_GRANTED_ADVANCED",
    newConsent.consentId,
    newConsent.packageName
  );

  return newConsent;
}


/* ======================================================
   AUTO CHECK EXPIRY
====================================================== */
function checkAndMarkExpired() {

  const consents = readConsents();
  let changed = false;

  const updated = consents.map(c => {

    if (c.expiryDate && !c.expired) {

      const now = new Date();
      const expiry = new Date(c.expiryDate);

      if (now > expiry) {
        c.expired = true;
        changed = true;

        logAction("CONSENT_EXPIRED", c.consentId, c.packageName);
      }
    }

    return c;
  });

  if (changed) {
    writeConsents(updated);
  }

  return updated;
}


/* ======================================================
   GRANULAR PURPOSE REVOKE
====================================================== */
function revokePurpose(consentId, purpose) {

  const consents = checkAndMarkExpired();
  let updatedConsent = null;

  const updated = consents.map(c => {

    if (c.consentId === consentId && !c.revoked && !c.expired) {

      if (!Array.isArray(c.revokedPurposes)) {
        c.revokedPurposes = [];
      }

      if (!c.revokedPurposes.includes(purpose)) {
        c.revokedPurposes.push(purpose);
      }

      if (c.revokedPurposes.length === c.purpose.length) {
        c.revoked = true;
        c.revokedAt = new Date().toISOString();
      }

      updatedConsent = c;

      logAction(
        "CONSENT_REVOKED_PURPOSE",
        c.consentId,
        c.packageName
      );
    }

    return c;
  });

  writeConsents(updated);
  return updatedConsent;
}


/* ======================================================
   GET HISTORY (WITH EXPIRY CHECK)
====================================================== */
function getConsentHistory(packageName) {

  const consents = checkAndMarkExpired();

  if (!packageName || packageName === "ALL") {
    return consents;
  }

  return consents.filter(c => c.packageName === packageName);
}


/* ======================================================
   GENERATE RECEIPT
====================================================== */
const crypto = require("crypto");

function generateReceipt(consent) {

  const fingerprint = crypto
    .createHash("sha256")
    .update(consent.consentId + consent.timestamp)
    .digest("hex");

  const receipt = {
    receiptId: uuidv4(),
    consentId: consent.consentId,
    issuedAt: new Date().toISOString(),

    controller: "ConsentLens Platform",

    purpose: consent.purpose,

    lawfulBasis: consent.lawfulBasisType,

    jurisdiction: consent.jurisdiction,

    expiryDate: consent.expiryDate,

    expired: consent.expired,

    fingerprint: fingerprint,

    userRights: [
      "Right to Withdraw",
      "Right to Access",
      "Right to Erasure"
    ]
  };

  logAction(
    "RECEIPT_GENERATED",
    consent.consentId,
    consent.packageName
  );

  return receipt;
}



/* ======================================================
   RIGHT TO ERASURE
====================================================== */
function eraseConsent(consentId) {

  const consents = readConsents();

  const existing = consents.find(c => c.consentId === consentId);

  if (!existing) {
    return null;
  }

  const updated = consents.filter(c => c.consentId !== consentId);

  writeConsents(updated);

  logAction(
    "CONSENT_ERASED",
    consentId,
    existing.packageName
  );

  return {
    consentId,
    packageName: existing.packageName,
    erased: true,
    erasedAt: new Date().toISOString()
  };
}


module.exports = {
  grantConsent,
  grantConsentAdvanced,
  revokePurpose,
  getConsentHistory,
  generateReceipt,
  eraseConsent
};