
package dao;

import database.DBConnection;
import model.Staff;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class StaffDAO {

    // Add a new staff member
    public boolean addStaff(Staff staff) throws SQLException {

        String sql = "INSERT INTO staff " +
                "(full_name, role, phone, email, salary, joining_date, active) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, staff.getFullName());
            ps.setString(2, staff.getRole());
            ps.setString(3, staff.getPhone());
            ps.setString(4, staff.getEmail());
            ps.setBigDecimal(5, staff.getSalary());
            ps.setDate(6, Date.valueOf(staff.getJoiningDate()));
            ps.setBoolean(7, staff.isActive());

            return ps.executeUpdate() > 0;
        }
    }

    // Retrieve all staff members
    public List<Staff> getAllStaff() throws SQLException {

        List<Staff> staffList = new ArrayList<>();

        String sql = "SELECT staff_id, full_name, role, phone, email, " +
                "salary, joining_date, active FROM staff ORDER BY staff_id";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                Staff staff = new Staff(
                        rs.getInt("staff_id"),
                        rs.getString("full_name"),
                        rs.getString("role"),
                        rs.getString("phone"),
                        rs.getString("email"),
                        rs.getBigDecimal("salary"),
                        rs.getDate("joining_date").toLocalDate(),
                        rs.getBoolean("active")
                );

                staffList.add(staff);
            }
        }

        return staffList;
    }

    // Update an existing staff member
    public boolean updateStaff(Staff staff) throws SQLException {

        String sql = "UPDATE staff SET full_name = ?, role = ?, " +
                "phone = ?, email = ?, salary = ?, joining_date = ?, " +
                "active = ? WHERE staff_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, staff.getFullName());
            ps.setString(2, staff.getRole());
            ps.setString(3, staff.getPhone());
            ps.setString(4, staff.getEmail());
            ps.setBigDecimal(5, staff.getSalary());
            ps.setDate(6, Date.valueOf(staff.getJoiningDate()));
            ps.setBoolean(7, staff.isActive());
            ps.setInt(8, staff.getStaffId());

            return ps.executeUpdate() > 0;
        }
    }

    // Deactivate a staff member without deleting the record
    public boolean deactivateStaff(int staffId) throws SQLException {

        String sql = "UPDATE staff SET active = FALSE WHERE staff_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, staffId);
            return ps.executeUpdate() > 0;
        }
    }
}
