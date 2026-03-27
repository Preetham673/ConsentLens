package com.example.consentlens.network;

import com.example.consentlens.model.ComplianceReportResponse;
import com.example.consentlens.model.ConsentHistoryResponse;
import com.example.consentlens.model.ConsentReceiptResponse;
import com.example.consentlens.model.RiskResponse;
import com.example.consentlens.model.AuditVerifyResponse;
import com.example.consentlens.model.AuditLogResponse;
import com.example.consentlens.model.ConsentRequest;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.DELETE;

public interface ApiService {

    // ======================================================
    // REQUEST MODELS
    // ======================================================

    class AppRequest {
        public String packageName;

        public AppRequest(String packageName) {
            this.packageName = packageName;
        }
    }

    class ConsentRequestLegacy {
        public String packageName;
        public String riskLevel;
        public List<String> purpose;
        public List<String> dataCategories;

        public ConsentRequestLegacy(String packageName,
                                    String riskLevel,
                                    List<String> purpose,
                                    List<String> dataCategories) {

            this.packageName = packageName;
            this.riskLevel = riskLevel;
            this.purpose = purpose;
            this.dataCategories = dataCategories;
        }
    }

    class RevokePurposeRequest {
        public String consentId;
        public String purpose;

        public RevokePurposeRequest(String consentId, String purpose) {
            this.consentId = consentId;
            this.purpose = purpose;
        }
    }

    class FeedbackRequest {
        public String packageName;
        public int rating;

        public FeedbackRequest(String packageName, int rating) {
            this.packageName = packageName;
            this.rating = rating;
        }
    }
    class LoginRequest {

        public String username;
        public String role;

        public LoginRequest(String username, String role) {
            this.username = username;
            this.role = role;
        }
    }

    @POST("auth/login")
    Call<Map<String,String>> login(@Body LoginRequest request);
    // ======================================================
    // RISK ANALYSIS
    // ======================================================

    @POST("risk/analyze")
    Call<RiskResponse> analyzeApp(@Body AppRequest request);


    @POST("parent/send-email-otp")
    Call<Map<String,Object>> sendEmailOTP(@Body Map<String,String> body);

    @POST("parent/verify-email-otp")
    Call<Map<String,Object>> verifyEmailOTP(@Body Map<String,String> body);
    // ======================================================
    // GRANT CONSENT
    // ======================================================



    @POST("/api/parent/send-sms-otp")
    Call<Map<String,Object>> sendSMSOTP(@Body Map<String,String> body);

    @POST("/api/parent/verify-sms-otp")
    Call<Map<String,Object>> verifySMSOTP(@Body Map<String,String> body);

    @POST("/api/parent/send-approval-link")
    Call<Map<String,Object>> sendApprovalLink(@Body Map<String,String> body);
    @POST("consent/grant")
    Call<Object> grantConsent(@Body ConsentRequest request);

    // ======================================================
    // ADVANCED CONSENT
    // ======================================================

    @POST("consent/grant-advanced")
    Call<Object> grantConsentAdvanced(@Body ConsentRequest request);

    // ======================================================
    // REVOKE PURPOSE
    // ======================================================

    @POST("consent/revoke-purpose")
    Call<Object> revokePurpose(@Body RevokePurposeRequest request);

    // ======================================================
    // CONSENT HISTORY
    // ======================================================

    @POST("consent/history")
    Call<ConsentHistoryResponse> getConsentHistory(@Body AppRequest request);

    // ======================================================
    // ALL CONSENTS
    // ======================================================

    @GET("consent/all")
    Call<ConsentHistoryResponse> getAllConsentHistory();

    // ======================================================
    // CONSENT RECEIPT
    // ======================================================

    @GET("consent/receipt/{id}")
    Call<ConsentReceiptResponse> getReceipt(
            @Path("id") String consentId
    );


    @GET("compliance/dashboard")
    Call<Map<String, Object>> getComplianceDashboard();

    // ======================================================
    // COMPLIANCE REPORT
    // ======================================================

    @POST("compliance/report")
    Call<ComplianceReportResponse> getComplianceReport(@Body AppRequest request);

    // ======================================================
    // AUDIT LOGS
    // ======================================================

    @GET("audit/logs")
    Call<AuditLogResponse> getAuditLogs();

    // ======================================================
    // ERASE CONSENT
    // ======================================================

    @DELETE("consent/erase/{id}")
    Call<Object> eraseConsent(@Path("id") String consentId);

    // ======================================================
    // VERIFY AUDIT LEDGER
    // ======================================================

    @GET("audit/verify")
    Call<AuditVerifyResponse> verifyAuditLogs();

    // ======================================================
    // KPIs
    // ======================================================

    @GET("kpis")
    Call<Object> getKPIs();

    // ======================================================
    // FEEDBACK
    // ======================================================

    @POST("feedback")
    Call<Object> submitFeedback(@Body FeedbackRequest request);
}