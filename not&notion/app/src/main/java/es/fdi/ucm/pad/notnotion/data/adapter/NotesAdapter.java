package es.fdi.ucm.pad.notnotion.data.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import es.fdi.ucm.pad.notnotion.data.model.Folder;
import es.fdi.ucm.pad.notnotion.data.model.Note;

import java.util.ArrayList;
import java.util.List;

import es.fdi.ucm.pad.notnotion.R;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NoteViewHolder> {

    private List<Note> notes = new ArrayList<>();
    public interface OnNoteClickListener {
        void onNoteClick(Note note);
    }
    private OnNoteClickListener listener;
    public void setOnNoteClickListener(NotesAdapter.OnNoteClickListener listener) {
        this.listener = listener;
    }
    @NonNull
    @Override
    public NotesAdapter.NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_note, parent, false);
        return new NotesAdapter.NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotesAdapter.NoteViewHolder holder, int position) {
        Note note = notes.get(position);
        holder.contentView.setText(note.getContent());
        holder.titleView.setText(note.getTitle());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onNoteClick(note);
            }
        });
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    public void setNotes(List<Note> newNotes){
        notes.clear();
        if(newNotes!= null)
            notes.addAll(newNotes);
        notifyDataSetChanged();
    }
    static class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView titleView, contentView;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            titleView = itemView.findViewById(R.id.itemTitle);
            contentView = itemView.findViewById(R.id.itemDescription);
        }

    }
}
