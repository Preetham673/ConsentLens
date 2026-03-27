package com.example.consentlens.model;

import java.util.List;

public class ConsentHistoryResponse {

    public List<ConsentItem> history;

    public static class ConsentItem {

        public String consentId;
        public String dataPrincipalId;

        public String packageName;

        public List<String> purpose;

        public List<String> dataCategories;

        public String lawfulBasisType;
        public String gdprArticle;
        public String dpdpSection;

        public String jurisdiction;

        public String decision;
        public String riskLevel;

        public String timestamp;

        public String expiryDate;

        public boolean expired;

        public List<String> revokedPurposes;

        public boolean revoked;

        public String revokedAt;
    }
}