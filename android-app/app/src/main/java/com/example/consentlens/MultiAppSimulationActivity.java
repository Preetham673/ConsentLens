package com.example.consentlens;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.consentlens.network.ApiService;
import com.example.consentlens.network.RetrofitClient;
import com.example.consentlens.model.RiskResponse;
import com.google.android.material.card.MaterialCardView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MultiAppSimulationActivity extends AppCompatActivity {

    // Status bar
    TextView txtStatus;
    TextView txtScanCount;
    int scanCount = 0;

    // Fintech
    Button   btnFintech, btnFintechConsent;
    TextView txtFintechRisk, txtFintechResult;
    LinearLayout panelFintechResult;

    // Health
    Button   btnHealth, btnHealthConsent;
    TextView txtHealthRisk, txtHealthResult;
    LinearLayout panelHealthResult;

    // Ecommerce
    Button   btnEcommerce, btnEcommerceConsent;
    TextView txtEcommerceRisk, txtEcommerceResult;
    LinearLayout panelEcommerceResult;

    // Summary
    MaterialCardView cardSummary;
    TextView txtSummary;

    // Track results
    String fintechRiskLevel   = null;
    String healthRiskLevel    = null;
    String ecommerceRiskLevel = null;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_app_simulation);

        MaterialCardView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        txtStatus    = findViewById(R.id.txtStatus);
        txtScanCount = findViewById(R.id.txtScanCount);

        btnFintech        = findViewById(R.id.btnFintech);
        btnFintechConsent = findViewById(R.id.btnFintechConsent);
        txtFintechRisk    = findViewById(R.id.txtFintechRisk);
        txtFintechResult  = findViewById(R.id.txtFintechResult);
        panelFintechResult= findViewById(R.id.panelFintechResult);

        btnHealth        = findViewById(R.id.btnHealth);
        btnHealthConsent = findViewById(R.id.btnHealthConsent);
        txtHealthRisk    = findViewById(R.id.txtHealthRisk);
        txtHealthResult  = findViewById(R.id.txtHealthResult);
        panelHealthResult= findViewById(R.id.panelHealthResult);

        btnEcommerce        = findViewById(R.id.btnEcommerce);
        btnEcommerceConsent = findViewById(R.id.btnEcommerceConsent);
        txtEcommerceRisk    = findViewById(R.id.txtEcommerceRisk);
        txtEcommerceResult  = findViewById(R.id.txtEcommerceResult);
        panelEcommerceResult= findViewById(R.id.panelEcommerceResult);

        cardSummary = findViewById(R.id.cardSummary);
        txtSummary  = findViewById(R.id.txtSummary);

        btnFintech.setOnClickListener(v ->
                scanApp("com.fintech.wallet", txtFintechRisk,
                        txtFintechResult, panelFintechResult,
                        btnFintech, btnFintechConsent, "fintech"));

        btnHealth.setOnClickListener(v ->
                scanApp("com.health.tracker", txtHealthRisk,
                        txtHealthResult, panelHealthResult,
                        btnHealth, btnHealthConsent, "health"));

        btnEcommerce.setOnClickListener(v ->
                scanApp("com.ecommerce.shop", txtEcommerceRisk,
                        txtEcommerceResult, panelEcommerceResult,
                        btnEcommerce, btnEcommerceConsent, "ecommerce"));

        btnFintechConsent.setOnClickListener(v ->
                openRiskAlert("com.fintech.wallet"));

        btnHealthConsent.setOnClickListener(v ->
                openRiskAlert("com.health.tracker"));

        btnEcommerceConsent.setOnClickListener(v ->
                openRiskAlert("com.ecommerce.shop"));
    }

    private void scanApp(String pkg, TextView riskBadge,
                         TextView resultText, LinearLayout resultPanel,
                         Button scanBtn, Button consentBtn, String key) {

        // Update status
        txtStatus.setText("Scanning " + pkg + "...");
        scanBtn.setText("Scanning...");
        scanBtn.setEnabled(false);

        ApiService api = RetrofitClient.getClient().create(ApiService.class);

        api.analyzeApp(new ApiService.AppRequest(pkg)).enqueue(new Callback<RiskResponse>() {

            @Override
            public void onResponse(Call<RiskResponse> call,
                                   Response<RiskResponse> response) {

                scanBtn.setEnabled(true);
                scanBtn.setText("Re-scan");

                if (!response.isSuccessful() || response.body() == null
                        || response.body().risk == null) {
                    txtStatus.setText("Scan failed for " + pkg);
                    riskBadge.setText("ERROR");
                    return;
                }

                RiskResponse r = response.body();
                String level   = r.risk.riskLevel != null ?
                        r.risk.riskLevel.toUpperCase() : "UNKNOWN";
                int    score   = r.risk.riskScore;

                // Update risk badge
                riskBadge.setText(level + " · " + score + "/100");
                applyRiskColor(riskBadge, level);

                // Build result text
                StringBuilder sb = new StringBuilder();
                sb.append("▶ Risk Score:    ").append(score).append("/100\n");
                sb.append("▶ Risk Level:    ").append(level).append("\n");
                sb.append("▶ Confidence:    ").append(r.risk.confidenceLevel).append("\n");

                if (r.privacyProfile != null && r.privacyProfile.dataCategories != null) {
                    sb.append("▶ Data Access:   ").append(r.privacyProfile.dataCategories).append("\n");
                }
                if (r.risk.legalMapping != null) {
                    if (r.risk.legalMapping.gdprArticles != null)
                        sb.append("▶ GDPR:          ").append(r.risk.legalMapping.gdprArticles).append("\n");
                    if (r.risk.legalMapping.dpdpPrinciples != null)
                        sb.append("▶ DPDP:          ").append(r.risk.legalMapping.dpdpPrinciples).append("\n");
                }
                sb.append("▶ Violations:    ").append(r.risk.regulatoryViolationsFound).append("\n");
                sb.append("▶ Recommendation: ").append(
                        level.equals("HIGH") ? "DO NOT grant consent" :
                                level.equals("MEDIUM") ? "Review before granting" :
                                        "Safe to grant consent");

                resultText.setText(sb.toString());
                resultPanel.setVisibility(View.VISIBLE);
                consentBtn.setVisibility(View.VISIBLE);

                // Store result
                if (key.equals("fintech"))   fintechRiskLevel   = level;
                if (key.equals("health"))    healthRiskLevel    = level;
                if (key.equals("ecommerce")) ecommerceRiskLevel = level;

                scanCount++;
                txtScanCount.setText(scanCount + " scanned");
                txtStatus.setText("✅ " + pkg + " scanned successfully");

                checkShowSummary();
            }

            @Override
            public void onFailure(Call<RiskResponse> call, Throwable t) {
                scanBtn.setEnabled(true);
                scanBtn.setText("Retry Scan");
                txtStatus.setText("Network error: " + t.getMessage());
                Toast.makeText(MultiAppSimulationActivity.this,
                        "Scan failed: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void applyRiskColor(TextView badge, String level) {
        switch (level) {
            case "HIGH":
                badge.setTextColor(0xFFEF4444);
                badge.setBackgroundResource(R.drawable.bg_audit_badge_red);
                break;
            case "MEDIUM":
                badge.setTextColor(0xFFFBBF24);
                badge.setBackgroundResource(R.drawable.bg_audit_badge_amber);
                break;
            case "LOW":
                badge.setTextColor(0xFF10B981);
                badge.setBackgroundResource(R.drawable.bg_audit_badge_green);
                break;
            default:
                badge.setTextColor(0xFF94A3B8);
                badge.setBackgroundResource(R.drawable.bg_audit_badge_amber);
        }
    }

    private void openRiskAlert(String pkg) {
        Intent intent = new Intent(this, RiskAlertActivity.class);
        intent.putExtra("packageName", pkg);
        startActivity(intent);
    }

    private void checkShowSummary() {
        if (fintechRiskLevel == null || healthRiskLevel == null
                || ecommerceRiskLevel == null) return;

        cardSummary.setVisibility(View.VISIBLE);

        int high = 0, medium = 0, low = 0;
        for (String l : new String[]{fintechRiskLevel, healthRiskLevel, ecommerceRiskLevel}) {
            if ("HIGH".equals(l))   high++;
            else if ("MEDIUM".equals(l)) medium++;
            else low++;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("📊 Simulation Complete\n\n");
        sb.append("• Fintech Wallet:   ").append(fintechRiskLevel).append("\n");
        sb.append("• Health Tracker:   ").append(healthRiskLevel).append("\n");
        sb.append("• E-Commerce Shop:  ").append(ecommerceRiskLevel).append("\n\n");
        sb.append("Results: ")
                .append(high).append(" High  ·  ")
                .append(medium).append(" Medium  ·  ")
                .append(low).append(" Low\n\n");

        if (high > 0) {
            sb.append("⚠ ").append(high).append(" app(s) pose HIGH privacy risk.\n");
            sb.append("  GDPR Article 7 & DPDP Section 6 require\n");
            sb.append("  explicit consent before data processing.\n\n");
        }
        if (low == 3) {
            sb.append("✅ All simulated apps are safe.\n");
            sb.append("  You may proceed with consent.");
        }

        txtSummary.setText(sb.toString());
        txtStatus.setText("✅ All 3 apps scanned · See summary below");
    }
}