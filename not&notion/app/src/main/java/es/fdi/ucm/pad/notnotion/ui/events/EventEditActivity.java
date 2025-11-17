package es.fdi.ucm.pad.notnotion.ui.events;

import android.os.Bundle;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import es.fdi.ucm.pad.notnotion.R;
import es.fdi.ucm.pad.notnotion.data.firebase.CalendarEventsManager;

public class EventEditActivity extends AppCompatActivity {

    private EditText editTitle, editDescription;
    private DatePicker datePicker;
    private TimePicker timePicker;
    private Button btnSave;

    private CalendarEventsManager eventsManager;
    private long selectedDateMillis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_edit);

        // --- REFERENCIAS UI ---
        editTitle = findViewById(R.id.editEventTitle);
        editDescription = findViewById(R.id.editEventDescription);
        datePicker = findViewById(R.id.datePicker);
        timePicker = findViewById(R.id.timePicker);
        btnSave = findViewById(R.id.btnSaveEvent);

        timePicker.setIs24HourView(true);

        // --- FIRESTORE MANAGER ---
        eventsManager = new CalendarEventsManager();

        // --- RECIBIR FECHA SELECCIONADA DESDE EL CALENDARIO ---
        selectedDateMillis = getIntent().getLongExtra("selectedDate", -1);

        if (selectedDateMillis == -1) {
            Toast.makeText(this, "Error: No se recibió fecha", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // --- CONFIGURAR DATEPICKER A LA FECHA SELECCIONADA ---
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(selectedDateMillis);

        datePicker.updateDate(
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        );

        // --- BOTÓN GUARDAR ---
        btnSave.setOnClickListener(v -> saveEvent());
    }


    // ============================================================================
    //                          GUARDAR EVENTO
    // ============================================================================

    private void saveEvent() {

        String title = editTitle.getText().toString().trim();
        String desc  = editDescription.getText().toString().trim();

        if (title.isEmpty()) {
            Toast.makeText(this, "El título es obligatorio", Toast.LENGTH_SHORT).show();
            return;
        }

        // --- OBTENER FECHA DEL DATEPICKER ---
        int day = datePicker.getDayOfMonth();
        int month = datePicker.getMonth();       // 0 = Enero
        int year = datePicker.getYear();

        // --- OBTENER HORA DEL TIMEPICKER ---
        int hour = timePicker.getHour();
        int minute = timePicker.getMinute();

        // --- CREAR TIMESTAMP ---
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, day, hour, minute, 0);

        Timestamp start = new Timestamp(cal.getTime());
        Timestamp end   = start; // por ahora el fin es igual al inicio

        // --- GUARDAR EN FIRESTORE ---
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
        finish();
    }
}
