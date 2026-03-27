package com.example.consentlens;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.consentlens.model.AuditLogResponse;
import com.example.consentlens.model.AuditVerifyResponse;
import com.example.consentlens.viewmodel.RiskViewModel;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class AuditLogActivity extends AppCompatActivity {

    private ListView listView;
    private TextView txtTitle;
    private TextView txtIntegrityStatus;

    private RiskViewModel viewModel;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audit_logs);

        listView           = findViewById(R.id.listLogs);
        txtTitle           = findViewById(R.id.txtTitle);
        txtIntegrityStatus = findViewById(R.id.txtIntegrityStatus);

        // Back button
        MaterialCardView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        viewModel = new ViewModelProvider(this).get(RiskViewModel.class);

        loadLogs();
        verifyAuditIntegrity();
    }

    /* ─────────────────────────────────────────── */
    /*  LOAD LOGS                                   */
    /* ─────────────────────────────────────────── */

    private void loadLogs() {
        viewModel.getAuditLogs().observe(this, response -> {

            if (response == null || response.logs == null || response.logs.isEmpty()) {
                txtTitle.setText("Audit Logs — No records found");
                return;
            }

            txtTitle.setText("Audit Logs");
            listView.setAdapter(new AuditAdapter(this, response.logs));
        });
    }

    /* ─────────────────────────────────────────── */
    /*  VERIFY INTEGRITY                            */
    /* ─────────────────────────────────────────── */

    private void verifyAuditIntegrity() {
        viewModel.verifyAuditLogs().observe(this, result -> {
            if (result == null) {
                txtIntegrityStatus.setText("Unable to verify ❓");
                txtIntegrityStatus.setTextColor(0xFF94A3B8);
                return;
            }
            if (result.valid) {
                txtIntegrityStatus.setText("All records VALID ✅");
                txtIntegrityStatus.setTextColor(0xFF10B981);
            } else {
                txtIntegrityStatus.setText("Integrity BROKEN ❌");
                txtIntegrityStatus.setTextColor(0xFFEF4444);
            }
        });
    }

    /* ─────────────────────────────────────────── */
    /*  CUSTOM ADAPTER                              */
    /* ─────────────────────────────────────────── */

    static class AuditAdapter extends BaseAdapter {

        private final Context ctx;
        private final List<AuditLogResponse.AuditLogItem> items;
        private final LayoutInflater inflater;

        AuditAdapter(Context ctx, List<AuditLogResponse.AuditLogItem> items) {
            this.ctx      = ctx;
            this.items    = items;
            this.inflater = LayoutInflater.from(ctx);
        }

        @Override public int    getCount()         { return items.size(); }
        @Override public Object getItem(int pos)   { return items.get(pos); }
        @Override public long   getItemId(int pos) { return pos; }

        @Override
        public View getView(int pos, View convertView, ViewGroup parent) {

            ViewHolder h;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.item_audit_log, parent, false);
                h             = new ViewHolder();
                h.action      = convertView.findViewById(R.id.txtAction);
                h.timestamp   = convertView.findViewById(R.id.txtTimestamp);
                h.pkg         = convertView.findViewById(R.id.txtPackage);
                h.consentId   = convertView.findViewById(R.id.txtConsentId);
                convertView.setTag(h);
            } else {
                h = (ViewHolder) convertView.getTag();
            }

            AuditLogResponse.AuditLogItem item = items.get(pos);

            // Action badge with color
            String action = item.action != null ? item.action.toUpperCase() : "ACTION";
            h.action.setText(action);

            if (action.contains("GRANT")) {
                h.action.setTextColor(0xFF10B981);
                h.action.setBackgroundResource(R.drawable.bg_audit_badge_green);
            } else if (action.contains("REVOK") || action.contains("DENY")) {
                h.action.setTextColor(0xFFEF4444);
                h.action.setBackgroundResource(R.drawable.bg_audit_badge_red);
            } else {
                h.action.setTextColor(0xFFFBBF24);
                h.action.setBackgroundResource(R.drawable.bg_audit_badge_amber);
            }

            h.pkg.setText(item.packageName != null ? item.packageName : "—");
            h.consentId.setText(item.consentId != null ? item.consentId : "—");

            // Format timestamp nicely
            if (item.timestamp != null) {
                String ts = item.timestamp.replace("T", "  ").replace("Z", "");
                if (ts.length() > 19) ts = ts.substring(0, 19);
                h.timestamp.setText(ts);
            } else {
                h.timestamp.setText("—");
            }

            return convertView;
        }

        static class ViewHolder {
            TextView action, timestamp, pkg, consentId;
        }
    }
}