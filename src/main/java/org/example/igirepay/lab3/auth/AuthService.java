package org.example.igirepay.lab3.auth;

import org.example.igirepay.lab1.model.Customer;
import org.example.igirepay.lab2.dao.AccountDAO;
import org.example.igirepay.lab2.dao.CustomerDAO;

public class AuthService {

    private static final String ADMIN_PHONE    = "0782361001";
    private static final String ADMIN_NAME     = "Olive Ntamakemwa";
    private static final String ADMIN_PIN_HASH = PinHasher.hash("45456");

    private final CustomerDAO customerDAO = new CustomerDAO();
    private final AccountDAO  accountDAO  = new AccountDAO();

    public Role getRole(String phone) {
        return isAdmin(phone) ? Role.ADMIN : Role.USER;
    }

    public boolean isAdmin(String phone) {
        return ADMIN_PHONE.equals(phone);
    }

    public boolean authenticateAdmin(String phone, String pin) {
        return ADMIN_PHONE.equals(phone) && PinHasher.verify(pin, ADMIN_PIN_HASH);
    }

    public boolean authenticateUser(String phone, String pin) {
        String storedHash = accountDAO.getPin("W-" + phone);
        if (storedHash == null) return false;
        return PinHasher.verify(pin, storedHash);
    }

    public boolean changePin(String phone, String oldPin, String newPin) {
        if (!Customer.isValidPin(newPin)) {
            System.out.println("✗ New PIN must be exactly 5 digits.");
            return false;
        }
        return changePinForAccount("W-" + phone, oldPin, newPin, "Wallet");
    }

    public boolean changeMoKashPin(String phone, String oldPin, String newPin) {
        if (!Customer.isValidPin(newPin)) {
            System.out.println("✗ New PIN must be exactly 5 digits.");
            return false;
        }
        return changePinForAccount("MK-" + phone, oldPin, newPin, "MoKash");
    }

    private boolean changePinForAccount(String accountId, String oldPin, String newPin, String label) {
        String storedHash = accountDAO.getPin(accountId);
        if (storedHash == null) {
            System.out.println("✗ " + label + " account not found.");
            return false;
        }
        if (!PinHasher.verify(oldPin, storedHash)) {
            System.out.println("✗ Incorrect current " + label + " PIN.");
            return false;
        }
        boolean updated = accountDAO.updatePin(accountId, PinHasher.hash(newPin));
        if (updated) System.out.println("✓ " + label + " PIN changed successfully.");
        return updated;
    }

    public String getAdminName()  { return ADMIN_NAME; }
    public String getAdminPhone() { return ADMIN_PHONE; }
}