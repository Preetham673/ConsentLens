function checkPermissionConsistency(permissions, purposes) {
  let mismatches = [];

  if (permissions.includes("LOCATION") && !purposes.includes("location-based")) {
    mismatches.push("Location permission without location-based purpose");
  }

  if (permissions.includes("CONTACTS") && !purposes.includes("social")) {
    mismatches.push("Contacts permission without social purpose");
  }

  return {
    hasMismatch: mismatches.length > 0,
    issues: mismatches
  };
}

module.exports = {
  checkPermissionConsistency
};
