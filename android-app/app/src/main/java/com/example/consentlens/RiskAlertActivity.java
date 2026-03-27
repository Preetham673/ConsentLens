package com.example.consentlens;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.view.ViewGroup;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import android.net.Uri;
import android.provider.Settings;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.example.consentlens.network.RetrofitClient;
import com.example.consentlens.network.ApiService;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.consentlens.model.RiskResponse;
import com.example.consentlens.viewmodel.RiskViewModel;
import com.example.consentlens.engine.DataCategoryClassifier;
import com.example.consentlens.engine.PurposeDriftDetector;
import com.example.consentlens.engine.DPIAGenerator;
import com.example.consentlens.engine.PrivacyMaturityIndex;
import com.example.consentlens.engine.RetentionCalculator;

// For chip group
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

public class RiskAlertActivity extends AppCompatActivity {

    // =============================================
    // HERO HEADER VIEWS
    // =============================================
    private TextView txtAppName;
    private TextView txtPackageSubtitle;
    private TextView txtRiskBadge;
    private TextView txtConfidenceHero;

    // =============================================
    // SECTION 1 — SAFETY OVERVIEW
    // =============================================
    private LinearLayout headerSection1;
    private LinearLayout bodySection1;
    private View divider1;
    private TextView chevron1;

    private TextView txtStatRiskScore;
    private TextView txtStatRiskLevel;
    private TextView txtStatConfidence;
    private ProgressBar progressRiskRing;
    private TextView txtDataImpact;
    private LinearLayout layoutSensitiveAlert;
    private LinearLayout layoutPurposeDriftAlert;
    private TextView txtReasons;
    private TextView txtPotentialRisks;

    // =============================================
    // SECTION 2 — YOUR DATA
    // =============================================
    private LinearLayout headerSection2;
    private LinearLayout bodySection2;
    private View divider2;
    private TextView chevron2;

    private ChipGroup chipGroupData;
    private TextView txtThirdParty;
    private TextView txtDatasetRef;
    private TextView txtViolations;
    private TextView txtLatency;
    private TextView txtAiRecommendation;
    private TextView txtDpiaReport;

    // =============================================
    // SECTION 3 — YOUR LEGAL RIGHTS
    // =============================================
    private LinearLayout headerSection3;
    private LinearLayout bodySection3;
    private View divider3;
    private TextView chevron3;

    private TextView txtGdprArticles;
    private TextView txtDpdpPrinciples;
    private TextView txtPolicyRisk;
    private TextView txtAiConfidence;
    private TextView txtExplanation;

    // =============================================
    // SECTION 4 — TRUST & CERTIFICATION
    // =============================================
    private LinearLayout headerSection4;
    private LinearLayout bodySection4;
    private View divider4;
    private TextView chevron4;

    private LinearLayout layoutCertBadge;
    private LinearLayout layoutNoCert;
    private LinearLayout layoutIsoNotCertified;   // NEW — shown when ISO 27001 missing
    private TextView txtCertStandard;
    private TextView txtCertAuthority;
    private TextView txtIsoStatus;                // NEW — shows ISO 27001 verified / not certified
    private LinearLayout layoutRetentionBanner;
    private TextView txtRetentionStatus;
    private TextView txtPmiScore;
    private TextView txtPmiLevel;

    // =============================================
    // SECTION 5 — YOUR DECISION
    // =============================================
    private TextView txtUserRecommendation;
    private Button btnGrant;
    private Button btnDeny;
    private Button btnHistory;

    // =============================================
    // STATE
    // =============================================
    private RiskViewModel viewModel;
    private String targetPackageName;
    private String riskLevel;
    private List<String> dataCategories;
    private String legalBasisText = "Unknown";

    // Track which sections are expanded
    private boolean section1Open = true;   // Safety overview open by default
    private boolean section2Open = false;
    private boolean section3Open = false;
    private boolean section4Open = false;


    // =============================================
    // LIFECYCLE
    // =============================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_risk_alert1);

        bindViews();
        setupSectionToggles();

        viewModel = new ViewModelProvider(this).get(RiskViewModel.class);

        targetPackageName = getIntent().getStringExtra("packageName");

        if (targetPackageName != null) {
            txtAppName.setText(formatAppName(targetPackageName));
            txtPackageSubtitle.setText(targetPackageName);
            analyzeApp(targetPackageName);
        } else {
            txtDataImpact.setText("No package received.");
        }

        // ==========================================
        // GRANT CONSENT — triggers age verification
        // ==========================================
        btnGrant.setOnClickListener(v -> {
            if (targetPackageName != null && riskLevel != null) {
                askUserAge();
            }
        });

        // ==========================================
        // DENY — opens app settings to uninstall
        // ==========================================
        btnDeny.setOnClickListener(v -> {
            if (targetPackageName == null) {
                Toast.makeText(this, "Package not found", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.parse("package:" + targetPackageName));
            startActivity(intent);
        });

        // ==========================================
        // HISTORY — opens consent history screen
        // ==========================================
        btnHistory.setOnClickListener(v -> {
            Intent intent = new Intent(this, ConsentHistoryActivity.class);
            intent.putExtra("packageName", targetPackageName);
            startActivity(intent);
        });

    } // ✅ onCreate properly closed


