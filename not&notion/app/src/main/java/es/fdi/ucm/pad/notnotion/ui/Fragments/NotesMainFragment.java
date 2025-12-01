package es.fdi.ucm.pad.notnotion.ui.Fragments;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import es.fdi.ucm.pad.notnotion.R;
import es.fdi.ucm.pad.notnotion.data.adapter.FoldersAdapter;
import es.fdi.ucm.pad.notnotion.data.adapter.NotesAdapter;
import es.fdi.ucm.pad.notnotion.data.firebase.CalendarEventsManager;
import es.fdi.ucm.pad.notnotion.data.model.CalendarEvent;
import es.fdi.ucm.pad.notnotion.data.model.Folder;
import es.fdi.ucm.pad.notnotion.data.model.Note;
import es.fdi.ucm.pad.notnotion.ui.firebase.FirebaseDataLoader;

public class NotesMainFragment extends Fragment {

    private RecyclerView foldersRecyclerView;
    private RecyclerView notesRecyclerView;
    private FoldersAdapter foldersAdapter;
    private NotesAdapter notesAdapter;
    private TextInputEditText busquedaBarra;
    private TextView emptyMessage;

    private FirebaseDataLoader loader;
    private String currentFolderId = "root"; // carpeta actual


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.notes_main, container, false);

        Log.e("FRAGMENT_DEBUG", "** ESTE ES EL FRAGMENT QUE SE ESTÁ EJECUTANDO **");

        foldersRecyclerView = view.findViewById(R.id.recyclerFolders);
        notesRecyclerView = view.findViewById(R.id.recyclerNotes);
        //busquedaBarra = view.findViewById(R.id.busquedaBarra);
        //emptyMessage = view.findViewById(R.id.emptyMessage);
        if (busquedaBarra == null) {
            Log.e("BUSQUEDA_DEBUG", "busquedaBarra es null!!");
        } else {
            Log.d("BUSQUEDA_DEBUG", "busquedaBarra inicializado correctamente");
        }
        setupAdapters();
        setupSearchBar();

        loader = new FirebaseDataLoader();
        loadData();

        return view;
    }

    private void setupAdapters() {
        foldersAdapter = new FoldersAdapter();
        notesAdapter = new NotesAdapter();

        GridLayoutManager foldersLayout = new GridLayoutManager(getContext(), 3);
        foldersRecyclerView.setLayoutManager(foldersLayout);
        foldersRecyclerView.setAdapter(foldersAdapter);
        foldersRecyclerView.setNestedScrollingEnabled(false);

        foldersAdapter.setOnFolderClickListener(folder -> {
            // Cambiar carpeta actual y recargar notas
            currentFolderId = folder.getId();
            loadData();
        });

        GridLayoutManager notesLayout = new GridLayoutManager(getContext(), 3);
        notesRecyclerView.setLayoutManager(notesLayout);
        notesRecyclerView.setAdapter(notesAdapter);
        notesRecyclerView.setNestedScrollingEnabled(false);

        notesAdapter.setOnNoteClickListener(note -> {
            Log.d("DEBUG_CLICK", "Click en nota: " + note.getTitle());
        });


        notesAdapter.setOnNoteLongClickListener((note, view) -> {
            Log.d("DEBUG_LONGCLICK", "Long click en nota: " + note.getTitle());

            androidx.appcompat.widget.PopupMenu menu =
                    new androidx.appcompat.widget.PopupMenu(getContext(), view);

            // Asignamos IDs explícitos a cada opción
            menu.getMenu().add(0, 0, 0, "Añadir a favoritos");
            menu.getMenu().add(0, 1, 1, "Renombrar");
            menu.getMenu().add(0, 2, 2, "Asociar a fecha");
            menu.getMenu().add(0, 3, 3, "Eliminar");

            menu.setOnMenuItemClickListener(item -> {
                Log.d("DEBUG_MENU", "Click en opción: id=" + item.getItemId()
                        + " title=" + item.getTitle());

                switch (item.getItemId()) {
                    case 2: // "Asociar a fecha"
                        openDateTimePicker(note);
                        return true;

                    case 0:
                        // TODO: implementar favoritos
                        return true;

                    case 1:
                        // TODO: implementar renombrar
                        return true;

                    case 3:
                        // TODO: implementar eliminar
                        return true;
                }

                return false;
            });

            menu.show();
        });



        foldersAdapter.setFolders(new ArrayList<>());
        notesAdapter.setNotes(new ArrayList<>());
    }



    private void setupSearchBar() {
        busquedaBarra.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s == null ? "" : s.toString();
                foldersAdapter.filter(query);
                //notesAdapter.filter(query);
                updateEmptyMessage();
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
    }

    private void loadData() {
        loader.loadFoldersInRoute(currentFolderId, this::onFoldersLoaded);
        loader.loadNotesInFolder(currentFolderId, this::onNotesLoaded);
    }

    private void onFoldersLoaded(List<Folder> folders) {
        foldersAdapter.setFolders(folders);
        updateEmptyMessage();
    }

    private void onNotesLoaded(List<Note> notes) {
        notesAdapter.setNotes(notes);
        updateEmptyMessage();
    }

    private void updateEmptyMessage() {
        boolean emptyFolders = foldersAdapter.getItemCount() == 0;
        boolean emptyNotes = notesAdapter.getItemCount() == 0;
        emptyMessage.setVisibility(emptyFolders && emptyNotes ? View.VISIBLE : View.GONE);
    }

    private void openDateTimePicker(Note note) {
        // DIALOG DE FECHA
        final Calendar calendar = Calendar.getInstance();

        Log.d("DEBUG_PICKER", "Entrando en openDateTimePicker para: " + note.getTitle());


        DatePickerDialog dateDialog = new DatePickerDialog(
                getContext(),
                (view, year, month, dayOfMonth) -> {

                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    // AHORA PEDIR HORA
                    TimePickerDialog timeDialog = new TimePickerDialog(
                            getContext(),
                            (timeView, hour, minute) -> {

                                calendar.set(Calendar.HOUR_OF_DAY, hour);
                                calendar.set(Calendar.MINUTE, minute);
                                calendar.set(Calendar.SECOND, 0);

                                long millis = calendar.getTimeInMillis();

                                createCalendarEventFromNote(note, millis);

                            },
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE),
                            true
                    );

                    timeDialog.show();

                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        dateDialog.show();
    }

    private void createCalendarEventFromNote(Note note, long millis) {

        CalendarEventsManager evManager = new CalendarEventsManager();

        Timestamp ts = new Timestamp(new java.util.Date(millis));

        evManager.addEvent(
                note.getTitle(),      // título = título de la nota
                "",                  // sin descripción
                ts,                  // fecha/hora inicio
                ts,                  // fecha/hora fin
                note.getId(),        // 🔥 vínculo a la nota
                0,                   // sin recordatorio
                false,               // no recurrente
                null                 // sin patrón de recurrencia
        );

        Toast.makeText(getContext(), "Nota añadida al calendario", Toast.LENGTH_SHORT).show();
    }



}
