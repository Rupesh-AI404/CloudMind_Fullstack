package com.cloudmind.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "subscribers")
public class Subscriber {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Personal Information
    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "company_name")
    private String companyName;

    @Column(name = "email")
    private String email;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "address")
    private String address;

    @Column(name = "city")
    private String city;

    @Column(name = "state")
    private String state;

    @Column(name = "country")
    private String country;

    @Column(name = "postal_code")
    private String postalCode;

    // Subscription Details
    @Column(name = "plan")
    private String plan;

    @Column(name = "billing")
    private String billing;

    @Column(name = "payment_method")
    private String paymentMethod;

    @Column(name = "status")
    private String status = "PENDING";

    @Column(name = "subscription_date")
    private LocalDateTime subscriptionDate;

    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;

    // Payment Information
    @Column(name = "card_number")
    private String cardNumber;

    @Column(name = "card_last4")
    private String cardLast4;

    @Column(name = "card_fingerprint_hash")
    private String cardFingerprintHash;

    @Column(name = "expiry_month")
    private String expiryMonth;

    @Column(name = "expiry_year")
    private String expiryYear;

    @Column(name = "cvv")
    private String cvv;

    @Column(name = "cardholder_name")
    private String cardholderName;

    // Wallet Information
    @Column(name = "wallet_phone_masked")
    private String walletPhoneMasked;

    @Column(name = "wallet_phone_hash")
    private String walletPhoneHash;

    // Default constructor
    public Subscriber() {}

    // Generate ALL getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }

    public String getPlan() { return plan; }
    public void setPlan(String plan) { this.plan = plan; }

    public String getBilling() { return billing; }
    public void setBilling(String billing) { this.billing = billing; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getSubscriptionDate() { return subscriptionDate; }
    public void setSubscriptionDate(LocalDateTime subscriptionDate) { this.subscriptionDate = subscriptionDate; }

    public LocalDateTime getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDateTime expiryDate) { this.expiryDate = expiryDate; }

    public String getCardNumber() { return cardNumber; }
    public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }

    public String getCardLast4() { return cardLast4; }
    public void setCardLast4(String cardLast4) { this.cardLast4 = cardLast4; }

    public String getCardFingerprintHash() { return cardFingerprintHash; }
    public void setCardFingerprintHash(String cardFingerprintHash) { this.cardFingerprintHash = cardFingerprintHash; }

    public String getExpiryMonth() { return expiryMonth; }
    public void setExpiryMonth(String expiryMonth) { this.expiryMonth = expiryMonth; }

    public String getExpiryYear() { return expiryYear; }
    public void setExpiryYear(String expiryYear) { this.expiryYear = expiryYear; }

    public String getCvv() { return cvv; }
    public void setCvv(String cvv) { this.cvv = cvv; }

    public String getCardholderName() { return cardholderName; }
    public void setCardholderName(String cardholderName) { this.cardholderName = cardholderName; }

    public String getWalletPhoneMasked() { return walletPhoneMasked; }
    public void setWalletPhoneMasked(String walletPhoneMasked) { this.walletPhoneMasked = walletPhoneMasked; }

    public String getWalletPhoneHash() { return walletPhoneHash; }
    public void setWalletPhoneHash(String walletPhoneHash) { this.walletPhoneHash = walletPhoneHash; }


    @Column(name = "website")
    private String website;

    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }


    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Column(name = "created_at")
    private LocalDateTime createdAt;

}