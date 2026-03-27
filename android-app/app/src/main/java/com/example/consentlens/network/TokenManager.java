package com.example.consentlens.network;

public class TokenManager {

    private static String token;

    public static void setToken(String t){
        token = t;
    }

    public static String getToken(){
        return token;
    }
}