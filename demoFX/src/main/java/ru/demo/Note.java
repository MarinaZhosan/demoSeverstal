package ru.demo;

import java.time.LocalDateTime;

public class Note {
    private int id;
    private String title;
    private LocalDateTime dateOfCreating;

    public Note(int id, String title, LocalDateTime createdDate) {
        this.id = id;
        this.title = title;
        this.dateOfCreating = createdDate;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public LocalDateTime getDateOfCreating() {
        return dateOfCreating;
    }

    @Override
    public String toString() {
        return title + " (" + dateOfCreating.toLocalDate() + ")";
    }
}