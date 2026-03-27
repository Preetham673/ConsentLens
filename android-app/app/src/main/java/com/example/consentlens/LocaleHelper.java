package com.example.consentlens;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.LocaleList;

import java.util.Locale;

public class LocaleHelper {

    private static final String PREFS        = "ConsentLensPrefs";
    private static final String PREF_LANG    = "language";
    private static final String DEFAULT_LANG = "en";

    // -------------------------------------------------------
    // Call this in every Activity's attachBaseContext()
    // -------------------------------------------------------
    public static Context onAttach(Context context) {
        String lang = getSavedLanguage(context);
        return setLocale(context, lang);
    }

    // -------------------------------------------------------
    // Save + apply a new language
    // -------------------------------------------------------
    public static Context setLocale(Context context, String language) {
        saveLanguage(context, language);
        return updateResources(context, language);
    }

    // -------------------------------------------------------
    // Get saved language (default English)
    // -------------------------------------------------------
    public static String getSavedLanguage(Context context) {
        return context
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getString(PREF_LANG, DEFAULT_LANG);
    }

    // -------------------------------------------------------
    // Save language to prefs
    // -------------------------------------------------------
    private static void saveLanguage(Context context, String language) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit()
                .putString(PREF_LANG, language)
                .apply();
    }

    // -------------------------------------------------------
    // Apply locale to context
    // -------------------------------------------------------
    private static Context updateResources(Context context, String language) {

        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Resources resources = context.getResources();
        Configuration config = new Configuration(resources.getConfiguration());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            LocaleList localeList = new LocaleList(locale);
            LocaleList.setDefault(localeList);
            config.setLocales(localeList);
            context = context.createConfigurationContext(config);
        } else {
            config.setLocale(locale);
            resources.updateConfiguration(config, resources.getDisplayMetrics());
        }

        return context;
    }
}
