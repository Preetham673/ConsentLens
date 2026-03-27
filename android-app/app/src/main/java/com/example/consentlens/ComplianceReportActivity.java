package com.example.consentlens;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.consentlens.model.ComplianceReportResponse;
import com.example.consentlens.viewmodel.RiskViewModel;
import com.google.android.material.card.MaterialCardView;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;

public class ComplianceReportActivity extends AppCompatActivity {

    private TextView txtReport;
    private Button btnExportPdf;
    private RiskViewModel viewModel;
    private String packageName;
    private String generatedReportText = "";

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compliance_report);

        txtReport    = findViewById(R.id.txtReport);
        btnExportPdf = findViewById(R.id.btnExportPdf);

        MaterialCardView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        viewModel   = new ViewModelProvider(this).get(RiskViewModel.class);
        packageName = getIntent().getStringExtra("packageName");

        if (packageName != null) {
            loadReport(packageName);
        } else {
            txtReport.setText("No package name received.");
        }

        if (btnExportPdf != null) {
            btnExportPdf.setOnClickListener(v -> exportPdf());
        }
    }

    private void loadReport(String packageName) {
        viewModel.getComplianceReport(packageName).observe(this, this::displayReport);
    }

    private void displayReport(ComplianceReportResponse response) {

        if (response == null || response.complianceReport == null) {
            txtReport.setText("No compliance report available.");
            return;
        }

        ComplianceReportResponse.ComplianceReport report = response.complianceReport;
        StringBuilder builder = new StringBuilder();

        builder.append("=== CONSENTLENS COMPLIANCE REPORT ===\n\n");
        builder.append("App Name: ").append(report.app.appName)
                .append("\nPackage: ").append(report.app.packageName).append("\n\n");

        builder.append("Risk Level: ").append(report.riskAssessment.riskLevel)
                .append("\nRisk Score: ").append(report.riskAssessment.riskScore)
                .append("\nConfidence: ").append(report.riskAssessment.confidenceLevel).append("\n");

        if (report.riskAssessment.datasetReference != null) {
            builder.append("Dataset Reference: ").append(report.riskAssessment.datasetReference).append("\n");
        }

        builder.append("Regulatory Violations: ").append(report.riskAssessment.regulatoryViolationsFound).append("\n\n");
        builder.append("\n=== LEGAL COMPLIANCE ANALYSIS ===\n\n");

        builder.append("🔵 GDPR Articles:\n");
        if (report.riskAssessment.legalMapping != null && report.riskAssessment.legalMapping.gdprArticles != null) {
            for (String article : report.riskAssessment.legalMapping.gdprArticles) {
                builder.append("• ").append(article).append("\n");
                if (article.contains("Article 5")) builder.append("  → Ensures transparency, fairness, and lawful data processing.\n");
                if (article.contains("Article 6")) builder.append("  → Requires lawful basis such as explicit consent.\n");
                if (article.contains("Article 7")) builder.append("  → Consent must be freely given and revocable.\n");
                if (article.contains("Article 9")) builder.append("  → Special protection for sensitive personal data.\n");
            }
        } else {
            builder.append("No GDPR mapping available.\n");
        }

        builder.append("\n🟠 DPDP Principles:\n");
        if (report.riskAssessment.legalMapping != null && report.riskAssessment.legalMapping.dpdpPrinciples != null) {
            for (String principle : report.riskAssessment.legalMapping.dpdpPrinciples) {
                builder.append("• ").append(principle).append("\n");
                if (principle.contains("Transparency")) builder.append("  → Data fiduciary must provide clear notice before processing.\n");
                if (principle.contains("Purpose"))      builder.append("  → Data must be used only for the stated purpose.\n");
                if (principle.contains("Minimization")) builder.append("  → Only necessary data should be collected.\n");
                if (principle.contains("Consent"))      builder.append("  → Explicit and revocable consent required.\n");
            }
        } else {
            builder.append("No DPDP mapping available.\n");
        }

        builder.append("\nConsent History:\n");
        if (report.consentHistory != null) {
            for (var consent : report.consentHistory) {
                builder.append("Decision: ").append(consent.decision)
                        .append(" | Risk: ").append(consent.riskLevel)
                        .append(" | Revoked: ").append(consent.revoked).append("\n");
            }
        }

        builder.append("\nAudit Events:\n");
        if (report.relatedAuditEvents != null) {
            for (ComplianceReportResponse.AuditEvent event : report.relatedAuditEvents) {
                builder.append(event.action).append(" - ").append(event.timestamp).append("\n");
            }
        }

        builder.append("\nGenerated At: ").append(report.generatedAt);

        generatedReportText = builder.toString();
        txtReport.setText(generatedReportText);
    }

    private void appendList(StringBuilder builder, List<String> list) {
        if (list == null) return;
        for (String item : list) builder.append("• ").append(item).append("\n");
    }

    /* ── PDF EXPORT ── */

    private void exportPdf() {

        if (generatedReportText.isEmpty()) {
            Toast.makeText(this, "No report to export", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            PdfDocument document = new PdfDocument();
            int pageWidth = 600, pageHeight = 800, margin = 30, lineHeight = 18;
            Paint paint = new Paint();
            paint.setTextSize(12);
            String[] lines = generatedReportText.split("\n");
            int pageNumber = 1;
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create();
            PdfDocument.Page page = document.startPage(pageInfo);
            int y = margin;

            for (String line : lines) {
                if (y > pageHeight - margin) {
                    document.finishPage(page);
                    pageNumber++;
                    pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create();
                    page = document.startPage(pageInfo);
                    y = margin;
                }
                page.getCanvas().drawText(line, margin, y, paint);
                y += lineHeight;
            }

            document.finishPage(page);
            OutputStream outputStream;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.MediaColumns.DISPLAY_NAME, "ConsentLens_Report.pdf");
                values.put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf");
                values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
                outputStream = getContentResolver().openOutputStream(
                        getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values));
            } else {
                outputStream = new FileOutputStream(
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                                + "/ConsentLens_Report.pdf");
            }

            document.writeTo(outputStream);
            document.close();
            outputStream.close();
            Toast.makeText(this, "PDF exported to Downloads ✅", Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "PDF export failed", Toast.LENGTH_SHORT).show();
        }
    }
}