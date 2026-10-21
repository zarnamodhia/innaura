
package dao;

import database.DBConnection;
import model.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.List;

public class ServiceDAO {

    // CREATE: Add a new service
    public boolean addService(Service service) throws SQLException {

        String sql = """
            INSERT INTO hotel_services
            (service_name, description, category, price, available)
            VALUES (?, ?, ?, ?, ?)
            """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, service.getServiceName());
            ps.setString(2, service.getDescription());
            ps.setString(3, service.getCategory());
            ps.setBigDecimal(4, service.getPrice());
            ps.setBoolean(5, service.isAvailable());

            return ps.executeUpdate() == 1;
        }
    }

    // READ: Retrieve all services
    public List<Service> getAllServices() throws SQLException {

        List<Service> services = new ArrayList<>();

        String sql = """
            SELECT service_id, service_name, description,
                   category, price, available
            FROM hotel_services
            ORDER BY service_id
            """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                Service service = new Service(
                    rs.getInt("service_id"),
                    rs.getString("service_name"),
                    rs.getString("description"),
                    rs.getString("category"),
                    rs.getBigDecimal("price"),
                    rs.getBoolean("available")
                );

                services.add(service);
            }
        }

        return services;
    }

    // UPDATE: Modify an existing service
    public boolean updateService(Service service) throws SQLException {

        String sql = """
            UPDATE hotel_services
            SET service_name = ?,
                description = ?,
                category = ?,
                price = ?,
                available = ?
            WHERE service_id = ?
            """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, service.getServiceName());
            ps.setString(2, service.getDescription());
            ps.setString(3, service.getCategory());
            ps.setBigDecimal(4, service.getPrice());
            ps.setBoolean(5, service.isAvailable());
            ps.setInt(6, service.getServiceId());

            return ps.executeUpdate() == 1;
        }
    }

    // DELETE-like operation: Deactivate a service
    public boolean deactivateService(int serviceId) throws SQLException {

        String sql = """
            UPDATE hotel_services
            SET available = FALSE
            WHERE service_id = ?
            """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, serviceId);

            return ps.executeUpdate() == 1;
        }
    }
}
