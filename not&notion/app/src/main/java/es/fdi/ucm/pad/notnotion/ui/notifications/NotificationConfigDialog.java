package es.fdi.ucm.pad.notnotion.ui.notifications;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import es.fdi.ucm.pad.notnotion.R;
import es.fdi.ucm.pad.notnotion.data.model.Notification;
import es.fdi.ucm.pad.notnotion.ui.notifications.NotificationAdapter;

public class NotificationConfigDialog extends Dialog {

    private CheckBox chkEnableNotifications;
    private CheckBox chkNotifyAtEventTime;
    private LinearLayout layoutNotificationOptions;
    private LinearLayout layoutEventTimeOptions;
    private RecyclerView recyclerNotifications;
    private TextView tvNoNotifications;
    private Button btnAddNotification;

    private RadioGroup radioGroupSound;
    private RadioButton radioDefault, radioSilent;

    // Radio group para sonido de alarma del momento
    private RadioGroup radioGroupEventTimeSound;
    private RadioButton radioAlarmSound, radioDefaultSound, radioSilentSound;

    private Button btnSave, btnCancel;

    private NotificationAdapter adapter;
    private OnNotificationConfigListener listener;

    private boolean notificationsEnabled;
    private List<Notification> currentItems;
    private String currentSoundType;

    // Estado de alarma del momento
    private boolean notifyAtEventTime;
    private String eventTimeSoundType;
    private Spinner spinnerMaxSnooze;

    public interface OnNotificationConfigListener {
        void onConfigSaved(boolean enabled, List<Long> times, String soundType,
                           boolean notifyAtEventTime, String eventTimeSoundType);
    }

    public NotificationConfigDialog(@NonNull Context context, OnNotificationConfigListener listener) {
        super(context);
        this.listener = listener;
        this.currentItems = new ArrayList<>();
        this.currentSoundType = "default";
        this.notifyAtEventTime = false;
        this.eventTimeSoundType = "alarm";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_notification_config);

        initViews();
        setupRecyclerView();
        setupListeners();
        setupMaxSnoozeSpinner();
        loadCurrentConfig();
    }

    private void initViews() {
        chkEnableNotifications = findViewById(R.id.chkEnableNotifications);
        chkNotifyAtEventTime = findViewById(R.id.chkNotifyAtEventTime);
        layoutNotificationOptions = findViewById(R.id.layoutNotificationOptions);
        layoutEventTimeOptions = findViewById(R.id.layoutEventTimeOptions);
        recyclerNotifications = findViewById(R.id.recyclerNotifications);
        tvNoNotifications = findViewById(R.id.tvNoNotifications);
        btnAddNotification = findViewById(R.id.btnAddNotification);

        radioGroupSound = findViewById(R.id.radioGroupSound);
        radioDefault = findViewById(R.id.radioDefault);
        radioSilent = findViewById(R.id.radioSilent);

        // Radio group para sonido de alarma del momento
        radioGroupEventTimeSound = findViewById(R.id.radioGroupEventTimeSound);
        radioAlarmSound = findViewById(R.id.radioAlarmSound);
        radioDefaultSound = findViewById(R.id.radioDefaultSound);
        radioSilentSound = findViewById(R.id.radioSilentSound);
        spinnerMaxSnooze = findViewById(R.id.spinnerMaxSnooze);

        btnSave = findViewById(R.id.btnSaveConfig);
        btnCancel = findViewById(R.id.btnCancelConfig);

    }

    private void setupRecyclerView() {
        adapter = new NotificationAdapter((item, position) -> {
            adapter.removeItem(position);
            updateEmptyState();
        });

        recyclerNotifications.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerNotifications.setAdapter(adapter);
    }

    private void setupListeners() {
        chkEnableNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            layoutNotificationOptions.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        // Listener para checkbox de alarma del momento
        chkNotifyAtEventTime.setOnCheckedChangeListener((buttonView, isChecked) -> {
            layoutEventTimeOptions.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        btnAddNotification.setOnClickListener(v -> openAddNotificationDialog());

        btnSave.setOnClickListener(v -> saveConfiguration());
        btnCancel.setOnClickListener(v -> dismiss());
    }

    private void setupMaxSnoozeSpinner() {
        String[] options = new String[]{
                "Sin límite",
                "Máximo 1 vez",
                "Máximo 2 veces",
                "Máximo 3 veces",
                "Máximo 5 veces"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_spinner_item,
                options
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMaxSnooze.setAdapter(adapter);
    }

    private void loadCurrentConfig() {
        chkEnableNotifications.setChecked(notificationsEnabled);
        layoutNotificationOptions.setVisibility(notificationsEnabled ? View.VISIBLE : View.GONE);

        adapter.setItems(currentItems);
        updateEmptyState();

        // Sonido de notificaciones previas
        if ("silent".equals(currentSoundType)) {
            radioSilent.setChecked(true);
        } else {
            radioDefault.setChecked(true);
        }

        // Configuración de alarma del momento
        chkNotifyAtEventTime.setChecked(notifyAtEventTime);
        layoutEventTimeOptions.setVisibility(notifyAtEventTime ? View.VISIBLE : View.GONE);

        // Sonido de alarma del momento
        if ("alarm".equals(eventTimeSoundType)) {
            radioAlarmSound.setChecked(true);
        } else if ("silent".equals(eventTimeSoundType)) {
            radioSilentSound.setChecked(true);
        } else {
            radioDefaultSound.setChecked(true);
        }
    }

    private void openAddNotificationDialog() {
        AddNotificationDialog dialog = new AddNotificationDialog(getContext(), item -> {
            adapter.addItem(item);
            updateEmptyState();
        });

        dialog.show();
    }

    private void updateEmptyState() {
        if (adapter.getItemCount() == 0) {
            tvNoNotifications.setVisibility(View.VISIBLE);
            recyclerNotifications.setVisibility(View.GONE);
        } else {
            tvNoNotifications.setVisibility(View.GONE);
            recyclerNotifications.setVisibility(View.VISIBLE);
        }
    }

    private void saveConfiguration() {
        boolean enabled = chkEnableNotifications.isChecked();
        int maxSnoozeAllowed = spinnerMaxSnooze.getSelectedItemPosition();

        List<Long> times = new ArrayList<>();
        if (enabled) {
            List<Notification> items = adapter.getItems();
            for (Notification item : items) {
                times.add(item.getMillisBeforeEvent());
            }
        }

        String sound = radioSilent.isChecked() ? "silent" : "default";

        // Configuración de alarma del momento
        boolean notifyAtTime = chkNotifyAtEventTime.isChecked();
        String eventTimeSound = "default";
        if (radioAlarmSound.isChecked()) {
            eventTimeSound = "alarm";
        } else if (radioSilentSound.isChecked()) {
            eventTimeSound = "silent";
        }

        if (listener != null) {
            listener.onConfigSaved(enabled, times, sound, notifyAtTime, eventTimeSound);
        }

        dismiss();
    }

    /**
     * Establece la configuración actual desde el evento
     */
    public void setCurrentConfig(boolean enabled, List<Long> times, String sound,
                                 boolean notifyAtEventTime, String eventTimeSoundType) {
        this.notificationsEnabled = enabled;
        this.currentSoundType = sound != null ? sound : "default";

        this.currentItems = new ArrayList<>();
        if (times != null) {
            for (Long millis : times) {
                currentItems.add(new Notification(millis));
            }
        }

        // Configuración de alarma del momento
        this.notifyAtEventTime = notifyAtEventTime;
        this.eventTimeSoundType = eventTimeSoundType != null ? eventTimeSoundType : "alarm";
    }
}