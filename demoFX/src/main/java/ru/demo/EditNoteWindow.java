package ru.demo;

import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.text.Font;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class EditNoteWindow {
    private Note note;
    private Runnable onClose;
    private Stage stageEdit;

    public EditNoteWindow(Stage stage, Note note, Runnable onClose) {

        this.note = note;
        this.onClose = onClose;     //переменная типа Runnable, в которую мы передаем ссылку на метод loadNotes класса MainApp
                                    // позволяет нам обновить список заметок в главном окне, после редактирования в этом
        this.stageEdit = stage;

        TextField titleField = new TextField(note != null ? note.getTitle() : "");
        TextArea contentArea = new TextArea();

        contentArea.setPrefColumnCount(20);
        contentArea.setWrapText(true);

        Label lblNoteTitle = new Label("Note title:");
        Label lblNoteContent = new Label("Note:");


        if (note != null) {
            loadNoteContent(note.getId(), contentArea);
        }

        Button saveButton = new Button("Save");
        saveButton.setOnAction(e -> saveNoteContent(titleField.getText(), contentArea.getText()));

        Button deleteButton = new Button("Delete");
        deleteButton.setOnAction(e -> {
            if (note != null) {
                deleteNote();
            }
        });

        Button backButton = new Button("Back");
        backButton.setOnAction(e -> closeWindow());

        Button fontButton = new Button("Bold font");                        //здесь задаем стилистические кнопки
        fontButton.setOnAction(e -> contentArea.setStyle("-fx-font-weight: bold"));
        Button fontThinButton = new Button("Thin font");
        fontThinButton.setOnAction(e -> contentArea.setStyle("-fx-font-weight: thin"));

        ObservableList<String> fonts = FXCollections.observableArrayList("Arial", "Times New Roman", "Verdana");
        ComboBox<String> fontsComboBox = new ComboBox<>(fonts);     //выпадающий список для выбора шрифта
        fontsComboBox.setValue("Arial");
        fontsComboBox.setOnAction(e -> { String chosenFont = fontsComboBox.getValue(); contentArea.setFont(Font.font(chosenFont)); });

        HBox styleButtons = new HBox(10, fontButton, fontThinButton, fontsComboBox);
        HBox optionalButtonsH = new HBox(10, saveButton, deleteButton,backButton);
        VBox root = new VBox(10, lblNoteTitle, titleField, lblNoteContent, contentArea, optionalButtonsH, styleButtons);
        Scene scene = new Scene(root, 400, 600);


        stageEdit.setTitle(note != null ? "Edit note" : "Create note");     //задаем название окну, в зависимости от note
        stageEdit.setScene(scene);
        stageEdit.show();
    }


    private void loadNoteContent(int noteId, TextArea contentArea) {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlserver://localhost;databaseName=NotesDB;integratedSecurity=true;encrypt=false;characterEncoding=UTF-8");
             PreparedStatement stmt = conn.prepareStatement("SELECT content FROM Notes WHERE id = ?")) {
            stmt.setInt(1, noteId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    contentArea.setText(rs.getString("content"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveNoteContent(String title, String content) {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlserver://localhost;databaseName=NotesDB;integratedSecurity=true;encrypt=false;characterEncoding=UTF-8")) {
            if (note == null) {
                try (PreparedStatement stmt = conn.prepareStatement("INSERT INTO Notes (title, content) VALUES (?, ?)")) {
                    stmt.setString(1, title);
                    stmt.setString(2, content);
                    stmt.executeUpdate();
                }
            } else {
                try (PreparedStatement stmt = conn.prepareStatement("UPDATE Notes SET title = ?, content = ? WHERE id = ?")) {
                    stmt.setString(1, title);
                    stmt.setString(2, content);
                    stmt.setInt(3, note.getId());
                    stmt.executeUpdate();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeWindow();
        }
    }

    private void deleteNote() {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlserver://localhost;databaseName=NotesDB;integratedSecurity=true;encrypt=false;characterEncoding=UTF-8");
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM Notes WHERE id = ?")) {
            stmt.setInt(1, note.getId());
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeWindow();
        }
    }

    private void closeWindow() {
        onClose.run();
        stageEdit.close();
    }
}