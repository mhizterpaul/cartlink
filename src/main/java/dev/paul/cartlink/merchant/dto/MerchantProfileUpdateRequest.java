package dev.paul.cartlink.merchant.dto;

public class MerchantProfileUpdateRequest {
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String image;

    public MerchantProfileUpdateRequest() {
    }

    public MerchantProfileUpdateRequest(String firstName, String lastName, String phoneNumber, String image) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.image = image;
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

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}