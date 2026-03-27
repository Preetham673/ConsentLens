package com.example.consentlens;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class ConsentGraphActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consent_graph);

        TextView txtApp = findViewById(R.id.txtGraphApp);
        TextView txtData = findViewById(R.id.txtGraphData);
        TextView txtPurpose = findViewById(R.id.txtGraphPurpose);
        TextView txtLegal = findViewById(R.id.txtGraphLegal);
        TextView txtRetention = findViewById(R.id.txtGraphRetention);

        String app = getIntent().getStringExtra("app");
        String data = getIntent().getStringExtra("data");
        String legal = getIntent().getStringExtra("legal");

        txtApp.setText(app);
        txtData.setText(data);
        txtPurpose.setText("General Processing");
        txtLegal.setText(legal);
        txtRetention.setText("30 Days");
    }
}