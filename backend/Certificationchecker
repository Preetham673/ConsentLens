// =====================================================
// ConsentLens — certificationChecker.js
// ISO 27001 & other certification database
// =====================================================

const certifiedApps = {

    // =============================================
    // ISO 27001 CERTIFIED APPS
    // =============================================

    // Google
    "com.google.android.apps.maps"          : { standard: "ISO27001", authority: "BSI Group",       year: 2023 },
    "com.google.android.gm"                 : { standard: "ISO27001", authority: "BSI Group",       year: 2023 },
    "com.google.android.youtube"            : { standard: "ISO27001", authority: "BSI Group",       year: 2023 },
    "com.google.android.apps.docs"          : { standard: "ISO27001", authority: "BSI Group",       year: 2023 },
    "com.google.android.apps.photos"        : { standard: "ISO27001", authority: "BSI Group",       year: 2023 },
    "com.google.android.keep"               : { standard: "ISO27001", authority: "BSI Group",       year: 2023 },
    "com.google.android.calendar"           : { standard: "ISO27001", authority: "BSI Group",       year: 2023 },

    // Meta / WhatsApp
    "com.whatsapp"                          : { standard: "ISO27001", authority: "TÜV Rheinland",   year: 2023 },
    "com.facebook.katana"                   : { standard: "ISO27001", authority: "TÜV Rheinland",   year: 2023 },
    "com.instagram.android"                 : { standard: "ISO27001", authority: "TÜV Rheinland",   year: 2023 },

    // Microsoft
    "com.microsoft.teams"                   : { standard: "ISO27001", authority: "BSI Group",       year: 2024 },
    "com.microsoft.launcher"                : { standard: "ISO27001", authority: "BSI Group",       year: 2024 },
    "com.microsoft.office.outlook"          : { standard: "ISO27001", authority: "BSI Group",       year: 2024 },
    "com.microsoft.skydrive"                : { standard: "ISO27001", authority: "BSI Group",       year: 2024 },

    // Zoom
    "us.zoom.videomeetings"                 : { standard: "ISO27001", authority: "Schellman",       year: 2023 },

    // Slack
    "com.Slack"                             : { standard: "ISO27001", authority: "A-LIGN",          year: 2023 },

    // Dropbox
    "com.dropbox.android"                   : { standard: "ISO27001", authority: "BrightLine",      year: 2023 },

    // Spotify
    "com.spotify.music"                     : { standard: "ISO27001", authority: "Deloitte",        year: 2023 },

    // Amazon
    "com.amazon.mShop.android.shopping"     : { standard: "ISO27001", authority: "BSI Group",       year: 2024 },
    "com.amazon.avod.thirdpartyclient"      : { standard: "ISO27001", authority: "BSI Group",       year: 2024 },

    // PayPal
    "com.paypal.android.p2pmobile"          : { standard: "ISO27001", authority: "Ernst & Young",   year: 2023 },

    // Salesforce
    "com.salesforce.chatter"                : { standard: "ISO27001", authority: "Coalfire",        year: 2024 },

    // LinkedIn
    "com.linkedin.android"                  : { standard: "ISO27001", authority: "BSI Group",       year: 2023 },

    // Adobe
    "com.adobe.reader"                      : { standard: "ISO27001", authority: "BSI Group",       year: 2023 },

    // TalkBack (Google accessibility)
    "com.google.android.marvin.talkback"    : { standard: "ISO27001", authority: "BSI Group",       year: 2023 },

    // =============================================
    // SOC 2 CERTIFIED (not ISO 27001)
    // =============================================

    "com.twitter.android"                   : { standard: "SOC2",     authority: "KPMG",            year: 2023 },
    "com.snapchat.android"                  : { standard: "SOC2",     authority: "Deloitte",        year: 2023 },
    "com.netflix.mediaclient"               : { standard: "SOC2",     authority: "PwC",             year: 2023 },
    "com.tiktok"                            : { standard: "SOC2",     authority: "KPMG",            year: 2023 },

    // =============================================
    // NO CERTIFICATION — explicitly marked
    // =============================================
    // (these are not listed — absence = not certified)
};


// =====================================================
// MAIN FUNCTION — check if app is ISO 27001 certified
// =====================================================

function checkCertification(packageName) {

    // Normalise — trim whitespace, lowercase
    const pkg = packageName ? packageName.trim().toLowerCase() : "";

    const entry = certifiedApps[pkg];

    if (!entry) {
        // App not in database — not certified
        return {
            certified:      false,
            standard:       null,
            authority:      null,
            year:           null,
            isISO27001:     false,
            riskReduction:  0,
            message:        "No certification record found for this app."
        };
    }

    const isISO27001 = entry.standard === "ISO27001";

    return {
        certified:      true,
        standard:       entry.standard,
        authority:      entry.authority,
        year:           entry.year,
        isISO27001:     isISO27001,

        // Only reduce risk if ISO 27001 specifically
        riskReduction:  isISO27001 ? 15 : 5,

        message:        isISO27001
                            ? `ISO 27001 certified by ${entry.authority} (${entry.year})`
                            : `Certified under ${entry.standard} by ${entry.authority} (${entry.year}) — not ISO 27001`
    };
}


// =====================================================
// HELPER — check specifically for ISO 27001 only
// =====================================================

function isISO27001Certified(packageName) {
    const result = checkCertification(packageName);
    return result.certified && result.isISO27001;
}


// =====================================================
// HELPER — add a new certified app at runtime
// (useful for admin endpoints to update the list)
// =====================================================

function addCertification(packageName, standard, authority, year) {
    certifiedApps[packageName.trim().toLowerCase()] = {
        standard,
        authority,
        year: year || new Date().getFullYear()
    };
}


module.exports = {
    checkCertification,
    isISO27001Certified,
    addCertification
};