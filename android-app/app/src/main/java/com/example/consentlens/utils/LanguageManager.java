package com.example.consentlens.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;

import java.util.Locale;

public class LanguageManager {

    private static final String PREF_NAME = "ConsentLensPrefs";
    private static final String KEY_LANG = "app_language";

    public static void setLanguage(Context context, String languageCode) {

        SharedPreferences prefs =
                context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        prefs.edit().putString(KEY_LANG, languageCode).apply();

        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        Configuration config = new Configuration();
        config.setLocale(locale);

        context.getResources().updateConfiguration(
                config,
                context.getResources().getDisplayMetrics()
        );
    }

    public static void loadLanguage(Context context) {

        SharedPreferences prefs =
                context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        String languageCode = prefs.getString(KEY_LANG, "en");

        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        Configuration config = new Configuration();
        config.setLocale(locale);

        context.getResources().updateConfiguration(
                config,
                context.getResources().getDisplayMetrics()
        );
    }
}