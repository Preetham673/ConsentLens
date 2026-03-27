package com.example.consentlens.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.consentlens.engine.LawfulBasisEngine;
import com.example.consentlens.engine.LawfulBasisResult;
import com.example.consentlens.model.AuditLogResponse;
import com.example.consentlens.model.ComplianceReportResponse;
import com.example.consentlens.model.ConsentHistoryResponse;
import com.example.consentlens.model.ConsentReceiptResponse;
import com.example.consentlens.model.AuditVerifyResponse;
import com.example.consentlens.model.RiskResponse;
import com.example.consentlens.model.ConsentRequest;
import com.example.consentlens.network.ApiService;
import com.example.consentlens.network.RetrofitClient;

import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
public class RiskRepository {

    private final ApiService apiService;

    public RiskRepository() {
        apiService = RetrofitClient.getClient().create(ApiService.class);
    }

    // ======================================================
    // ANALYZE APP
    // ======================================================
    public LiveData<RiskResponse> analyzeApp(String packageName) {

        MutableLiveData<RiskResponse> liveData = new MutableLiveData<>();

        ApiService.AppRequest request =
                new ApiService.AppRequest(packageName);

        apiService.analyzeApp(request)
                .enqueue(new Callback<RiskResponse>() {
                    @Override
                    public void onResponse(Call<RiskResponse> call,
                                           Response<RiskResponse> response) {

                        liveData.setValue(response.isSuccessful()
                                ? response.body() : null);
                    }

                    @Override
                    public void onFailure(Call<RiskResponse> call,
                                          Throwable t) {
                        liveData.setValue(null);
                    }
                });

        return liveData;
    }

    // ======================================================
    // GRANT CONSENT
    // ======================================================
    public LiveData<Boolean> grantConsent(String packageName,
                                          String riskLevel,
                                          List<String> dataCategories) {

        MutableLiveData<Boolean> result = new MutableLiveData<>();

        List<String> purposeList =
                Arrays.asList("General Processing");

        ConsentRequest request =
                new ConsentRequest(
                        packageName,
                        riskLevel,
                        purposeList,
                        dataCategories
                );

        apiService.grantConsent(request)
                .enqueue(new Callback<Object>() {
                    @Override
                    public void onResponse(Call<Object> call,
                                           Response<Object> response) {
                        result.setValue(response.isSuccessful());
                    }

                    @Override
                    public void onFailure(Call<Object> call,
                                          Throwable t) {
                        result.setValue(false);
                    }
                });

        return result;
    }

    // ======================================================
    // REVOKE PURPOSE
    // ======================================================
    public LiveData<Boolean> revokePurpose(String consentId,
                                           String purpose) {

        MutableLiveData<Boolean> result = new MutableLiveData<>();

        ApiService.RevokePurposeRequest request =
                new ApiService.RevokePurposeRequest(consentId, purpose);

        apiService.revokePurpose(request)
                .enqueue(new Callback<Object>() {
                    @Override
                    public void onResponse(Call<Object> call,
                                           Response<Object> response) {
                        result.setValue(response.isSuccessful());
                    }

                    @Override
                    public void onFailure(Call<Object> call,
                                          Throwable t) {
                        result.setValue(false);
                    }
                });

        return result;
    }
    // ======================================================
    // 🔥 NEW ADVANCED GRANT CONSENT (RegTech Version)
    // ======================================================
    public LiveData<Boolean> grantConsentAdvanced(String packageName,
                                                  String riskLevel,
                                                  List<String> dataCategories,
                                                  boolean isMinor,
                                                  boolean thirdPartySharing,
                                                  String jurisdiction) {

        MutableLiveData<Boolean> result = new MutableLiveData<>();

        int retentionPeriodDays = 30;
        String purposeId = "GENERAL_PROCESSING";

        // 🔥 Lawful Basis Evaluation
        LawfulBasisResult basisResult =
                LawfulBasisEngine.evaluate(
                        dataCategories,
                        isMinor,
                        thirdPartySharing
                );

        // 🔥 Advanced Consent Object
        ConsentRequest advancedRequest = new ConsentRequest(
                packageName,
                riskLevel,
                purposeId,
                dataCategories,
                thirdPartySharing,
                retentionPeriodDays,
                jurisdiction,
                basisResult.getLawfulBasisType(),
                basisResult.getGdprArticle(),
                basisResult.getDpdpSection(),
                basisResult.isRequiresExplicitConsent(),
                basisResult.isSensitiveData(),
                isMinor
        );

        // 🔥 Call new backend endpoint
        apiService.grantConsentAdvanced(advancedRequest)
                .enqueue(new Callback<Object>() {
                    @Override
                    public void onResponse(Call<Object> call,
                                           Response<Object> response) {
                        result.setValue(response.isSuccessful());
                    }

                    @Override
                    public void onFailure(Call<Object> call,
                                          Throwable t) {
                        result.setValue(false);
                    }
                });

        return result;
    }

    // ======================================================
    // CONSENT HISTORY
    // ======================================================
    public LiveData<ConsentHistoryResponse> getConsentHistory(String packageName) {

        MutableLiveData<ConsentHistoryResponse> liveData =
                new MutableLiveData<>();

        ApiService.AppRequest request =
                new ApiService.AppRequest(packageName);

        apiService.getConsentHistory(request)
                .enqueue(new Callback<ConsentHistoryResponse>() {
                    @Override
                    public void onResponse(Call<ConsentHistoryResponse> call,
                                           Response<ConsentHistoryResponse> response) {

                        liveData.setValue(response.isSuccessful()
                                ? response.body() : null);
                    }

                    @Override
                    public void onFailure(Call<ConsentHistoryResponse> call,
                                          Throwable t) {
                        liveData.setValue(null);
                    }
                });

        return liveData;
    }

