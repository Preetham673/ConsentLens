package com.example.consentlens;

import android.content.Context;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.consentlens.network.ApiService;
import com.example.consentlens.network.RetrofitClient;
import com.google.android.material.card.MaterialCardView;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ComplianceDashboardActivity extends AppCompatActivity {

    TextView txtGDPR, txtDPDP, txtActive, txtExpired,
            txtHighRisk, txtComplianceScore,
            txtRevocationRate, txtSatisfaction, txtRecentEvents;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compliance_dashboard);

        txtGDPR            = findViewById(R.id.txtGDPR);
        txtDPDP            = findViewById(R.id.txtDPDP);
        txtActive          = findViewById(R.id.txtActive);
        txtExpired         = findViewById(R.id.txtExpired);
        txtHighRisk        = findViewById(R.id.txtHighRisk);
        txtComplianceScore = findViewById(R.id.txtComplianceScore);
        txtRevocationRate  = findViewById(R.id.txtRevocationRate);
        txtSatisfaction    = findViewById(R.id.txtSatisfaction);
        txtRecentEvents    = findViewById(R.id.txtRecentEvents);

        MaterialCardView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        loadDashboard();
    }

    private void loadDashboard() {

        ApiService api = RetrofitClient.getClient().create(ApiService.class);

        api.getComplianceDashboard().enqueue(new Callback<Map<String, Object>>() {

            @Override
            public void onResponse(Call<Map<String, Object>> call,
                                   Response<Map<String, Object>> response) {

                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(ComplianceDashboardActivity.this,
                            "Failed to load dashboard", Toast.LENGTH_SHORT).show();
                    return;
                }

                Object dashObj = response.body().get("dashboard");
                if (!(dashObj instanceof Map)) return;

                Map<?, ?> d = (Map<?, ?>) dashObj;

                txtComplianceScore.setText(getInt(d, "complianceScore") + "%");
                txtGDPR.setText(getInt(d, "gdprCoverage") + "%");
                txtDPDP.setText(getInt(d, "dpdpCoverage") + "%");
                txtActive.setText(String.valueOf(getInt(d, "activeConsents")));
                txtExpired.setText(String.valueOf(getInt(d, "expiredConsents")));
                txtHighRisk.setText(String.valueOf(getInt(d, "highRiskApps")));
                txtRevocationRate.setText(getDouble(d, "revocationErrorRate") + "%");
                txtSatisfaction.setText(getDouble(d, "userSatisfactionScore") + " ⭐");

                // Recent audit events
                Object eventsObj = d.get("recentAuditEvents");
                if (eventsObj instanceof List) {
                    List<?> events = (List<?>) eventsObj;
                    StringBuilder sb = new StringBuilder();
                    if (events.isEmpty()) {
                        sb.append("No recent events.");
                    } else {
                        for (Object ev : events) {
                            if (ev instanceof Map) {
                                Map<?, ?> e = (Map<?, ?>) ev;
                                sb.append("• ").append(e.get("action"))
                                        .append("  —  ").append(e.get("packageName"))
                                        .append("\n  ").append(e.get("timestamp"))
                                        .append("\n\n");
                            }
                        }
                    }
                    txtRecentEvents.setText(sb.toString().trim());
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(ComplianceDashboardActivity.this,
                        "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private int getInt(Map<?, ?> map, String key) {
        Object val = map.get(key);
        return val instanceof Number ? ((Number) val).intValue() : 0;
    }

    private double getDouble(Map<?, ?> map, String key) {
        Object val = map.get(key);
        return val instanceof Number ? ((Number) val).doubleValue() : 0.0;
    }
}