package br.com.bergamin.patterns.shipping;

/** Regras simples de CEP usadas pelas politicas de frete. */
public final class ZipCodes {

    private ZipCodes() {
    }

    public static String digitsOf(String zipCode) {
        return zipCode == null ? "" : zipCode.replaceAll("[^0-9]", "");
    }

    public static boolean isValid(String zipCode) {
        return digitsOf(zipCode).length() == 8;
    }

    /** Faixas 01000-39999 cobrem SP, RJ, ES e MG. */
    public static boolean isSoutheast(String zipCode) {
        String digits = digitsOf(zipCode);
        if (digits.length() != 8) {
            return false;
        }
        int prefix = Integer.parseInt(digits.substring(0, 2));
        return prefix >= 1 && prefix <= 39;
    }
}