    public LiveData<ConsentHistoryResponse> getAllConsentHistory() {

        MutableLiveData<ConsentHistoryResponse> liveData =
                new MutableLiveData<>();

        apiService.getAllConsentHistory()
                .enqueue(new Callback<ConsentHistoryResponse>() {
                    @Override
                    public void onResponse(Call<ConsentHistoryResponse> call,
                                           Response<ConsentHistoryResponse> response) {

                        liveData.setValue(response.isSuccessful()
                                ? response.body() : null);
                    }

                    @Override
                    public void onFailure(Call<ConsentHistoryResponse> call,
                                          Throwable t) {
                        liveData.setValue(null);
                    }
                });

        return liveData;
    }

    // ======================================================
    // RECEIPT
    // ======================================================
    public LiveData<ConsentReceiptResponse> getReceipt(String consentId) {

        MutableLiveData<ConsentReceiptResponse> liveData =
                new MutableLiveData<>();

        apiService.getReceipt(consentId)
                .enqueue(new Callback<ConsentReceiptResponse>() {
                    @Override
                    public void onResponse(Call<ConsentReceiptResponse> call,
                                           Response<ConsentReceiptResponse> response) {

                        liveData.setValue(response.isSuccessful()
                                ? response.body() : null);
                    }

                    @Override
                    public void onFailure(Call<ConsentReceiptResponse> call,
                                          Throwable t) {
                        liveData.setValue(null);
                    }
                });

        return liveData;
    }

    // ======================================================
    // COMPLIANCE REPORT
    // ======================================================
    public LiveData<ComplianceReportResponse> getComplianceReport(String packageName) {

        MutableLiveData<ComplianceReportResponse> liveData =
                new MutableLiveData<>();

        ApiService.AppRequest request =
                new ApiService.AppRequest(packageName);

        apiService.getComplianceReport(request)
                .enqueue(new Callback<ComplianceReportResponse>() {
                    @Override
                    public void onResponse(Call<ComplianceReportResponse> call,
                                           Response<ComplianceReportResponse> response) {

                        liveData.setValue(response.isSuccessful()
                                ? response.body() : null);
                    }

                    @Override
                    public void onFailure(Call<ComplianceReportResponse> call,
                                          Throwable t) {
                        liveData.setValue(null);
                    }
                });

        return liveData;
    }

    // ======================================================
    // AUDIT LOGS
    // ======================================================
    public LiveData<AuditLogResponse> getAuditLogs() {

        MutableLiveData<AuditLogResponse> liveData =
                new MutableLiveData<>();

        apiService.getAuditLogs()
                .enqueue(new Callback<AuditLogResponse>() {
                    @Override
                    public void onResponse(Call<AuditLogResponse> call,
                                           Response<AuditLogResponse> response) {

                        liveData.setValue(response.isSuccessful()
                                ? response.body() : null);
                    }

                    @Override
                    public void onFailure(Call<AuditLogResponse> call,
                                          Throwable t) {
                        liveData.setValue(null);
                    }
                });

        return liveData;


    }


    // ======================================================
// ERASE CONSENT
// ======================================================

    public LiveData<Boolean> eraseConsent(String consentId) {

        MutableLiveData<Boolean> result = new MutableLiveData<>();

        apiService.eraseConsent(consentId)
                .enqueue(new Callback<Object>() {
                    @Override
                    public void onResponse(Call<Object> call,
                                           Response<Object> response) {
                        result.setValue(response.isSuccessful());
                    }

                    @Override
                    public void onFailure(Call<Object> call,
                                          Throwable t) {
                        result.setValue(false);
                    }
                });

        return result;
    }

    // ======================================================
    // KPIs
    // ======================================================
    public LiveData<Object> getKPIs() {

        MutableLiveData<Object> liveData = new MutableLiveData<>();

        apiService.getKPIs()
                .enqueue(new Callback<Object>() {
                    @Override
                    public void onResponse(Call<Object> call,
                                           Response<Object> response) {

                        liveData.setValue(response.isSuccessful()
                                ? response.body() : null);
                    }

                    @Override
                    public void onFailure(Call<Object> call,
                                          Throwable t) {
                        liveData.setValue(null);
                    }
                });

        return liveData;
    }

    // ======================================================
// AUDIT VERIFY
// ======================================================

    public LiveData<AuditVerifyResponse> verifyAuditLogs() {

        MutableLiveData<AuditVerifyResponse> liveData =
                new MutableLiveData<>();

        apiService.verifyAuditLogs()
                .enqueue(new Callback<AuditVerifyResponse>() {
                    @Override
                    public void onResponse(
                            Call<AuditVerifyResponse> call,
                            Response<AuditVerifyResponse> response) {

                        liveData.setValue(response.isSuccessful()
                                ? response.body() : null);
                    }

                    @Override
                    public void onFailure(
                            Call<AuditVerifyResponse> call,
                            Throwable t) {

                        liveData.setValue(null);
                    }
                });

        return liveData;
    }


    public LiveData<Boolean> submitFeedback(String packageName, int rating) {

        MutableLiveData<Boolean> result = new MutableLiveData<>();

        ApiService.FeedbackRequest request =
                new ApiService.FeedbackRequest(packageName, rating);

        apiService.submitFeedback(request)
                .enqueue(new Callback<Object>() {
                    @Override
                    public void onResponse(Call<Object> call,
                                           Response<Object> response) {
                        result.setValue(response.isSuccessful());
                    }

                    @Override
                    public void onFailure(Call<Object> call,
                                          Throwable t) {
                        result.setValue(false);
                    }
                });

        return result;
    }


}
