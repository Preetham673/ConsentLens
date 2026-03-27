package com.example.consentlens.model;

public class KPIResponse {

    public KPIs kpis;

    public static class KPIs {
        public int totalConsents;
        public int grantedConsents;
        public int revokedConsents;
        public int highRiskConsents;
        public int totalRiskAnalyses;
    }
}
