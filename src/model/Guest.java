package model;

/**
 * Guest Model Class (POJO - Plain Old Java Object)
 * Represents a customer staying at the hotel.
 * Part of Member 2: Reservations, Guests & Billing.
 */
public class Guest {

    private int guestId;
    private String fullName;
    private String email;
    private String phone;
    private String idProof;
    private String address;

    // Full constructor (retrieval from database)
    public Guest(int guestId, String fullName, String email, String phone, String idProof, String address) {
        this.guestId = guestId;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.idProof = idProof;
        this.address = address;
    }

    // Constructor without ID (for creating a new guest before DB generates ID)
    public Guest(String fullName, String email, String phone, String idProof, String address) {
        this(0, fullName, email, phone, idProof, address);
    }

    // Getters and Setters (Encapsulation)
    public int getGuestId() {
        return guestId;
    }

    public void setGuestId(int guestId) {
        this.guestId = guestId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getIdProof() {
        return idProof;
    }

    public void setIdProof(String idProof) {
        this.idProof = idProof;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public String toString() {
        return fullName + " (ID: " + guestId + " | Phone: " + phone + ")";
    }
}
