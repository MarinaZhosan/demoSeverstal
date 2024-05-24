package ru.demo;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.*;
import java.util.Locale;

public class MainApp extends Application {
    private ListView<Note> noteListView;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
       // Locale.setDefault(new Locale("ru", "RU"));

        noteListView = new ListView<>();
        noteListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        addDefaultNote();   //������� ����� ��� �������� ������� �� ���������, ����� ��� �������� ����������, ��� ���� 1 �������
        loadNotes();        //�����, ������� ��������� ������� �� ���� ������ � �������� �� � noteListView

        Button createNoteButton = new Button("New note");
        createNoteButton.setStyle("-fx-font-family: 'Arial';");
        createNoteButton.setOnAction(e -> openEditWindow(null));

        noteListView.setOnMouseClicked(e -> {               //������������ ������� ��� ������� ����� �� �������
            if (e.getClickCount() == 2) {
                Note selectedNote = noteListView.getSelectionModel().getSelectedItem();
                if (selectedNote != null) {
                    openEditWindow(selectedNote);           //������� ��������� ������� � �����, ��� �������� �� � ����� ����
                }
            }
        });

        VBox root = new VBox(10, noteListView, createNoteButton);
        Scene scene = new Scene(root, 400, 600);

        primaryStage.setTitle("Notes");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    private void addDefaultNote() {
        String defaultNoteTitle = "� ����������";
        String defaultNote = "����� ���������� � ���������� �������.\n������� ������ �� �������, ������� ������ �������. " +
                "� ����������� ���� ������� ����� �����������, ������������� ��� �������.\n������ ��������� �����������!";
        String sqlCheck = "SELECT COUNT(*) FROM notes";         //sql ������ ��� ��������, ���� �� � ������� ����-�� ���� ������
        String sqlAddDefaultNote = "INSERT INTO notes (title, content) VALUES (?, ?)"; //������, ��� ���������� ��������� �������

        try (Connection conn = DriverManager.getConnection("jdbc:sqlserver://localhost;databaseName=NotesDB;integratedSecurity=true;encrypt=false;characterEncoding=UTF-8")) {
            PreparedStatement pstmt = conn.prepareStatement(sqlCheck);
            ResultSet result = pstmt.executeQuery();
             if(result.next()) {
                 int rowCount = result.getInt(1);  //������� �������� 1 ������� ���������� �������
                 if (rowCount == 0) {           //���� � ������� ��� �� ����� ������, �� ��������� ������� ����� 0
                     try (PreparedStatement statement = conn.prepareStatement(sqlAddDefaultNote)){
                        statement.setString(1, defaultNoteTitle);
                        statement.setString(2, defaultNote);
                        int rowsAddString = statement.executeUpdate();
                     }
                 }
             }
            }
         catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadNotes() {

        noteListView.getItems().clear();   //������ ���� � �������� ��� ������

        try (Connection conn = DriverManager.getConnection("jdbc:sqlserver://localhost;databaseName=NotesDB;integratedSecurity=true;encrypt=false;characterEncoding=UTF-8")){
            Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, title, created_date FROM Notes");
            while (rs.next()) {
                Note note = new Note(rs.getInt("id"), rs.getString("title"), rs.getTimestamp("created_date").toLocalDateTime());
                noteListView.getItems().add(note);          //��������� � ����, ���������� ������� �� ��
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openEditWindow(Note note) {
        Stage stageEditWindow = new Stage();
        EditNoteWindow editNoteWindow = new EditNoteWindow(stageEditWindow, note, this::loadNotes);       //����� �������� �����
        //��� �������� ����, ��� ���������� ������ � ��������. �������� ���� ����� �����, ������� � ������ ���������, � �������
        //��������� ������ �� ����� loadNotes();

    }
}