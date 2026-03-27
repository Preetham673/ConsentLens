package com.example.consentlens;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AppListActivity extends AppCompatActivity {

    private static final String TAG = "AppListActivity";

    private ListView   listApps;
    private SearchView searchView;
    private TextView   txtTotalApps;
    private TextView   txtSafeApps;
    private TextView   txtRiskyApps;

    private AppAdapter          adapter;
    private final List<AppItem> allApps  = new ArrayList<>();
    private final List<AppItem> filtered = new ArrayList<>();

    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();
    private final Handler handler =
            new Handler(Looper.getMainLooper());

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_list);

        Log.d(TAG, "onCreate started");

        listApps     = findViewById(R.id.listApps);
        searchView   = findViewById(R.id.searchView);
        txtTotalApps = findViewById(R.id.txtTotalApps);
        txtSafeApps  = findViewById(R.id.txtSafeApps);
        txtRiskyApps = findViewById(R.id.txtRiskyApps);

        Log.d(TAG, "listApps null? " + (listApps == null));
        Log.d(TAG, "searchView null? " + (searchView == null));

        // Back button
        com.google.android.material.card.MaterialCardView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        loadApps();
        setupSearch();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }

    /* ─────────────────────────────────────────── */
    /*  LOAD APPS                                   */
    /* ─────────────────────────────────────────── */

    private void loadApps() {
        Log.d(TAG, "loadApps called");

        executor.execute(() -> {
            Log.d(TAG, "Background thread started");

            try {
                PackageManager pm = getPackageManager();

                // Get ALL apps including system
                // Get only user-installed apps using MATCH_UNINSTALLED_PACKAGES=0
                List<ApplicationInfo> installed =
                        pm.getInstalledApplications(PackageManager.GET_META_DATA);

                Log.d(TAG, "Total packages found: " + installed.size());

                List<AppItem> result = new ArrayList<>();

                for (ApplicationInfo info : installed) {
                    try {
                        String name = pm.getApplicationLabel(info).toString();
                        String pkg  = info.packageName;

                        // Skip our own app only
                        if (pkg.equals(getPackageName())) continue;

                        Drawable icon;
                        try {
                            icon = pm.getApplicationIcon(pkg);
                        } catch (Exception e) {
                            icon = getDrawable(android.R.drawable.sym_def_app_icon);
                        }

                        result.add(new AppItem(name, pkg, icon));

                    } catch (Exception e) {
                        Log.e(TAG, "Error processing app: " + e.getMessage());
                    }
                }

                Log.d(TAG, "Apps loaded: " + result.size());

                Collections.sort(result,
                        (a, b) -> a.appName.compareToIgnoreCase(b.appName));

                final List<AppItem> finalResult = result;

                handler.post(() -> {
                    Log.d(TAG, "Posting to UI thread, count=" + finalResult.size());

                    allApps.clear();
                    allApps.addAll(finalResult);
                    filtered.clear();
                    filtered.addAll(finalResult);

                    updateStats();

                    adapter = new AppAdapter(AppListActivity.this, filtered);
                    listApps.setAdapter(adapter);

                    Log.d(TAG, "Adapter set with " + adapter.getCount() + " items");

                    Toast.makeText(AppListActivity.this,
                            "Loaded " + finalResult.size() + " apps",
                            Toast.LENGTH_SHORT).show();

                    listApps.setOnItemClickListener((parent, view, pos, id) -> {
                        AppItem item = (AppItem) adapter.getItem(pos);
                        if (item == null) return;
                        Intent intent = new Intent(
                                AppListActivity.this, RiskAlertActivity.class);
                        intent.putExtra("packageName", item.packageName);
                        startActivity(intent);
                    });
                });

            } catch (Exception e) {
                Log.e(TAG, "FATAL in loadApps: " + e.getMessage(), e);
                handler.post(() ->
                        Toast.makeText(AppListActivity.this,
                                "Error: " + e.getMessage(),
                                Toast.LENGTH_LONG).show()
                );
            }
        });
    }

    /* ─────────────────────────────────────────── */
    /*  STATS                                       */
    /* ─────────────────────────────────────────── */

    private void updateStats() {
        int total  = allApps.size();
        int risky  = 0;
        int safe   = 0;
        int scanned = 0;

        for (AppItem a : allApps) {
            if (a.riskLevel == RiskLevel.HIGH || a.riskLevel == RiskLevel.MEDIUM) {
                risky++; scanned++;
            } else if (a.riskLevel == RiskLevel.LOW) {
                safe++; scanned++;
            }
        }

        // Unscanned apps count as safe in the display
        int unscanned = total - scanned;
        safe += unscanned;

        if (txtTotalApps != null) txtTotalApps.setText(String.valueOf(total));
        if (txtSafeApps  != null) txtSafeApps.setText(String.valueOf(safe));
        if (txtRiskyApps != null) txtRiskyApps.setText(String.valueOf(risky));
    }

    /* ─────────────────────────────────────────── */
    /*  SEARCH                                      */
    /* ─────────────────────────────────────────── */

    private void setupSearch() {
        if (searchView == null) return;
        searchView.setOnQueryTextListener(
                new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String q) {
                        filter(q); return false;
                    }
                    @Override
                    public boolean onQueryTextChange(String q) {
                        filter(q); return false;
                    }
                });
    }

    private void filter(String query) {
        filtered.clear();
        if (query == null || query.trim().isEmpty()) {
            filtered.addAll(allApps);
        } else {
            String q = query.toLowerCase();
            for (AppItem a : allApps) {
                if (a.appName.toLowerCase().contains(q)
                        || a.packageName.toLowerCase().contains(q)) {
                    filtered.add(a);
                }
            }
        }
        if (adapter != null) adapter.notifyDataSetChanged();
    }

    /* ─────────────────────────────────────────── */
    /*  MODEL                                       */
    /* ─────────────────────────────────────────── */

    public enum RiskLevel { HIGH, MEDIUM, LOW, UNKNOWN }

    public static class AppItem {
        public String    appName;
        public String    packageName;
        public Drawable  icon;
        public RiskLevel riskLevel = RiskLevel.UNKNOWN;

        AppItem(String n, String p, Drawable i) {
            appName = n; packageName = p; icon = i;
        }
    }

    /* ─────────────────────────────────────────── */
    /*  ADAPTER                                     */
    /* ─────────────────────────────────────────── */

    public static class AppAdapter extends BaseAdapter {

        private final Context        ctx;
        private final List<AppItem>  items;
        private final LayoutInflater inflater;

        AppAdapter(Context ctx, List<AppItem> items) {
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
                convertView = inflater.inflate(
                        R.layout.item_app, parent, false);
                h        = new ViewHolder();
                h.icon   = convertView.findViewById(R.id.imgAppIcon);
                h.name   = convertView.findViewById(R.id.txtAppName);
                h.pkg    = convertView.findViewById(R.id.txtPackageName);
                h.badge  = convertView.findViewById(R.id.txtRiskBadge);
                convertView.setTag(h);
            } else {
                h = (ViewHolder) convertView.getTag();
            }

            AppItem item = items.get(pos);

            if (item.icon != null && h.icon != null)
                h.icon.setImageDrawable(item.icon);
            if (h.name  != null) h.name.setText(item.appName);
            if (h.pkg   != null) h.pkg.setText(item.packageName);

            if (h.badge != null) {
                switch (item.riskLevel) {
                    case HIGH:
                        h.badge.setText("⚠ HIGH RISK");
                        h.badge.setTextColor(0xFF991B1B);
                        h.badge.setBackgroundResource(
                                R.drawable.bg_pill_high);
                        break;
                    case MEDIUM:
                        h.badge.setText("~ MEDIUM");
                        h.badge.setTextColor(0xFF92400E);
                        h.badge.setBackgroundResource(
                                R.drawable.bg_pill_medium);
                        break;
                    case LOW:
                        h.badge.setText("✓ LOW RISK");
                        h.badge.setTextColor(0xFF166534);
                        h.badge.setBackgroundResource(
                                R.drawable.bg_pill_low);
                        break;
                    default:
                        h.badge.setText("Tap to scan");
                        h.badge.setTextColor(0xFF6B7280);
                        h.badge.setBackgroundResource(
                                R.drawable.bg_kv_premium);
                        break;
                }
            }

            return convertView;
        }

        static class ViewHolder {
            ImageView icon;
            TextView  name, pkg, badge;
        }
    }
    /* ─────────────────────────────────────────── */
    /*  RECEIVE RISK RESULT FROM RiskAlertActivity  */
    /* ─────────────────────────────────────────── */

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            String pkg       = data.getStringExtra("packageName");
            String riskLevel = data.getStringExtra("riskLevel");

            if (pkg == null || riskLevel == null) return;

            // Find the app in the list and update its risk level
            for (AppItem item : allApps) {
                if (item.packageName.equals(pkg)) {
                    switch (riskLevel) {
                        case "HIGH":   item.riskLevel = RiskLevel.HIGH;   break;
                        case "MEDIUM": item.riskLevel = RiskLevel.MEDIUM; break;
                        case "LOW":    item.riskLevel = RiskLevel.LOW;    break;
                        default:       item.riskLevel = RiskLevel.UNKNOWN; break;
                    }
                    break;
                }
            }

            // Refresh filtered list and stats
            filter(searchView != null ?
                    searchView.getQuery().toString() : "");
            updateStats();
        }
    }


}