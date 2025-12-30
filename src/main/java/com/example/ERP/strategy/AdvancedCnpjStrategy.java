package com.example.erp.strategy;

public class AdvancedCnpjStrategy implements CnpjStrategy {
    @Override
    public boolean validate(String cnpj) {
        if (cnpj == null) return false;
        String s = cnpj.replaceAll("\\D", "");
        if (s.length() != 14) return false;
        if (s.chars().distinct().count() == 1) return false;
        try {
            int[] numbers = new int[14];
            for (int i = 0; i < 14; i++) numbers[i] = Integer.parseInt(s.substring(i, i+1));
            int sum = 0;
            int[] weights1 = {5,4,3,2,9,8,7,6,5,4,3,2};
            for (int i = 0; i < 12; i++) sum += numbers[i] * weights1[i];
            int mod = sum % 11;
            int expected1 = (mod < 2) ? 0 : 11 - mod;
            if (numbers[12] != expected1) return false;
            sum = 0;
            int[] weights2 = {6,5,4,3,2,9,8,7,6,5,4,3,2};
            for (int i = 0; i < 13; i++) sum += numbers[i] * weights2[i];
            mod = sum % 11;
            int expected2 = (mod < 2) ? 0 : 11 - mod;
            if (numbers[13] != expected2) return false;
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // Atalho estático utilizado pelos shims no pacote underscore
    public static boolean validateStatic(String cnpj) { return new AdvancedCnpjStrategy().validate(cnpj); }
}
