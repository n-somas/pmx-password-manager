package controller;

import app.AppState;
import db.DatabaseHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.VaultEntry;
import util.EncryptionUtil;

import java.io.IOException;

public class VaultController {

    private double windowDragOffsetX;
    private double windowDragOffsetY;

    @FXML private TableView<VaultEntry> vaultTable;
    @FXML private TableColumn<VaultEntry, String> websiteColumn;
    @FXML private TableColumn<VaultEntry, String> usernameColumn;
    @FXML private TableColumn<VaultEntry, String> passwordColumn;
    @FXML private TableColumn<VaultEntry, Void> actionColumn;
    @FXML private TextField searchField;

    private final ObservableList<VaultEntry> entries = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        configureTableLayout();

        websiteColumn.setCellValueFactory(new PropertyValueFactory<>("website"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        passwordColumn.setCellValueFactory(new PropertyValueFactory<>("passwordMasked"));

        configureActionColumn();
        reloadFromDb();
        configureSearch();
        configureContextMenu();
    }

    private void configureTableLayout() {
        vaultTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        websiteColumn.setMinWidth(170);
        usernameColumn.setMinWidth(170);
        passwordColumn.setMinWidth(140);

        actionColumn.setMinWidth(170);
        actionColumn.setPrefWidth(180);
        actionColumn.setMaxWidth(200);
        actionColumn.setResizable(false);
        actionColumn.setSortable(false);
    }

    private void configureActionColumn() {
        actionColumn.setCellFactory(col -> new TableCell<>() {
            private final Button viewButton = iconButton(
                    "Anzeigen",
                    "M12 4.5C7 4.5 2.7 7.6 1 12c1.7 4.4 6 7.5 11 7.5s9.3-3.1 11-7.5c-1.7-4.4-6-7.5-11-7.5zm0 12a4.5 4.5 0 1 1 0-9 4.5 4.5 0 0 1 0 9zm0-2.2a2.3 2.3 0 1 0 0-4.6 2.3 2.3 0 0 0 0 4.6z",
                    "icon-view"
            );

            private final Button editButton = iconButton(
                    "Bearbeiten",
                    "M4 17.2V20h2.8L17.1 9.7l-2.8-2.8L4 17.2zM19.3 7.5c.3-.3.3-.8 0-1.1l-1.7-1.7a.8.8 0 0 0-1.1 0l-1.3 1.3 2.8 2.8 1.3-1.3z",
                    "icon-edit"
            );

            private final Button deleteButton = iconButton(
                    "Löschen",
                    "M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM8 4l1-1h6l1 1h4v2H4V4h4z",
                    "icon-delete"
            );

            private final HBox box = new HBox(10, viewButton, editButton, deleteButton);

            {
                box.setAlignment(Pos.CENTER);

                viewButton.setOnAction(e -> {
                    VaultEntry entry = getEntry();
                    if (entry != null) showPassword(entry);
                });

                editButton.setOnAction(e -> {
                    VaultEntry entry = getEntry();
                    if (entry != null) editEntry(entry);
                });

                deleteButton.setOnAction(e -> {
                    VaultEntry entry = getEntry();
                    if (entry != null) deleteEntry(entry);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }

            private VaultEntry getEntry() {
                int index = getIndex();
                if (index < 0 || index >= getTableView().getItems().size()) {
                    return null;
                }
                return getTableView().getItems().get(index);
            }
        });
    }

    private Button iconButton(String tooltipText, String svgPath, String extraClass) {
        SVGPath icon = new SVGPath();
        icon.setContent(svgPath);
        icon.getStyleClass().addAll("action-icon", extraClass + "-shape");
        icon.setScaleX(0.78);
        icon.setScaleY(0.78);

        Button button = new Button();
        button.setGraphic(icon);
        button.getStyleClass().addAll("icon-button", extraClass);
        button.setTooltip(new Tooltip(tooltipText));
        button.setMinWidth(40);
        button.setPrefWidth(40);
        button.setMaxWidth(40);
        button.setMinHeight(34);
        button.setPrefHeight(34);
        button.setFocusTraversable(false);
        button.setAccessibleText(tooltipText);

        return button;
    }

    private void configureSearch() {
        FilteredList<VaultEntry> filtered = new FilteredList<>(entries, e -> true);

        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldV, newV) -> {
                String query = (newV == null ? "" : newV.trim().toLowerCase());

                filtered.setPredicate(entry -> {
                    if (query.isEmpty()) return true;

                    String website = entry.getWebsite() == null ? "" : entry.getWebsite().toLowerCase();
                    String username = entry.getUsername() == null ? "" : entry.getUsername().toLowerCase();

                    return website.contains(query) || username.contains(query);
                });
            });
        }

        SortedList<VaultEntry> sorted = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(vaultTable.comparatorProperty());
        vaultTable.setItems(sorted);
    }

