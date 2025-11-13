package es.fdi.ucm.pad.notnotion.data.adapter;

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
    private List<Folder> folders = new ArrayList<>();



    public interface OnFolderClickListener {
        void onFolderClick(Folder folder);
    }
    private OnFolderClickListener listener; //para poder abrir el enlace
    public void setOnFolderClickListener(OnFolderClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public FoldersAdapter.FolderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_folder, parent, false);
        return new FolderViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull FoldersAdapter.FolderViewHolder holder, int position) {
        Folder folder = folders.get(position);
        holder.folderName.setText(folder.getName());
        holder.folderIcon.setImageResource(R.drawable.folder);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onFolderClick(folder);
            }
        });
    }

    @Override
    public int getItemCount() {
        return folders.size();
    }
    public void setFolders(List<Folder> newFolders) {
        folders.clear();
        if (newFolders != null) {
            folders.addAll(newFolders);
        }
        notifyDataSetChanged();
    }

    static class FolderViewHolder extends RecyclerView.ViewHolder {
        TextView folderName;
        ImageView folderIcon;

        FolderViewHolder(View itemView) {
            super(itemView);
            folderName = itemView.findViewById(R.id.folderName);
            folderIcon = itemView.findViewById(R.id.folderIcon);
        }

    }
}
