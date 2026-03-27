package com.example.consentlens.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.consentlens.model.AuditLogResponse;
import com.example.consentlens.model.ComplianceReportResponse;
import com.example.consentlens.model.ConsentHistoryResponse;
import com.example.consentlens.model.ConsentReceiptResponse;
import com.example.consentlens.model.RiskResponse;
import com.example.consentlens.model.AuditVerifyResponse;
import com.example.consentlens.repository.RiskRepository;


import java.util.List;

public class RiskViewModel extends ViewModel {

    private final RiskRepository repository;

    public RiskViewModel() {
        repository = new RiskRepository();
    }

    // =========================
    // ANALYZE APP
    // =========================
    public LiveData<RiskResponse> analyzeApp(String packageName) {
        return repository.analyzeApp(packageName);
    }

    // =========================
    // GRANT CONSENT
    // =========================
    public LiveData<Boolean> grantConsent(String packageName,
                                          String riskLevel,
                                          List<String> dataCategories) {
        return repository.grantConsent(packageName, riskLevel, dataCategories);
    }

    // =========================
    // REVOKE PURPOSE
    // =========================
    public LiveData<Boolean> revokePurpose(String consentId,
                                           String purpose) {
        return repository.revokePurpose(consentId, purpose);
    }

    // =========================
    // CONSENT HISTORY
    // =========================
    public LiveData<ConsentHistoryResponse> getConsentHistory(String packageName) {
        return repository.getConsentHistory(packageName);
    }

    public LiveData<ConsentHistoryResponse> getAllConsentHistory() {
        return repository.getAllConsentHistory();
    }

    // =========================
    // RECEIPT
    // =========================
    public LiveData<ConsentReceiptResponse> getReceipt(String consentId) {
        return repository.getReceipt(consentId);
    }

    // =========================
    // COMPLIANCE REPORT
    // =========================
    public LiveData<ComplianceReportResponse> getComplianceReport(String packageName) {
        return repository.getComplianceReport(packageName);
    }

    // =========================
    // AUDIT LOGS
    // =========================
    public LiveData<AuditLogResponse> getAuditLogs() {
        return repository.getAuditLogs();
    }

    // =========================
// ERASE CONSENT
// =========================

    public LiveData<Boolean> eraseConsent(String consentId) {
        return repository.eraseConsent(consentId);
    }

    public LiveData<AuditVerifyResponse> verifyAuditLogs() {
        return repository.verifyAuditLogs();
    }



    // =========================
    // KPIs
    // =========================
    public LiveData<Object> getKPIs() {
        return repository.getKPIs();
    }

    public LiveData<Boolean> submitFeedback(String packageName, int rating) {
        return repository.submitFeedback(packageName, rating);
    }

}
