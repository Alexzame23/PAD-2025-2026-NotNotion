package es.fdi.ucm.pad.notnotion.ui.notifications;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;

import java.util.List;

import es.fdi.ucm.pad.notnotion.data.model.CalendarEvent;
import es.fdi.ucm.pad.notnotion.ui.notifications.NotificationReceiver;
import es.fdi.ucm.pad.notnotion.utils.NotificationHelper;

public class NotificationScheduler {

    private static final String TAG = "NotificationScheduler";

    /**
     * Programa todas las notificaciones de un evento (previas + momento)
     */
    public static void scheduleNotifications(Context context, CalendarEvent event) {

        // Programar notificaciones previas
        if (event.isNotificationsEnabled() && event.getNotificationTimes() != null) {
            long eventTimeMillis = event.getStartDate().toDate().getTime();
            List<Long> notificationTimes = event.getNotificationTimes();

            for (int i = 0; i < notificationTimes.size(); i++) {
                long millisBeforeEvent = notificationTimes.get(i);
                long notificationTime = eventTimeMillis - millisBeforeEvent;

                if (notificationTime > System.currentTimeMillis()) {
                    scheduleNotificationElapsed(context, event, notificationTime, i, false);
                } else {
                    Log.d(TAG, "Notificación previa en el pasado, se omite");
                }
            }
        }

        // Programar alarma en el momento del evento
        if (event.isNotifyAtEventTime()) {
            long eventTimeMillis = event.getStartDate().toDate().getTime();

            if (eventTimeMillis > System.currentTimeMillis()) {
                scheduleEventTimeAlarm(context, event, eventTimeMillis);
            } else {
                Log.d(TAG, "Alarma del momento en el pasado, se omite");
            }
        }
    }

    /**
     * Programa una notificación usando ELAPSED_REALTIME_WAKEUP
     *
     * @param targetTimeMillis Tiempo absoluto (System.currentTimeMillis) en que debe disparar
     */
    private static void scheduleNotificationElapsed(
            Context context,
            CalendarEvent event,
            long targetTimeMillis,
            int notificationIndex,
            boolean isEventTimeAlarm
    ) {

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            Log.e(TAG, "AlarmManager no disponible");
            return;
        }

        // Convertir tiempo absoluto a tiempo relativo (ELAPSED)
        long currentTimeMillis = System.currentTimeMillis();
        long elapsedRealtimeNow = SystemClock.elapsedRealtime();
        long delayMillis = targetTimeMillis - currentTimeMillis;
        long triggerAtElapsed = elapsedRealtimeNow + delayMillis;

        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.putExtra("eventId", event.getId());
        intent.putExtra("eventTitle", event.getTitle());
        intent.putExtra("eventDescription", event.getDescription());
        intent.putExtra("eventTimeMillis", event.getStartDate().toDate().getTime());
        intent.putExtra("soundType", event.getNotificationSound());
        intent.putExtra("isEventTimeAlarm", isEventTimeAlarm);
        intent.putExtra("isSnoozed", false);

