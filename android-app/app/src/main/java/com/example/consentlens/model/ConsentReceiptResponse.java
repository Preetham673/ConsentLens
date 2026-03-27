package com.example.consentlens.model;

import java.util.List;

public class ConsentReceiptResponse {

    public Receipt receipt;

    public static class Receipt {

        public String receiptId;
        public String consentId;

        public String issuedAt;

        public String controller;

        public List<String> purpose;

        public String lawfulBasis;

        public String jurisdiction;

        public String expiryDate;

        public boolean expired;

        public String fingerprint;

        public List<String> userRights;
    }


}