    private void configureContextMenu() {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem editItem = new MenuItem("Bearbeiten");
        MenuItem deleteItem = new MenuItem("Löschen");

        contextMenu.getItems().addAll(editItem, deleteItem);
        editItem.setOnAction(e -> onEditSelected());
        deleteItem.setOnAction(e -> onDeleteSelected());

        vaultTable.setRowFactory(tv -> {
            TableRow<VaultEntry> row = new TableRow<>();

            row.setOnContextMenuRequested(event -> {
                if (!row.isEmpty()) {
                    vaultTable.getSelectionModel().select(row.getIndex());
                    contextMenu.show(row, event.getScreenX(), event.getScreenY());
                }
            });

            row.setOnMouseClicked(event -> {
                if (event.getButton() == MouseButton.SECONDARY && !row.isEmpty()) {
                    vaultTable.getSelectionModel().select(row.getIndex());
                    contextMenu.show(row, event.getScreenX(), event.getScreenY());
                } else {
                    contextMenu.hide();
                }
            });

            return row;
        });
    }

    private void reloadFromDb() {
        entries.clear();

        if (DatabaseHelper.findAll() == null) {
            return;
        }

        for (VaultEntry entry : DatabaseHelper.findAll()) {
            entries.add(entry);
        }
    }

    @FXML
    private void onAddEntry() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/add_entry.fxml"));
            Parent root = loader.load();

            AddEntryController controller = loader.getController();
            controller.setVaultController(this);

