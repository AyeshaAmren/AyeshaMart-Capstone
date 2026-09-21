package com.ayeshamart.dto;

/**
 * Mock payment selection made at checkout (Phase 7).
 *
 * <p>UPI / Google Pay / Card / Cash on Delivery. This is a DEMO only:
 * the raw form values are validated here and then thrown away. Only the
 * safe metadata (method, status, reference) is stored on the order -
 * card number, CVV, OTP and banking passwords are never persisted.
 */
public class PaymentDetails {

    public static final String METHOD_UPI = "UPI";
    public static final String METHOD_GPAY = "GPAY";
    public static final String METHOD_CARD = "CARD";
    public static final String METHOD_COD = "COD";

    private String method;
    private String upiId;
    private String cardName;
    private String cardNumber;
    private String expiry;
    private String cvv;

    public PaymentDetails() {
    }

    public PaymentDetails(String method, String upiId, String cardName,
                          String cardNumber, String expiry, String cvv) {
        this.method = method;
        this.upiId = upiId;
        this.cardName = cardName;
        this.cardNumber = cardNumber;
        this.expiry = expiry;
        this.cvv = cvv;
    }

    /** A COD payment requires no details and always starts as PENDING. */
    public static PaymentDetails cod() {
        return new PaymentDetails(METHOD_COD, null, null, null, null, null);
    }

    /**
     * Server-side demo validation. Returns an error message, or null when
     * the selection is acceptable. Nothing here is ever written to disk.
     */
    public String validate() {
        String selected = method == null ? null : method.trim().toUpperCase();
        if (selected == null || selected.isBlank()) {
            return "Select a payment method";
        }
        if (!METHOD_UPI.equals(selected) && !METHOD_GPAY.equals(selected)
                && !METHOD_CARD.equals(selected) && !METHOD_COD.equals(selected)) {
            return "Unsupported payment method";
        }

        if (METHOD_UPI.equals(selected) || METHOD_GPAY.equals(selected)) {
            if (upiId == null || !upiId.trim().contains("@")) {
                return "Enter a valid UPI ID (yourid@bankname)";
            }
            return null;
        }

        if (METHOD_CARD.equals(selected)) {
            if (cardName == null || cardName.isBlank()) {
                return "Cardholder name is required";
            }
            if (cardNumber == null || !cardNumber.replaceAll("\\s", "").matches("\\d{12,19}")) {
                return "Enter a valid card number (12-19 digits)";
            }
            if (expiry == null || !expiry.replaceAll("\\s", "").matches("(0[1-9]|1[0-2])/\\d{2}")) {
                return "Enter a valid expiry date (MM/YY)";
            }
            if (cvv == null || !cvv.trim().matches("\\d{3,4}")) {
                return "Enter a valid CVV (3-4 digits)";
            }
            return null;
        }

        return null; // COD
    }

    /** Normalized (upper-case) method, or the raw value when blank. */
    public String normalizedMethod() {
        return method == null ? null : method.trim().toUpperCase();
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getUpiId() {
        return upiId;
    }

    public void setUpiId(String upiId) {
        this.upiId = upiId;
    }

    public String getCardName() {
        return cardName;
    }

    public void setCardName(String cardName) {
        this.cardName = cardName;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getExpiry() {
        return expiry;
    }

    public void setExpiry(String expiry) {
        this.expiry = expiry;
    }

    public String getCvv() {
        return cvv;
    }

    public void setCvv(String cvv) {
        this.cvv = cvv;
    }
}