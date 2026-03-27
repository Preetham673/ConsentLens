const jwt = require("jsonwebtoken");

require("dotenv").config();

/* ======================================================
   GENERATE TOKEN
====================================================== */

function generateToken(payload) {

  return jwt.sign(
    payload,
    process.env.JWT_SECRET,
    { expiresIn: "2h" }
  );

}

/* ======================================================
   VERIFY TOKEN
====================================================== */

function verifyToken(token) {

  try {

    return jwt.verify(
      token,
      process.env.JWT_SECRET
    );

  } catch (err) {

    return null;

  }

}

module.exports = {
  generateToken,
  verifyToken
};