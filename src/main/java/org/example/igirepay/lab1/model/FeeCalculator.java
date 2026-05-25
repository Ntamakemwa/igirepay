package org.example.igirepay.lab1.model;

import java.util.HashMap;
import java.util.Map;

public class FeeCalculator {


    public static final Map<String, String> SUPPORTED_COUNTRIES = new HashMap<>() {{
        put("254", "Kenya");
        put("255", "Tanzania");
        put("256", "Uganda");
        put("257", "Burundi");
        put("243", "DRC Congo");
        put("237", "Cameroon");
        put("233", "Ghana");
        put("260", "Zambia");
        put("231", "Liberia");
        put("232", "Sierra Leone");
        put("265", "Malawi");
        put("242", "Congo Brazzaville");
        put("229", "Benin");
        put("228", "Togo");
        put("225", "Cote d'Ivoire");
        put("224", "Guinea Conakry");
        put("245", "Guinea Bissau");
        put("220", "Gambia");
        put("261", "Madagascar");
        put("221", "Senegal");
        put("251", "Ethiopia");
        put("241", "Gabon");
        put("27", "South Africa");
        put("44", "United Kingdom");
        put("1", "USA/Canada");
    }};

    public static boolean isSupportedCountry(String countryCode) {
        return SUPPORTED_COUNTRIES.containsKey(countryCode);
    }

    public static String getCountryName(String countryCode) {
        return SUPPORTED_COUNTRIES.getOrDefault(countryCode, "Unknown");
    }


    public static double getSendOnNetFee(double amount) {
        if (amount <= 1000) return 20;
        else if (amount <= 10000) return 100;
        else if (amount <= 150000) return 250;
        else if (amount <= 2000000) return 1500;
        else if (amount <= 5000000) return 3000;
        else if (amount <= 10000000) return 5000;
        else throw new IllegalArgumentException("Amount exceeds maximum limit of 10,000,000 RWF");
    }


    public static double getSendOffNetFee(double amount) {
        if (amount <= 1000) return 40;
        else if (amount <= 10000) return 120;
        else if (amount <= 150000) return 270;
        else if (amount <= 2000000) return 1520;
        else if (amount <= 5000000) return 3020;
        else throw new IllegalArgumentException("Off-net transfers limited to 5,000,000 RWF");
    }


    public static double getWithdrawFee(double amount) {
        if (amount <= 100) return 50;
        else if (amount <= 300) return 50;
        else if (amount <= 1000) return 100;
        else if (amount <= 3000) return 200;
        else if (amount <= 5000) return 250;
        else if (amount <= 10000) return 275;
        else if (amount <= 20000) return 350;
        else if (amount <= 40000) return 600;
        else if (amount <= 75000) return 1100;
        else if (amount <= 150000) return 2000;
        else if (amount <= 300000) return 3000;
        else if (amount <= 500000) return 6000;
        else if (amount <= 1000000) return 9000;
        else if (amount <= 2000000) return 17000;
        else if (amount <= 5000000) return 25000;
        else if (amount <= 8000000) return 30000;
        else if (amount <= 10000000) return 35000;
        else throw new IllegalArgumentException("Amount exceeds maximum limit of 10,000,000 RWF");
    }


    public static double getInternationalFee(double amount) {
        if (amount < 1000) throw new IllegalArgumentException("Minimum international transfer is 1,000 RWF");
        else if (amount <= 3000) return 120;
        else if (amount <= 5000) return 240;
        else if (amount <= 10000) return 280;
        else if (amount <= 20000) return 400;
        else if (amount <= 40000) return 800;
        else if (amount <= 75000) return 1200;
        else if (amount <= 150000) return 2000;
        else if (amount <= 300000) return 3200;
        else if (amount <= 500000) return 4800;
        else if (amount <= 1000000) return 8000;
        else if (amount <= 2000000) return 14000;
        else if (amount <= 10000000) return 20000;
        else throw new IllegalArgumentException("Amount exceeds maximum limit of 10,000,000 RWF");
    }

    public static double getDepositFee() { return 0; }

    public static double getMoKashInterestRate(double balance) {
        if (balance <= 100000) return 0.04;
        else if (balance <= 500000) return 0.05;
        else return 0.06;
    }
}