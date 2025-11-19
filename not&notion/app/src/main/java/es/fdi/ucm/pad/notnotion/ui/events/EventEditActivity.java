package es.fdi.ucm.pad.notnotion.ui.events;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import es.fdi.ucm.pad.notnotion.R;
import es.fdi.ucm.pad.notnotion.data.firebase.CalendarEventsManager;
import es.fdi.ucm.pad.notnotion.data.model.CalendarEvent;

public class EventEditActivity extends AppCompatActivity {

    private EditText editTitle, editDescription;
    private Button btnPickDate, btnPickTime, btnSave, btnDelete;

    private CalendarEventsManager eventsManager;
    private long selectedDateMillis;
    private CalendarEvent currentEvent;

    private Calendar selectedCal;
    private SimpleDateFormat dateFormat;
    private SimpleDateFormat timeFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_edit);

        // --- REFERENCIAS UI ---
        editTitle = findViewById(R.id.editEventTitle);
        editDescription = findViewById(R.id.editEventDescription);
        btnPickDate = findViewById(R.id.btnPickDate);
        btnPickTime = findViewById(R.id.btnPickTime);
        btnSave = findViewById(R.id.btnSaveEvent);
        btnDelete = findViewById(R.id.btnDeleteEvent);

        // Formatos
        dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

        // --- FIRESTORE MANAGER ---
        eventsManager = new CalendarEventsManager();

        selectedCal = Calendar.getInstance();

        // ¿Venimos de pulsar un evento (edición) o del botón "+" (creación)?
        String eventId = getIntent().getStringExtra("eventId");

        if (eventId != null) {
            // ============================
            //      MODO EDICIÓN
            // ============================
            btnDelete.setVisibility(View.VISIBLE);

            eventsManager.getEventById(eventId, event -> {
                currentEvent = event;

                editTitle.setText(event.getTitle());
                editDescription.setText(event.getDescription());

                selectedCal.setTime(event.getStartDate().toDate());

                // Mostrar fecha y hora en los botones
                btnPickDate.setText(dateFormat.format(selectedCal.getTime()));
                btnPickTime.setText(timeFormat.format(selectedCal.getTime()));

                btnDelete.setOnClickListener(v -> deleteEvent());
            });

        } else {
            // ============================
            //      MODO CREAR
            // ============================
            btnDelete.setVisibility(View.GONE);

            selectedDateMillis = getIntent().getLongExtra("selectedDate", -1);

            if (selectedDateMillis == -1) {
                Toast.makeText(this, "Error: No se recibió fecha", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            selectedCal.setTimeInMillis(selectedDateMillis);

            // Valores iniciales en los botones
            btnPickDate.setText(dateFormat.format(selectedCal.getTime()));
            btnPickTime.setText(timeFormat.format(selectedCal.getTime()));
        }

        // --- PICKERS ---
        btnPickDate.setOnClickListener(v -> openDatePicker());
        btnPickTime.setOnClickListener(v -> openTimePicker());

        // --- BOTÓN GUARDAR ---
        btnSave.setOnClickListener(v -> saveEvent());
    }

    // ============================
    //      DIÁLOGO FECHA
    // ============================

    private void openDatePicker() {
        int year = selectedCal.get(Calendar.YEAR);
        int month = selectedCal.get(Calendar.MONTH);
        int day = selectedCal.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, y, m, d) -> {
                    selectedCal.set(Calendar.YEAR, y);
                    selectedCal.set(Calendar.MONTH, m);
                    selectedCal.set(Calendar.DAY_OF_MONTH, d);

                    btnPickDate.setText(dateFormat.format(selectedCal.getTime()));

                    // Justo después de elegir fecha, abrimos la hora (como Google Calendar)
                    openTimePicker();
                },
                year, month, day
        );

        dialog.show();
    }

    // ============================
    //      DIÁLOGO HORA
    // ============================

    private void openTimePicker() {
        int hour = selectedCal.get(Calendar.HOUR_OF_DAY);
        int minute = selectedCal.get(Calendar.MINUTE);

        TimePickerDialog dialog = new TimePickerDialog(
                this,
                (view, h, m) -> {
                    selectedCal.set(Calendar.HOUR_OF_DAY, h);
                    selectedCal.set(Calendar.MINUTE, m);

                    btnPickTime.setText(timeFormat.format(selectedCal.getTime()));
                },
                hour,
                minute,
                true // formato 24h
        );

        dialog.show();
    }

    // =====================================================================
    //                          GUARDAR EVENTO
    // =====================================================================

    private void saveEvent() {

        String title = editTitle.getText().toString().trim();
        String desc  = editDescription.getText().toString().trim();

        if (title.isEmpty()) {
            Toast.makeText(this, "El título es obligatorio", Toast.LENGTH_SHORT).show();
            return;
        }

        Timestamp start = new Timestamp(selectedCal.getTime());
        Timestamp end   = start; // por ahora el fin es igual al inicio

        if (currentEvent == null) {
            // CREAR NUEVO
            eventsManager.addEvent(
                    title,
                    desc,
                    start,
                    end,
                    null,   // noteId
                    0,      // reminderMinutes
                    false,  // isRecurring
                    null    // recurrencePattern
            );
            Toast.makeText(this, "Evento creado correctamente", Toast.LENGTH_SHORT).show();

        } else {
            // ACTUALIZAR EXISTENTE
            currentEvent.setTitle(title);
            currentEvent.setDescription(desc);
            currentEvent.setStartDate(start);
            currentEvent.setEndDate(end);

            eventsManager.updateEvent(currentEvent, () -> {
                Toast.makeText(this, "Evento actualizado", Toast.LENGTH_SHORT).show();
            });
        }

        finish();
    }

    // =====================================================================
    //                          ELIMINAR EVENTO
    // =====================================================================

    private void deleteEvent() {
        if (currentEvent == null) return;

        eventsManager.deleteEvent(currentEvent, () -> {
            Toast.makeText(this, "Evento eliminado", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}
