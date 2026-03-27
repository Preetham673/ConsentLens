package com.example.consentlens;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.consentlens.model.ConsentHistoryResponse;
import com.example.consentlens.model.ConsentReceiptResponse;
import com.example.consentlens.viewmodel.RiskViewModel;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class ConsentHistoryActivity extends AppCompatActivity {

    private ListView  listView;
    private TextView  txtTitle;
    private TextView  txtEmpty;
    private RiskViewModel viewModel;
    private String packageName;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consent_history);

        listView  = findViewById(R.id.listHistory);
        txtTitle  = findViewById(R.id.txtTitle);
        txtEmpty  = findViewById(R.id.txtEmpty);

        MaterialCardView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        viewModel   = new ViewModelProvider(this).get(RiskViewModel.class);
        packageName = getIntent().getStringExtra("packageName");

        if (packageName == null || packageName.equals("ALL")) {
            txtTitle.setText("All Consent History");
            viewModel.getAllConsentHistory().observe(this, this::displayHistory);
        } else {
            txtTitle.setText("Consent: " + packageName);
            viewModel.getConsentHistory(packageName).observe(this, this::displayHistory);
        }
    }

    private void displayHistory(ConsentHistoryResponse response) {

        if (response == null || response.history == null || response.history.isEmpty()) {
            txtEmpty.setVisibility(View.VISIBLE);
            return;
        }

        txtEmpty.setVisibility(View.GONE);

        List<ConsentHistoryResponse.ConsentItem> items = response.history;

        listView.setAdapter(new HistoryAdapter(this, items));

        // TAP → receipt
        listView.setOnItemClickListener((parent, view, pos, id) -> {
            ConsentHistoryResponse.ConsentItem item = items.get(pos);
            viewModel.getReceipt(item.consentId).observe(this, receiptResponse -> {
                if (receiptResponse != null && receiptResponse.receipt != null) {
                    ConsentReceiptResponse.Receipt r = receiptResponse.receipt;
                    StringBuilder b = new StringBuilder();
                    b.append("CONSENT RECEIPT\n\n");
                    b.append("Receipt ID:\n").append(r.receiptId).append("\n\n");
                    b.append("Consent ID:\n").append(r.consentId).append("\n\n");
                    b.append("Purpose:\n").append(r.purpose).append("\n\n");
                    b.append("Legal Basis:\n").append(r.lawfulBasis).append("\n\n");
                    b.append("Controller:\n").append(r.controller).append("\n\n");
                    b.append("Jurisdiction:\n").append(r.jurisdiction).append("\n\n");
                    b.append("Issued At:\n").append(r.issuedAt).append("\n\n");
                    b.append("Fingerprint:\n").append(r.fingerprint).append("\n\n");
                    b.append("User Rights:\n");
                    if (r.userRights != null) for (String right : r.userRights) b.append("• ").append(right).append("\n");
                    new AlertDialog.Builder(this)
                            .setTitle("Consent Receipt")
                            .setMessage(b.toString())
                            .setPositiveButton("Close", null)
                            .show();
                } else {
                    Toast.makeText(this, "Receipt not found", Toast.LENGTH_SHORT).show();
                }
            });
        });

        // LONG PRESS → revoke/erase
        listView.setOnItemLongClickListener((parent, view, pos, id) -> {
            ConsentHistoryResponse.ConsentItem item = items.get(pos);
            String[] options = {"Revoke Purpose", "Erase Consent"};
            new AlertDialog.Builder(this)
                    .setTitle("Select Action")
                    .setItems(options, (dialog, which) -> {
                        if (which == 0 && item.purpose != null && !item.purpose.isEmpty()) {
                            viewModel.revokePurpose(item.consentId, item.purpose.get(0))
                                    .observe(this, ok -> {
                                        if (ok != null && ok) { Toast.makeText(this, "Purpose revoked", Toast.LENGTH_SHORT).show(); recreate(); }
                                    });
                        }
                        if (which == 1) {
                            viewModel.eraseConsent(item.consentId)
                                    .observe(this, ok -> {
                                        if (ok != null && ok) { Toast.makeText(this, "Consent erased", Toast.LENGTH_SHORT).show(); recreate(); }
                                    });
                        }
                    }).show();
            return true;
        });
    }

    /* ─── ADAPTER ─── */

    static class HistoryAdapter extends BaseAdapter {

        private final Context ctx;
        private final List<ConsentHistoryResponse.ConsentItem> items;
        private final LayoutInflater inflater;

        HistoryAdapter(Context ctx, List<ConsentHistoryResponse.ConsentItem> items) {
            this.ctx = ctx; this.items = items;
            this.inflater = LayoutInflater.from(ctx);
        }

        @Override public int    getCount()         { return items.size(); }
        @Override public Object getItem(int pos)   { return items.get(pos); }
        @Override public long   getItemId(int pos) { return pos; }

        @Override
        public View getView(int pos, View convertView, ViewGroup parent) {
            ViewHolder h;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.item_consent_history, parent, false);
                h = new ViewHolder();
                h.decision        = convertView.findViewById(R.id.txtDecision);
                h.revStatus       = convertView.findViewById(R.id.txtRevocationStatus);
                h.timestamp       = convertView.findViewById(R.id.txtTimestamp);
                h.pkg             = convertView.findViewById(R.id.txtPackage);
                h.riskLevel       = convertView.findViewById(R.id.txtRiskLevel);
                h.jurisdiction    = convertView.findViewById(R.id.txtJurisdiction);
                h.purpose         = convertView.findViewById(R.id.txtPurpose);
                h.dataCat         = convertView.findViewById(R.id.txtDataCategories);
                h.revoked         = convertView.findViewById(R.id.txtRevoked);
                h.revokedPurposes = convertView.findViewById(R.id.txtRevokedPurposes);
                convertView.setTag(h);
            } else { h = (ViewHolder) convertView.getTag(); }

            ConsentHistoryResponse.ConsentItem item = items.get(pos);

            // Decision badge
            String dec = item.decision != null ? item.decision.toUpperCase() : "—";
            h.decision.setText(dec);
            if (dec.contains("GRANT")) {
                h.decision.setTextColor(0xFF10B981);
                h.decision.setBackgroundResource(R.drawable.bg_audit_badge_green);
            } else if (dec.contains("DENY") || dec.contains("REVOK")) {
                h.decision.setTextColor(0xFFEF4444);
                h.decision.setBackgroundResource(R.drawable.bg_audit_badge_red);
            } else {
                h.decision.setTextColor(0xFFFBBF24);
                h.decision.setBackgroundResource(R.drawable.bg_audit_badge_amber);
            }

            // Revocation status
            if (item.revoked) {
                h.revStatus.setText("⏳ Pending Deletion");
                h.revStatus.setTextColor(0xFFFBBF24);
            } else {
                h.revStatus.setText("✔ Active");
                h.revStatus.setTextColor(0xFF10B981);
            }

            // Timestamp
            String ts = item.timestamp != null ? item.timestamp.replace("T", " ").replace("Z", "") : "—";
            if (ts.length() > 16) ts = ts.substring(0, 16);
            h.timestamp.setText(ts);

            h.pkg.setText(item.packageName != null ? item.packageName : "—");

            // Risk level
            String risk = item.riskLevel != null ? item.riskLevel.toUpperCase() + " RISK" : "UNKNOWN";
            h.riskLevel.setText(risk);
            if (risk.contains("LOW")) {
                h.riskLevel.setTextColor(0xFF10B981);
                h.riskLevel.setBackgroundResource(R.drawable.bg_audit_badge_green);
            } else if (risk.contains("HIGH")) {
                h.riskLevel.setTextColor(0xFFEF4444);
                h.riskLevel.setBackgroundResource(R.drawable.bg_audit_badge_red);
            } else {
                h.riskLevel.setTextColor(0xFFFBBF24);
                h.riskLevel.setBackgroundResource(R.drawable.bg_audit_badge_amber);
            }

            h.jurisdiction.setText(item.jurisdiction != null ? item.jurisdiction : "—");
            h.purpose.setText(item.purpose != null && !item.purpose.isEmpty() ? item.purpose.toString() : "—");
            h.dataCat.setText(item.dataCategories != null ? item.dataCategories.toString() : "—");
            if (h.revoked != null) h.revoked.setText(String.valueOf(item.revoked));
            if (h.revokedPurposes != null)
                h.revokedPurposes.setText(item.revokedPurposes != null && !item.revokedPurposes.isEmpty()
                        ? item.revokedPurposes.toString() : "None");

            return convertView;
        }

        static class ViewHolder {
            TextView decision, revStatus, timestamp, pkg, riskLevel, jurisdiction, purpose, dataCat, revoked, revokedPurposes;
        }
    }
}