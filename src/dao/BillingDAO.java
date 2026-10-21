
package dao;

import database.DBConnection;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import model.Billing;

public class BillingDAO {

    public boolean createBill(Billing bill) throws SQLException {
        String lockSql = """
            SELECT status FROM reservations
            WHERE reservation_id = ? FOR UPDATE
            """;

        String duplicateSql =
            "SELECT bill_id FROM bills WHERE reservation_id = ? LIMIT 1";

        String insertSql = """
            INSERT INTO bills
            (reservation_id, room_charges, service_charges, discount,
             tax_amount, total_amount, payment_mode, payment_status)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;

        String updateSql = """
            UPDATE reservations SET status = 'CHECKED_OUT'
            WHERE reservation_id = ?
            """;

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);

            try {
                // Lock the reservation while checkout is processed.
                String status;

                try (PreparedStatement ps = con.prepareStatement(lockSql)) {
                    ps.setInt(1, bill.getReservationId());

                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new SQLException("Reservation not found.");
                        }
                        status = rs.getString("status");
                    }
                }

                if ("CHECKED_OUT".equalsIgnoreCase(status)) {
                    throw new SQLException(
                        "This reservation has already been checked out."
                    );
                }

                if ("CANCELLED".equalsIgnoreCase(status)) {
                    throw new SQLException(
                        "A cancelled reservation cannot be billed."
                    );
                }

                // Prevent more than one bill per reservation.
                try (PreparedStatement ps = con.prepareStatement(duplicateSql)) {
                    ps.setInt(1, bill.getReservationId());

                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            throw new SQLException(
                                "A bill already exists for this reservation."
                            );
                        }
                    }
                }

                // Validate amounts before inserting.
                if (bill.getRoomCharges() == null
                        || bill.getServiceCharges() == null
                        || bill.getDiscount() == null
                        || bill.getTaxAmount() == null
                        || bill.getTotalAmount() == null) {
                    throw new SQLException("Bill amounts cannot be empty.");
                }

                if (bill.getRoomCharges().signum() < 0
                        || bill.getServiceCharges().signum() < 0
                        || bill.getDiscount().signum() < 0
                        || bill.getTaxAmount().signum() < 0
                        || bill.getTotalAmount().signum() < 0) {
                    throw new SQLException("Bill amounts cannot be negative.");
                }

                try (PreparedStatement ps = con.prepareStatement(
                        insertSql, Statement.RETURN_GENERATED_KEYS)) {

                    ps.setInt(1, bill.getReservationId());
                    ps.setBigDecimal(2, bill.getRoomCharges());
                    ps.setBigDecimal(3, bill.getServiceCharges());
                    ps.setBigDecimal(4, bill.getDiscount());
                    ps.setBigDecimal(5, bill.getTaxAmount());
                    ps.setBigDecimal(6, bill.getTotalAmount());
                    ps.setString(7, bill.getPaymentMode());
                    ps.setString(8, bill.getPaymentStatus());

                    if (ps.executeUpdate() != 1) {
                        throw new SQLException("Bill could not be created.");
                    }

                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new SQLException(
                                "Bill was saved but its ID could not be retrieved."
                            );
                        }
                        bill.setBillId(keys.getInt(1));
                    }
                }

                try (PreparedStatement ps = con.prepareStatement(updateSql)) {
                    ps.setInt(1, bill.getReservationId());

                    if (ps.executeUpdate() != 1) {
                        throw new SQLException(
                            "Checkout failed. Reservation status was not updated."
                        );
                    }
                }

                con.commit();
                return true;

            } catch (SQLException | RuntimeException ex) {
                try {
                    con.rollback();
                } catch (SQLException rollbackError) {
                    ex.addSuppressed(rollbackError);
                }
                throw ex;
            } finally {
                try {
                    con.setAutoCommit(true);
                } catch (SQLException ignored) {
                    // Connection closes immediately afterward.
                }
            }
        }
    }

    public List<Billing> getAllBills() throws SQLException {
        List<Billing> list = new ArrayList<>();

        String sql = """
            SELECT b.bill_id, b.reservation_id,
                   g.full_name AS guest_name, r.room_number,
                   b.room_charges, b.service_charges, b.discount,
                   b.tax_amount, b.total_amount, b.payment_mode,
                   b.payment_status, DATE(b.billing_date) AS bill_date
            FROM bills b
            JOIN reservations r ON b.reservation_id = r.reservation_id
            JOIN guests g ON r.guest_id = g.guest_id
            ORDER BY b.bill_id DESC
            """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Date date = rs.getDate("bill_date");

                list.add(new Billing(
                    rs.getInt("bill_id"),
                    rs.getInt("reservation_id"),
                    rs.getString("guest_name"),
                    rs.getString("room_number"),
                    rs.getBigDecimal("room_charges"),
                    rs.getBigDecimal("service_charges"),
                    rs.getBigDecimal("discount"),
                    rs.getBigDecimal("tax_amount"),
                    rs.getBigDecimal("total_amount"),
                    rs.getString("payment_mode"),
                    rs.getString("payment_status"),
                    date != null ? date.toLocalDate() : null
                ));
            }
        }

        return list;
    }

    public boolean updatePaymentStatus(
            int billId, String paymentStatus, String paymentMode)
            throws SQLException {

        String sql = """
            UPDATE bills
            SET payment_status = ?, payment_mode = ?
            WHERE bill_id = ?
            """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, paymentStatus);
            ps.setString(2, paymentMode);
            ps.setInt(3, billId);

            return ps.executeUpdate() > 0;
        }
    }
}
