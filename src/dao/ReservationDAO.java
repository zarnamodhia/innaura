package dao;

import database.DBConnection;
import model.Reservation;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * ReservationDAO - Data Access Object for Room Reservations.
 * Manages booking creation, status transitions (CHECKED_IN, CHECKED_OUT, CANCELLED).
 */
public class ReservationDAO {

    // CREATE: Book a new reservation
    
public boolean addReservation(Reservation res) throws SQLException {
    if (res.getCheckInDate() == null || res.getCheckOutDate() == null) {
        throw new IllegalArgumentException("Check-in and check-out dates are required.");
    }

    if (!res.getCheckOutDate().isAfter(res.getCheckInDate())) {
        throw new IllegalArgumentException(
            "Check-out date must be after check-in date."
        );
    }

    if (res.getRoomRate() == null ||
            res.getRoomRate().signum() <= 0) {
        throw new IllegalArgumentException(
            "Room rate must be greater than zero."
        );
    }

    String overlapSql = """
        SELECT COUNT(*) FROM reservations
        WHERE room_number = ?
          AND status <> 'CANCELLED'
          AND check_in_date < ?
          AND check_out_date > ?
        """;

    String insertSql = """
        INSERT INTO reservations
        (guest_id, room_number, check_in_date, check_out_date,
         room_rate, status)
        VALUES (?, ?, ?, ?, ?, ?)
        """;

    try (Connection con = DBConnection.getConnection()) {
        try (PreparedStatement check = con.prepareStatement(overlapSql)) {
            check.setString(1, res.getRoomNumber());
            check.setDate(2, Date.valueOf(res.getCheckOutDate()));
            check.setDate(3, Date.valueOf(res.getCheckInDate()));

            try (ResultSet rs = check.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    throw new IllegalArgumentException(
                        "This room is already booked for the selected dates."
                    );
                }
            }
        }

        try (PreparedStatement ps = con.prepareStatement(
                insertSql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, res.getGuestId());
            ps.setString(2, res.getRoomNumber());
            ps.setDate(3, Date.valueOf(res.getCheckInDate()));
            ps.setDate(4, Date.valueOf(res.getCheckOutDate()));
            ps.setBigDecimal(5, res.getRoomRate());
            ps.setString(6, res.getStatus());

            if (ps.executeUpdate() != 1) {
                return false;
            }

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    res.setReservationId(keys.getInt(1));
                }
            }

            return res.getReservationId() > 0;
        }
    }
}


    // READ: Retrieve all reservations with guest names joined
    public List<Reservation> getAllReservations() throws SQLException {
        List<Reservation> list = new ArrayList<>();
        String sql = """
            SELECT r.reservation_id, r.guest_id, g.full_name AS guest_name,
                   r.room_number, r.check_in_date, r.check_out_date,
                   r.room_rate, r.status
            FROM reservations r
            JOIN guests g ON r.guest_id = g.guest_id
            ORDER BY r.reservation_id DESC
            """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Reservation res = new Reservation(
                    rs.getInt("reservation_id"),
                    rs.getInt("guest_id"),
                    rs.getString("guest_name"),
                    rs.getString("room_number"),
                    rs.getDate("check_in_date").toLocalDate(),
                    rs.getDate("check_out_date").toLocalDate(),
                    rs.getBigDecimal("room_rate"),
                    rs.getString("status")
                );
                list.add(res);
            }
        }
        return list;
    }

    // READ: Get single reservation by ID
    public Reservation getReservationById(int reservationId) throws SQLException {
        String sql = """
            SELECT r.reservation_id, r.guest_id, g.full_name AS guest_name,
                   r.room_number, r.check_in_date, r.check_out_date,
                   r.room_rate, r.status
            FROM reservations r
            JOIN guests g ON r.guest_id = g.guest_id
            WHERE r.reservation_id = ?
            """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, reservationId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Reservation(
                        rs.getInt("reservation_id"),
                        rs.getInt("guest_id"),
                        rs.getString("guest_name"),
                        rs.getString("room_number"),
                        rs.getDate("check_in_date").toLocalDate(),
                        rs.getDate("check_out_date").toLocalDate(),
                        rs.getBigDecimal("room_rate"),
                        rs.getString("status")
                    );
                }
            }
        }
        return null;
    }

    // UPDATE: Update status (e.g., CHECKED_IN, CHECKED_OUT, CANCELLED)
    public boolean updateStatus(int reservationId, String newStatus) throws SQLException {
        String sql = "UPDATE reservations SET status = ? WHERE reservation_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, newStatus);
            ps.setInt(2, reservationId);
            return ps.executeUpdate() > 0;
        }
    }

    // UPDATE: Update full reservation
    public boolean updateReservation(Reservation res) throws SQLException {
        String sql = """
            UPDATE reservations
            SET room_number = ?, check_in_date = ?, check_out_date = ?, room_rate = ?, status = ?
            WHERE reservation_id = ?
            """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, res.getRoomNumber());
            ps.setDate(2, Date.valueOf(res.getCheckInDate()));
            ps.setDate(3, Date.valueOf(res.getCheckOutDate()));
            ps.setBigDecimal(4, res.getRoomRate());
            ps.setString(5, res.getStatus());
            ps.setInt(6, res.getReservationId());

            return ps.executeUpdate() > 0;
        }
    }

    // Retrieve active checked-in or confirmed reservations eligible for checkout/billing
    public List<Reservation> getActiveReservations() throws SQLException {
        List<Reservation> list = new ArrayList<>();
        String sql = """
            SELECT r.reservation_id, r.guest_id, g.full_name AS guest_name,
                   r.room_number, r.check_in_date, r.check_out_date,
                   r.room_rate, r.status
            FROM reservations r
            JOIN guests g ON r.guest_id = g.guest_id
            WHERE r.status IN ('CONFIRMED', 'CHECKED_IN')
            ORDER BY r.reservation_id DESC
            """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Reservation res = new Reservation(
                    rs.getInt("reservation_id"),
                    rs.getInt("guest_id"),
                    rs.getString("guest_name"),
                    rs.getString("room_number"),
                    rs.getDate("check_in_date").toLocalDate(),
                    rs.getDate("check_out_date").toLocalDate(),
                    rs.getBigDecimal("room_rate"),
                    rs.getString("status")
                );
                list.add(res);
            }
        }
        return list;
    }
}
