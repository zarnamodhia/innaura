package model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Billing Model Class (POJO)
 * Represents an invoice / bill generated when a guest checks out.
 * Part of Member 2: Reservations, Guests & Billing.
 */
public class Billing {

    private int billId;
    private int reservationId;
    private String guestName;   // Display helper
    private String roomNumber;  // Display helper
    private BigDecimal roomCharges;
    private BigDecimal serviceCharges;
    private BigDecimal discount;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private String paymentMode;    // CASH, CREDIT_CARD, UPI, DEBIT_CARD
    private String paymentStatus;  // PAID, PENDING
    private LocalDate billingDate;

    // Full constructor (retrieval from database)
    public Billing(int billId, int reservationId, String guestName, String roomNumber,
                   BigDecimal roomCharges, BigDecimal serviceCharges, BigDecimal discount,
                   BigDecimal taxAmount, BigDecimal totalAmount,
                   String paymentMode, String paymentStatus, LocalDate billingDate) {
        this.billId = billId;
        this.reservationId = reservationId;
        this.guestName = guestName;
        this.roomNumber = roomNumber;
        this.roomCharges = roomCharges;
        this.serviceCharges = serviceCharges;
        this.discount = discount;
        this.taxAmount = taxAmount;
        this.totalAmount = totalAmount;
        this.paymentMode = paymentMode;
        this.paymentStatus = paymentStatus;
        this.billingDate = billingDate;
    }

    // Constructor for creating a new bill
    public Billing(int reservationId, BigDecimal roomCharges, BigDecimal serviceCharges,
                   BigDecimal discount, BigDecimal taxAmount, BigDecimal totalAmount,
                   String paymentMode, String paymentStatus) {
        this(0, reservationId, "", "", roomCharges, serviceCharges, discount,
             taxAmount, totalAmount, paymentMode, paymentStatus, LocalDate.now());
    }

    // Helper method to compute total: (roomCharges + serviceCharges - discount) + tax
    public static BigDecimal calculateTotal(BigDecimal room, BigDecimal service, BigDecimal discount, BigDecimal taxRatePercent) {
        BigDecimal subtotal = room.add(service).subtract(discount);
        if (subtotal.compareTo(BigDecimal.ZERO) < 0) {
            subtotal = BigDecimal.ZERO;
        }
        BigDecimal tax = subtotal.multiply(taxRatePercent).divide(BigDecimal.valueOf(100));
        return subtotal.add(tax);
    }

    // Getters and Setters
    public int getBillId() {
        return billId;
    }

    public void setBillId(int billId) {
        this.billId = billId;
    }

    public int getReservationId() {
        return reservationId;
    }

    public void setReservationId(int reservationId) {
        this.reservationId = reservationId;
    }

    public String getGuestName() {
        return guestName;
    }

    public void setGuestName(String guestName) {
        this.guestName = guestName;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public BigDecimal getRoomCharges() {
        return roomCharges;
    }

    public void setRoomCharges(BigDecimal roomCharges) {
        this.roomCharges = roomCharges;
    }

    public BigDecimal getServiceCharges() {
        return serviceCharges;
    }

    public void setServiceCharges(BigDecimal serviceCharges) {
        this.serviceCharges = serviceCharges;
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public void setDiscount(BigDecimal discount) {
        this.discount = discount;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(BigDecimal taxAmount) {
        this.taxAmount = taxAmount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getPaymentMode() {
        return paymentMode;
    }

    public void setPaymentMode(String paymentMode) {
        this.paymentMode = paymentMode;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public LocalDate getBillingDate() {
        return billingDate;
    }

    public void setBillingDate(LocalDate billingDate) {
        this.billingDate = billingDate;
    }
}
