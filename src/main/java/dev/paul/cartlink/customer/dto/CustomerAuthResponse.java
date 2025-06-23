package dev.paul.cartlink.customer.dto;

public class CustomerAuthResponse {
    private String token;
    private CustomerDetails customerDetails;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public CustomerDetails getCustomerDetails() {
        return customerDetails;
    }

    public void setCustomerDetails(CustomerDetails customerDetails) {
        this.customerDetails = customerDetails;
    }

    public static class CustomerDetails {
        private String email;
        private String firstName;
        private String lastName;
        private String phoneNumber;
        private CustomerProfileUpdateRequest.AddressDto address;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
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

        public CustomerProfileUpdateRequest.AddressDto getAddress() {
            return address;
        }

        public void setAddress(CustomerProfileUpdateRequest.AddressDto address) {
            this.address = address;
        }
    }
}