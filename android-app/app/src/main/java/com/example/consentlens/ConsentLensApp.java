package com.example.consentlens;

import android.app.Application;
import android.content.Context;

public class ConsentLensApp extends Application {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }
}
