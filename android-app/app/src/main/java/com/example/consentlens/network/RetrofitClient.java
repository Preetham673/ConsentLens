package com.example.consentlens.network;

import okhttp3.OkHttpClient;
import okhttp3.Request;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import okhttp3.CertificatePinner;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class RetrofitClient {

    private static Retrofit retrofit;

    private static final String BASE_URL = "http://11.12.19.25:5000/api/";
    // Replace with your current PC IP

    public static Retrofit getClient() {


        /* =====================================================
   NEW: TLS CERTIFICATE PINNING
===================================================== */

        CertificatePinner certificatePinner =
                new CertificatePinner.Builder()
                        .add("11.12.18.229",
                                "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=")
                        .build();

        if (retrofit == null) {

            /* =====================================================
               NEW: HTTP CLIENT WITH JWT AUTHORIZATION HEADER
               (Old Retrofit logic preserved)
            ===================================================== */

          /* =====================================================
   NEW: HTTP CLIENT WITH JWT AUTHORIZATION HEADER
   + TLS CERTIFICATE PINNING
===================================================== */

            OkHttpClient client = new OkHttpClient.Builder()

                    .certificatePinner(certificatePinner)   // NEW LINE

                    .addInterceptor(chain -> {

                        Request original = chain.request();

                        Request.Builder builder = original.newBuilder();

                        // Get token from TokenManager
                        String token = TokenManager.getToken();

                        if (token != null && !token.isEmpty()) {

                            builder.header(
                                    "Authorization",
                                    "Bearer " + token
                            );

                        }

                        Request request = builder.build();

                        return chain.proceed(request);
                    })
                    .build();


            /* =====================================================
               OLD RETROFIT CLIENT (NOT REMOVED)
            ===================================================== */

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client) // NEW
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }

        return retrofit;
    }
}