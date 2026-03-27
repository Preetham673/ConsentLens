package com.example.consentlens.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class RiskResponse {

    public String packageName;
    public int processingLatencyMs;

    public AIPolicyRisk aiPolicyRisk;

    public List<String> recommendations;



    @SerializedName("privacyProfile")
    public PrivacyProfile privacyProfile;

    @SerializedName("risk")
    public Risk risk;

    // =========================
    // Privacy Profile
    // =========================
    public static class PrivacyProfile {

        public String appName;
        public List<String> dataCategories;
        public boolean thirdPartySharing;
        public String policyClarity;

        // New optional backend fields
        public List<String> regulatoryFlags;
        public boolean dynamicallyGenerated;
    }

    // =========================
    // Risk Object
    // =========================
    public static class Risk {

        public int riskScore;
        public String riskLevel;

        public List<String> reasons;
        public List<String> potentialRisks;

        public String confidenceLevel;
        public String confidenceReason;

        public String dataImpactSummary;
        public String userRecommendation;
        public String whyYouAreSeeingThis;

        // 🔥 NEW FIELDS FROM BACKEND
        public String datasetReference;
        public int regulatoryViolationsFound;
        public List<String> explanation;

        public LegalMapping legalMapping;

        @SerializedName("certification")
        public Certification certification;
    }
    public class AIPolicyRisk {

        public String policyRisk;
        public double confidence;

    }
    // =========================
    // Legal Mapping
    // =========================
    public static class LegalMapping {
        public List<String> gdprArticles;
        public List<String> dpdpPrinciples;
    }
}
