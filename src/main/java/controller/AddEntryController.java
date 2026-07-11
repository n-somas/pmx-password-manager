package controller;

import app.AppState;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import model.VaultEntry;
import util.EncryptionUtil;
import util.PasswordGenerator;

public class AddEntryController {

    private double windowDragOffsetX;
    private double windowDragOffsetY;

    @FXML private TextField platformField;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label messageLabel;
    @FXML private Label strengthLabel;
    @FXML private Label dialogTitleLabel;
    @FXML private Label dialogSubtitleLabel;
    @FXML private Button saveButton;

    private VaultController vaultController;

    private boolean editMode = false;
    private String oldWebsite = null;
    private String oldEncryptedPassword = null;
    private String oldCreatedAt = null;
    private String oldUpdatedAt = null;
    private String oldPasswordChangedAt = null;

    public void setVaultController(VaultController controller) {
        this.vaultController = controller;
    }

    @FXML
    private void initialize() {
        if (passwordField != null) {
            passwordField.textProperty().addListener((obs, oldVal, newVal) -> updateStrength(newVal));
            updateStrength(passwordField.getText());
        }

        hideMessage();
    }

    public void setEditEntry(VaultEntry entry) {
        entry.ensureMetadata();

        this.editMode = true;
        this.oldWebsite = entry.getWebsite();
        this.oldEncryptedPassword = entry.getPasswordEncrypted();
        this.oldCreatedAt = entry.getCreatedAt();
        this.oldUpdatedAt = entry.getUpdatedAt();
        this.oldPasswordChangedAt = entry.getPasswordChangedAt();

        platformField.setText(entry.getWebsite());
        usernameField.setText(entry.getUsername());
        passwordField.clear();

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

    @FXML
    private void onGeneratePassword() {
        String newPass = PasswordGenerator.generate(16, true, true, true, true);
        passwordField.setText(newPass);
        updateStrength(newPass);
    }

    @FXML
    public void onSave() {
        String platform = trim(platformField.getText());
        String username = trim(usernameField.getText());
        String password = passwordField.getText();

        if (platform.isEmpty() || username.isEmpty()) {
            fail("Webseite und Benutzername dürfen nicht leer sein.");
            return;
        }

        byte[] dek = AppState.getInstance().getDataKey();

        if (dek == null || dek.length == 0) {
            fail("Kein Daten Schlüssel vorhanden. Bitte neu einloggen.");
            return;
        }

        String encrypted;
        boolean passwordChanged = false;

        try {
            if (editMode) {
                if (password == null || password.isEmpty()) {
                    encrypted = oldEncryptedPassword;
                } else {
                    encrypted = EncryptionUtil.encrypt(password, dek);
                    passwordChanged = true;
                }
            } else {
                if (password == null || password.isEmpty()) {
                    fail("Bitte ein Passwort eingeben.");
                    return;
                }

                encrypted = EncryptionUtil.encrypt(password, dek);
                passwordChanged = true;
            }

            VaultEntry entry = new VaultEntry(platform, username, encrypted);

            if (editMode) {
                entry.setCreatedAt(oldCreatedAt);
                entry.setUpdatedAt(oldUpdatedAt);
                entry.setPasswordChangedAt(oldPasswordChangedAt);
                entry.markUpdated(passwordChanged);
            } else {
                entry.ensureMetadata();
            }

            if (vaultController == null) {
                fail("Fehler: VaultController nicht gefunden.");
                return;
            }

            if (editMode) {
                vaultController.updateVaultEntry(oldWebsite, entry);
            } else {
                vaultController.addVaultEntry(entry);
            }

            closeWindow();
        } catch (Exception ex) {
            ex.printStackTrace();
            fail("Eintrag konnte nicht gespeichert werden.");
        }
    }

    private void updateStrength(String pw) {
        if (strengthLabel == null) {
            return;
        }

        int score = PasswordGenerator.strengthScore100(pw);
        String text;
        Color color;

        if (pw == null || pw.isEmpty()) {
            text = "Passwortstärke: -";
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

    private void fail(String msg) {
        showMessage(msg, Color.RED);
    }

    private String trim(String s) {
        return s == null ? "" : s.trim();
    }

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
        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        stage.setX(event.getScreenX() - windowDragOffsetX);
        stage.setY(event.getScreenY() - windowDragOffsetY);
    }

    @FXML
    private void onCloseWindow() {
        closeWindow();
    }
}
