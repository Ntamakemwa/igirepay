package org.example.igirepay.lab3.service;

import org.example.igirepay.lab1.model.Customer;
import org.example.igirepay.lab2.dao.CustomerDAO;

public class CustomerService {

    private final CustomerDAO customerDAO = new CustomerDAO();

    public boolean registerCustomer(Customer customer) {
        if (!Customer.isValidPhoneNumber(customer.getPhoneNumber())) {
            System.out.println("✗ Invalid phone number.");
            return false;
        }
        if (customerDAO.customerExists(customer.getPhoneNumber())) {
            System.out.println("✗ Customer already exists.");
            return false;
        }
        boolean saved = customerDAO.createCustomer(customer);
        if (saved) System.out.println("✓ Customer registered successfully.");
        return saved;
    }

    public Customer getCustomerByPhone(String phone) {
        if (!Customer.isValidPhoneNumber(phone)) {
            System.out.println("✗ Invalid phone number.");
            return null;
        }
        Customer customer = customerDAO.getCustomerByPhone(phone);
        if (customer == null) System.out.println("✗ Customer not found.");
        return customer;
    }

    public boolean updateCustomer(Customer customer) {
        if (!Customer.isValidPhoneNumber(customer.getPhoneNumber())) {
            System.out.println("✗ Invalid phone number.");
            return false;
        }
        boolean updated = customerDAO.updateCustomer(customer);
        if (updated) System.out.println("✓ Customer updated successfully.");
        return updated;
    }

    public boolean deleteCustomer(String customerId) {
        boolean deleted = customerDAO.deleteCustomer(customerId);
        if (deleted) System.out.println("✓ Customer deleted successfully.");
        return deleted;
    }

    public boolean customerExists(String phone) {
        return customerDAO.customerExists(phone);
    }
}
