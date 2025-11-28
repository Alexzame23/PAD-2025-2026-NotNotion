package es.fdi.ucm.pad.notnotion.data.model;

import java.io.Serializable;

/**
 * Representa una notificación individual configurada por el usuario
 */
public class Notification implements Serializable {

    private long millisBeforeEvent; // Tiempo en milisegundos
    private int quantity;            // Cantidad (ej: 5, 30, 2)
    private TimeUnit timeUnit;       // Unidad (MINUTES, HOURS, DAYS, WEEKS)

    public enum TimeUnit {
        MINUTES("Minutos", 60 * 1000L),
        HOURS("Horas", 60 * 60 * 1000L),
        DAYS("Días", 24 * 60 * 60 * 1000L),
        WEEKS("Semanas", 7 * 24 * 60 * 60 * 1000L);

        private final String displayName;
        private final long millisPerUnit;

        TimeUnit(String displayName, long millisPerUnit) {
            this.displayName = displayName;
            this.millisPerUnit = millisPerUnit;
        }

        public String getDisplayName() {
            return displayName;
        }

        public long getMillisPerUnit() {
            return millisPerUnit;
        }
    }

    public Notification() {
        this.quantity = 5;
        this.timeUnit = TimeUnit.MINUTES;
        calculateMillis();
    }

    public Notification(int quantity, TimeUnit timeUnit) {
        this.quantity = quantity;
        this.timeUnit = timeUnit;
        calculateMillis();
    }

    public Notification(long millisBeforeEvent) {
        this.millisBeforeEvent = millisBeforeEvent;
        decomposeMillis();
    }

    /**
     * Calcula millisBeforeEvent basado en quantity y timeUnit
     */
    private void calculateMillis() {
        this.millisBeforeEvent = quantity * timeUnit.getMillisPerUnit();
    }

    /**
     * Descompone millisBeforeEvent en quantity y timeUnit
     * (intenta encontrar la mejor representación)
     */
    private void decomposeMillis() {
        // Intentar descomponer en la unidad más grande posible
        for (TimeUnit unit : new TimeUnit[]{TimeUnit.WEEKS, TimeUnit.DAYS, TimeUnit.HOURS, TimeUnit.MINUTES}) {
            long unitsCount = millisBeforeEvent / unit.getMillisPerUnit();
            if (unitsCount > 0 && (unitsCount * unit.getMillisPerUnit() == millisBeforeEvent)) {
                this.quantity = (int) unitsCount;
                this.timeUnit = unit;
                return;
            }
        }

        // Por defecto, usar minutos
        this.quantity = (int) (millisBeforeEvent / TimeUnit.MINUTES.getMillisPerUnit());
        this.timeUnit = TimeUnit.MINUTES;
    }

    // ===== GETTERS Y SETTERS =====

    public long getMillisBeforeEvent() {
        return millisBeforeEvent;
    }

    public void setMillisBeforeEvent(long millisBeforeEvent) {
        this.millisBeforeEvent = millisBeforeEvent;
        decomposeMillis();
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
        calculateMillis();
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    public void setTimeUnit(TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
        calculateMillis();
    }

    /**
     * Devuelve una descripción legible de la notificación
     */
    public String getDisplayText() {
        String unitName = timeUnit.getDisplayName().toLowerCase();

        // Singular/plural
        if (quantity == 1) {
            // Quitar la 's' final para singular
            if (unitName.endsWith("s")) {
                unitName = unitName.substring(0, unitName.length() - 1);
            }
        }

        return quantity + " " + unitName + " antes";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Notification that = (Notification) obj;
        return millisBeforeEvent == that.millisBeforeEvent;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(millisBeforeEvent);
    }
}