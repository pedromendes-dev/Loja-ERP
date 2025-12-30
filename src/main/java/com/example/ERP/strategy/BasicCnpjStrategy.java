package com.example.erp.strategy;

public class BasicCnpjStrategy implements CnpjStrategy {
    public BasicCnpjStrategy() {}

    @Override
    public boolean validate(String cnpj) {
        return isValid(cnpj);
    }

    public static boolean isValid(String cnpj) {
        if (cnpj == null) return false;
        String d = cnpj.replaceAll("\\D", "");
        if (d.length() != 14) return false;
        // reject all-equal sequences
        boolean allEquals = true;
        for (int i = 1; i < d.length(); i++) { if (d.charAt(i) != d.charAt(0)) { allEquals = false; break; } }
        if (allEquals) return false;

        try {
            int[] nums = new int[14];
            for (int i = 0; i < 14; i++) nums[i] = Character.getNumericValue(d.charAt(i));
            int v1 = calcDigit(nums, 12, new int[]{5,4,3,2,9,8,7,6,5,4,3,2});
            if (v1 != nums[12]) return false;
            int v2 = calcDigit(nums, 13, new int[]{6,5,4,3,2,9,8,7,6,5,4,3,2});
            return v2 == nums[13];
        } catch (Exception ex) {
            return false;
        }
    }

    private static int calcDigit(int[] nums, int length, int[] weights) {
        int sum = 0;
        for (int i = 0; i < length; i++) sum += nums[i] * weights[i];
        int mod = sum % 11;
        return (mod < 2) ? 0 : 11 - mod;
    }
}
