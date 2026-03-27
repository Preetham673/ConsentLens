package com.example.consentlens.sdk;

import android.content.Context;
import android.content.Intent;

import com.example.consentlens.RiskAlertActivity;

public class ConsentSDK {

    /*
        Request Consent
        Launches ConsentLens risk screen
     */
    public static void requestConsent(Context context, String packageName) {

        Intent intent = new Intent(context, RiskAlertActivity.class);
        intent.putExtra("packageName", packageName);
        context.startActivity(intent);
    }

    /*
        Check Consent Status
        (MVP placeholder)
     */
    public static boolean checkConsentStatus(String packageName) {

        // In future this will call backend API
        return false;
    }

    /*
        Revoke Consent
     */
    public static void revokeConsent(Context context, String consentId) {

        // Future: call revoke API
        // For MVP we trigger history screen

        Intent intent = new Intent(context,
                com.example.consentlens.ConsentHistoryActivity.class);

        intent.putExtra("consentId", consentId);

        context.startActivity(intent);
    }
}