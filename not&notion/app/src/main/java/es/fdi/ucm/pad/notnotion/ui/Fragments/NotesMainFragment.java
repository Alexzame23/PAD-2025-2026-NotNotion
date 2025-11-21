package es.fdi.ucm.pad.notnotion.ui.firebase;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import es.fdi.ucm.pad.notnotion.R;
import es.fdi.ucm.pad.notnotion.data.adapter.FoldersAdapter;
import es.fdi.ucm.pad.notnotion.data.adapter.NotesAdapter;
import es.fdi.ucm.pad.notnotion.data.model.Folder;
import es.fdi.ucm.pad.notnotion.data.model.Note;

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

        foldersRecyclerView = view.findViewById(R.id.recyclerFolders);
        notesRecyclerView = view.findViewById(R.id.recyclerNotes);
        busquedaBarra = view.findViewById(R.id.busquedaBarra);
        emptyMessage = view.findViewById(R.id.emptyMessage);
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
            // Abrir detalle de nota
            // openNoteDetail(note);
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
                notesAdapter.filter(query);
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
}
