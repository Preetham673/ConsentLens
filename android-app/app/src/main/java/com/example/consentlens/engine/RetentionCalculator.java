package com.example.consentlens.engine;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class RetentionCalculator {

    public static String getRemainingDays(String expiryDate) {

        try {

            SimpleDateFormat format =
                    new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

            Date expiry = format.parse(expiryDate);
            Date now = new Date();

            long diff = expiry.getTime() - now.getTime();

            long days = TimeUnit.MILLISECONDS.toDays(diff);

            if (days <= 0) {
                return "Consent Expired";
            }

            return days + " days remaining";

        } catch (Exception e) {
            return "Unknown expiry";
        }
    }
}
