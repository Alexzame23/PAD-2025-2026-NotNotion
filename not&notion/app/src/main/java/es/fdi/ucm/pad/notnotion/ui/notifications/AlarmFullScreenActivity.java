package es.fdi.ucm.pad.notnotion.ui.notifications;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import es.fdi.ucm.pad.notnotion.R;
import es.fdi.ucm.pad.notnotion.data.firebase.CalendarEventsManager;
import es.fdi.ucm.pad.notnotion.data.model.CalendarEvent;
import es.fdi.ucm.pad.notnotion.utils.NotificationHelper;

/**
 * Actividad de pantalla completa que se muestra cuando suena una alarma
 *
 * Características:
 * - Se muestra sobre la pantalla de bloqueo
 * - Pantalla completa inmersiva
 * - Reproduce sonido de alarma en bucle
 * - Vibración continua
 * - Botones grandes para posponer y descartar
 */
public class AlarmFullScreenActivity extends AppCompatActivity {

    private static final String TAG = "AlarmFullScreen";

    private TextView tvEventTitle;
    private TextView tvEventDescription;
    private TextView tvEventTime;
    private TextView tvSnoozeInfo;
    private Button btnSnooze;
    private Button btnDismiss;

    private MediaPlayer mediaPlayer;
    private Vibrator vibrator;
    private boolean isVibrating = false;

    private String eventId;
    private CalendarEvent currentEvent;
    private CalendarEventsManager eventsManager;

    private SimpleDateFormat timeFormat;
    private SimpleDateFormat dateFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ✅ Configurar para mostrarse sobre pantalla de bloqueo
        setupWindowFlags();

        setContentView(R.layout.activity_alarm_fullscreen);

        // Formatos de fecha
        timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

        eventsManager = new CalendarEventsManager();

