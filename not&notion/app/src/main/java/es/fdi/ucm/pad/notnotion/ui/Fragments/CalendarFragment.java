package es.fdi.ucm.pad.notnotion.ui.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import es.fdi.ucm.pad.notnotion.R;
import es.fdi.ucm.pad.notnotion.data.firebase.CalendarEventsManager;
import es.fdi.ucm.pad.notnotion.data.model.CalendarEvent;
import es.fdi.ucm.pad.notnotion.ui.events.EventAdapter;
import es.fdi.ucm.pad.notnotion.ui.events.EventEditActivity;

public class CalendarFragment extends Fragment {

    private CalendarView calendarView;
    private long selectedDateMillis;

    private RecyclerView recyclerEvents;
    private EventAdapter eventAdapter;
    private CalendarEventsManager eventsManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.calendar_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        // --- UI ---
        calendarView = view.findViewById(R.id.calendarView);
        View btnAddEvent = view.findViewById(R.id.btnAddEvent);
        recyclerEvents = view.findViewById(R.id.recyclerEvents);

        recyclerEvents.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(getContext()));

        // --- ADAPTER ---
        eventAdapter = new EventAdapter();
        recyclerEvents.setAdapter(eventAdapter);

        eventAdapter.setOnEventClickListener(event -> {
            Intent intent = new Intent(getContext(), EventEditActivity.class);
            intent.putExtra("eventId", event.getId());
            startActivity(intent);
        });


        // --- FIRESTORE MANAGER ---
        eventsManager = new CalendarEventsManager();

        // --- FECHA INICIAL ---
        selectedDateMillis = calendarView.getDate();
        loadEventsForThisDay(selectedDateMillis);

        // --- CAMBIO DE FECHA ---
        calendarView.setOnDateChangeListener((v, year, month, dayOfMonth) -> {

            Calendar cal = Calendar.getInstance();
            cal.set(year, month, dayOfMonth, 0, 0, 0);
            selectedDateMillis = cal.getTimeInMillis();

            Log.d("CalendarFragment", "Nueva fecha: " + dayOfMonth + "/" + (month + 1) + "/" + year);

            loadEventsForThisDay(selectedDateMillis);
        });

        // --- BOTÓN AÑADIR EVENTO ---
        btnAddEvent.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), EventEditActivity.class);
            intent.putExtra("selectedDate", selectedDateMillis);
            startActivity(intent);
        });
    }

    // ============================================================================
    //      CARGAR EVENTOS DESDE FIRESTORE FILTRADOS POR FECHA SELECCIONADA
    // ============================================================================

    private void loadEventsForThisDay(long millis) {

        // Inicio del día
        Calendar start = Calendar.getInstance();
        start.setTimeInMillis(millis);
        start.set(Calendar.HOUR_OF_DAY, 0);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);

        long dayStart = start.getTimeInMillis();

        // Fin del día
        Calendar end = Calendar.getInstance();
        end.setTimeInMillis(millis);
        end.set(Calendar.HOUR_OF_DAY, 23);
        end.set(Calendar.MINUTE, 59);
        end.set(Calendar.SECOND, 59);

        long dayEnd = end.getTimeInMillis();

        Log.d("CalendarFragment", "Cargando eventos entre: " + dayStart + " y " + dayEnd);

        eventsManager.getAllEvents(query -> {

            List<CalendarEvent> matchingEvents = new ArrayList<>();

            for (QueryDocumentSnapshot doc : query) {
                CalendarEvent ev = doc.toObject(CalendarEvent.class);
                ev.setId(doc.getId());

                long time = ev.getStartDate().toDate().getTime();

                if (time >= dayStart && time <= dayEnd) {
                    matchingEvents.add(ev);
                }
            }

            Log.d("CalendarFragment", "EVENTOS ENCONTRADOS PARA ESTE DÍA: " + matchingEvents.size());

            eventAdapter.setEvents(matchingEvents);
        });
    }
}
