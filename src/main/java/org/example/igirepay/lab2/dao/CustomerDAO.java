package org.example.igirepay.lab2.dao;

import org.example.igirepay.lab1.model.Customer;
import org.example.igirepay.lab2.db.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CustomerDAO {

    public boolean createCustomer(Customer customer) {
        String sql = "INSERT INTO customers (id, full_name, phone_number) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, customer.getCustomerId());
            stmt.setString(2, customer.getFullName());
            stmt.setString(3, customer.getPhoneNumber());
            stmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.out.println("✗ Error creating customer: " + e.getMessage());
            return false;
        }
    }


    public Customer getCustomerByPhone(String phoneNumber) {
        String sql = "SELECT * FROM customers WHERE phone_number = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, phoneNumber);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new Customer(
                        rs.getString("id"),
                        rs.getString("full_name"),
                        rs.getString("phone_number")
                );
            }

        } catch (SQLException e) {
            System.out.println("✗ Error fetching customer: " + e.getMessage());
        }
        return null;
    }
    public Customer getCustomerById(String id) {
        String sql = "SELECT * FROM customers WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new Customer(
                        rs.getString("id"),
                        rs.getString("full_name"),
                        rs.getString("phone_number")
                );
            }

        } catch (SQLException e) {
            System.out.println("✗ Error fetching customer: " + e.getMessage());
        }
        return null;
    }


    public boolean updateCustomer(Customer customer) {
        String sql = "UPDATE customers SET full_name = ?, phone_number = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, customer.getFullName());
            stmt.setString(2, customer.getPhoneNumber());
            stmt.setString(3, customer.getCustomerId());
            stmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.out.println("✗ Error updating customer: " + e.getMessage());
            return false;
        }
    }


    public boolean deleteCustomer(String customerId) {
        String sql = "DELETE FROM customers WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, customerId);
            stmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.out.println("✗ Error deleting customer: " + e.getMessage());
            return false;
        }
    }


    public boolean customerExists(String phoneNumber) {
        String sql = "SELECT id FROM customers WHERE phone_number = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, phoneNumber);
            ResultSet rs = stmt.executeQuery();
            return rs.next();

        } catch (SQLException e) {
            System.out.println("✗ Error checking customer: " + e.getMessage());
            return false;
        }
    }
}