        int requestCode = generateRequestCode(event.getId(), notificationIndex);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // Usar ELAPSED_REALTIME_WAKEUP con setExactAndAllowWhileIdle
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        triggerAtElapsed,
                        pendingIntent
                );
            } else {
                alarmManager.setExact(
                        AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        triggerAtElapsed,
                        pendingIntent
                );
            }

            Log.d(TAG, "Notificación programada (ELAPSED_REALTIME): " + event.getTitle() +
                    "\n   Disparará en: " + (delayMillis / 1000) + " segundos" +
                    "\n   Hora objetivo: " + new java.util.Date(targetTimeMillis) +
                    "\n   ElapsedRealtime: " + triggerAtElapsed);

        } catch (SecurityException e) {
            Log.e(TAG, "Permiso SCHEDULE_EXACT_ALARM denegado", e);
        }
    }

    /**
     * Programa la alarma en el momento exacto del evento
     */
    private static void scheduleEventTimeAlarm(Context context, CalendarEvent event, long eventTimeMillis) {

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            Log.e(TAG, "AlarmManager no disponible");
            return;
        }

        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.putExtra("eventId", event.getId());
        intent.putExtra("eventTitle", event.getTitle());
        intent.putExtra("eventDescription", event.getDescription());
        intent.putExtra("eventTimeMillis", eventTimeMillis);
        intent.putExtra("soundType", event.getEventTimeNotificationSound());
        intent.putExtra("isEventTimeAlarm", true); // ✅ Marcar como alarma del momento

        // RequestCode especial para alarma del momento
        int requestCode = generateEventTimeAlarmRequestCode(event.getId());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // Usar setAlarmClock para máxima prioridad
                AlarmManager.AlarmClockInfo alarmClockInfo = new AlarmManager.AlarmClockInfo(
                        eventTimeMillis,
                        pendingIntent
                );
                alarmManager.setAlarmClock(alarmClockInfo, pendingIntent);
            } else {
                alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        eventTimeMillis,
                        pendingIntent
                );
            }

            Log.d(TAG, "ALARMA DEL MOMENTO programada: " + event.getTitle() +
                    " a las " + new java.util.Date(eventTimeMillis));

        } catch (SecurityException e) {
            Log.e(TAG, "Permiso SCHEDULE_EXACT_ALARM denegado", e);
        }
    }

    /**
     * Cancela todas las notificaciones programadas de un evento
     */
    public static void cancelNotifications(Context context, CalendarEvent event) {

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        // Cancelar notificaciones previas
        List<Long> notificationTimes = event.getNotificationTimes();
        if (notificationTimes != null) {
            for (int i = 0; i < notificationTimes.size(); i++) {
                Intent intent = new Intent(context, NotificationReceiver.class);
                int requestCode = generateRequestCode(event.getId(), i);

                PendingIntent pendingIntent = PendingIntent.getBroadcast(
                        context,
                        requestCode,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                );

                alarmManager.cancel(pendingIntent);
            }
        }

        // Cancelar alarma del momento
        Intent intent = new Intent(context, NotificationReceiver.class);
        int requestCode = generateEventTimeAlarmRequestCode(event.getId());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        alarmManager.cancel(pendingIntent);

        NotificationHelper.cancelEventNotifications(context, event.getId());

        // Cancelar posibles snoozes pendientes
        for (int i = 0; i <= event.getSnoozeCount(); i++) {
            Intent snoozeIntent = new Intent(context, NotificationReceiver.class);
            int snoozeRequestCode = generateSnoozeRequestCode(event.getId(), i);

            PendingIntent snoozePendingIntent = PendingIntent.getBroadcast(
                    context,
                    snoozeRequestCode,
                    snoozeIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
            alarmManager.cancel(snoozePendingIntent);
        }

        Log.d(TAG, "Todas las notificaciones canceladas para: " + event.getTitle());
    }

    private static int generateRequestCode(String eventId, int notificationIndex) {
        return (eventId.hashCode() * 100) + notificationIndex;
    }

    // RequestCode especial para alarma del momento
    private static int generateEventTimeAlarmRequestCode(String eventId) {
        return (eventId.hashCode() * 1000);
    }

    public static void rescheduleNotifications(Context context, CalendarEvent event) {
        cancelNotifications(context, event);
        scheduleNotifications(context, event);
    }

    /**
     * Programa una alarma pospuesta usando ELAPSED_REALTIME_WAKEUP
     */
    public static void scheduleSnoozeAlarm(Context context, CalendarEvent event, long targetTimeMillis) {

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            Log.e(TAG, "AlarmManager no disponible");
            return;
        }

        long currentTimeMillis = System.currentTimeMillis();
        long elapsedRealtimeNow = SystemClock.elapsedRealtime();
        long delayMillis = targetTimeMillis - currentTimeMillis;
        long triggerAtElapsed = elapsedRealtimeNow + delayMillis;

        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.putExtra("eventId", event.getId());
        intent.putExtra("eventTitle", event.getTitle());
        intent.putExtra("eventDescription", event.getDescription());
        intent.putExtra("eventTimeMillis", event.getStartDate().toDate().getTime());
        intent.putExtra("soundType", event.getEventTimeNotificationSound());
        intent.putExtra("isEventTimeAlarm", true);
        intent.putExtra("isSnoozed", true);

        int requestCode = generateSnoozeRequestCode(event.getId(), event.getSnoozeCount());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // Usar ELAPSED_REALTIME para snooze
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        triggerAtElapsed,
                        pendingIntent
                );
            } else {
                alarmManager.setExact(
                        AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        triggerAtElapsed,
                        pendingIntent
                );
            }

            Log.d(TAG, "🔁 Alarma POSPUESTA programada (ELAPSED_REALTIME): " + event.getTitle() +
                    "\n   Disparará en: " + (delayMillis / 1000) + " segundos (" + (delayMillis / 60000) + " min)" +
                    "\n   Snooze #" + event.getSnoozeCount() +
                    "\n   ElapsedRealtime: " + triggerAtElapsed);

        } catch (SecurityException e) {
            Log.e(TAG, "Permiso SCHEDULE_EXACT_ALARM denegado", e);
        }
    }

    // RequestCode único para cada snooze
    private static int generateSnoozeRequestCode(String eventId, int snoozeNumber) {
        return (eventId.hashCode() * 2000) + snoozeNumber;
    }
}