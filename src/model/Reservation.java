package model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Reservation Model Class (POJO)
 * Represents a room booking made by a guest.
 * Part of Member 2: Reservations, Guests & Billing.
 */
public class Reservation {

    private int reservationId;
    private int guestId;
    private String guestName; // Joined from guests table for easy UI display
    private String roomNumber;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private BigDecimal roomRate;
    private String status; // CONFIRMED, CHECKED_IN, CHECKED_OUT, CANCELLED

    // Full constructor including guest name for UI display
    public Reservation(int reservationId, int guestId, String guestName, String roomNumber,
                       LocalDate checkInDate, LocalDate checkOutDate,
                       BigDecimal roomRate, String status) {
        this.reservationId = reservationId;
        this.guestId = guestId;
        this.guestName = guestName;
        this.roomNumber = roomNumber;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.roomRate = roomRate;
        this.status = status;
    }

    // Constructor for creating a new booking (before ID is assigned)
    public Reservation(int guestId, String roomNumber,
                       LocalDate checkInDate, LocalDate checkOutDate,
                       BigDecimal roomRate, String status) {
        this(0, guestId, "", roomNumber, checkInDate, checkOutDate, roomRate, status);
    }

    // Calculates total nights stayed (minimum 1 night)
    public long getNumberOfNights() {
        if (checkInDate == null || checkOutDate == null) {
            return 1;
        }
        long nights = ChronoUnit.DAYS.between(checkInDate, checkOutDate);
        return nights <= 0 ? 1 : nights;
    }

    // Calculates total room cost based on nights and nightly rate
    public BigDecimal calculateRoomTotal() {
        long nights = getNumberOfNights();
        return roomRate.multiply(BigDecimal.valueOf(nights));
    }

    // Getters and Setters
    public int getReservationId() {
        return reservationId;
    }

    public void setReservationId(int reservationId) {
        this.reservationId = reservationId;
    }

    public int getGuestId() {
        return guestId;
    }

    public void setGuestId(int guestId) {
        this.guestId = guestId;
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

    public LocalDate getCheckInDate() {
        return checkInDate;
    }

    public void setCheckInDate(LocalDate checkInDate) {
        this.checkInDate = checkInDate;
    }

    public LocalDate getCheckOutDate() {
        return checkOutDate;
    }

    public void setCheckOutDate(LocalDate checkOutDate) {
        this.checkOutDate = checkOutDate;
    }

    public BigDecimal getRoomRate() {
        return roomRate;
    }

    public void setRoomRate(BigDecimal roomRate) {
        this.roomRate = roomRate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Booking #" + reservationId + " - Room " + roomNumber + " (" + status + ")";
    }
}