        initViews();
        loadEventData();
        startAlarmSoundAndVibration();
    }

    /**
     * Configura las flags de la ventana para mostrar sobre pantalla de bloqueo
     */
    private void setupWindowFlags() {
        // Actividad de pantalla completa
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            // Android 8.1+
            setShowWhenLocked(true);
            setTurnScreenOn(true);

            KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
            if (keyguardManager != null) {
                keyguardManager.requestDismissKeyguard(this, null);
            }
        } else {
            // Android 8.0 y anteriores
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                    WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
        }

        // Pantalla completa inmersiva
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );
    }

    private void initViews() {
        tvEventTitle = findViewById(R.id.tvAlarmTitle);
        tvEventDescription = findViewById(R.id.tvAlarmDescription);
        tvEventTime = findViewById(R.id.tvAlarmTime);
        tvSnoozeInfo = findViewById(R.id.tvAlarmSnoozeInfo);
        btnSnooze = findViewById(R.id.btnAlarmSnooze);
        btnDismiss = findViewById(R.id.btnAlarmDismiss);

        btnSnooze.setOnClickListener(v -> handleSnooze());
        btnDismiss.setOnClickListener(v -> handleDismiss());
    }

    private void loadEventData() {
        eventId = getIntent().getStringExtra("eventId");
        String eventTitle = getIntent().getStringExtra("eventTitle");
        String eventDescription = getIntent().getStringExtra("eventDescription");
        long eventTimeMillis = getIntent().getLongExtra("eventTimeMillis", 0);
        boolean isSnoozed = getIntent().getBooleanExtra("isSnoozed", false);

        if (eventId == null || eventTitle == null) {
            Log.e(TAG, "Datos del evento incompletos");
            finish();
            return;
        }

        // Mostrar información básica inmediatamente
        tvEventTitle.setText(eventTitle);

        if (eventDescription != null && !eventDescription.isEmpty()) {
            tvEventDescription.setText(eventDescription);
            tvEventDescription.setVisibility(View.VISIBLE);
        } else {
            tvEventDescription.setVisibility(View.GONE);
        }

        if (eventTimeMillis > 0) {
            Date eventDate = new Date(eventTimeMillis);
            String timeStr = timeFormat.format(eventDate);
            String dateStr = dateFormat.format(eventDate);
            tvEventTime.setText(timeStr + " • " + dateStr);
        }

        // Indicador de pospuesto
        if (isSnoozed) {
            tvSnoozeInfo.setText("🔁 Alarma pospuesta");
            tvSnoozeInfo.setVisibility(View.VISIBLE);
        } else {
            tvSnoozeInfo.setVisibility(View.GONE);
        }

        // Cargar evento completo desde Firebase para info adicional
        eventsManager.getEventById(eventId, event -> {
            if (event != null) {
                currentEvent = event;

                // Actualizar UI si hay más información
                if (event.getSnoozeCount() > 0) {
                    tvSnoozeInfo.setText("🔁 Pospuesto " + event.getSnoozeCount() +
                            " vez" + (event.getSnoozeCount() > 1 ? "es" : ""));
                    tvSnoozeInfo.setVisibility(View.VISIBLE);
                }

                // Verificar si puede posponer
                if (!event.canSnooze()) {
                    btnSnooze.setEnabled(false);
                    btnSnooze.setText("Límite alcanzado");
                }
            }
        });
    }

    /**
     * Inicia el sonido de alarma y la vibración
     */
    private void startAlarmSoundAndVibration() {
        String soundType = getIntent().getStringExtra("soundType");

        // Reproducir sonido
        if (!"silent".equals(soundType)) {
            try {
                Uri alarmUri;

                if ("alarm".equals(soundType)) {
                    alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
                } else {
                    alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                }

                mediaPlayer = new MediaPlayer();
                mediaPlayer.setDataSource(this, alarmUri);

                // Configurar como alarma
                AudioAttributes attributes = new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build();
                mediaPlayer.setAudioAttributes(attributes);

                // Volumen máximo
                AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                if (audioManager != null) {
                    int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);
                    audioManager.setStreamVolume(AudioManager.STREAM_ALARM, maxVolume, 0);
                }

                // Reproducir en bucle
                mediaPlayer.setLooping(true);
                mediaPlayer.prepare();
                mediaPlayer.start();

                Log.d(TAG, "✅ Sonido de alarma iniciado");

            } catch (Exception e) {
                Log.e(TAG, "Error al reproducir sonido", e);
            }
        }

        // Iniciar vibración
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null && vibrator.hasVibrator()) {
            long[] pattern = {0, 1000, 500, 1000, 500, 1000}; // Patrón de vibración

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                VibrationEffect effect = VibrationEffect.createWaveform(pattern, 0); // 0 = repetir
                vibrator.vibrate(effect);
            } else {
                vibrator.vibrate(pattern, 0);
            }

            isVibrating = true;
            Log.d(TAG, "✅ Vibración iniciada");
        }
    }

    /**
     * Detiene el sonido y la vibración
     */
    private void stopAlarmSoundAndVibration() {
        // Detener sonido
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                mediaPlayer.release();
                mediaPlayer = null;
                Log.d(TAG, "Sonido detenido");
            } catch (Exception e) {
                Log.e(TAG, "Error al detener sonido", e);
            }
        }

        // Detener vibración
        if (vibrator != null && isVibrating) {
            vibrator.cancel();
            isVibrating = false;
            Log.d(TAG, "Vibración detenida");
        }
    }

    /**
     * Maneja el postponimiento de la alarma
     */
    private void handleSnooze() {
        Log.d(TAG, "Usuario pulsó POSPONER");

        // Cancelar notificación actual
        NotificationHelper.cancelEventNotifications(this, eventId);

        if (currentEvent != null) {
            // Verificar si puede posponer
            if (!currentEvent.canSnooze()) {
                NotificationHelper.showToast(this,
                        "Has alcanzado el límite de postponimientos");
                handleDismiss();
                return;
            }

            // Incrementar contador
            currentEvent.incrementSnoozeCount();

            // Calcular tiempo de snooze (5 minutos)
            long snoozeTimeMillis = System.currentTimeMillis() + (5 * 60 * 1000L);

            // Actualizar en Firebase
            eventsManager.updateEvent(currentEvent, () -> {
                // Programar nueva alarma
                NotificationScheduler.scheduleSnoozeAlarm(this, currentEvent, snoozeTimeMillis);

                NotificationHelper.showToast(this, "⏰ Alarma pospuesta 5 minutos");
                Log.d(TAG, "Alarma pospuesta correctamente");
            });
        }

        // Cerrar actividad
        stopAlarmSoundAndVibration();
        finish();
    }

    /**
     * Maneja el descarte de la alarma
     */
    private void handleDismiss() {
        Log.d(TAG, "Usuario pulsó DESCARTAR");

        // Cancelar notificación
        NotificationHelper.cancelEventNotifications(this, eventId);

        // Resetear contador de snooze si existe el evento
        if (currentEvent != null && currentEvent.getSnoozeCount() > 0) {
            currentEvent.resetSnoozeCount();
            eventsManager.updateEvent(currentEvent, () -> {
                Log.d(TAG, "Contador de snooze reseteado");
            });
        }

        // Cerrar actividad
        stopAlarmSoundAndVibration();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopAlarmSoundAndVibration();
    }

    @Override
    public void onBackPressed() {
        // Evitar que se cierre con el botón atrás accidentalmente
        // El usuario debe usar los botones de la interfaz
    }
}