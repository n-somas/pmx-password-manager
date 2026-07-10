package controller;


// App-Status und Schlüsselverwaltung
import app.AppState;

// JavaFX: FXML & UI
import javafx.fxml.FXML;                   // @FXML-Bindings aus der FXML
import javafx.scene.control.Label;
import javafx.scene.control.Button;         // Textausgabe
import javafx.scene.control.PasswordField; // Passworteingabe
import javafx.scene.control.TextField;     // Texteingabe
import javafx.scene.paint.Color;           // Label-Farben
import javafx.stage.Stage;                 // Fenster/Stage steuern

// Modell- und Hilfsklassen
import model.VaultEntry;                   // Datenmodell eines Eintrags
import util.EncryptionUtil;                // Verschlüsselung/Entschlüsselung
import util.PasswordGenerator;             // Passwortgenerator & Stärke

public class AddEntryController {

    private double windowDragOffsetX;
    private double windowDragOffsetY;

    // FXML-Felder
    @FXML private TextField platformField;      // Plattform/Webseite
    @FXML private TextField usernameField;      // Benutzername
    @FXML private PasswordField passwordField;  // Passwort
    @FXML private Label messageLabel;           // Status-/Fehlermeldungen
    @FXML private Label strengthLabel;          // Anzeige der Passwortstärke
    @FXML private Label dialogTitleLabel;       // Dialogtitel
    @FXML private Label dialogSubtitleLabel;    // Dialoguntertitel
    @FXML private Button saveButton;            // Speichern/Aktualisieren-Button
    private VaultController vaultController;    // Referenz zum VaultController

    // Bearbeitungsmodus
    private boolean editMode = false;
    private String oldWebsite = null;
    private String oldEncryptedPassword = null;

    // Controller-Referenz setzen
    public void setVaultController(VaultController controller) {
        this.vaultController = controller;
    }

    // Live-Berechnung der Passwortstärke
    @FXML
    private void initialize() {
        if (passwordField != null) {
            passwordField.textProperty().addListener((obs, oldVal, newVal)
                    -> updateStrength(newVal));
            updateStrength(passwordField.getText());
        }
        hideMessage();
    }

    // Eintrag in Bearbeitungsmodus laden
    public void setEditEntry(VaultEntry entry) {
        this.editMode = true;
        this.oldWebsite = entry.getWebsite();
        this.oldEncryptedPassword = entry.getPasswordEncrypted();

        platformField.setText(entry.getWebsite());
        usernameField.setText(entry.getUsername());
        passwordField.clear(); // Sicherheit: Passwort nie vorausfüllen

        if (dialogTitleLabel != null) {
            dialogTitleLabel.setText("Eintrag bearbeiten");
        }
        if (dialogSubtitleLabel != null) {
            dialogSubtitleLabel.setText("Bestehende Zugangsdaten bearbeiten.");
        }
        if (saveButton != null) {
            saveButton.setText("Aktualisieren");
        }

        hideMessage();
        updateStrength(passwordField.getText());
    }

    // Button „Generieren“: Zufallspasswort erzeugen
    @FXML
    private void onGeneratePassword() {
        String newPass = PasswordGenerator.generate(12, true, true, true, true);
        passwordField.setText(newPass);

        int score = PasswordGenerator.strengthScore100(newPass); // 0..100
        strengthLabel.setText("Passwortstärke: " + score);
    }

    // Button „Speichern“: Eintrag anlegen oder aktualisieren
    @FXML
    public void onSave() {
        String platform = trim(platformField.getText());
        String username = trim(usernameField.getText());
        String password = passwordField.getText(); // bewusst nicht trimmen

        // Pflichtfelder prüfen
        if (platform.isEmpty() || username.isEmpty()) {
            fail("Webseite und Benutzername dürfen nicht leer sein.");
            return;
        }

        // DEK (Data Encryption Key) abrufen
        byte[] dek = AppState.getInstance().getDataKey();
        if (dek == null || dek.length == 0) {
            fail("Kein Daten‑Schlüssel (bitte neu einloggen).");
            return;
        }

        // Passwort verschlüsseln (abhängig vom Modus)
        String encrypted;
        if (editMode) {
            if (password == null || password.isEmpty()) {
                encrypted = oldEncryptedPassword; // altes verschl. Passwort behalten
            } else {
                encrypted = EncryptionUtil.encrypt(password, dek);
            }
        } else {
            if (password == null || password.isEmpty()) {
                fail("Bitte ein Passwort eingeben.");
                return;
            }
            encrypted = EncryptionUtil.encrypt(password, dek);
        }

        // Eintrag erstellen
        VaultEntry entry = new VaultEntry(platform, username, encrypted);

        // Speichern im Vault
        if (vaultController == null) {
            fail("Fehler: VaultController nicht gefunden.");
            return;
        }

        if (editMode) {
            vaultController.updateVaultEntry(oldWebsite, entry);
            ok("Eintrag aktualisiert.");
        } else {
            vaultController.addVaultEntry(entry);
            ok("Eintrag verschlüsselt hinzugefügt.");
        }

        // Fenster schließen
        closeWindow();
    }



    // Anzeige der Passwortstärke aktualisieren
    private void updateStrength(String pw) {
        if (strengthLabel == null) return;
        int score = PasswordGenerator.strengthScore100(pw); // 0..100
        String text;
        Color color;

        if (pw == null || pw.isEmpty()) {
            text = "Passwortstärke: –";
            color = Color.GRAY;
        } else if (score < 30) {
            text = "Passwortstärke: schwach";
            color = Color.CRIMSON;
        } else if (score < 60) {
            text = "Passwortstärke: mittel";
            color = Color.ORANGE;
        } else if (score < 85) {
            text = "Passwortstärke: stark";
            color = Color.DARKGREEN;
        } else {
            text = "Passwortstärke: sehr stark";
            color = Color.DARKGREEN;
        }

        strengthLabel.setText(text);
        strengthLabel.setTextFill(color);
    }

    private void hideMessage() {
        if (messageLabel != null) {
            messageLabel.setText("");
            messageLabel.setVisible(false);
            messageLabel.setManaged(false);
        }
    }

    private void showMessage(String msg, Color color) {
        if (messageLabel != null) {
            messageLabel.setText(msg);
            messageLabel.setTextFill(color);
            messageLabel.setVisible(true);
            messageLabel.setManaged(true);
        }
    }

    // Erfolgsmeldung
    private void ok(String msg) {
        showMessage(msg, Color.GREEN);
    }

    // Fehlermeldung
    private void fail(String msg) {
        showMessage(msg, Color.RED);
    }

    // Trim null-sicher
    private String trim(String s) {
        return s == null ? "" : s.trim();
    }

    // Fenster schließen
    private void closeWindow() {
        Stage stage = (Stage) platformField.getScene().getWindow();
        stage.close();
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
        javafx.stage.Stage stage = (javafx.stage.Stage) platformField.getScene().getWindow();
        stage.close();
    }
}
