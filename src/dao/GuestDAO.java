package dao;

import database.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import model.Guest;

/**
 * GuestDAO - Data Access Object for Guest operations.
 * Implements CRUD (Create, Read, Update, Delete) and Search.
 */
public class GuestDAO {

    // CREATE: Add a new guest
    public boolean addGuest(Guest guest) throws SQLException {
        String sql = """
            INSERT INTO guests (full_name, email, phone, id_proof, address)
            VALUES (?, ?, ?, ?, ?)
            """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, guest.getFullName());
            ps.setString(2, guest.getEmail());
            ps.setString(3, guest.getPhone());
            ps.setString(4, guest.getIdProof());
            ps.setString(5, guest.getAddress());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        guest.setGuestId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
            return false;
        }
    }

    // READ: Retrieve all guests
    public List<Guest> getAllGuests() throws SQLException {
        List<Guest> list = new ArrayList<>();
        String sql = "SELECT guest_id, full_name, email, phone, id_proof, address FROM guests ORDER BY guest_id";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Guest guest = new Guest(
                    rs.getInt("guest_id"),
                    rs.getString("full_name"),
                    rs.getString("email"),
                    rs.getString("phone"),
                    rs.getString("id_proof"),
                    rs.getString("address")
                );
                list.add(guest);
            }
        }
        return list;
    }

    // READ: Get single guest by ID
    public Guest getGuestById(int guestId) throws SQLException {
        String sql = "SELECT guest_id, full_name, email, phone, id_proof, address FROM guests WHERE guest_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, guestId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Guest(
                        rs.getInt("guest_id"),
                        rs.getString("full_name"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getString("id_proof"),
                        rs.getString("address")
                    );
                }
            }
        }
        return null;
    }

    // UPDATE: Update guest details
    public boolean updateGuest(Guest guest) throws SQLException {
        String sql = """
            UPDATE guests
            SET full_name = ?, email = ?, phone = ?, id_proof = ?, address = ?
            WHERE guest_id = ?
            """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, guest.getFullName());
            ps.setString(2, guest.getEmail());
            ps.setString(3, guest.getPhone());
            ps.setString(4, guest.getIdProof());
            ps.setString(5, guest.getAddress());
            ps.setInt(6, guest.getGuestId());

            return ps.executeUpdate() > 0;
        }
    }

    // DELETE: Delete a guest
    
public boolean deleteGuest(int guestId) throws SQLException {
    String checkSql =
        "SELECT COUNT(*) FROM reservations WHERE guest_id = ?";
    String deleteSql =
        "DELETE FROM guests WHERE guest_id = ?";

    try (Connection con = DBConnection.getConnection()) {
        try (PreparedStatement check = con.prepareStatement(checkSql)) {
            check.setInt(1, guestId);

            try (ResultSet rs = check.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    throw new SQLException(
                        "This guest has reservation history and cannot be deleted. " +
                        "Preserve the records and update the guest details instead."
                    );
                }
            }
        }

        try (PreparedStatement ps = con.prepareStatement(deleteSql)) {
            ps.setInt(1, guestId);
            return ps.executeUpdate() > 0;
        }
    }
}

}
