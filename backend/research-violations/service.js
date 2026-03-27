const fs = require("fs");
const path = require("path");

const dataPath = path.join(__dirname, "data.json");

function getViolationsByApp(packageName) {
  const rawData = fs.readFileSync(dataPath);
  const violations = JSON.parse(rawData);

  return violations.filter(v =>
    v.apps.includes(packageName)
  );
}

module.exports = {
  getViolationsByApp
};
