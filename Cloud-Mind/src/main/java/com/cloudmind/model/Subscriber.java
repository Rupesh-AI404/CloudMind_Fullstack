package com.cloudmind.model;

import jakarta.persistence.*;

@Entity
@Table(name = "subscribers")
public class Subscriber {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;
    private String lastName;
    private String email;
    private String companyName;
    private String phoneNumber;
    private String website;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getPlan() {
        return plan;
    }

    public void setPlan(String plan) {
        this.plan = plan;
    }

    public String getBilling() {
        return billing;
    }

    public void setBilling(String billing) {
        this.billing = billing;
    }

    private String plan; // Add these if not present
    private String billing;

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    // monthly|annually
    private String paymentMethod; //


    // ...
    @Column(name = "card_last4")            private String cardLast4;
    @Column(name = "card_fingerprint_hash") private String cardFingerprintHash;
    @Column(name = "wallet_phone_masked")   private String walletPhoneMasked;
    @Column(name = "wallet_phone_hash")     private String walletPhoneHash;
// + getters/setters


    public String getCardLast4() {
        return cardLast4;
    }

    public void setCardLast4(String cardLast4) {
        this.cardLast4 = cardLast4;
    }

    public String getCardFingerprintHash() {
        return cardFingerprintHash;
    }

    public void setCardFingerprintHash(String cardFingerprintHash) {
        this.cardFingerprintHash = cardFingerprintHash;
    }

    public String getWalletPhoneMasked() {
        return walletPhoneMasked;
    }

    public void setWalletPhoneMasked(String walletPhoneMasked) {
        this.walletPhoneMasked = walletPhoneMasked;
    }

    public String getWalletPhoneHash() {
        return walletPhoneHash;
    }

    public void setWalletPhoneHash(String walletPhoneHash) {
        this.walletPhoneHash = walletPhoneHash;
    }
}