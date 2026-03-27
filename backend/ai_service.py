from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
import re

app = FastAPI()

# =====================================================
# CORS — allow requests from Node.js backend
# =====================================================
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)


# =====================================================
# REQUEST MODEL
# =====================================================
class PolicyRequest(BaseModel):
    policy: str


# =====================================================
# PRIVACY RISK KEYWORD DATABASE
# High risk = these words in policy = danger to user
# =====================================================

HIGH_RISK_KEYWORDS = [
    # Data selling / sharing
    "sell your data",
    "sell user data",
    "share with third parties",
    "share your personal information with advertisers",
    "transfer to third parties",
    "disclose to partners",
    "share with our affiliates",

    # Tracking
    "track your location",
    "precise location",
    "track your activity",
    "behavioral tracking",
    "cross-site tracking",
    "device fingerprinting",

    # Sensitive data
    "biometric data",
    "facial recognition",
    "health information",
    "financial information",
    "government id",
    "social security",

    # No user control
    "cannot opt out",
    "no opt-out",
    "irrevocable",
    "non-revocable",
    "perpetual license",
    "worldwide license",

    # Retention
    "retain indefinitely",
    "stored permanently",
    "never deleted",

    # Children
    "children under 13",
    "minors data",
]

MEDIUM_RISK_KEYWORDS = [
    # Advertising
    "advertising",
    "targeted ads",
    "personalized ads",
    "ad network",
    "marketing purposes",
    "promotional",

    # Analytics
    "analytics",
    "usage statistics",
    "crash reports",
    "performance data",

    # Third party
    "third party",
    "service providers",
    "business partners",
    "vendors",

    # Data sharing
    "share with",
    "disclose",
    "transfer",

    # Profiling
    "profiling",
    "infer",
    "predict your behavior",

    # Cookies
    "cookies",
    "web beacons",
    "pixel tags",
]

LOW_RISK_KEYWORDS = [
    # User rights
    "you can delete",
    "right to erasure",
    "right to access",
    "right to correct",
    "opt out",
    "opt-out",
    "unsubscribe",
    "withdraw consent",

    # Security
    "encrypted",
    "encryption",
    "ssl",
    "tls",
    "secure",
    "protected",

    # Minimal collection
    "we do not sell",
    "we never sell",
    "do not share",
    "no third party",
    "minimal data",
    "only necessary",
    "anonymized",
    "anonymised",
    "pseudonymized",

    # Compliance
    "gdpr",
    "dpdp",
    "ccpa",
    "hipaa",
    "iso 27001",
    "iso27001",
]


# =====================================================
# CORE ANALYSIS FUNCTION
# Rule-based keyword scoring — no model needed
# Works offline, no GPU required
# =====================================================

def analyze_privacy_policy(text: str) -> dict:

    text_lower = text.lower()

    # Count keyword hits in each category
    high_hits   = []
    medium_hits = []
    low_hits    = []

    for kw in HIGH_RISK_KEYWORDS:
        if kw in text_lower:
            high_hits.append(kw)

    for kw in MEDIUM_RISK_KEYWORDS:
        if kw in text_lower:
            medium_hits.append(kw)

    for kw in LOW_RISK_KEYWORDS:
        if kw in text_lower:
            low_hits.append(kw)

    # ---- Score calculation ----
    # High risk keywords are weighted heavily
    score = (len(high_hits) * 3) + (len(medium_hits) * 1) - (len(low_hits) * 2)

    # ---- Risk level decision ----
    if len(high_hits) >= 2 or score >= 6:
        risk_level = "HIGH"
        confidence = min(0.95, 0.70 + (len(high_hits) * 0.05))

    elif len(high_hits) == 1 or score >= 3:
        risk_level = "MEDIUM"
        confidence = min(0.90, 0.60 + (len(medium_hits) * 0.03))

    else:
        risk_level = "LOW"
        confidence = min(0.90, 0.65 + (len(low_hits) * 0.04))

    # ---- Build explanation ----
    reasons = []

    if high_hits:
        reasons.append(f"High risk terms found: {', '.join(high_hits[:3])}")

    if medium_hits:
        reasons.append(f"Medium risk terms found: {', '.join(medium_hits[:3])}")

    if low_hits:
        reasons.append(f"Positive privacy signals: {', '.join(low_hits[:3])}")

    if not reasons:
        reasons.append("No specific privacy terms detected — treat as unknown risk")

    return {
        "policyRisk":       risk_level,
        "confidence":       round(confidence, 2),
        "score":            score,
        "highRiskTerms":    high_hits,
        "mediumRiskTerms":  medium_hits[:5],
        "positiveTerms":    low_hits[:5],
        "reasons":          reasons,
        "analyzedChars":    len(text)
    }


# =====================================================
# ENDPOINT — POST /analyze-policy
# Called by Node.js routes.js
# =====================================================

@app.post("/analyze-policy")
def analyze_policy(data: PolicyRequest):

    text = data.policy.strip() if data.policy else ""

    if not text:
        return {
            "policyRisk":   "UNKNOWN",
            "confidence":   0.0,
            "error":        "policy text required"
        }

    # Analyse full text (no 512 char limit anymore)
    result = analyze_privacy_policy(text)

    return result


# =====================================================
# HEALTH CHECK — GET /health
# Use this to verify the server is running
# =====================================================

@app.get("/health")
def health():
    return {
        "status":  "running",
        "service": "ConsentLens AI Policy Analyzer",
        "version": "2.0"
    }


# =====================================================
# RUN — python ai_service.py
# =====================================================

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8001)