    // =============================================
    // BIND ALL VIEWS
    // =============================================

    private void bindViews() {

        // Hero header
        txtAppName         = findViewById(R.id.txtAppName);
        txtPackageSubtitle = findViewById(R.id.txtPackageSubtitle);
        txtRiskBadge       = findViewById(R.id.txtRiskBadge);
        txtConfidenceHero  = findViewById(R.id.txtConfidenceHero);

        // Section 1
        headerSection1         = findViewById(R.id.headerSection1);
        bodySection1           = findViewById(R.id.bodySection1);
        divider1               = findViewById(R.id.divider1);
        chevron1               = findViewById(R.id.chevron1);
        txtStatRiskScore       = findViewById(R.id.txtStatRiskScore);
        txtStatRiskLevel       = findViewById(R.id.txtStatRiskLevel);
        txtStatConfidence      = findViewById(R.id.txtStatConfidence);
        progressRiskRing       = findViewById(R.id.progressRiskBar);
        txtDataImpact          = findViewById(R.id.txtDataImpact);
        layoutSensitiveAlert   = findViewById(R.id.layoutSensitiveAlert);
        layoutPurposeDriftAlert= findViewById(R.id.layoutPurposeDriftAlert);
        txtReasons             = findViewById(R.id.txtReasons);
        txtPotentialRisks      = findViewById(R.id.txtPotentialRisks);

        // Section 2
        headerSection2    = findViewById(R.id.headerSection2);
        bodySection2      = findViewById(R.id.bodySection2);
        divider2          = findViewById(R.id.divider2);
        chevron2          = findViewById(R.id.chevron2);
        chipGroupData     = findViewById(R.id.chipGroupData);
        txtThirdParty     = findViewById(R.id.txtThirdParty);
        txtDatasetRef     = findViewById(R.id.txtDatasetRef);
        txtViolations     = findViewById(R.id.txtViolations);
        txtLatency        = findViewById(R.id.txtLatency);
        txtAiRecommendation= findViewById(R.id.txtAiRecommendation);
        txtDpiaReport     = findViewById(R.id.txtDpiaReport);

        // Section 3
        headerSection3   = findViewById(R.id.headerSection3);
        bodySection3     = findViewById(R.id.bodySection3);
        divider3         = findViewById(R.id.divider3);
        chevron3         = findViewById(R.id.chevron3);
        txtGdprArticles  = findViewById(R.id.txtGdprArticles);
        txtDpdpPrinciples= findViewById(R.id.txtDpdpPrinciples);
        txtPolicyRisk    = findViewById(R.id.txtPolicyRisk);
        txtAiConfidence  = findViewById(R.id.txtAiConfidence);
        txtExplanation   = findViewById(R.id.txtExplanation);

        // Section 4
        headerSection4       = findViewById(R.id.headerSection4);
        bodySection4         = findViewById(R.id.bodySection4);
        divider4             = findViewById(R.id.divider4);
        chevron4             = findViewById(R.id.chevron4);
        layoutCertBadge          = findViewById(R.id.layoutCertBadge);
        layoutNoCert             = findViewById(R.id.layoutNoCert);
        layoutIsoNotCertified    = findViewById(R.id.layoutIsoNotCertified);
        txtCertStandard          = findViewById(R.id.txtCertStandard);
        txtCertAuthority         = findViewById(R.id.txtCertAuthority);
        txtIsoStatus             = findViewById(R.id.txtIsoStatus);
        layoutRetentionBanner    = findViewById(R.id.layoutRetentionBanner);
        txtRetentionStatus   = findViewById(R.id.txtRetentionStatus);
        txtPmiScore          = findViewById(R.id.txtPmiScore);
        txtPmiLevel          = findViewById(R.id.txtPmiLevel);

        // Section 5
        txtUserRecommendation = findViewById(R.id.txtUserRecommendation);
        btnGrant              = findViewById(R.id.btnGrant);
        btnDeny               = findViewById(R.id.btnDeny);
        btnHistory            = findViewById(R.id.btnHistory);
    }


    // =============================================
    // SECTION TOGGLE LOGIC
    // =============================================

    private void setupSectionToggles() {

        // Section 1 default open
        applySectionState(bodySection1, divider1, chevron1, section1Open);

        headerSection1.setOnClickListener(v -> {
            section1Open = !section1Open;
            applySectionState(bodySection1, divider1, chevron1, section1Open);
        });

        headerSection2.setOnClickListener(v -> {
            section2Open = !section2Open;
            applySectionState(bodySection2, divider2, chevron2, section2Open);
        });

        headerSection3.setOnClickListener(v -> {
            section3Open = !section3Open;
            applySectionState(bodySection3, divider3, chevron3, section3Open);
        });

        headerSection4.setOnClickListener(v -> {
            section4Open = !section4Open;
            applySectionState(bodySection4, divider4, chevron4, section4Open);
        });
    }

