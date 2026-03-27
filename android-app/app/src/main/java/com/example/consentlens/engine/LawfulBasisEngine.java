package com.example.consentlens.engine;

import java.util.List;

public class LawfulBasisEngine {

    public static LawfulBasisResult evaluate(
            List<String> dataCategories,
            boolean isMinor,
            boolean thirdPartySharing) {

        boolean sensitive =
                DataCategoryClassifier.containsSensitiveData(dataCategories);

        /* ==========================================
           CHILD DATA (DPDP + GDPR Article 8)
        ========================================== */
        if (isMinor) {
            return new LawfulBasisResult(
                    "Parental Consent Required",
                    "GDPR Article 8",
                    "DPDP - Parental Consent",
                    true,
                    sensitive
            );
        }

        /* ==========================================
           SENSITIVE DATA (GDPR Article 9)
        ========================================== */
        if (sensitive) {
            return new LawfulBasisResult(
                    "Explicit Consent",
                    "GDPR Article 9",
                    "DPDP Section 6",
                    true,
                    true
            );
        }

        /* ==========================================
           THIRD PARTY SHARING
        ========================================== */
        if (thirdPartySharing) {
            return new LawfulBasisResult(
                    "Consent Required",
                    "GDPR Article 6(1)(a)",
                    "DPDP Section 6",
                    true,
                    false
            );
        }

        /* ==========================================
           LEGITIMATE USE
        ========================================== */
        return new LawfulBasisResult(
                "Legitimate Use",
                "GDPR Article 6(1)(f)",
                "DPDP Section 7",
                false,
                false
        );
    }
}