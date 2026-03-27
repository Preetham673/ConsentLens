const rateLimit = require("express-rate-limit");

/* ======================================================
   GLOBAL API RATE LIMITER
====================================================== */

const apiLimiter = rateLimit({

  windowMs: 15 * 60 * 1000, // 15 minutes

  max: 100, // limit each IP

  message: {
    error: "Too many API requests. Please try again later."
  },

  standardHeaders: true,

  legacyHeaders: false

});

module.exports = {
  apiLimiter
};