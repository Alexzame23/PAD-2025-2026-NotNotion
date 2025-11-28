package es.fdi.ucm.pad.notnotion.data.model;

import com.google.firebase.Timestamp;

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;


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

    private boolean notificationsEnabled;
    private List<Long> notificationTimes; // Milisegundos antes del evento
    private String notificationSound;

    private boolean notifyAtEventTime;
    private String eventTimeNotificationSound;

    private int snoozeCount;              // Contador de veces que se ha pospuesto
    private Timestamp lastSnoozeTime;     // Última vez que se pospuso
    private int maxSnoozeAllowed;

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
        this.notificationsEnabled = false;
        this.notificationTimes = new ArrayList<>();
        this.notificationSound = "default";
        this.notifyAtEventTime = false;
        this.eventTimeNotificationSound = "alarm";
        this.snoozeCount = 0;
        this.lastSnoozeTime = null;
        this.maxSnoozeAllowed = 3;
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

    public boolean isNotificationsEnabled() { return notificationsEnabled; }
    public void setNotificationsEnabled(boolean notificationsEnabled) {
        this.notificationsEnabled = notificationsEnabled;
    }
    public List<Long> getNotificationTimes() {
        if (notificationTimes == null) {
            notificationTimes = new ArrayList<>();
        }
        return notificationTimes;
    }

    public void setNotificationTimes(List<Long> notificationTimes) {
        this.notificationTimes = notificationTimes;
    }

    public String getNotificationSound() {
        return notificationSound != null ? notificationSound : "default";
    }

    public void setNotificationSound(String notificationSound) {
        this.notificationSound = notificationSound;
    }

    public boolean isNotifyAtEventTime() {
        return notifyAtEventTime;
    }
    public void setNotifyAtEventTime(boolean notifyAtEventTime) {
        this.notifyAtEventTime = notifyAtEventTime;
    }

    public String getEventTimeNotificationSound() {
        return eventTimeNotificationSound != null ? eventTimeNotificationSound : "alarm";
    }

    public void setEventTimeNotificationSound(String eventTimeNotificationSound) {
        this.eventTimeNotificationSound = eventTimeNotificationSound;
    }

    public int getSnoozeCount() {
        return snoozeCount;
    }

    public void setSnoozeCount(int snoozeCount) {
        this.snoozeCount = snoozeCount;
    }

    public Timestamp getLastSnoozeTime() {
        return lastSnoozeTime;
    }

    public void setLastSnoozeTime(Timestamp lastSnoozeTime) {
        this.lastSnoozeTime = lastSnoozeTime;
    }

    public int getMaxSnoozeAllowed() {
        return maxSnoozeAllowed;
    }

    public void setMaxSnoozeAllowed(int maxSnoozeAllowed) {
        this.maxSnoozeAllowed = maxSnoozeAllowed;
    }
    // 🔹 Métodos adicionales
    public void addNotificationTime(long millisBeforeEvent) {
        if (notificationTimes == null) {
            notificationTimes = new ArrayList<>();
        }
        if (!notificationTimes.contains(millisBeforeEvent)) {
            notificationTimes.add(millisBeforeEvent);
        }
    }

    public void removeNotificationTime(long millisBeforeEvent) {
        if (notificationTimes != null) {
            notificationTimes.remove(millisBeforeEvent);
        }
    }

    // MÉTODOS AUXILIARES PARA SNOOZE

    /**
     * Incrementa el contador de postponimientos
     */
    public void incrementSnoozeCount() {
        this.snoozeCount++;
        this.lastSnoozeTime = Timestamp.now();
    }

    /**
     * Resetea el contador de postponimientos (útil al abrir el evento)
     */
    public void resetSnoozeCount() {
        this.snoozeCount = 0;
        this.lastSnoozeTime = null;
    }

    /**
     * Verifica si se puede posponer (si hay límite configurado)
     */
    public boolean canSnooze() {
        if (maxSnoozeAllowed == 0) {
            return true; // Sin límite
        }
        return snoozeCount < maxSnoozeAllowed;
    }

    /**
     * Devuelve cuántos postponimientos quedan disponibles
     */
    public int getRemainingSnoozes() {
        if (maxSnoozeAllowed == 0) {
            return -1; // Ilimitado
        }
        return Math.max(0, maxSnoozeAllowed - snoozeCount);
    }
}
