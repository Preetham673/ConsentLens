package com.example.consentlens.engine;

import java.util.List;

public class DataCategoryClassifier {

    public static boolean containsSensitiveData(List<String> dataCategories) {

        if (dataCategories == null) return false;

        for (String category : dataCategories) {
            if (category.equalsIgnoreCase("health") ||
                    category.equalsIgnoreCase("biometric") ||
                    category.equalsIgnoreCase("financial") ||
                    category.equalsIgnoreCase("children") ||
                    category.equalsIgnoreCase("location")) {

                return true;
            }
        }
        return false;
    }
}