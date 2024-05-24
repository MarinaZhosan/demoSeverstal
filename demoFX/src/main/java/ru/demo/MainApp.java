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

        addDefaultNote();   //вызываю метод для создания заметки по умолчанию, чтобы при загрузке приложения, там была 1 заметка
        loadNotes();        //метод, который загружает заметки из базы данных и помещает их в noteListView

        Button createNoteButton = new Button("New note");
        createNoteButton.setStyle("-fx-font-family: 'Arial';");
        createNoteButton.setOnAction(e -> openEditWindow(null));

        noteListView.setOnMouseClicked(e -> {               //устанавливаю дествие при двойном клике на заметку
            if (e.getClickCount() == 2) {
                Note selectedNote = noteListView.getSelectionModel().getSelectedItem();
                if (selectedNote != null) {
                    openEditWindow(selectedNote);           //передаю выбранную заметку в метод, для открытия ее в новом окне
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
        String defaultNoteTitle = "О приложении";
        String defaultNote = "Добро пожаловать в приложение заметки.\nНажмите дважды на заметку, которую хотите открыть. " +
                "В открывшемся окне заметку можно просмотреть, редактировать или удалить.\nЖелаем приятного пользования!";
        String sqlCheck = "SELECT COUNT(*) FROM notes";         //sql запрос для проверки, есть ли в таблице хотя-бы одна запись
        String sqlAddDefaultNote = "INSERT INTO notes (title, content) VALUES (?, ?)"; //запрос, для добавления дефолтной заметки

        try (Connection conn = DriverManager.getConnection("jdbc:sqlserver://localhost;databaseName=NotesDB;integratedSecurity=true;encrypt=false;characterEncoding=UTF-8")) {
            PreparedStatement pstmt = conn.prepareStatement(sqlCheck);
            ResultSet result = pstmt.executeQuery();
             if(result.next()) {
                 int rowCount = result.getInt(1);  //получаю значение 1 столбца результата запроса
                 if (rowCount == 0) {           //если в таблице нет ни одной строки, то разультат запроса равен 0
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

        noteListView.getItems().clear();   //очищаю лист и заполняю его заново

        try (Connection conn = DriverManager.getConnection("jdbc:sqlserver://localhost;databaseName=NotesDB;integratedSecurity=true;encrypt=false;characterEncoding=UTF-8")){
            Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, title, created_date FROM Notes");
            while (rs.next()) {
                Note note = new Note(rs.getInt("id"), rs.getString("title"), rs.getTimestamp("created_date").toLocalDateTime());
                noteListView.getItems().add(note);          //добавляем в лист, полученные заметки из БД
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openEditWindow(Note note) {
        Stage stageEditWindow = new Stage();
        EditNoteWindow editNoteWindow = new EditNoteWindow(stageEditWindow, note, this::loadNotes);       //здесь вызываем метод
        //для открытия окна, для дальнейшей работы с заметкой. Передаем туда новую сцену, заметку и лямбда выражение, в котором
        //указыввем ссылку на метод loadNotes();

    }
}