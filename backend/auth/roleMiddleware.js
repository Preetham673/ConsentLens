/* ======================================================
   ROLE BASED ACCESS CONTROL
====================================================== */

function requireRole(roles) {

  return (req, res, next) => {

    if (!req.user) {
      return res.status(401).json({
        error: "Unauthorized"
      });
    }

    // convert single role to array
    if (!Array.isArray(roles)) {
      roles = [roles];
    }

    if (!roles.includes(req.user.role)) {
      return res.status(403).json({
        error: "Access denied"
      });
    }

    next();
  };
}

module.exports = { requireRole };
