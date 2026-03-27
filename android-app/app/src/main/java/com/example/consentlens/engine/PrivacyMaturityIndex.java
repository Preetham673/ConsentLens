package com.example.consentlens.engine;

public class PrivacyMaturityIndex {

    public static int calculatePMI(
            boolean consentImplemented,
            boolean auditEnabled,
            boolean sensitiveDetection,
            boolean dpiaSupport,
            boolean lawfulBasisMapping) {

        int score = 0;

        if (consentImplemented) score += 20;
        if (auditEnabled) score += 20;
        if (sensitiveDetection) score += 20;
        if (dpiaSupport) score += 20;
        if (lawfulBasisMapping) score += 20;

        return score;
    }

    public static String getComplianceLevel(int score) {

        if (score >= 80)
            return "ADVANCED";

        if (score >= 60)
            return "MODERATE";

        if (score >= 40)
            return "BASIC";

        return "LOW";
    }
}