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

    public interface OnFolderClickListener {
        void onFolderClick(Folder folder);
    }

    private OnFolderClickListener listener;

    public void setOnFolderClickListener(OnFolderClickListener listener) {
        this.listener = listener;
    }

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
        holder.folderIcon.setImageResource(R.drawable.folder);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onFolderClick(folder);
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
        Log.d("BUSQUEDA_DEBUG", "setFolders -> recibida=" + (list != null ? list.size() : "null"));
        fullList.clear();
        folders.clear();

        if (list != null) {
            fullList.addAll(list);
            folders.addAll(list);
        }

        notifyDataSetChanged();
    }

    public void filter(String text) {
        Log.d(TAG, "FoldersAdapter.filter text='" + text + "'");
        folders.clear();

        if (text == null || text.trim().isEmpty()) {
            folders.addAll(fullList);
        } else {
            String q = text.toLowerCase();
            for (Folder f : fullList) {
                Log.d("BUSQUEDA_DEBUG", "Filtrando: " + text + " | folder: " + f.getName());
                if (f.getName() != null && f.getName().toLowerCase().contains(q)) {
                    folders.add(f);
                }
            }
        }
        Log.d(TAG, "FoldersAdapter.filter -> result size=" + folders.size());
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
