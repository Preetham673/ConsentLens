const fs = require("fs");
const path = require("path");

const feedbackPath = path.join(__dirname, "feedback.json");

/* ============================
   READ FEEDBACK FILE
============================ */
function readFeedback() {
  if (!fs.existsSync(feedbackPath)) return [];
  return JSON.parse(fs.readFileSync(feedbackPath, "utf8") || "[]");
}

/* ============================
   WRITE FEEDBACK FILE
============================ */
function writeFeedback(data) {
  fs.writeFileSync(feedbackPath, JSON.stringify(data, null, 2));
}

/* ============================
   SUBMIT FEEDBACK
============================ */
function submitFeedback({ packageName, rating }) {

  const feedbackList = readFeedback();

  const newEntry = {
    packageName,
    rating: Number(rating),
    timestamp: new Date().toISOString()
  };

  feedbackList.push(newEntry);
  writeFeedback(feedbackList);

  return newEntry;
}

/* ============================
   CALCULATE AVERAGE RATING
============================ */
function calculateAverageRating() {

  const feedbackList = readFeedback();

  if (feedbackList.length === 0) return 0;

  const total = feedbackList.reduce(
    (sum, item) => sum + item.rating,
    0
  );

  return Number((total / feedbackList.length).toFixed(2));
}

module.exports = {
  submitFeedback,
  calculateAverageRating
};
