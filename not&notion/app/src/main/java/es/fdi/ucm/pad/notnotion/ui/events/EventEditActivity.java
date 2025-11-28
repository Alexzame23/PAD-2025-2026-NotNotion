package es.fdi.ucm.pad.notnotion.ui.events;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.NotificationManager;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import es.fdi.ucm.pad.notnotion.R;
import es.fdi.ucm.pad.notnotion.data.firebase.CalendarEventsManager;
import es.fdi.ucm.pad.notnotion.data.model.CalendarEvent;
import es.fdi.ucm.pad.notnotion.ui.notifications.NotificationConfigDialog;
import es.fdi.ucm.pad.notnotion.ui.notifications.NotificationScheduler;
import es.fdi.ucm.pad.notnotion.utils.NotificationHelper;

public class EventEditActivity extends AppCompatActivity {

    private EditText editTitle, editDescription;
    private Button btnPickDate, btnPickTime, btnSave, btnDelete;

    private ImageButton btnConfigureNotifications;
    private TextView tvNotificationStatus;
    private CalendarEventsManager eventsManager;
    private long selectedDateMillis;
    private CalendarEvent currentEvent;

    private Calendar selectedCal;
    private SimpleDateFormat dateFormat;
    private SimpleDateFormat timeFormat;
    // Configuración de notificaciones
    private boolean tempNotificationsEnabled = false;
    private List<Long> tempNotificationTimes;
    private String tempSoundType = "default";
    // Launcher para solicitar permisos
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private boolean tempNotifyAtEventTime = false;
    private String tempEventTimeSoundType = "alarm";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_edit);

        // Crear canal de notificaciones y lanzar permisos
        NotificationHelper.createNotificationChannel(this);
        setupPermissionLauncher();
        checkFullScreenIntentPermission();

        // --- REFERENCIAS UI ---
        editTitle = findViewById(R.id.editEventTitle);
        editDescription = findViewById(R.id.editEventDescription);
        btnPickDate = findViewById(R.id.btnPickDate);
        btnPickTime = findViewById(R.id.btnPickTime);
        btnSave = findViewById(R.id.btnSaveEvent);
        btnDelete = findViewById(R.id.btnDeleteEvent);

        btnConfigureNotifications = findViewById(R.id.btnConfigureNotifications);
        tvNotificationStatus = findViewById(R.id.tvNotificationStatus);

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

                // Cargar configuración de notificaciones
                tempNotificationsEnabled = event.isNotificationsEnabled();
                tempNotificationTimes = event.getNotificationTimes();
                tempSoundType = event.getNotificationSound();
                updateNotificationStatusText();
                tempNotifyAtEventTime = event.isNotifyAtEventTime();
                tempEventTimeSoundType = event.getEventTimeNotificationSound();

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

            updateNotificationStatusText();
        }

        // --- PICKERS ---
        btnPickDate.setOnClickListener(v -> openDatePicker());
        btnPickTime.setOnClickListener(v -> openTimePicker());
        btnConfigureNotifications.setOnClickListener(v -> openNotificationConfigDialog());

        // --- BOTÓN GUARDAR ---
        btnSave.setOnClickListener(v -> saveEvent());

        // --- BOTÓN RETURN ---
        ImageButton btnGoBack = findViewById(R.id.btnGoBackCalendar);
        btnGoBack.setOnClickListener(v -> finish());
    }

    // CONFIGURAR LAUNCHER DE PERMISOS
    private void setupPermissionLauncher() {
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        Toast.makeText(this, "Permiso de notificaciones concedido", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Permiso denegado. Las notificaciones no funcionarán", Toast.LENGTH_LONG).show();
                    }
                }
        );
    }

    // VERIFICAR Y PEDIR PERMISOS
    private void checkAndRequestPermissions() {
        // Android 13+ requiere permiso POST_NOTIFICATIONS
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    // ABRIR DIÁLOGO DE CONFIGURACIÓN
    private void openNotificationConfigDialog() {
        NotificationConfigDialog dialog = new NotificationConfigDialog(this,
                (enabled, times, soundType, notifyAtEventTime, eventTimeSoundType) -> { // ✅ Parámetros actualizados
                    tempNotificationsEnabled = enabled;
                    tempNotificationTimes = times;
                    tempSoundType = soundType;

                    // Guardar configuración de alarma del momento
                    tempNotifyAtEventTime = notifyAtEventTime;
                    tempEventTimeSoundType = eventTimeSoundType;

                    updateNotificationStatusText();

                    if (enabled || notifyAtEventTime) { // ✅ Pedir permisos si cualquiera está activo
                        checkAndRequestPermissions();
                    }
                }
        );

        // Pasar configuración actual incluyendo alarma del momento
        dialog.setCurrentConfig(
                tempNotificationsEnabled,
                tempNotificationTimes,
                tempSoundType,
                tempNotifyAtEventTime,
                tempEventTimeSoundType
        );
        dialog.show();
    }

    // ACTUALIZAR TEXTO DE ESTADO
    private void updateNotificationStatusText() {
        int totalReminders = 0;

        if (tempNotificationsEnabled && tempNotificationTimes != null) {
            totalReminders += tempNotificationTimes.size();
        }

        // Contar alarma del momento
        if (tempNotifyAtEventTime) {
            totalReminders += 1;
        }

        if (totalReminders > 0) {
            String text = totalReminders + " recordatorio" + (totalReminders > 1 ? "s" : "") + " configurado" + (totalReminders > 1 ? "s" : "");

            // Añadir indicador visual si hay alarma del momento
            if (tempNotifyAtEventTime) {
                text += " (⏰ incluye alarma)";
            }

            tvNotificationStatus.setText(text);
            tvNotificationStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        } else {
            tvNotificationStatus.setText("Sin recordatorios");
            tvNotificationStatus.setTextColor(getResources().getColor(android.R.color.darker_gray));
        }
    }
    // Permisos para pantalla completa
    private void checkFullScreenIntentPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            // Android 14+
            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            if (notificationManager != null &&
                    !notificationManager.canUseFullScreenIntent()) {

                // Pedir permiso al usuario
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivity(intent);

                Toast.makeText(this,
                        "Por favor, activa 'Mostrar a pantalla completa' para las alarmas",
                        Toast.LENGTH_LONG).show();
            }
        }
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
    //                          GUARDAR EVENTO CON NOTIFICACIONES
    // =====================================================================

    private void saveEvent() {
        String title = editTitle.getText().toString().trim();
        String desc  = editDescription.getText().toString().trim();

        if (title.isEmpty()) {
            Toast.makeText(this, "El título es obligatorio", Toast.LENGTH_SHORT).show();
            return;
        }

        Timestamp start = new Timestamp(selectedCal.getTime());
        Timestamp end   = start;

        if (currentEvent == null) {
            // CREAR NUEVO
            CalendarEvent newEvent = new CalendarEvent();
            newEvent.setTitle(title);
            newEvent.setDescription(desc);
            newEvent.setStartDate(start);
            newEvent.setEndDate(end);
            newEvent.setNotificationsEnabled(tempNotificationsEnabled);
            newEvent.setNotificationTimes(tempNotificationTimes);
            newEvent.setNotificationSound(tempSoundType);

            // Configurar alarma del momento
            newEvent.setNotifyAtEventTime(tempNotifyAtEventTime);
            newEvent.setEventTimeNotificationSound(tempEventTimeSoundType);

            eventsManager.addEventWithNotifications(newEvent, eventId -> {
                newEvent.setId(eventId);

                // Programar todas las notificaciones (previas + momento)
                NotificationScheduler.scheduleNotifications(this, newEvent);

                Toast.makeText(this, "Evento creado correctamente", Toast.LENGTH_SHORT).show();
                finish();
            });

        } else {
            // ACTUALIZAR EXISTENTE
            currentEvent.setTitle(title);
            currentEvent.setDescription(desc);
            currentEvent.setStartDate(start);
            currentEvent.setEndDate(end);
            currentEvent.setNotificationsEnabled(tempNotificationsEnabled);
            currentEvent.setNotificationTimes(tempNotificationTimes);
            currentEvent.setNotificationSound(tempSoundType);

            // Actualizar alarma del momento
            currentEvent.setNotifyAtEventTime(tempNotifyAtEventTime);
            currentEvent.setEventTimeNotificationSound(tempEventTimeSoundType);

            eventsManager.updateEvent(currentEvent, () -> {
                NotificationScheduler.rescheduleNotifications(this, currentEvent);

                Toast.makeText(this, "Evento actualizado", Toast.LENGTH_SHORT).show();
                finish();
            });
        }
    }

    // =====================================================================
    //                          ELIMINAR EVENTO Y NOTIFICACIONES
    // =====================================================================
    private void deleteEvent() {
        if (currentEvent == null) return;

        // Cancelar notificaciones programadas
        NotificationScheduler.cancelNotifications(this, currentEvent);

        eventsManager.deleteEvent(currentEvent, () -> {
            Toast.makeText(this, "Evento eliminado", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}
