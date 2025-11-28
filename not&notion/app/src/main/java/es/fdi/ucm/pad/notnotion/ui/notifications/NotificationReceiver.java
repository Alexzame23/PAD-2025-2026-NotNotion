package es.fdi.ucm.pad.notnotion.ui.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import es.fdi.ucm.pad.notnotion.utils.NotificationHelper;

public class NotificationReceiver extends BroadcastReceiver {

    private static final String TAG = "NotificationReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d(TAG, "Notificación recibida");

        String eventId = intent.getStringExtra("eventId");
        String eventTitle = intent.getStringExtra("eventTitle");
        String eventDescription = intent.getStringExtra("eventDescription");
        long eventTimeMillis = intent.getLongExtra("eventTimeMillis", 0);
        String soundType = intent.getStringExtra("soundType");
        boolean isEventTimeAlarm = intent.getBooleanExtra("isEventTimeAlarm", false);
        boolean isSnoozed = intent.getBooleanExtra("isSnoozed", false); // ✅ NUEVO

        if (eventId == null || eventTitle == null) {
            Log.e(TAG, "Datos del evento incompletos");
            return;
        }

        if (isEventTimeAlarm) {
            // Alarma en el momento del evento (con snooze)
            NotificationHelper.showEventAlarm(
                    context,
                    eventId,
                    eventTitle,
                    eventDescription != null ? eventDescription : "",
                    soundType != null ? soundType : "alarm",
                    isSnoozed // ✅ Pasar flag
            );
        } else {
            // Notificación previa (recordatorio)
            NotificationHelper.showEventNotification(
                    context,
                    eventId,
                    eventTitle,
                    eventDescription != null ? eventDescription : "",
                    eventTimeMillis,
                    soundType != null ? soundType : "default"
            );
        }
    }
}