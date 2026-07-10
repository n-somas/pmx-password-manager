package controller;



// Datenbank-Zugriff
import db.UserRepository;                // Nutzer anlegen & Schlüssel speichern

// Hilfs- & Krypto-Funktionen
import util.HashUtil;                    // Base64, Salt, Hashing
import crypto.KeyStoreService;           // KEK-Ableitung, Wrap/Unwrap, DEK

// JavaFX: FXML & UI
import javafx.fxml.FXML;                 // @FXML-Bindings
import javafx.scene.control.Label;       // Meldungen
import javafx.scene.control.PasswordField; // Passwort-Eingaben
import javafx.scene.control.TextField;   // Text-/Recovery-Key-Anzeige

import java.security.SecureRandom;

public class RegisterController {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    // FXML-Felder
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label messageLabel;
    @FXML private TextField recoveryKeyField;

    private final UserRepository userRepo = new UserRepository();

    // Init: Recovery-Feld schreibgeschützt & monospace
    @FXML
    private void initialize() {
        if (recoveryKeyField != null) {
            recoveryKeyField.setEditable(false);
            recoveryKeyField.setStyle("-fx-font-family: monospace;");
        }
    }

    // Button „Registrieren“
    @FXML
    private void onRegister() {
        String username = safeTrim(usernameField.getText());
        String pw1 = passwordField.getText();              // bewusst nicht trimmen
        String pw2 = confirmPasswordField.getText();

        // Pflichtfelder prüfen
        if (username.isEmpty() || pw1.isEmpty() || pw2.isEmpty()) {
            show("Bitte alle Felder ausfüllen.");
            return;
        }
        if (!pw1.equals(pw2)) {
            show("Passwörter stimmen nicht überein.");
            return;
        }
        if (userRepo.userExists(username)) {
            show("Benutzername existiert bereits.");
            return;
        }

        try {
            // 1) Recovery-Key (kurzes, gut ablesbares Format)
            String recoveryKey = generateRecoveryKey();
            recoveryKeyField.setText(recoveryKey);
            recoveryKeyField.selectAll();

            // 2) Recovery-Key als Hash speichern (mit eigenem Salt)
            byte[] recSalt = HashUtil.generateSalt();
            String recSaltB64 = HashUtil.encodeBase64(recSalt);
            String recHash = HashUtil.hashWithSalt(recoveryKey, recSalt);

            // 3) Benutzer anlegen (PW-Hash + Recovery-Hash)
            userRepo.createUser(username, pw1, recHash, recSaltB64);

            // 4) DEK generieren (Datenverschlüsselungs-Schlüssel)
            byte[] dek = KeyStoreService.generateDataKey();

            // 5) DEK mit Passwort wrappen (PW-Salt)
            byte[] saltPw = KeyStoreService.random(16);
            byte[] kekPw  = KeyStoreService.deriveKek(pw1.toCharArray(), saltPw);
            KeyStoreService.Wrapped wPw = KeyStoreService.wrapKey(dek, kekPw);

            // 6) DEK mit Recovery-Key wrappen (Recovery-Salt)
            byte[] saltRec = KeyStoreService.random(16);
            byte[] kekRec  = KeyStoreService.deriveKek(recoveryKey.toCharArray(), saltRec);
            KeyStoreService.Wrapped wRec = KeyStoreService.wrapKey(dek, kekRec);

            // 7) Beide Wraps persistieren
            userRepo.saveWrappedKeys(
                    username,
                    wPw.ctB64(),  wPw.ivB64(),  HashUtil.encodeBase64(saltPw),
                    wRec.ctB64(), wRec.ivB64(), HashUtil.encodeBase64(saltRec)
            );

            show("Benutzer erstellt! Recovery-Key angezeigt – bitte sicher aufbewahren.");
        } catch (Exception e) {
            e.printStackTrace();
            show("Fehler beim Erstellen des Benutzers.");
        }
    }

    // Button „Schließen“
    @FXML
    private void onClose() {
        if (messageLabel != null && messageLabel.getScene() != null) {
            messageLabel.getScene().getWindow().hide();
        }
    }

    // ---------- Helpers ----------

    private void show(String msg) {
        if (messageLabel != null) messageLabel.setText(msg);
    }
    private String safeTrim(String s) {
        return s == null ? "" : s.trim();
    }

    private String generateRecoveryKey() {
        return String.format("%04d-%04d-%04d-%04d-%04d",
                SECURE_RANDOM.nextInt(10_000),
                SECURE_RANDOM.nextInt(10_000),
                SECURE_RANDOM.nextInt(10_000),
                SECURE_RANDOM.nextInt(10_000),
                SECURE_RANDOM.nextInt(10_000));
    }}
