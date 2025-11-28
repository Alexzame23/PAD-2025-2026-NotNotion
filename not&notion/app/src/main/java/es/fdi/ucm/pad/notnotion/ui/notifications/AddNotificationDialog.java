package es.fdi.ucm.pad.notnotion.ui.notifications;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;

import es.fdi.ucm.pad.notnotion.R;
import es.fdi.ucm.pad.notnotion.data.model.Notification;

/**
 * Diálogo para añadir una notificación personalizada
 */
public class AddNotificationDialog extends Dialog {

    private EditText editQuantity;
    private Spinner spinnerTimeUnit;
    private Button btnAdd, btnCancel;

    private OnNotificationAddedListener listener;

    public interface OnNotificationAddedListener {
        void onNotificationAdded(Notification item);
    }

    public AddNotificationDialog(@NonNull Context context, OnNotificationAddedListener listener) {
        super(context);
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_add_notification);

        initViews();
        setupSpinner();
        setupListeners();
    }

    private void initViews() {
        editQuantity = findViewById(R.id.editQuantity);
        spinnerTimeUnit = findViewById(R.id.spinnerTimeUnit);
        btnAdd = findViewById(R.id.btnAddNotification);
        btnCancel = findViewById(R.id.btnCancelAddNotification);
    }

    private void setupSpinner() {
        // Crear array de opciones
        String[] timeUnits = new String[]{
                Notification.TimeUnit.MINUTES.getDisplayName(),
                Notification.TimeUnit.HOURS.getDisplayName(),
                Notification.TimeUnit.DAYS.getDisplayName(),
                Notification.TimeUnit.WEEKS.getDisplayName()
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_spinner_item,
                timeUnits
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTimeUnit.setAdapter(adapter);

        // Por defecto: Minutos
        spinnerTimeUnit.setSelection(0);
    }

    private void setupListeners() {
        btnAdd.setOnClickListener(v -> addNotification());
        btnCancel.setOnClickListener(v -> dismiss());
    }

    private void addNotification() {
        String quantityStr = editQuantity.getText().toString().trim();

        if (quantityStr.isEmpty()) {
            Toast.makeText(getContext(), "Introduce una cantidad", Toast.LENGTH_SHORT).show();
            return;
        }

        int quantity;
        try {
            quantity = Integer.parseInt(quantityStr);
            if (quantity <= 0) {
                Toast.makeText(getContext(), "La cantidad debe ser mayor que 0", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Cantidad inválida", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validar límites razonables
        Notification.TimeUnit selectedUnit = getSelectedTimeUnit();

        if (selectedUnit == Notification.TimeUnit.MINUTES && quantity > 10080) {
            // Más de una semana en minutos
            Toast.makeText(getContext(), "Cantidad demasiado grande, usa una unidad mayor", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedUnit == Notification.TimeUnit.HOURS && quantity > 168) {
            // Más de una semana en horas
            Toast.makeText(getContext(), "Cantidad demasiado grande, usa días o semanas", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedUnit == Notification.TimeUnit.DAYS && quantity > 365) {
            Toast.makeText(getContext(), "Máximo 365 días", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedUnit == Notification.TimeUnit.WEEKS && quantity > 52) {
            Toast.makeText(getContext(), "Máximo 52 semanas", Toast.LENGTH_SHORT).show();
            return;
        }

        Notification item = new Notification(quantity, selectedUnit);

        if (listener != null) {
            listener.onNotificationAdded(item);
        }

        dismiss();
    }

    private Notification.TimeUnit getSelectedTimeUnit() {
        int position = spinnerTimeUnit.getSelectedItemPosition();

        switch (position) {
            case 0: return Notification.TimeUnit.MINUTES;
            case 1: return Notification.TimeUnit.HOURS;
            case 2: return Notification.TimeUnit.DAYS;
            case 3: return Notification.TimeUnit.WEEKS;
            default: return Notification.TimeUnit.MINUTES;
        }
    }
}