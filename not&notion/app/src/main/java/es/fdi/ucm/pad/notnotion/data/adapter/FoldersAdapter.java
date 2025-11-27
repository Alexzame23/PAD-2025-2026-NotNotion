package es.fdi.ucm.pad.notnotion.data.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import es.fdi.ucm.pad.notnotion.R;
import es.fdi.ucm.pad.notnotion.data.model.Folder;

public class FoldersAdapter extends RecyclerView.Adapter<FoldersAdapter.FolderViewHolder> {

    private static final String TAG = "BUSQUEDA_DEBUG";
    private List<Folder> folders = new ArrayList<>();
    private List<Folder> fullList = new ArrayList<>();

    // ------------------- CLICK NORMAL --------------------
    public interface OnFolderClickListener {
        void onFolderClick(Folder folder);
    }

    private OnFolderClickListener listener;

    public void setOnFolderClickListener(OnFolderClickListener listener) {
        this.listener = listener;
    }

    // ------------------- LONG CLICK --------------------
    public interface OnFolderLongClickListener {
        void onFolderLongClick(Folder folder, View view);
    }

    private OnFolderLongClickListener longClickListener;

    public void setOnFolderLongClickListener(OnFolderLongClickListener longClickListener) {
        this.longClickListener = longClickListener;
    }

    // -----------------------------------------------------

    @NonNull
    @Override
    public FolderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_folder, parent, false);
        return new FolderViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull FolderViewHolder holder, int position) {
        Folder folder = folders.get(position);

        holder.folderName.setText(folder.getName());
        holder.folderIcon.setImageResource(R.drawable.icon_folder);

        // CLICK NORMAL
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onFolderClick(folder);
        });

        // LONG CLICK (2 segundos)
        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) longClickListener.onFolderLongClick(folder, v);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return folders.size();
    }

    public int getFullSize() {
        return fullList.size();
    }

    public void setFolders(List<Folder> list) {
        fullList.clear();
        folders.clear();

        if (list != null) {
            fullList.addAll(list);
            folders.addAll(list);
        }

        notifyDataSetChanged();
    }

    public void filter(String text) {
        folders.clear();
        if (text == null || text.trim().isEmpty()) {
            folders.addAll(fullList);
        } else {
            String q = text.toLowerCase();
            for (Folder f : fullList) {
                if (f.getName() != null && f.getName().toLowerCase().contains(q)) {
                    folders.add(f);
                }
            }
        }
        notifyDataSetChanged();
    }

    static class FolderViewHolder extends RecyclerView.ViewHolder {
        TextView folderName;
        ImageView folderIcon;

        public FolderViewHolder(View itemView) {
            super(itemView);
            folderName = itemView.findViewById(R.id.folderName);
            folderIcon = itemView.findViewById(R.id.folderIcon);
        }
    }
}
