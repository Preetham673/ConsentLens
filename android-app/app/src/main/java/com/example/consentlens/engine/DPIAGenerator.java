package com.example.consentlens.engine;

import java.util.List;

public class DPIAGenerator {

    public static String generateDPIA(
            int riskScore,
            boolean sensitiveData,
            boolean thirdPartySharing,
            List<String> dataCategories) {

        if (riskScore < 60 && !sensitiveData && !thirdPartySharing) {
            return null;
        }

        StringBuilder dpia = new StringBuilder();

        dpia.append("=== DATA PROTECTION IMPACT ASSESSMENT ===\n");

        dpia.append("Risk Score: ").append(riskScore).append("\n");

        if (sensitiveData) {
            dpia.append("Sensitive Data Processing Detected\n");
        }

        if (thirdPartySharing) {
            dpia.append("Third Party Data Sharing Detected\n");
        }

        if (dataCategories != null) {
            dpia.append("Data Categories:\n");

            for (String cat : dataCategories) {
                dpia.append("• ").append(cat).append("\n");
            }
        }

        dpia.append("\nRecommended Safeguards:\n");
        dpia.append("• Data minimization\n");
        dpia.append("• Encryption\n");
        dpia.append("• User consent transparency\n");

        return dpia.toString();
    }
}