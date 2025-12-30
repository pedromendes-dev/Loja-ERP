package com.example.erp.strategy;

public class AdvancedCpfStrategy implements CpfStrategy {
    @Override
    public boolean validate(String cpf) {
        if (cpf == null) return false;
        String s = cpf.replaceAll("\\D", "");
        if (s.length() != 11) return false;
        if (s.chars().distinct().count() == 1) return false;
        try {
            int[] n = new int[11];
            for (int i = 0; i < 11; i++) n[i] = Integer.parseInt(s.substring(i,i+1));
            int sum = 0;
            for (int i = 0; i < 9; i++) sum += n[i] * (10 - i);
            int r = sum % 11;
            int d1 = (r < 2) ? 0 : 11 - r;
            if (n[9] != d1) return false;
            sum = 0;
            for (int i = 0; i < 10; i++) sum += n[i] * (11 - i);
            r = sum % 11;
            int d2 = (r < 2) ? 0 : 11 - r;
            if (n[10] != d2) return false;
            return true;
        } catch (Exception e) { return false; }
    }

    // Atalho estático utilizado pelos shims no pacote underscore
    public static boolean validateStatic(String cpf) { return new AdvancedCpfStrategy().validate(cpf); }
}
