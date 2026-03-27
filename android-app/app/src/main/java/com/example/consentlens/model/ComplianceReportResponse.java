package com.example.consentlens.model;

import java.util.List;

public class ComplianceReportResponse {

    public ComplianceReport complianceReport;

    public static class ComplianceReport {
        public App app;
        public PrivacyProfile privacyProfile;
        public RiskAssessment riskAssessment;
        public List<ConsentHistoryResponse.ConsentItem> consentHistory;
        public List<AuditEvent> relatedAuditEvents;
        public String generatedAt;

        // NEW SAFE FIELD
        public String complianceStatus;
    }

    public static class App {
        public String packageName;
        public String appName;
    }

    public static class PrivacyProfile {
        public String appName;
        public List<String> dataCategories;
        public boolean thirdPartySharing;
        public String policyClarity;
    }

    public static class RiskAssessment {
        public int riskScore;
        public String riskLevel;
        public String confidenceLevel;
        public List<String> reasons;
        public LegalMapping legalMapping;

        // NEW SAFE FIELDS
        public String datasetReference;
        public int regulatoryViolationsFound;
    }

    public static class LegalMapping {
        public List<String> gdprArticles;
        public List<String> dpdpPrinciples;
    }

    public static class AuditEvent {
        public String logId;
        public String action;     // ✅ MUST match backend
        public String consentId;
        public String packageName;
        public String timestamp;
    }

}
