package com.example.consentlens.model;

import java.util.List;

public class ConsentRequest {

    public String packageName;
    public String riskLevel;

    /* ======================================================
       OLD BASIC FIELDS (DO NOT REMOVE)
    ====================================================== */
    public List<String> purpose;
    public List<String> dataCategories;

    /* ======================================================
       NEW FIELD FOR CHILD CONSENT CHECK
    ====================================================== */
    public int age;

    /* ======================================================
       ADVANCED REGULATORY FIELDS
    ====================================================== */
    public String purposeId;
    public boolean thirdPartySharing;
    public int retentionPeriodDays;
    public String jurisdiction; // India / EU

    /* ======================================================
       LAWFUL BASIS FIELDS
    ====================================================== */
    public String lawfulBasisType;
    public String gdprArticle;
    public String dpdpSection;
    public boolean requiresExplicitConsent;
    public boolean sensitiveData;
    public boolean isMinor;

    /* ======================================================
       SIMPLE CONSTRUCTOR (OLD FLOW SUPPORT)
    ====================================================== */
    public ConsentRequest(String packageName,
                          String riskLevel,
                          List<String> purpose,
                          List<String> dataCategories) {

        this.packageName = packageName;
        this.riskLevel = riskLevel;
        this.purpose = purpose;
        this.dataCategories = dataCategories;
    }

    /* ======================================================
       SIMPLE CONSTRUCTOR WITH AGE (NEW)
    ====================================================== */
    public ConsentRequest(String packageName,
                          String riskLevel,
                          List<String> purpose,
                          List<String> dataCategories,
                          int age) {

        this.packageName = packageName;
        this.riskLevel = riskLevel;
        this.purpose = purpose;
        this.dataCategories = dataCategories;
        this.age = age;
    }

    /* ======================================================
       ADVANCED CONSTRUCTOR
    ====================================================== */
    public ConsentRequest(String packageName,
                          String riskLevel,
                          String purposeId,
                          List<String> dataCategories,
                          boolean thirdPartySharing,
                          int retentionPeriodDays,
                          String jurisdiction,
                          String lawfulBasisType,
                          String gdprArticle,
                          String dpdpSection,
                          boolean requiresExplicitConsent,
                          boolean sensitiveData,
                          boolean isMinor) {

        this.packageName = packageName;
        this.riskLevel = riskLevel;
        this.purposeId = purposeId;
        this.dataCategories = dataCategories;

        this.thirdPartySharing = thirdPartySharing;
        this.retentionPeriodDays = retentionPeriodDays;
        this.jurisdiction = jurisdiction;

        this.lawfulBasisType = lawfulBasisType;
        this.gdprArticle = gdprArticle;
        this.dpdpSection = dpdpSection;

        this.requiresExplicitConsent = requiresExplicitConsent;
        this.sensitiveData = sensitiveData;
        this.isMinor = isMinor;
    }
}