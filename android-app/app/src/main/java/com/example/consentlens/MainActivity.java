package com.example.consentlens;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.appcompat.app.AlertDialog;

import com.example.consentlens.network.ApiService;
import com.example.consentlens.network.RetrofitClient;
import com.example.consentlens.network.TokenManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.example.consentlens.viewmodel.RiskViewModel;

import java.util.Locale;
import java.util.Map;

import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {

    // =============================================
    // KPI VIEWS
    // =============================================
    private TextView txtTotalScans;       // Total Consents (top left card)
    private TextView txtHighRisk;         // High Risk Apps (top right card)
    private TextView txtConsents;         // Compliance Score % (bottom wide card)
    private TextView txtRevocationRate;   // Revocation Error Rate (bottom wide card)
    private TextView txtSatisfaction;     // User Satisfaction (bottom wide card)
    private TextView txtComplianceBadge;

    // =============================================
    // BUTTONS
    // =============================================
    private Button btnScanApps;
    private Button btnViewHistory;
    private Button btnComplianceReport;

    private RiskViewModel viewModel;


    // -------------------------------------------------------
    // Attach locale BEFORE layout inflation — critical
    // -------------------------------------------------------
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        /* =============================
           TOOLBAR
        ============================== */
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /* =============================
           BIND KPI VIEWS
        ============================== */
        txtTotalScans      = findViewById(R.id.txtTotalScans);
        txtHighRisk        = findViewById(R.id.txtHighRisk);
        txtConsents        = findViewById(R.id.txtConsents);
        txtRevocationRate  = findViewById(R.id.txtRevocationRate);
        txtSatisfaction    = findViewById(R.id.txtSatisfaction);
        txtComplianceBadge = findViewById(R.id.txtComplianceBadge);

        /* =============================
           BIND BUTTONS
        ============================== */
        btnScanApps         = findViewById(R.id.btnViewApps);
        btnViewHistory      = findViewById(R.id.btnViewHistory);
        Button btnAuditLogs = findViewById(R.id.btnAuditLogs);
        btnComplianceReport = findViewById(R.id.btnComplianceReport);
        Button btnSimulation = findViewById(R.id.btnSimulation);
        Button btnDashboard  = findViewById(R.id.btnComplianceDashboard);

        viewModel = new ViewModelProvider(this).get(RiskViewModel.class);

        requestNotificationPermission();
        startMonitoringService();
        loginBackend();

        /* =============================
           BUTTON ACTIONS
        ============================== */

        btnScanApps.setOnClickListener(v ->
                startActivity(new Intent(this, AppListActivity.class)));

        btnViewHistory.setOnClickListener(v -> {
            Intent intent = new Intent(this, ConsentHistoryActivity.class);
            intent.putExtra("packageName", "ALL");
            startActivity(intent);
        });

        btnAuditLogs.setOnClickListener(v ->
                startActivity(new Intent(this, AuditLogActivity.class)));

        btnComplianceReport.setOnClickListener(v -> {
            Intent intent = new Intent(this, ComplianceReportActivity.class);
            intent.putExtra("packageName", "ALL");
            startActivity(intent);
        });

        btnSimulation.setOnClickListener(v ->
                startActivity(new Intent(this, MultiAppSimulationActivity.class)));

        btnDashboard.setOnClickListener(v ->
                startActivity(new Intent(this, ComplianceDashboardActivity.class)));
    }


    /* =============================
       LANGUAGE MENU
    ============================== */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.dashboard_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_language) {
            showLanguageDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    /* =============================
       LANGUAGE SELECTOR
    ============================== */

    private void showLanguageDialog() {

        String[] languages = {
                "English",
                "हिन्दी (Hindi)",
                "தமிழ் (Tamil)",
                "తెలుగు (Telugu)"
        };

        String[] codes = { "en", "hi", "ta", "te" };

        new AlertDialog.Builder(this)
                .setTitle("Select Language")
                .setItems(languages, (dialog, which) -> {
                    // Apply locale and restart activity cleanly
                    LocaleHelper.setLocale(MainActivity.this, codes[which]);
                    Intent intent = new Intent(MainActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                            | Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .show();
    }

    private void setLocale(String lang) {
        LocaleHelper.setLocale(this, lang);
    }


    /* =============================
       NOTIFICATION PERMISSION
    ============================== */

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        1001
                );
            }
        }
    }


    /* =============================
       START MONITOR SERVICE
    ============================== */

    private void startMonitoringService() {
        Intent serviceIntent = new Intent(this, AppMonitorService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
    }


    /* =============================
       LOGIN BACKEND (JWT AUTH)
    ============================== */

    private void loginBackend() {

        ApiService api = RetrofitClient.getClient().create(ApiService.class);

        ApiService.LoginRequest request =
                new ApiService.LoginRequest("mobileUser", "ADMIN");

        api.login(request).enqueue(new Callback<Map<String, String>>() {

            @Override
            public void onResponse(Call<Map<String, String>> call,
                                   Response<Map<String, String>> response) {
                if (response.body() != null) {
                    String token = response.body().get("token");
                    TokenManager.setToken(token);
                    loadKPIs();
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                android.widget.Toast.makeText(
                        MainActivity.this,
                        "Backend login failed",
                        android.widget.Toast.LENGTH_SHORT
                ).show();
            }
        });
    }


    /* =============================
       LOAD KPIs FROM BACKEND
    ============================== */

    private void loadKPIs() {

        viewModel.getKPIs().observe(this, response -> {

            if (!(response instanceof Map)) return;

            Map<?, ?> data = (Map<?, ?>) response;
            Object kpisObj = data.get("kpis");

            if (!(kpisObj instanceof Map)) return;

            Map<?, ?> kpis = (Map<?, ?>) kpisObj;

            int totalConsents    = ((Number) kpis.get("totalConsents")).intValue();
            int highRisk         = ((Number) kpis.get("highRiskConsents")).intValue();
            int complianceScore  = ((Number) kpis.get("complianceReadinessScore")).intValue();
            double revocationRate= ((Number) kpis.get("revocationErrorRate")).doubleValue();
            Object satisfaction  = kpis.get("userSatisfactionScore");

            // ---- TOP ROW KPI CARDS ----
            // Card 1 — show just the number, clean and bold
            txtTotalScans.setText(String.valueOf(totalConsents));

            // Card 2 — show just the number
            txtHighRisk.setText(String.valueOf(highRisk));

            // ---- BOTTOM WIDE CARD — 3 data points ----

            // Point 1 — Compliance Score
            txtConsents.setText(complianceScore + "%");

            // Point 2 — Revocation Error Rate
            if (txtRevocationRate != null) {
                txtRevocationRate.setText(
                        String.format("%.1f%%", revocationRate)
                );
            }

            // Point 3 — User Satisfaction
            if (txtSatisfaction != null) {
                txtSatisfaction.setText(
                        satisfaction != null ? satisfaction + " ⭐" : "N/A"
                );
            }

            // ---- COMPLIANCE BADGE ----
            if (complianceScore >= 80) {
                txtComplianceBadge.setText("🟢 COMPLIANT (" + complianceScore + "%)");
                txtComplianceBadge.setBackgroundResource(R.drawable.bg_badge_green);

            } else if (complianceScore >= 50) {
                txtComplianceBadge.setText("🟡 PARTIALLY COMPLIANT (" + complianceScore + "%)");
                txtComplianceBadge.setBackgroundResource(R.drawable.bg_badge_yellow);

            } else {
                txtComplianceBadge.setText("🔴 NON-COMPLIANT (" + complianceScore + "%)");
                txtComplianceBadge.setBackgroundResource(R.drawable.bg_badge_red);
            }
        });
    }
}