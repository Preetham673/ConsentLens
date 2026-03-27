package com.example.consentlens.engine;

import java.util.List;

public class PurposeDriftDetector {

    /*
     Detect if the app is using data for a new purpose
     that was not originally consented.
     */

    public static boolean detectDrift(List<String> consentedPurposes,
                                      String currentPurpose) {

        if (consentedPurposes == null || currentPurpose == null) {
            return false;
        }

        for (String purpose : consentedPurposes) {

            if (purpose.equalsIgnoreCase(currentPurpose)) {
                return false; // no drift
            }
        }

        // new purpose detected
        return true;
    }
}