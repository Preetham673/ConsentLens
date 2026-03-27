package com.example.consentlens.engine;

public class LawfulBasisResult {

    private String lawfulBasisType;
    private String gdprArticle;
    private String dpdpSection;
    private boolean requiresExplicitConsent;
    private boolean sensitiveData;

    public LawfulBasisResult(String lawfulBasisType,
                             String gdprArticle,
                             String dpdpSection,
                             boolean requiresExplicitConsent,
                             boolean sensitiveData) {

        this.lawfulBasisType = lawfulBasisType;
        this.gdprArticle = gdprArticle;
        this.dpdpSection = dpdpSection;
        this.requiresExplicitConsent = requiresExplicitConsent;
        this.sensitiveData = sensitiveData;
    }

    /* ==========================================
       NEW STANDARD METHOD
    ========================================== */
    public String getLawfulBasisType() {
        return lawfulBasisType;
    }

    /* ==========================================
       OLD METHOD (KEPT FOR COMPATIBILITY)
    ========================================== */
    public String getBasisType() {
        return lawfulBasisType;
    }

    public String getGdprArticle() {
        return gdprArticle;
    }

    public String getDpdpSection() {
        return dpdpSection;
    }

    public boolean isRequiresExplicitConsent() {
        return requiresExplicitConsent;
    }

    public boolean isSensitiveData() {
        return sensitiveData;
    }
}