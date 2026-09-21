package com.ayeshamart.dto;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Server-side checkout validation tests (Phase 7). These guard the rules a
 * buyer can never bypass from the browser.
 */
class CheckoutDetailsTest {

    // ------------------------------ shipping ------------------------------

    private ShippingDetails validShipping() {
        return new ShippingDetails("Sara Buyer", "9876543210", "12 Main Road",
                "Block C", "Chennai", "Tamil Nadu", "600001", "Near Metro");
    }

    @Test
    void validShippingHasNoErrors() {
        assertTrue(validShipping().isValid());
        assertTrue(validShipping().validate().isEmpty());
    }

    @Test
    void blankRequiredFieldsAreRejected() {
        ShippingDetails empty = new ShippingDetails("", "", "", "", "", "", "", "");
        List<String> errors = empty.validate();
        assertFalse(errors.isEmpty());
        assertTrue(errors.contains("Full name is required"));
        assertTrue(errors.contains("Address line 1 is required"));
        assertTrue(errors.contains("City is required"));
        assertTrue(errors.contains("State is required"));
    }

    @Test
    void phoneMustBeTenToThirteenDigits() {
        assertFalse(new ShippingDetails("Sara", "12345", "1 Rd", "", "Chennai", "TN", "600001", null).isValid());
        assertFalse(new ShippingDetails("Sara", "12345hello", "1 Rd", "", "Chennai", "TN", "600001", null).isValid());
        assertTrue(new ShippingDetails("Sara", "919876543210", "1 Rd", "", "Chennai", "TN", "600001", null).isValid());
    }

    @Test
    void pincodeMustBeExactlySixDigits() {
        assertFalse(new ShippingDetails("Sara", "9876543210", "1 Rd", "", "Chennai", "TN", "60000", null).isValid());
        assertFalse(new ShippingDetails("Sara", "9876543210", "1 Rd", "", "Chennai", "TN", "6000011", null).isValid());
        assertFalse(new ShippingDetails("Sara", "9876543210", "1 Rd", "", "Chennai", "TN", "60a001", null).isValid());
        assertTrue(new ShippingDetails("Sara", "9876543210", "1 Rd", "", "Chennai", "TN", "600001", null).isValid());
    }

    // ------------------------------ payment ------------------------------

    @Test
    void nullOrBlankMethodIsRejected() {
        assertNotNullError(new PaymentDetails(null, null, null, null, null, null));
        assertNotNullError(new PaymentDetails("", null, null, null, null, null));
        assertNotNullError(new PaymentDetails("   ", null, null, null, null, null));
    }

    @Test
    void unknownMethodIsRejected() {
        assertEquals("Unsupported payment method",
                new PaymentDetails("CASH", null, null, null, null, null).validate());
    }

    @Test
    void upiAndGpayRequireAnIdWithAtSign() {
        assertEquals("Enter a valid UPI ID (yourid@bankname)",
                new PaymentDetails("UPI", null, null, null, null, null).validate());
        assertEquals("Enter a valid UPI ID (yourid@bankname)",
                new PaymentDetails("UPI", "sara", null, null, null, null).validate());
        assertEquals("Enter a valid UPI ID (yourid@bankname)",
                new PaymentDetails("GPAY", "1234", null, null, null, null).validate());
        assertNull(new PaymentDetails("upi", "sara@okbank", null, null, null, null).validate());
    }

    @Test
    void cardRequiresAllFields() {
        assertTrue(new PaymentDetails("CARD", null, "Sara", "4111 1111 1111 1111", "12/29", "123").validate()
                == null);
        assertTrue(new PaymentDetails("card", null, "Sara", "4111111111111111", "12/29", "123").validate()
                == null);
    }

    @Test
    void codNeedsNoDetails() {
        assertNull(PaymentDetails.cod().validate());
        assertEquals("COD", PaymentDetails.cod().normalizedMethod());
    }

    private void assertNotNullError(PaymentDetails details) {
        assertEquals("Select a payment method", details.validate());
    }
}