    private void applySectionState(LinearLayout body, View divider,
                                   TextView chevron, boolean open) {
        body.setVisibility(open ? View.VISIBLE : View.GONE);
        divider.setVisibility(open ? View.VISIBLE : View.GONE);
        chevron.setRotation(open ? 90f : 0f);
    }


    // =============================================
    // HELPER — format package name to readable title
    // =============================================

    private String formatAppName(String pkg) {
        String[] parts = pkg.split("\\.");
        if (parts.length > 0) {
            String last = parts[parts.length - 1];
            return Character.toUpperCase(last.charAt(0)) + last.substring(1);
        }
        return pkg;
    }


    /* ==========================================
       AGE VERIFICATION
       (DPDP + GDPR CHILD CONSENT COMPLIANCE)
    ========================================== */

    private void askUserAge() {

        android.view.View dialogView = getLayoutInflater()
                .inflate(R.layout.dialog_age_verification, null);

        EditText etAge = dialogView.findViewById(R.id.etAge);

        android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.setContentView(dialogView);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.getWindow().setLayout(
                (int) (getResources().getDisplayMetrics().widthPixels * 0.9),
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(true);

        dialogView.findViewById(R.id.btnAgeCancel)
                .setOnClickListener(v -> dialog.dismiss());

        dialogView.findViewById(R.id.btnAgeSubmit)
                .setOnClickListener(v -> {
                    String ageStr = etAge.getText().toString().trim();
                    if (ageStr.isEmpty()) {
                        Toast.makeText(this, "Please enter your age",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    int age = Integer.parseInt(ageStr);
                    dialog.dismiss();
                    if (age < 18) {
                        askParentConsent();
                    } else {
                        executeOriginalGrantLogic();
                    }
                });

        dialog.show();
    }


    /* ====================================================
       ASK PARENT CONTACT (EMAIL OR WHATSAPP)
    ==================================================== */

    private void askParentConsent() {

        android.view.View dialogView = getLayoutInflater()
                .inflate(R.layout.dialog_guardian_consent, null);

        android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.setContentView(dialogView);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.getWindow().setLayout(
                (int) (getResources().getDisplayMetrics().widthPixels * 0.9),
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(true);

        dialogView.findViewById(R.id.cardEmailOtp)
                .setOnClickListener(v -> {
                    dialog.dismiss();
                    askParentEmail();
                });

        dialogView.findViewById(R.id.cardWhatsappOtp)
                .setOnClickListener(v -> {
                    dialog.dismiss();
                    askParentPhone();
                });

        dialogView.findViewById(R.id.btnGuardianCancel)
                .setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }


    /* ====================================================
       ASK PARENT EMAIL FOR CONSENT
    ==================================================== */

    private void askParentEmail() {

        android.view.View v = getLayoutInflater().inflate(R.layout.dialog_input, null);

        ((TextView) v.findViewById(R.id.txtDialogIcon)).setText("📧");
        ((TextView) v.findViewById(R.id.txtDialogTitle)).setText("Parent Email");
        ((TextView) v.findViewById(R.id.txtDialogSubtitle)).setText("Enter your parent or guardian's email. An OTP will be sent for verification.");
        ((TextView) v.findViewById(R.id.txtInputLabel)).setText("Parent Email Address");
        ((TextView) v.findViewById(R.id.txtInputIcon)).setText("📧");
        ((com.google.android.material.card.MaterialCardView) v.findViewById(R.id.cardDialogIcon)).setCardBackgroundColor(0xFFDCFCE7);
        ((Button) v.findViewById(R.id.btnDialogSubmit)).setText("Send OTP");

        EditText etInput = v.findViewById(R.id.etInput);
        etInput.setHint("parent@example.com");
        etInput.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS | android.text.InputType.TYPE_CLASS_TEXT);

        android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.setContentView(v);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.getWindow().setLayout((int)(getResources().getDisplayMetrics().widthPixels * 0.9), ViewGroup.LayoutParams.WRAP_CONTENT);

        v.findViewById(R.id.btnDialogCancel).setOnClickListener(x -> dialog.dismiss());
        v.findViewById(R.id.btnDialogSubmit).setOnClickListener(x -> {
            String email = etInput.getText().toString().trim();
            if (email.isEmpty()) { Toast.makeText(this, "Email required", Toast.LENGTH_SHORT).show(); return; }
            dialog.dismiss();
            sendEmailOTP(email);
        });
        dialog.show();
    }


    /* ====================================================
       SEND EMAIL OTP TO BACKEND
    ==================================================== */

    private void sendEmailOTP(String parentEmail) {

        Map<String, String> body = new HashMap<>();
        body.put("parentEmail", parentEmail);

        ApiService api = RetrofitClient.getClient().create(ApiService.class);

        api.sendEmailOTP(body).enqueue(new Callback<Map<String, Object>>() {

            @Override
            public void onResponse(Call<Map<String, Object>> call,
                                   Response<Map<String, Object>> response) {

                System.out.println("EMAIL OTP RESPONSE CODE: " + response.code());
                System.out.println("EMAIL OTP RESPONSE BODY: " + response.body());

                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(RiskAlertActivity.this,
                            "OTP sent to parent email", Toast.LENGTH_LONG).show();
                    showOTPDialog(parentEmail);
                } else {
                    Toast.makeText(RiskAlertActivity.this,
                            "Failed to send OTP: " + response.code(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                t.printStackTrace();
                Toast.makeText(RiskAlertActivity.this,
                        "OTP request failed: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }


    /* ====================================================
       OTP INPUT DIALOG (EMAIL)
    ==================================================== */

    private void showOTPDialog(String parentEmail) {

        android.view.View v = getLayoutInflater().inflate(R.layout.dialog_otp_verify, null);

        ((TextView) v.findViewById(R.id.txtOtpTitle)).setText("Email OTP Verification");
        ((TextView) v.findViewById(R.id.txtOtpSubtitle)).setText("Check parent's email inbox for the 6-digit code");
        ((TextView) v.findViewById(R.id.txtOtpSentTo)).setText("OTP sent to: " + parentEmail);

        EditText etOtp = v.findViewById(R.id.etOtp);

        android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.setContentView(v);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.getWindow().setLayout((int)(getResources().getDisplayMetrics().widthPixels * 0.9), ViewGroup.LayoutParams.WRAP_CONTENT);

        v.findViewById(R.id.btnOtpCancel).setOnClickListener(x -> dialog.dismiss());
        v.findViewById(R.id.btnOtpVerify).setOnClickListener(x -> {
            String otp = etOtp.getText().toString().trim();
            if (otp.isEmpty()) { Toast.makeText(this, "OTP required", Toast.LENGTH_SHORT).show(); return; }
            dialog.dismiss();
            verifyEmailOTP(parentEmail, otp);
        });
        dialog.show();
    }


    /* ====================================================
       VERIFY EMAIL OTP WITH BACKEND
    ==================================================== */

    private void verifyEmailOTP(String parentEmail, String otp) {

        Map<String, String> body = new HashMap<>();
        body.put("parentEmail", parentEmail);
        body.put("otp", otp);

        ApiService api = RetrofitClient.getClient().create(ApiService.class);

        api.verifyEmailOTP(body).enqueue(new Callback<Map<String, Object>>() {

            @Override
            public void onResponse(Call<Map<String, Object>> call,
                                   Response<Map<String, Object>> response) {

                if (response.body() != null &&
                        Boolean.TRUE.equals(response.body().get("approved"))) {
                    Toast.makeText(RiskAlertActivity.this,
                            "Parent verified successfully", Toast.LENGTH_SHORT).show();
                    executeOriginalGrantLogic();
                } else {
                    Toast.makeText(RiskAlertActivity.this,
                            "OTP verification failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(RiskAlertActivity.this,
                        "Verification error", Toast.LENGTH_SHORT).show();
            }
        });
    }


    /* ====================================================
       ASK PARENT WHATSAPP NUMBER
    ==================================================== */

    private void askParentPhone() {

        android.view.View v = getLayoutInflater().inflate(R.layout.dialog_input, null);

        ((TextView) v.findViewById(R.id.txtDialogIcon)).setText("💬");
        ((TextView) v.findViewById(R.id.txtDialogTitle)).setText("WhatsApp Verification");
        ((TextView) v.findViewById(R.id.txtDialogSubtitle)).setText("Enter parent's WhatsApp number with country code");
        ((TextView) v.findViewById(R.id.txtInputLabel)).setText("WhatsApp Number");
        ((TextView) v.findViewById(R.id.txtInputIcon)).setText("📱");
        ((Button) v.findViewById(R.id.btnDialogSubmit)).setText("Send OTP");

        TextView helper = v.findViewById(R.id.txtInputHelper);
        helper.setText("India example: 919876543210  (91 + 10 digit number, no + sign)");
        helper.setVisibility(android.view.View.VISIBLE);

        EditText etInput = v.findViewById(R.id.etInput);
        etInput.setHint("e.g. 919876543210");
        etInput.setInputType(android.text.InputType.TYPE_CLASS_PHONE);

        android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.setContentView(v);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.getWindow().setLayout((int)(getResources().getDisplayMetrics().widthPixels * 0.9), ViewGroup.LayoutParams.WRAP_CONTENT);

        v.findViewById(R.id.btnDialogCancel).setOnClickListener(x -> dialog.dismiss());
        v.findViewById(R.id.btnDialogSubmit).setOnClickListener(x -> {
            String phone = etInput.getText().toString().trim().replaceAll("[\\s\\-\\+]", "");
            if (phone.isEmpty()) { Toast.makeText(this, "Phone number required", Toast.LENGTH_SHORT).show(); return; }
            dialog.dismiss();
            sendWhatsAppOTP(phone);
        });
        dialog.show();
    }


    /* ====================================================
       SEND WHATSAPP OTP
    ==================================================== */

    private void sendWhatsAppOTP(String phone) {

        Map<String, String> body = new HashMap<>();
        body.put("phone", phone);

        ApiService api = RetrofitClient.getClient().create(ApiService.class);

        Toast.makeText(this, "Sending OTP via WhatsApp...", Toast.LENGTH_SHORT).show();

        api.sendSMSOTP(body).enqueue(new Callback<Map<String, Object>>() {

            @Override
            public void onResponse(Call<Map<String, Object>> call,
                                   Response<Map<String, Object>> response) {

                if (response.isSuccessful()) {
                    Toast.makeText(RiskAlertActivity.this,
                            "✅ OTP sent to parent's WhatsApp",
                            Toast.LENGTH_LONG).show();
                    showWhatsAppOTPDialog(phone);
                } else {
                    Toast.makeText(RiskAlertActivity.this,
                            "Failed to send WhatsApp OTP. Check phone number.",
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(RiskAlertActivity.this,
                        "Network error: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }


    /* ====================================================
       WHATSAPP OTP INPUT DIALOG
    ==================================================== */

    private void showWhatsAppOTPDialog(String phone) {

        android.view.View v = getLayoutInflater().inflate(R.layout.dialog_otp_verify, null);

        ((TextView) v.findViewById(R.id.txtOtpTitle)).setText("WhatsApp OTP Verification");
        ((TextView) v.findViewById(R.id.txtOtpSubtitle)).setText("Enter the 6-digit code sent to parent's WhatsApp");
        String last4 = phone.length() >= 4 ? phone.substring(phone.length() - 4) : phone;
        ((TextView) v.findViewById(R.id.txtOtpSentTo)).setText("OTP sent to WhatsApp number ending in ..." + last4);

        EditText etOtp = v.findViewById(R.id.etOtp);

        android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.setContentView(v);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.getWindow().setLayout((int)(getResources().getDisplayMetrics().widthPixels * 0.9), ViewGroup.LayoutParams.WRAP_CONTENT);

        v.findViewById(R.id.btnOtpCancel).setOnClickListener(x -> dialog.dismiss());
        v.findViewById(R.id.btnOtpVerify).setOnClickListener(x -> {
            String otp = etOtp.getText().toString().trim();
            if (otp.isEmpty()) { Toast.makeText(this, "OTP required", Toast.LENGTH_SHORT).show(); return; }
            dialog.dismiss();
            verifyWhatsAppOTP(phone, otp);
        });
        dialog.show();
    }


    /* ====================================================
       VERIFY WHATSAPP OTP
    ==================================================== */

    private void verifyWhatsAppOTP(String phone, String otp) {

        Map<String, String> body = new HashMap<>();
        body.put("phone", phone);
        body.put("otp", otp);

        ApiService api = RetrofitClient.getClient().create(ApiService.class);

        api.verifySMSOTP(body).enqueue(new Callback<Map<String, Object>>() {

            @Override
            public void onResponse(Call<Map<String, Object>> call,
                                   Response<Map<String, Object>> response) {

                if (response.body() != null &&
                        Boolean.TRUE.equals(response.body().get("approved"))) {
                    Toast.makeText(RiskAlertActivity.this,
                            "✅ Parent verified successfully",
                            Toast.LENGTH_SHORT).show();
                    executeOriginalGrantLogic();
                } else {
                    Toast.makeText(RiskAlertActivity.this,
                            "❌ Wrong OTP. Please try again.",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(RiskAlertActivity.this,
                        "Network error. Check connection.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }


    /* ==========================================
       MINOR BLOCK POPUP
    ========================================== */

    private void showMinorBlockDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Child Protection")
                .setMessage("Users under 18 cannot grant consent.\nGuardian approval required.")
                .setPositiveButton("OK", null)
                .show();
    }


    /* ==========================================
       ORIGINAL CONSENT LOGIC
    ========================================== */

    private void executeOriginalGrantLogic() {

        viewModel.grantConsent(targetPackageName, riskLevel, dataCategories)
                .observe(this, success -> {

                    if (success != null && success) {

                        Toast.makeText(this, "Consent saved successfully",
                                Toast.LENGTH_SHORT).show();

                        showRatingDialog(targetPackageName);

                        Intent graphIntent = new Intent(this, ConsentGraphActivity.class);
                        graphIntent.putExtra("app", targetPackageName);

                        if (dataCategories != null) {
                            graphIntent.putExtra("data", dataCategories.toString());
                        } else {
                            graphIntent.putExtra("data", "Unknown Data");
                        }

                        graphIntent.putExtra("legal", legalBasisText);
                        startActivity(graphIntent);

                    } else {
                        Toast.makeText(this, "Failed to save consent",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }


    /* ==========================================
       ANALYZE APP
    ========================================== */

    private void analyzeApp(String packageName) {
        txtDataImpact.setText("Analyzing privacy risk...");
        viewModel.analyzeApp(packageName).observe(this, this::displayResult);
    }


    /* ==========================================
       DISPLAY RESULT — populates all 5 sections
    ========================================== */

    private void displayResult(RiskResponse response) {

        System.out.println("RAW RESPONSE RISK: " + response.risk);
        System.out.println("RAW CERTIFICATION: " + response.risk.certification);

        if (response == null || response.risk == null || response.privacyProfile == null) {
            txtDataImpact.setText("No risk data available.");
            return;
        }

        riskLevel      = response.risk.riskLevel;
        dataCategories = response.privacyProfile.dataCategories;

        // Send risk result back to AppListActivity
        android.content.Intent resultIntent = new android.content.Intent();
        resultIntent.putExtra("packageName", targetPackageName);
        resultIntent.putExtra("riskLevel", riskLevel != null ? riskLevel.toUpperCase() : "UNKNOWN");
        setResult(RESULT_OK, resultIntent);

        // ---- Build legalBasisText (unchanged from original) ----
        if (response.risk.legalMapping != null) {
            String gdpr = "";
            String dpdp = "";
            if (response.risk.legalMapping.gdprArticles != null &&
                    !response.risk.legalMapping.gdprArticles.isEmpty()) {
                gdpr = "GDPR: " + response.risk.legalMapping.gdprArticles.toString();
            }
            if (response.risk.legalMapping.dpdpPrinciples != null &&
                    !response.risk.legalMapping.dpdpPrinciples.isEmpty()) {
                dpdp = "DPDP: " + response.risk.legalMapping.dpdpPrinciples.toString();
            }
            legalBasisText = gdpr + "\n" + dpdp;
        }

        // ---- Engine calls (all preserved) ----
        boolean sensitiveDetected =
                DataCategoryClassifier.containsSensitiveData(dataCategories);

        String dpiaReport = DPIAGenerator.generateDPIA(
                response.risk.riskScore,
                sensitiveDetected,
                response.privacyProfile.thirdPartySharing,
                dataCategories
        );

        String currentPurpose = "Advertising";
        boolean purposeDrift = PurposeDriftDetector.detectDrift(
                java.util.Arrays.asList("General Processing"),
                currentPurpose
        );

        int pmiScore = PrivacyMaturityIndex.calculatePMI(
                true,
                true,
                sensitiveDetected,
                dpiaReport != null,
                response.risk.legalMapping != null
        );
        String pmiLevel = PrivacyMaturityIndex.getComplianceLevel(pmiScore);

        String expiryStatus =
                RetentionCalculator.getRemainingDays("2025-12-31T23:59:59");


        // ======================================================
        // SECTION 1 — SAFETY OVERVIEW
        // ======================================================

        // Hero badge
        String riskLevelDisplay = response.risk.riskLevel != null ?
                response.risk.riskLevel.toUpperCase() : "UNKNOWN";
        txtRiskBadge.setText(riskLevelDisplay + " RISK");
        txtConfidenceHero.setText(
                response.risk.confidenceLevel + " confidence · Score " +
                        response.risk.riskScore + "/100");
        applyRiskColorToHeroBadge(response.risk.riskLevel);

        // Stat cards
        txtStatRiskScore.setText(String.valueOf(response.risk.riskScore));
        txtStatRiskLevel.setText(response.risk.riskLevel != null ?
                response.risk.riskLevel : "—");
        txtStatConfidence.setText(response.risk.confidenceLevel != null ?
                abbreviateConfidence(response.risk.confidenceLevel) : "—");

        applyRiskColorToText(txtStatRiskScore, response.risk.riskLevel);
        applyRiskColorToText(txtStatRiskLevel, response.risk.riskLevel);

        // Also update the hero mini stat duplicate
        TextView txtStatRiskScore2 = findViewById(R.id.txtStatRiskScore2);
        if (txtStatRiskScore2 != null) {
            txtStatRiskScore2.setText(String.valueOf(response.risk.riskScore));
            applyRiskColorToText(txtStatRiskScore2, response.risk.riskLevel);
        }

        // Also update latency in section 2 duplicate
        TextView txtLatency2 = findViewById(R.id.txtLatency2);
        if (txtLatency2 != null) {
            txtLatency2.setText(response.processingLatencyMs + " ms");
        }

        // Progress ring
        progressRiskRing.setProgress(response.risk.riskScore);

        // App name display (formatted)
        txtAppName.setText(
                response.privacyProfile.appName != null ?
                        response.privacyProfile.appName :
                        formatAppName(targetPackageName));
        txtPackageSubtitle.setText(targetPackageName);

        // Data impact
        txtDataImpact.setText(response.risk.dataImpactSummary != null ?
                response.risk.dataImpactSummary : "No data impact info available.");

        // Sensitive data alert
        if (sensitiveDetected) {
            layoutSensitiveAlert.setVisibility(View.VISIBLE);
        }

        // Purpose drift alert
        if (purposeDrift) {
            layoutPurposeDriftAlert.setVisibility(View.VISIBLE);
        }

        // Reasons
        if (response.risk.reasons != null && !response.risk.reasons.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (String r : response.risk.reasons) {
                sb.append("• ").append(r).append("\n");
            }
            txtReasons.setText(sb.toString().trim());
        } else {
            txtReasons.setText("No specific reasons listed.");
        }

        // Potential risks
        if (response.risk.potentialRisks != null && !response.risk.potentialRisks.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (String r : response.risk.potentialRisks) {
                sb.append("• ").append(r).append("\n");
            }
            txtPotentialRisks.setText(sb.toString().trim());
        } else {
            txtPotentialRisks.setText("No significant risks identified.");
        }


        // ======================================================
        // SECTION 2 — YOUR DATA
        // ======================================================

        // Data category chips
        chipGroupData.removeAllViews();
        if (dataCategories != null) {
            for (String cat : dataCategories) {
                Chip chip = new Chip(this);
                chip.setText(cat);
                chip.setChipBackgroundColorResource(R.color.chip_bg);
                chip.setTextColor(getResources().getColor(R.color.chip_text, null));
                chip.setClickable(false);
                chipGroupData.addView(chip);
            }
        }

        // Third party sharing
        boolean thirdParty = response.privacyProfile.thirdPartySharing;
        txtThirdParty.setText(thirdParty ? "Yes" : "No");
        txtThirdParty.setTextColor(getResources().getColor(
                thirdParty ? R.color.risk_high : R.color.risk_low, null));

        // Dataset reference
        txtDatasetRef.setText(response.risk.datasetReference != null ?
                response.risk.datasetReference : "N/A");

        // Violations
        int violations = response.risk.regulatoryViolationsFound;
        txtViolations.setText(violations == 0 ? "None" : String.valueOf(violations));
        txtViolations.setTextColor(getResources().getColor(
                violations == 0 ? R.color.risk_low : R.color.risk_high, null));

        // Latency
        txtLatency.setText(response.processingLatencyMs + " ms");

        // AI recommendation
        if (response.recommendations != null && !response.recommendations.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (String rec : response.recommendations) {
                sb.append("• ").append(rec).append("\n");
            }
            txtAiRecommendation.setText(sb.toString().trim());
        } else {
            txtAiRecommendation.setText("No AI recommendations available.");
        }

        // DPIA report
        txtDpiaReport.setText(dpiaReport != null ? dpiaReport : "DPIA not required.");


        // ======================================================
        // SECTION 3 — YOUR LEGAL RIGHTS
        // ======================================================

        // GDPR articles
        if (response.risk.legalMapping != null &&
                response.risk.legalMapping.gdprArticles != null) {
            StringBuilder sb = new StringBuilder();
            for (String a : response.risk.legalMapping.gdprArticles) {
                sb.append("• ").append(a).append("\n");
            }
            txtGdprArticles.setText(sb.toString().trim());
        } else {
            txtGdprArticles.setText("No GDPR articles mapped.");
        }

        // DPDP principles
        if (response.risk.legalMapping != null &&
                response.risk.legalMapping.dpdpPrinciples != null) {
            StringBuilder sb = new StringBuilder();
            for (String p : response.risk.legalMapping.dpdpPrinciples) {
                sb.append("• ").append(p).append("\n");
            }
            txtDpdpPrinciples.setText(sb.toString().trim());
        } else {
            txtDpdpPrinciples.setText("No DPDP principles mapped.");
        }

        // AI Policy risk
        if (response.aiPolicyRisk != null) {
            txtPolicyRisk.setText(response.aiPolicyRisk.policyRisk != null ?
                    response.aiPolicyRisk.policyRisk : "—");
            txtAiConfidence.setText(String.format("%.0f%%",
                    response.aiPolicyRisk.confidence * 100));
        } else {
            txtPolicyRisk.setText("Unavailable");
            txtAiConfidence.setText("—");
        }

        // Explanation bullets
        if (response.risk.explanation != null && !response.risk.explanation.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (String exp : response.risk.explanation) {
                sb.append("• ").append(exp).append("\n");
            }
            txtExplanation.setText(sb.toString().trim());
        } else {
            txtExplanation.setText("No explanation provided.");
        }


        // ======================================================
        // SECTION 4 — TRUST & CERTIFICATION
        // ======================================================

        /*
         * ISO 27001 has 3 states:
         *
         *  STATE 1 — CERTIFIED
         *    backend returned certification object
         *    AND certified = true
         *    AND standard contains "27001"
         *    → show green badge + green status row
         *
         *  STATE 2 — OTHER CERT / EXPLICITLY NOT ISO 27001
         *    backend returned certification object
         *    AND certified = true
         *    BUT standard does NOT contain "27001"
         *    → show that cert badge + amber "not ISO 27001" info row
         *
         *  STATE 3 — NO CERT DATA (backend returned null or certified=false)
         *    → show neutral "Certification data not available" row
         *    → DO NOT show a red warning — we simply don't know
         */

        boolean certDataExists = response.risk.certification != null
                && response.risk.certification.certified;

        boolean isIso27001Certified = false;
        boolean isOtherCertified    = false;

        if (certDataExists) {
            String standard = response.risk.certification.standard;
            if (standard != null &&
                    (standard.equalsIgnoreCase("ISO27001") ||
                            standard.equalsIgnoreCase("ISO 27001") ||
                            standard.contains("27001"))) {
                isIso27001Certified = true;
            } else {
                isOtherCertified = true;
            }
        }

        if (isIso27001Certified) {

            // STATE 1 — ISO 27001 verified ✅
            layoutCertBadge.setVisibility(View.VISIBLE);
            layoutNoCert.setVisibility(View.GONE);
            layoutIsoNotCertified.setVisibility(View.GONE);

            txtCertStandard.setText("ISO 27001 Certified");
            txtCertAuthority.setText(
                    "Standard: " + response.risk.certification.standard
                            + " · Verified · Risk reduced");

            txtIsoStatus.setText("✅  ISO 27001 — VERIFIED");
            txtIsoStatus.setTextColor(
                    getResources().getColor(R.color.risk_low, null));
            txtIsoStatus.setBackgroundResource(R.drawable.bg_cert_green);

        } else if (isOtherCertified) {

            // STATE 2 — certified under a different standard
            layoutCertBadge.setVisibility(View.VISIBLE);
            layoutNoCert.setVisibility(View.GONE);
            layoutIsoNotCertified.setVisibility(View.VISIBLE);

            txtCertStandard.setText(
                    response.risk.certification.standard + " Certified");
            txtCertAuthority.setText("Verified · Risk reduced");

            txtIsoStatus.setText(
                    "ℹ️  ISO 27001 — Not checked for this app");
            txtIsoStatus.setTextColor(
                    getResources().getColor(R.color.risk_medium, null));
            txtIsoStatus.setBackgroundResource(R.drawable.bg_stat_card);

        } else {

            // STATE 3 — no certification data from backend at all
            layoutCertBadge.setVisibility(View.GONE);
            layoutNoCert.setVisibility(View.VISIBLE);
            layoutIsoNotCertified.setVisibility(View.GONE);

            txtIsoStatus.setText(
                    "ℹ️  ISO 27001 — Certification data not available");
            txtIsoStatus.setTextColor(
                    getResources().getColor(R.color.text_secondary, null));
            txtIsoStatus.setBackgroundResource(R.drawable.bg_stat_card);
        }

        // Consent retention
        txtRetentionStatus.setText(expiryStatus);

        // PMI
        txtPmiScore.setText(String.valueOf(pmiScore));
        txtPmiLevel.setText(pmiLevel + " compliance level");


        // ======================================================
        // SECTION 5 — YOUR DECISION
        // ======================================================

        txtUserRecommendation.setText(
                response.risk.userRecommendation != null ?
                        response.risk.userRecommendation :
                        "Review permissions and stay informed before making a choice.");

        // Update grant button colour based on risk level
        applyRiskColorToGrantButton(response.risk.riskLevel);
    }


    /* ==========================================
       COLOUR HELPERS
    ========================================== */

    private void applyRiskColorToHeroBadge(String level) {
        // Tint is handled via drawable backgrounds set dynamically if needed
        // The badge text alone changes here
        if (level == null) return;
        switch (level.toUpperCase()) {
            case "HIGH":
                txtRiskBadge.setTextColor(getResources().getColor(R.color.risk_high, null));
                break;
            case "MEDIUM":
                txtRiskBadge.setTextColor(getResources().getColor(R.color.risk_medium, null));
                break;
            default:
                txtRiskBadge.setTextColor(getResources().getColor(R.color.risk_low, null));
        }
    }

    private void applyRiskColorToText(TextView tv, String level) {
        if (level == null) return;
        switch (level.toUpperCase()) {
            case "HIGH":
                tv.setTextColor(getResources().getColor(R.color.risk_high, null));
                break;
            case "MEDIUM":
                tv.setTextColor(getResources().getColor(R.color.risk_medium, null));
                break;
            default:
                tv.setTextColor(getResources().getColor(R.color.risk_low, null));
        }
    }

    private void applyRiskColorToGrantButton(String level) {
        if (level == null) return;
        int color;
        switch (level.toUpperCase()) {
            case "HIGH":
                color = getResources().getColor(R.color.risk_medium, null);
                break;
            default:
                color = getResources().getColor(R.color.risk_low, null);
        }
        btnGrant.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(color));
    }

    private String abbreviateConfidence(String confidence) {
        if (confidence == null) return "—";
        switch (confidence.toUpperCase()) {
            case "HIGH":   return "HIGH";
            case "MEDIUM": return "MED";
            case "LOW":    return "LOW";
            default:       return confidence.length() > 4 ?
                    confidence.substring(0, 4).toUpperCase() : confidence.toUpperCase();
        }
    }


    /* ==========================================
       RATING DIALOG
    ========================================== */

    private void showRatingDialog(String packageName) {

        String[] ratings = {"1 ⭐", "2 ⭐", "3 ⭐", "4 ⭐", "5 ⭐"};

        new AlertDialog.Builder(this)
                .setTitle("Rate Consent Experience")
                .setItems(ratings, (dialog, which) -> {

                    int rating = which + 1;

                    viewModel.submitFeedback(packageName, rating)
                            .observe(this, result -> {
                                Toast.makeText(this,
                                        "Thanks for your feedback!",
                                        Toast.LENGTH_SHORT).show();
                                finish();
                            });
                })
                .show();
    }
}