package org.example.igirepay.lab1.model;

public class Language {

    public enum Lang { EN, RW }

    private static Lang current = Lang.EN;

    public static void setLanguage(Lang lang) { current = lang; }
    public static Lang getCurrent() { return current; }

    public static String get(String key) {
        if (current == Lang.RW) return RW.getOrDefault(key, EN.getOrDefault(key, key));
        return EN.getOrDefault(key, key);
    }

    private static final java.util.Map<String, String> EN = new java.util.HashMap<>() {{
        put("welcome", "======================================");
        put("app_name", "     IGIREPAY - DIGITAL WALLET SYSTEM");
        put("register", "--- Register ---");
        put("full_name", "Full Name: ");
        put("phone_prompt", "Phone Number (078/079XXXXXXX): ");
        put("invalid_phone", "✗ Invalid number. Must be 10 digits starting with 078 or 079.");
        put("create_pin", "Create your 5-digit wallet PIN: ");
        put("invalid_pin", "✗ PIN must be exactly 5 digits.");
        put("wallet_created", "✓ Wallet account created successfully!");
        put("mokash_ask", "Would you like to activate MoKash (savings)? (yes/no): ");
        put("mokash_pin", "Create your 5-digit MoKash PIN: ");
        put("mokash_activated", "✓ MoKash account activated!");
        put("login", "--- Please Login ---");
        put("enter_pin", "Enter your wallet PIN: ");
        put("login_success", "✓ Login successful! Welcome ");
        put("account_locked", "✗ Account locked. Contact support on 100.");
        put("main_menu", "========= MAIN MENU =========");
        put("deposit", "1. Deposit");
        put("withdraw", "2. Withdraw");
        put("send_local", "3. Send Money (Local)");
        put("send_intl", "4. Send Money (International)");
        put("check_balance", "5. Check Wallet Balance");
        put("mokash_menu", "6. MoKash");
        put("failed_tx", "7. View Failed Transactions");
        put("change_lang", "8. Change Language");
        put("exit", "0. Exit");
        put("choose", "Choose: ");
        put("mokash_menu_title", "========= MOKASH MENU =========");
        put("mokash_deposit", "1. Deposit to MoKash");
        put("mokash_withdraw", "2. Withdraw from MoKash");
        put("mokash_balance", "3. MoKash Balance");
        put("mokash_history", "4. MoKash Transaction History");
        put("mokash_interest", "5. Apply Monthly Interest");
        put("back", "0. Back to Main Menu");
        put("enter_amount", "Enter amount (RWF): ");
        put("confirm", "Confirm? (yes/no): ");
        put("recipient", "Recipient phone number: ");
        put("invalid_recipient", "✗ Invalid recipient number.");
        put("country_code", "Country code (e.g. 254 for Kenya): ");
        put("recipient_number", "Recipient number: ");
        put("fee_info", "Fee: ");
        put("total_info", " RWF | Total: ");
        put("cancelled", "Transaction cancelled.");
        put("invalid_option", "✗ Invalid option. Try again.");
        put("mokash_not_active", "✗ MoKash is not activated.");
        put("goodbye", "Thank you for using IgirePay. Goodbye!");
        put("select_lang", "Select Language / Hitamo Ururimi:");
        put("lang_en", "1. English");
        put("lang_rw", "2. Kinyarwanda");
        put("wrong_pin", "✗ Wrong PIN. Attempts: ");
    }};

    private static final java.util.Map<String, String> RW = new java.util.HashMap<>() {{
        put("welcome", "======================================");
        put("app_name", "     IGIREPAY - SISITEMU Y'UBWISHYU");
        put("register", "--- Iyandikishe ---");
        put("full_name", "Amazina yose: ");
        put("phone_prompt", "Nimero ya telefoni (078/079XXXXXXX): ");
        put("invalid_phone", "✗ Nimero ntabwo ari yo. Igomba kuba imibare 10 itangira na 078 cyangwa 079.");
        put("create_pin", "Shyiraho PIN y'imibare 5 ya Wallet: ");
        put("invalid_pin", "✗ PIN igomba kuba imibare 5 gusa.");
        put("wallet_created", "✓ Konti ya Wallet yafunguwe neza!");
        put("mokash_ask", "Urashaka gufungura MoKash (konti y'izigama)? (yego/oya): ");
        put("mokash_pin", "Shyiraho PIN y'imibare 5 ya MoKash: ");
        put("mokash_activated", "✓ Konti ya MoKash yafunguwe!");
        put("login", "--- Injira ---");
        put("enter_pin", "Injiza PIN ya Wallet: ");
        put("login_success", "✓ Winjiye neza! Murakaza neza ");
        put("account_locked", "✗ Konti yarafunzwe. Vugana na serivisi kuri 100.");
        put("main_menu", "========= MENYU NKURU =========");
        put("deposit", "1. Shyira amafaranga");
        put("withdraw", "2. Akura amafaranga");
        put("send_local", "3. Ohereza amafaranga (mu gihugu)");
        put("send_intl", "4. Ohereza amafaranga (hanze y'igihugu)");
        put("check_balance", "5. Reba amafaranga ufite");
        put("mokash_menu", "6. MoKash");
        put("failed_tx", "7. Reba ibikorwa byanze");
        put("change_lang", "8. Hindura ururimi");
        put("exit", "0. Sohoka");
        put("choose", "Hitamo: ");
        put("mokash_menu_title", "========= MENYU YA MOKASH =========");
        put("mokash_deposit", "1. Shyira amafaranga muri MoKash");
        put("mokash_withdraw", "2. Akura amafaranga muri MoKash");
        put("mokash_balance", "3. Reba amafaranga muri MoKash");
        put("mokash_history", "4. Reba amateka ya MoKash");
        put("mokash_interest", "5. Ongeraho inyungu za ko ukwezi");
        put("back", "0. Subira kuri Menyu Nkuru");
        put("enter_amount", "Injiza amafaranga (RWF): ");
        put("confirm", "Emeza? (yego/oya): ");
        put("recipient", "Nimero ya ukiriye: ");
        put("invalid_recipient", "✗ Nimero ntabwo ari yo.");
        put("country_code", "Kode y'igihugu (urugero: 254 ya Kenya): ");
        put("recipient_number", "Nimero ya ukiriye: ");
        put("fee_info", "Amafaranga y'ubwishyu: ");
        put("total_info", " RWF | Igiteranyo: ");
        put("cancelled", "Igikorwa cyahagaritswe.");
        put("invalid_option", "✗ Hitamo ntabwo ari yo. Gerageza nanone.");
        put("mokash_not_active", "✗ MoKash ntabwo ifunguwe.");
        put("goodbye", "Murakoze gukoresha IgirePay. Murabeho!");
        put("select_lang", "Hitamo Ururimi / Select Language:");
        put("lang_en", "1. Icyongereza (English)");
        put("lang_rw", "2. Kinyarwanda");
        put("wrong_pin", "✗ PIN ntabwo ari yo. Ugerageje: ");
    }};
}
