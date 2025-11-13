package es.fdi.ucm.pad.notnotion.ui.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import es.fdi.ucm.pad.notnotion.R;

public class CalendarFragment extends Fragment {

    private CalendarView calendarView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Transformamos xml en vista
        return inflater.inflate(R.layout.calendar_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) { //tiene que haber una view, pero la instancia depende de si ya se habia cargado antes
        super.onViewCreated(view, savedInstanceState);

        // Inicializamos CalendarView
        calendarView = view.findViewById(R.id.calendarView);

        //Podemos cambiar de dia en funcion de donde toques
        calendarView.setOnDateChangeListener((view1, year, month, dayOfMonth) -> {
            // month empieza en 0
            int displayMonth = month + 1;

            Log.d("CalendarFragment", "Fecha seleccionada: " + dayOfMonth + "/" + displayMonth + "/" + year);
        });
    }
}
