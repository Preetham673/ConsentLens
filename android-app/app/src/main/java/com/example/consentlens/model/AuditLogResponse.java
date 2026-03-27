package com.example.consentlens.model;

import java.util.List;

public class AuditLogResponse {

    public List<AuditLogItem> logs;

    public static class AuditLogItem {
        public String logId;
        public String action;
        public String consentId;
        public String packageName;
        public String timestamp;
    }
}
