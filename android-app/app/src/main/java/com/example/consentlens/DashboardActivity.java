package com.example.consentlens;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.consentlens.network.RetrofitClient;
import com.example.consentlens.network.ApiService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardActivity extends AppCompatActivity {

    private TextView txtTotalScans, txtHighRisk, txtConsents;
    private Button btnViewApps, btnViewHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        txtTotalScans = findViewById(R.id.txtTotalScans);
        txtHighRisk = findViewById(R.id.txtHighRisk);
        txtConsents = findViewById(R.id.txtConsents);
        btnViewApps = findViewById(R.id.btnViewApps);
        btnViewHistory = findViewById(R.id.btnViewHistory);

        loadKPIs();

        btnViewApps.setOnClickListener(v ->
                startActivity(new Intent(this, MainActivity.class)));

        btnViewHistory.setOnClickListener(v ->
                startActivity(new Intent(this, ConsentHistoryActivity.class)));
    }

    private void loadKPIs() {

        ApiService apiService =
                RetrofitClient.getClient().create(ApiService.class);

        apiService.getKPIs().enqueue(new Callback<Object>() {

            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {

                if (response.isSuccessful() && response.body() != null) {

                    // For now simple display (can improve later)
                    txtTotalScans.setText("Total Risk Analyses Loaded");
                    txtHighRisk.setText("High Risk Apps Loaded");
                    txtConsents.setText("Consents Loaded");
                }
            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                txtTotalScans.setText("Server not reachable");
            }
        });
    }
}
