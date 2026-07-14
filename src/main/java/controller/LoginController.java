package controller;

import app.AppState;
import crypto.KeyStoreService;
import db.DatabaseHelper;
import db.UserRepository;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.util.Duration;
import util.HashUtil;

public class LoginController {

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCK_SECONDS = 30;

    private double windowDragOffsetX;
    private double windowDragOffsetY;

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private Button loginButton;

    private final UserRepository userRepo = new UserRepository();

    private int failedLoginAttempts = 0;
    private int remainingLockSeconds = 0;
    private Timeline lockTimeline;

    @FXML
    private void onLogin(ActionEvent event) {
        if (isLoginLocked()) {
            updateLockMessage();
            return;
        }

        String username = usernameField.getText() == null ? "" : usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password == null || password.isEmpty()) {
            errorLabel.setText("Bitte Benutzername und Passwort eingeben.");
            return;
        }

        try {
            boolean ok = userRepo.verifyLogin(username, password);

            if (!ok) {
                registerFailedLogin();
                return;
            }

            resetLoginProtection();

            AppState.getInstance().setCurrentUser(username);
            AppState.getInstance().setMasterPassword(password.toCharArray());

            var wk = userRepo.loadWrappedKeys(username);
            if (wk == null || wk.dekPw() == null) {
                errorLabel.setText("Kein Schlüsselmaterial gefunden.");
                return;
            }

            byte[] saltPw = HashUtil.decodeBase64(wk.dekPwSalt());
            byte[] kekPw = KeyStoreService.deriveKek(password.toCharArray(), saltPw);

            KeyStoreService.Wrapped wrapped = KeyStoreService.Wrapped.fromB64(wk.dekPw(), wk.dekPwIv());
            byte[] dek = KeyStoreService.unwrapKey(wrapped, kekPw);

            AppState.getInstance().setDataKey(dek);
            DatabaseHelper.initDatabase(username);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/vault.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("PMX Tresor");
        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("Unerwarteter Fehler beim Login.");
        }
    }

    private void registerFailedLogin() {
        failedLoginAttempts++;

        if (failedLoginAttempts >= MAX_FAILED_ATTEMPTS) {
            startLoginLock();
            return;
        }

        errorLabel.setText("Login fehlgeschlagen. Versuch " + failedLoginAttempts + " von " + MAX_FAILED_ATTEMPTS + ".");
    }

    private void startLoginLock() {
        remainingLockSeconds = LOCK_SECONDS;
        setLoginControlsLocked(true);
        updateLockMessage();

        if (lockTimeline != null) {
            lockTimeline.stop();
        }

        lockTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            remainingLockSeconds--;

            if (remainingLockSeconds <= 0) {
                stopLoginLock();
            } else {
                updateLockMessage();
            }
        }));

        lockTimeline.setCycleCount(Timeline.INDEFINITE);
        lockTimeline.playFromStart();
    }

    private void stopLoginLock() {
        if (lockTimeline != null) {
            lockTimeline.stop();
        }

        remainingLockSeconds = 0;
        failedLoginAttempts = 0;
        setLoginControlsLocked(false);
        errorLabel.setText("Login wieder möglich.");
    }

    private void resetLoginProtection() {
        if (lockTimeline != null) {
            lockTimeline.stop();
        }

        remainingLockSeconds = 0;
        failedLoginAttempts = 0;
        setLoginControlsLocked(false);
        errorLabel.setText("");
    }

    private boolean isLoginLocked() {
        return remainingLockSeconds > 0;
    }

    private void updateLockMessage() {
        errorLabel.setText("Zu viele Fehlversuche. Bitte " + remainingLockSeconds + " Sekunden warten.");
    }

    private void setLoginControlsLocked(boolean locked) {
        if (loginButton != null) {
            loginButton.setDisable(locked);
        }
    }

    @FXML
    private void onOpenRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/register.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.initStyle(javafx.stage.StageStyle.UNDECORATED);
            stage.setTitle("PMX");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onOpenRecovery() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/recovery.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.initStyle(javafx.stage.StageStyle.UNDECORATED);
            stage.setTitle("PMX");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onWindowMousePressed(MouseEvent event) {
        windowDragOffsetX = event.getSceneX();
        windowDragOffsetY = event.getSceneY();
    }

    @FXML
    private void onWindowMouseDragged(MouseEvent event) {
        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        stage.setX(event.getScreenX() - windowDragOffsetX);
        stage.setY(event.getScreenY() - windowDragOffsetY);
    }

    @FXML
    private void onMinimizeWindow() {
        Stage stage = (Stage) usernameField.getScene().getWindow();
        stage.setIconified(true);
    }

    @FXML
    private void onCloseWindow() {
        if (lockTimeline != null) {
            lockTimeline.stop();
        }

        Stage stage = (Stage) usernameField.getScene().getWindow();
        stage.close();
    }
}
