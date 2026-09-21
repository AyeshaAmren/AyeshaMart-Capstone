package com.ayeshamart.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Shipping address captured on the checkout page (Phase 7).
 *
 * <p>This is a delivery snapshot: the values are copied into the order at
 * placement time so the original address is preserved even if the buyer
 * later edits their profile. It is a transient request DTO and is never
 * written anywhere except the orders table columns.
 */
public class ShippingDetails {

    private String fullName;
    private String phone;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String pincode;
    private String landmark;

    public ShippingDetails() {
    }

    public ShippingDetails(String fullName, String phone, String addressLine1, String addressLine2,
                           String city, String state, String pincode, String landmark) {
        this.fullName = fullName;
        this.phone = phone;
        this.addressLine1 = addressLine1;
        this.addressLine2 = addressLine2;
        this.city = city;
        this.state = state;
        this.pincode = pincode;
        this.landmark = landmark;
    }

    /** Returns all validation problems; an empty list means the details are valid. */
    public List<String> validate() {
        List<String> errors = new ArrayList<>();
        if (fullName == null || fullName.isBlank()) {
            errors.add("Full name is required");
        }
        if (phone == null || !phone.trim().matches("\\d{10,13}")) {
            errors.add("Enter a valid 10-digit phone number");
        }
        if (addressLine1 == null || addressLine1.isBlank()) {
            errors.add("Address line 1 is required");
        }
        if (city == null || city.isBlank()) {
            errors.add("City is required");
        }
        if (state == null || state.isBlank()) {
            errors.add("State is required");
        }
        if (pincode == null || !pincode.trim().matches("\\d{6}")) {
            errors.add("Enter a valid 6-digit pincode");
        }
        return errors;
    }

    public boolean isValid() {
        return validate().isEmpty();
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddressLine1() {
        return addressLine1;
    }

    public void setAddressLine1(String addressLine1) {
        this.addressLine1 = addressLine1;
    }

    public String getAddressLine2() {
        return addressLine2;
    }

    public void setAddressLine2(String addressLine2) {
        this.addressLine2 = addressLine2;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getPincode() {
        return pincode;
    }

    public void setPincode(String pincode) {
        this.pincode = pincode;
    }

    public String getLandmark() {
        return landmark;
    }

    public void setLandmark(String landmark) {
        this.landmark = landmark;
    }
}