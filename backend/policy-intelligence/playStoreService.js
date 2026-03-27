const axios = require("axios");
const cheerio = require("cheerio");

async function fetchPlayStoreData(packageName) {
  try {
    const playStoreUrl = `https://play.google.com/store/apps/details?id=${packageName}&hl=en&gl=us`;

    const response = await axios.get(playStoreUrl, {
      headers: {
        "User-Agent":
          "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/115 Safari/537.36",
      },
    });

    const $ = cheerio.load(response.data);

    // Extract privacy policy link
    let privacyPolicyUrl = null;

    $("a").each((i, el) => {
      const text = $(el).text().toLowerCase();
      if (text.includes("privacy policy")) {
        privacyPolicyUrl = $(el).attr("href");
      }
    });

    return {
      success: true,
      privacyPolicyUrl,
    };
  } catch (error) {
    return {
      success: false,
      error: error.message,
    };
  }
}

module.exports = {
  fetchPlayStoreData,
};
