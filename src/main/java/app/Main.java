package app;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {

        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            e.printStackTrace();
            Platform.runLater(() -> {
                new Alert(Alert.AlertType.ERROR) {{
                    setTitle("Unerwarteter Fehler");
                    setHeaderText(null);
                    setContentText("Die Anwendung hat einen Fehler festgestellt und wird beendet.");
                }}.showAndWait();
                
                safeShutdown();
                System.exit(1);
            });
        });

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root, 480, 560);
            primaryStage.setTitle("PasswortManagerX – Login");
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(440);
            primaryStage.setMinHeight(520);

            // Bei Fenster-Schließen sensitives Material löschen
            primaryStage.setOnCloseRequest(evt -> safeShutdown());

            primaryStage.show();
        } catch (Exception ex) {
            ex.printStackTrace();
            new Alert(Alert.AlertType.ERROR) {{
                setTitle("Startfehler");
                setHeaderText("Oberfläche konnte nicht geladen werden");
                setContentText("Die Datei »/login.fxml« wurde nicht gefunden oder ist fehlerhaft.");
            }}.showAndWait();
            safeShutdown();
            System.exit(2);
        }
    }

    @Override
    public void stop() {
        // Wird von JavaFX beim regulären Beenden aufgerufen
        safeShutdown();
    }

    private void safeShutdown() {
        try { db.DatabaseHelper.closeDatabase(); } catch (Throwable ignored) {}
        try { app.AppState.getInstance().clearSensitive(); } catch (Throwable ignored) {}
    }

    public static void main(String[] args) {
        launch(args);
    }
}


