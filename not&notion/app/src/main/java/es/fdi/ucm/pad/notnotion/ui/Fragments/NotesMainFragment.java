package es.fdi.ucm.pad.notnotion.ui.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.constraintlayout.helper.widget.Grid;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import es.fdi.ucm.pad.notnotion.R;
import es.fdi.ucm.pad.notnotion.data.adapter.FoldersAdapter;
import es.fdi.ucm.pad.notnotion.data.adapter.NotesAdapter;


public class NotesMainFragment extends Fragment {
    private RecyclerView foldersRecyclerView;
    private RecyclerView notesRecyclerView;
    private FoldersAdapter foldersAdapter;
    private NotesAdapter notesAdapter;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.notes_main,container,false);

        foldersRecyclerView = view.findViewById(R.id.recyclerFolders);
        notesRecyclerView = view.findViewById(R.id.recyclerNotes);

        //Grid de carpetas
        foldersAdapter = new FoldersAdapter();
        GridLayoutManager foldersLayout = new GridLayoutManager(getContext(),3);
        foldersRecyclerView.setLayoutManager(foldersLayout);
        foldersRecyclerView.setAdapter(foldersAdapter);
        foldersRecyclerView.setNestedScrollingEnabled(false); // scroll único

        foldersAdapter.setOnFolderClickListener(folder -> {
            // Aquí cargas las notas de la carpeta seleccionada
           // loadNotesForFolder(folder.getId());
        });


        notesAdapter = new NotesAdapter();
        GridLayoutManager notesLayout = new GridLayoutManager(getContext(),3);
        notesRecyclerView.setLayoutManager(notesLayout);
        notesRecyclerView.setAdapter(notesAdapter);
        notesRecyclerView.setNestedScrollingEnabled(false);

        notesAdapter.setOnNoteClickListener(note -> {
            // Aquí abres el item_note o fragmento de detalle
          //  openNoteDetail(note);
        });

        foldersAdapter.setFolders(new ArrayList<>());
        notesAdapter.setNotes(new ArrayList<>());

        return view;
    }
}