            Stage stage = new Stage();
            stage.initStyle(javafx.stage.StageStyle.UNDECORATED);
            stage.initOwner(vaultTable.getScene().getWindow());
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setTitle("PMX");
            stage.setScene(new Scene(root, 560, 650));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showModal("Fehler", "Der Dialog konnte nicht geoeffnet werden.");
        }
    }

    private void onEditSelected() {
        VaultEntry selected = vaultTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showModal("Hinweis", "Bitte zuerst einen Eintrag auswaehlen.");
            return;
        }

        editEntry(selected);
    }

    private void onDeleteSelected() {
        VaultEntry selected = vaultTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showModal("Hinweis", "Bitte zuerst einen Eintrag auswaehlen.");
            return;
        }

        deleteEntry(selected);
    }

    private void showPassword(VaultEntry entry) {
        byte[] dek = AppState.getInstance().getDataKey();

        if (dek == null || dek.length == 0) {
            showModal("Fehler", "Kein Daten-Schluessel vorhanden. Bitte neu einloggen.");
            return;
        }

        try {
            String plain = EncryptionUtil.decrypt(entry.getPasswordEncrypted(), dek);
            showModal("Passwort fuer " + entry.getWebsite(), plain);
        } catch (Exception ex) {
            ex.printStackTrace();
            showModal("Fehler", "Entschluesselung fehlgeschlagen.");
        }
    }

    private void editEntry(VaultEntry entry) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/add_entry.fxml"));
            Parent root = loader.load();

            AddEntryController controller = loader.getController();
            controller.setVaultController(this);
            controller.setEditEntry(entry);

            Stage stage = new Stage();
            stage.initStyle(javafx.stage.StageStyle.UNDECORATED);
            stage.initOwner(vaultTable.getScene().getWindow());
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setTitle("PMX");
            stage.setScene(new Scene(root, 560, 650));
            stage.show();
        } catch (IOException ex) {
            ex.printStackTrace();
            showModal("Fehler", "Der Bearbeitungsdialog konnte nicht geoeffnet werden.");
        }
    }

    private void deleteEntry(VaultEntry entry) {
        boolean confirmed = confirmModal(
                "Eintrag löschen",
                "Soll der Eintrag \"" + entry.getWebsite() + "\" wirklich gelöscht werden?"
        );

        if (!confirmed) {
            return;
        }

        DatabaseHelper.deleteByWebsite(entry.getWebsite());
        entries.remove(entry);
    }

    public void addVaultEntry(VaultEntry entry) {
        DatabaseHelper.addEntry(entry);
        entries.add(entry);
    }

    public void updateVaultEntry(String oldWebsite, VaultEntry updated) {
        if (!oldWebsite.equals(updated.getWebsite())) {
            DatabaseHelper.deleteByWebsite(oldWebsite);
            DatabaseHelper.addEntry(updated);
            entries.removeIf(entry -> entry.getWebsite().equals(oldWebsite));
            entries.add(updated);
        } else {
            DatabaseHelper.upsert(updated);

            for (int i = 0; i < entries.size(); i++) {
                if (entries.get(i).getWebsite().equals(updated.getWebsite())) {
                    entries.set(i, updated);
                    break;
                }
            }
        }
    }

    @FXML
    private void onLogout() {
        try {
            DatabaseHelper.closeDatabase();
            AppState.getInstance().logout();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) vaultTable.getScene().getWindow();
            stage.setScene(new Scene(root, 480, 560));
            stage.setTitle("PMX Login");
        } catch (IOException ex) {
            ex.printStackTrace();
            showModal("Fehler", "Login konnte nicht geladen werden.");
        }
    }

    private void showModal(String title, String message) {
        Stage modal = createBaseModal();

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("modal-title");

        Label messageLabel = new Label(message);
        messageLabel.getStyleClass().add("modal-message");
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(420);

        Button okButton = new Button("OK");
        okButton.getStyleClass().add("primary-button");
        okButton.setMinWidth(110);
        okButton.setOnAction(e -> modal.close());

        VBox card = new VBox(16, titleLabel, messageLabel, okButton);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(26));
        card.getStyleClass().add("modal-card");

        Scene scene = new Scene(card, 480, 220);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

        modal.setScene(scene);
        modal.showAndWait();
    }

    private boolean confirmModal(String title, String message) {
        Stage modal = createBaseModal();
        final boolean[] confirmed = {false};

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("modal-title");

        Label messageLabel = new Label(message);
        messageLabel.getStyleClass().add("modal-message");
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(420);

        Button cancelButton = new Button("Abbrechen");
        cancelButton.getStyleClass().add("secondary-button");
        cancelButton.setOnAction(e -> modal.close());

        Button deleteButton = new Button("Löschen");
        deleteButton.getStyleClass().add("danger-button");
        deleteButton.setOnAction(e -> {
            confirmed[0] = true;
            modal.close();
        });

        HBox buttons = new HBox(10, cancelButton, deleteButton);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        VBox card = new VBox(16, titleLabel, messageLabel, buttons);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(26));
        card.getStyleClass().add("modal-card");

        Scene scene = new Scene(card, 500, 230);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

        modal.setScene(scene);
        modal.showAndWait();

        return confirmed[0];
    }

    private Stage createBaseModal() {
        Stage modal = new Stage();
        modal.initOwner(vaultTable.getScene().getWindow());
        modal.initModality(Modality.WINDOW_MODAL);
        modal.initStyle(javafx.stage.StageStyle.UNDECORATED);
        modal.setTitle("PMX");
        return modal;
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
        Stage stage = (Stage) vaultTable.getScene().getWindow();
        stage.setIconified(true);
    }

    @FXML
    private void onCloseWindow() {
        try {
            DatabaseHelper.closeDatabase();
            AppState.getInstance().logout();
        } catch (Exception ignored) {
        }

        Stage stage = (Stage) vaultTable.getScene().getWindow();
        stage.close();
    }
}