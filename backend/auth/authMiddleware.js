const { verifyToken } = require("./jwtService");

/* ======================================================
   AUTH MIDDLEWARE
====================================================== */

function authenticate(req, res, next) {

  const authHeader = req.headers["authorization"];

  if (!authHeader) {

    return res.status(401).json({
      error: "Authorization token required"
    });

  }

  const token = authHeader.split(" ")[1];

  const decoded = verifyToken(token);

  if (!decoded) {

    return res.status(403).json({
      error: "Invalid or expired token"
    });

  }

  req.user = decoded;

  next();

}

module.exports = {
  authenticate
};