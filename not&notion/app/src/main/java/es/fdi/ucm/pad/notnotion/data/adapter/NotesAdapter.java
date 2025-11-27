package es.fdi.ucm.pad.notnotion.data.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.graphics.Bitmap;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import es.fdi.ucm.pad.notnotion.R;
import es.fdi.ucm.pad.notnotion.data.model.Note;
import es.fdi.ucm.pad.notnotion.utils.ImageHelper;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NoteViewHolder> {

    private List<Note> notes = new ArrayList<>();
    private List<Note> fullList = new ArrayList<>(); // Lista completa

    // ------------------- CLICK NORMAL --------------------
    public interface OnNoteClickListener {
        void onNoteClick(Note note);
    }

    private OnNoteClickListener listener;

    public void setOnNoteClickListener(OnNoteClickListener listener) {
        this.listener = listener;
    }

    // ------------------- LONG CLICK --------------------
    public interface OnNoteLongClickListener {
        void onNoteLongClick(Note note, View view);
    }

    private OnNoteLongClickListener longClickListener;

    public void setOnNoteLongClickListener(OnNoteLongClickListener longClickListener) {
        this.longClickListener = longClickListener;
    }

    // -----------------------------------------------------

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Note note = notes.get(position);

        holder.titleView.setText(note.getTitle());

        // Cargar imagen de portada si existe
        String coverUrl = note.getCoverImageUrl();

        if (coverUrl != null && !coverUrl.isEmpty()) {
            // HAY PORTADA

            // Mostrar portada, OCULTAR icono por defecto
            holder.coverImageView.setVisibility(View.VISIBLE);
            holder.defaultIconView.setVisibility(View.GONE);

            // Detectar si es Base64 o URL
            if (ImageHelper.isValidBase64(coverUrl)) {
                // Convertir a Bitmap
                Bitmap bitmap = ImageHelper.convertBase64ToBitmap(coverUrl);

                if (bitmap != null) {
                    holder.coverImageView.setImageBitmap(bitmap);
                } else {
                    // Si falla la decodificación, volver a mostrar icono por defecto
                    holder.coverImageView.setVisibility(View.GONE);
                    holder.defaultIconView.setVisibility(View.VISIBLE);
                }

            } else {
                // Es URL - Usar Picasso
                Picasso.get()
                        .load(coverUrl)
                        .placeholder(R.drawable.icon_note)
                        .error(R.drawable.icon_note)
                        .resize(200, 200)
                        .centerCrop()
                        .into(holder.coverImageView);
            }
        } else {
            // NO HAY PORTADA

            // MOSTRAR icono por defecto
            holder.coverImageView.setVisibility(View.GONE);
            holder.defaultIconView.setVisibility(View.VISIBLE);
        }

        // CLICK NORMAL
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onNoteClick(note);
        });

        // LONG CLICK
        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) longClickListener.onNoteLongClick(note, v);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    // ------------------- NUEVO setNotes -------------------
    public void setNotes(List<Note> newNotes) {
        fullList.clear();
        notes.clear();

        if (newNotes != null) {
            fullList.addAll(newNotes);
            notes.addAll(newNotes);
        }
        notifyDataSetChanged();
    }

    // ------------------- NUEVO filter -------------------
    public void filter(String text) {
        notes.clear();
        if (text == null || text.trim().isEmpty()) {
            notes.addAll(fullList);
        } else {
            String q = text.toLowerCase();
            for (Note n : fullList) {
                if (n.getTitle() != null && n.getTitle().toLowerCase().contains(q)) {
                    notes.add(n);
                }
            }
        }
        notifyDataSetChanged();
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView titleView;
        ImageView coverImageView;
        ImageView defaultIconView;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            titleView = itemView.findViewById(R.id.noteTitle);
            coverImageView = itemView.findViewById(R.id.noteCoverImage);
            defaultIconView = itemView.findViewById(R.id.noteIcon);
        }
    }
}
