package dev.paul.cartlink.customer.dto;

public class CustomerProfileUpdateRequest {
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private AddressDto address;

    public CustomerProfileUpdateRequest() {
    }

    public CustomerProfileUpdateRequest(String firstName, String lastName, String phoneNumber, AddressDto address) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.address = address;
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

    public AddressDto getAddress() {
        return address;
    }

    public void setAddress(AddressDto address) {
        this.address = address;
    }

    public static class AddressDto {
        private String street;
        private String city;
        private String state;
        private String country;
        private String postalCode;

        public AddressDto() {
        }

        public AddressDto(String street, String city, String state, String country, String postalCode) {
            this.street = street;
            this.city = city;
            this.state = state;
            this.country = country;
            this.postalCode = postalCode;
        }

        public String getStreet() {
            return street;
        }

        public void setStreet(String street) {
            this.street = street;
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

        public String getCountry() {
            return country;
        }

        public void setCountry(String country) {
            this.country = country;
        }

        public String getPostalCode() {
            return postalCode;
        }

        public void setPostalCode(String postalCode) {
            this.postalCode = postalCode;
        }
    }
}