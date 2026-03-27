const crypto = require("crypto");

/* ======================================================
   AES-256 CONFIG
====================================================== */

const algorithm = "aes-256-cbc";

// 32 byte encryption key
const key = crypto
  .createHash("sha256")
  .update("CONSENTLENS_SUPER_SECRET_KEY")
  .digest();

/* ======================================================
   ENCRYPT DATA
====================================================== */

function encryptData(data) {

  const iv = crypto.randomBytes(16);

  const cipher = crypto.createCipheriv(
    algorithm,
    key,
    iv
  );

  let encrypted = cipher.update(
    JSON.stringify(data),
    "utf8",
    "hex"
  );

  encrypted += cipher.final("hex");

  return {
    iv: iv.toString("hex"),
    data: encrypted
  };
}

/* ======================================================
   DECRYPT DATA
====================================================== */

function decryptData(encryptedObject) {

  const iv = Buffer.from(
    encryptedObject.iv,
    "hex"
  );

  const encryptedText = encryptedObject.data;

  const decipher = crypto.createDecipheriv(
    algorithm,
    key,
    iv
  );

  let decrypted = decipher.update(
    encryptedText,
    "hex",
    "utf8"
  );

  decrypted += decipher.final("utf8");

  return JSON.parse(decrypted);
}

module.exports = {
  encryptData,
  decryptData
};