package org.example.igirepay.lab3.auth;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PinHasher {

    private static final int SHA256_HEX_LENGTH = 64;

    public static String hash(String pin) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(pin.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Hashing failed: " + e.getMessage());
        }
    }

    public static boolean verify(String inputPin, String hashedPin) {
        if (inputPin == null || hashedPin == null) return false;
        return hash(inputPin).equals(hashedPin);
    }

    public static boolean isHashed(String value) {
        if (value == null || value.length() != SHA256_HEX_LENGTH) return false;
        return value.matches("[0-9a-f]{64}");
    }
}
