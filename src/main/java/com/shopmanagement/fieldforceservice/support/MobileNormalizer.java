package com.shopmanagement.fieldforceservice.support;

public final class MobileNormalizer {

    private MobileNormalizer() {
    }

    /** Keeps last 10 digits for Indian mobile matching. */
    public static String normalize(String mobile) {
        if (mobile == null) {
            return "";
        }
        String digits = mobile.replaceAll("\\D", "");
        if (digits.length() > 10) {
            return digits.substring(digits.length() - 10);
        }
        return digits;
    }
}
