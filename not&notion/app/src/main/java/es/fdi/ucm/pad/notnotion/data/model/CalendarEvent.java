package es.fdi.ucm.pad.notnotion.data.model;

import com.google.firebase.Timestamp;

import java.io.Serializable;

public class CalendarEvent implements Serializable {

    private String id;
    private String title;
    private String description;
    private Timestamp startDate;
    private Timestamp endDate;
    private String noteId;
    private int reminderMinutes;
    private boolean isRecurring;
    private String recurrencePattern; // puede ser null
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public CalendarEvent() {}

    public CalendarEvent(String id, String title, String description,
                         Timestamp startDate, Timestamp endDate, String noteId,
                         int reminderMinutes, boolean isRecurring, String recurrencePattern,
                         Timestamp createdAt, Timestamp updatedAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.noteId = noteId;
        this.reminderMinutes = reminderMinutes;
        this.isRecurring = isRecurring;
        this.recurrencePattern = recurrencePattern;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // 🔹 Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Timestamp getStartDate() { return startDate; }
    public void setStartDate(Timestamp startDate) { this.startDate = startDate; }

    public Timestamp getEndDate() { return endDate; }
    public void setEndDate(Timestamp endDate) { this.endDate = endDate; }

    public String getNoteId() { return noteId; }
    public void setNoteId(String noteId) { this.noteId = noteId; }

    public int getReminderMinutes() { return reminderMinutes; }
    public void setReminderMinutes(int reminderMinutes) { this.reminderMinutes = reminderMinutes; }

    public boolean isRecurring() { return isRecurring; }
    public void setRecurring(boolean recurring) { isRecurring = recurring; }

    public String getRecurrencePattern() { return recurrencePattern; }
    public void setRecurrencePattern(String recurrencePattern) { this.recurrencePattern = recurrencePattern; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }
}
