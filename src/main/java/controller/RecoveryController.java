package controller;



// Datenbank-Zugriff
import db.UserRepository;                // Nutzer-/Schlüssel-Daten

// JavaFX: FXML & UI
import javafx.fxml.FXML;                 // @FXML-Bindings
import javafx.scene.control.Label;       // Meldungen
import javafx.scene.control.PasswordField; // Passworteingaben
import javafx.scene.control.TextField;   // Texteingaben

// Hilfs- & Krypto-Funktionen
import util.HashUtil;                    // Base64, Zufallsbytes, Hashing
import crypto.KeyStoreService;           // KEK-Ableitung, Wrap/Unwrap

public class RecoveryController {

    private double windowDragOffsetX;
    private double windowDragOffsetY;

    // FXML-Felder
    @FXML private TextField usernameField;
    @FXML private TextField recoveryKeyField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label messageLabel;

    private final UserRepository userRepo = new UserRepository();

    // Button: Wiederherstellen
    @FXML
    public void onRecover() {
        String username = safeTrim(usernameField.getText());
        String recoveryKey = safeTrim(recoveryKeyField.getText());
        String newPassword = newPasswordField.getText();       // bewusst nicht trimmen
        String confirmPassword = confirmPasswordField.getText();

        // Pflichtfelder prüfen
        if (username.isEmpty() || recoveryKey.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            show("Bitte alle Felder ausfüllen.");
            return;
        }
        if (!newPassword.equals(confirmPassword)) {
            show("Passwörter stimmen nicht überein.");
            return;
        }

        try {
            // 1) Recovery Key gegen gespeicherten Hash prüfen (mit recoverySalt)
            String recHash  = userRepo.getRecoveryHash(username);
            String recSaltB = userRepo.getRecoverySalt(username);
            if (recHash == null || recSaltB == null) {
                show("Kein Recovery-Material vorhanden.");
                return;
            }
            byte[] recHashSalt = HashUtil.decodeBase64(recSaltB);     // nur für Hashvergleich
            String recCalc = HashUtil.hashWithSalt(recoveryKey, recHashSalt);
            if (!recHash.equals(recCalc)) {
                show("Recovery Key ist ungültig.");
                return;
            }

            // 2) Wrapped Keys (Recovery) laden
            var wk = userRepo.loadWrappedKeys(username);
            if (wk == null || wk.dekRec() == null || wk.dekRecIv() == null || wk.dekRecSalt() == null) {
                show("Kein DEK über Recovery vorhanden.");
                return;
            }

            // 3) DEK mit Recovery-KEK auspacken
            byte[] dekRecSalt = HashUtil.decodeBase64(wk.dekRecSalt());
            byte[] recKek     = KeyStoreService.deriveKek(recoveryKey.toCharArray(), dekRecSalt);
            KeyStoreService.Wrapped wrappedRec = KeyStoreService.Wrapped.fromB64(wk.dekRec(), wk.dekRecIv());
            byte[] dek = KeyStoreService.unwrapKey(wrappedRec, recKek);

            // 4) DEK mit neuem Passwort neu verpacken (neuer PW-Salt)
            byte[] newPwSalt = HashUtil.randomBytes(16);
            byte[] newKekPw  = KeyStoreService.deriveKek(newPassword.toCharArray(), newPwSalt);
            KeyStoreService.Wrapped wrappedPw = KeyStoreService.wrapKey(dek, newKekPw);

            // 5) Nur dek_pw* aktualisieren; dek_rec* bleiben unverändert
            userRepo.saveWrappedKeys(
                    username,
                    wrappedPw.ctB64(), wrappedPw.ivB64(), HashUtil.encodeBase64(newPwSalt),
                    wk.dekRec(), wk.dekRecIv(), wk.dekRecSalt()
            );

            // 6) Login-Passwort-Hash (SQLite) aktualisieren
            userRepo.updatePassword(username, newPassword);

            show("Passwort zurückgesetzt. Bitte mit neuem Passwort einloggen.");
        } catch (Exception ex) {
            ex.printStackTrace();
            show("Fehler bei Wiederherstellung.");
        }
    }

    // Button: Fenster schließen
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

    @FXML
    private void onWindowMousePressed(javafx.scene.input.MouseEvent event) {
        windowDragOffsetX = event.getSceneX();
        windowDragOffsetY = event.getSceneY();
    }

    @FXML
    private void onWindowMouseDragged(javafx.scene.input.MouseEvent event) {
        javafx.stage.Stage stage = (javafx.stage.Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        stage.setX(event.getScreenX() - windowDragOffsetX);
        stage.setY(event.getScreenY() - windowDragOffsetY);
    }

    @FXML
    private void onCloseWindow() {
        javafx.stage.Stage stage = (javafx.stage.Stage) usernameField.getScene().getWindow();
        stage.close();
    }
}
