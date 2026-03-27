const fs = require("fs");
const path = require("path");
const crypto = require("crypto");
const { v4: uuidv4 } = require("uuid");

// 🔐 NEW: Encryption Import
const { encryptData, decryptData } =
require("../security/encryptionService");

const auditPath = path.join(__dirname, "auditLogs.json");

/* ======================================================
   SAFE READ (DECRYPTED STORAGE)
====================================================== */
function readLogs() {
  try {

    if (!fs.existsSync(auditPath)) return [];

    const raw = fs.readFileSync(auditPath, "utf8");

    if (!raw) return [];

    const encryptedObject = JSON.parse(raw);

    try {

      const decrypted = decryptData(encryptedObject);
      return decrypted;

    } catch (err) {

      console.error("Audit Decryption Error:", err.message);
      return [];
    }

  } catch (err) {

    console.error("Audit Read Error:", err.message);
    return [];
  }
}

/* ======================================================
   SAFE WRITE (ENCRYPTED STORAGE)
====================================================== */
function writeLogs(logs) {
  try {

    // 🔐 encrypt logs before saving
    const encrypted = encryptData(logs);

    fs.writeFileSync(
      auditPath,
      JSON.stringify(encrypted, null, 2)
    );

  } catch (err) {
    console.error("Audit Write Error:", err.message);
  }
}

/* ======================================================
   HASH GENERATOR
====================================================== */
function generateHash(data) {
  return crypto
    .createHash("sha256")
    .update(JSON.stringify(data))
    .digest("hex");
}

/* ======================================================
   LOG ACTION (HASH CHAINED LEDGER)
====================================================== */
function logAction(action, consentId, packageName, metadata = {}) {

  const logs = readLogs();

  const previousHash =
    logs.length > 0
      ? logs[logs.length - 1].currentHash
      : "GENESIS";

  const chainIndex = logs.length;

  const baseLog = {
    logId: uuidv4(),
    chainIndex,
    action,
    consentId,
    packageName,
    eventMetadata: metadata,
    timestamp: new Date().toISOString(),
    previousHash
  };

  const currentHash = generateHash(baseLog);

  const finalLog = {
    ...baseLog,
    currentHash
  };

  logs.push(finalLog);
  writeLogs(logs);

  return finalLog;
}

/* ======================================================
   VERIFY LOG INTEGRITY
====================================================== */
function verifyLogsIntegrity() {

  const logs = readLogs();

  if (logs.length === 0) {
    return {
      valid: true,
      totalLogs: 0,
      message: "No logs available (empty chain)"
    };
  }

  if (logs[0].previousHash !== "GENESIS") {
    return {
      valid: false,
      error: "Genesis block corrupted"
    };
  }

  for (let i = 0; i < logs.length; i++) {

    const currentLog = logs[i];

    const recalculatedHash = generateHash({
      logId: currentLog.logId,
      chainIndex: currentLog.chainIndex,
      action: currentLog.action,
      consentId: currentLog.consentId,
      packageName: currentLog.packageName,
      eventMetadata: currentLog.eventMetadata,
      timestamp: currentLog.timestamp,
      previousHash: currentLog.previousHash
    });

    if (recalculatedHash !== currentLog.currentHash) {
      return {
        valid: false,
        error: `Hash mismatch at log ${currentLog.logId}`
      };
    }

    if (i > 0) {
      const previousLog = logs[i - 1];

      if (currentLog.previousHash !== previousLog.currentHash) {
        return {
          valid: false,
          error: `Chain broken between ${previousLog.logId} and ${currentLog.logId}`
        };
      }
    }
  }

  return {
    valid: true,
    totalLogs: logs.length,
    lastLogHash: logs[logs.length - 1].currentHash,
    message: "Audit ledger verified successfully"
  };
}

/* ======================================================
   GET ALL LOGS
====================================================== */
function getAllLogs() {
  return readLogs();
}

/* ======================================================
   FILTER LOGS (OPTIONAL)
====================================================== */
function getLogsByAction(action) {
  const logs = readLogs();
  return logs.filter(l => l.action === action);
}

/* ======================================================
   FILTER BY PACKAGE
====================================================== */
function getLogsByPackage(packageName) {
  const logs = readLogs();
  return logs.filter(l => l.packageName === packageName);
}

module.exports = {
  logAction,
  getAllLogs,
  verifyLogsIntegrity,
  getLogsByAction,
  getLogsByPackage
};