/* ======================================================
   CHILD CONSENT MODULE (DPDP)
====================================================== */

function checkChildConsent(age) {

  if (!age) {
    return {
      allowed: true
    };
  }

  if (age < 18) {

    return {
      allowed: false,
      reason: "MINOR_DETECTED",
      message: "User is below 18. Guardian consent required."
    };

  }

  return {
    allowed: true
  };
}

module.exports = {
  checkChildConsent
};