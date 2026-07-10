package controller;



// App-Status (aktueller Benutzer, Master-Passwort, DEK)
import app.AppState;

// Datenbank-Zugriff
import db.UserRepository;              // Nutzer-Logins & Schlüssel laden
import db.DatabaseHelper;              // Öffnet die Nitrite-DB (per Username)

// Kryptografie & Hilfen
import util.HashUtil;                  // Base64/Hash-Helfer
import crypto.KeyStoreService;         // KEK-Ableitung & Key-Unwrap

// JavaFX: Events, FXML, Laden & UI
import javafx.event.ActionEvent;       // Button-/UI-Events
import javafx.fxml.FXML;               // @FXML-Bindings
import javafx.fxml.FXMLLoader;          // FXML-Dateien laden
import javafx.scene.Parent;            // Root-Knoten
import javafx.scene.Scene;             // Szene
import javafx.scene.control.Label;     // Meldungen
import javafx.scene.control.PasswordField; // Passwort-Eingabe
import javafx.scene.control.TextField; // Text-Eingabe
import javafx.stage.Stage;             // Fenster/Stage steuern

public class LoginController {

    // FXML-Felder
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    private final UserRepository userRepo = new UserRepository();

    // Login-Button
    @FXML
    private void onLogin(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText(); // bewusst nicht trimmen

        // Pflichtfelder prüfen
        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Bitte Benutzername und Passwort eingeben.");
            return;
        }

        try {
            // 1) Passwort gegen SQLite prüfen
            boolean ok = userRepo.verifyLogin(username, password);
            if (!ok) {
                errorLabel.setText("Login fehlgeschlagen!");
                return;
            }

            // 2) AppState aktualisieren (User + Master-Passwort im RAM)
            AppState.getInstance().setCurrentUser(username);
            AppState.getInstance().setMasterPassword(password.toCharArray());

            // 3) Wrapped Keys laden (aus DB)
            var wk = userRepo.loadWrappedKeys(username);
            if (wk == null || wk.dekPw() == null) {
                errorLabel.setText("Kein Schlüsselmaterial gefunden.");
                return;
            }

            // 4) KEK aus Passwort + dek_pw_salt ableiten
            byte[] saltPw = HashUtil.decodeBase64(wk.dekPwSalt());
            byte[] kekPw  = KeyStoreService.deriveKek(password.toCharArray(), saltPw);

            // 5) DEK auspacken (unwrap)
            KeyStoreService.Wrapped wrapped = KeyStoreService.Wrapped.fromB64(wk.dekPw(), wk.dekPwIv());
            byte[] dek = KeyStoreService.unwrapKey(wrapped, kekPw);

            // 6) DEK in AppState legen, Nitrite-DB (ohne DB-Passwort) per Username öffnen
            AppState.getInstance().setDataKey(dek);
            DatabaseHelper.initDatabase(username);

            // 7) Vault laden (Scene-Wechsel)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/vault.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("PasswortManagerX â€“ Tresor");
            // stage.show(); // nicht nötig, vorhandenes Fenster

        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("Unerwarteter Fehler beim Login.");
        }
    }

    // Link/Knopf: Registrieren öffnen
    @FXML
    private void onOpenRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/register.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Benutzer registrieren");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Link/Knopf: Wiederherstellung öffnen
    @FXML
    private void onOpenRecovery() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/recovery.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Passwort wiederherstellen");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
