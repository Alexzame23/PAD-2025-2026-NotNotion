package es.fdi.ucm.pad.notnotion.ui.notifications;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import es.fdi.ucm.pad.notnotion.R;
import es.fdi.ucm.pad.notnotion.data.model.Notification;

/**
 * Adapter para mostrar la lista de notificaciones configuradas
 */
public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private List<Notification> items = new ArrayList<>();
    private OnItemDeleteListener deleteListener;

    public interface OnItemDeleteListener {
        void onDelete(Notification item, int position);
    }

    public NotificationAdapter(OnItemDeleteListener deleteListener) {
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Notification item = items.get(position);
        holder.bind(item, position);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void setItems(List<Notification> items) {
        this.items = items != null ? new ArrayList<>(items) : new ArrayList<>();
        notifyDataSetChanged();
    }

    public List<Notification> getItems() {
        return new ArrayList<>(items);
    }

    public void addItem(Notification item) {
        // Evitar duplicados
        if (!items.contains(item)) {
            items.add(item);
            notifyItemInserted(items.size() - 1);
        }
    }

    public void removeItem(int position) {
        if (position >= 0 && position < items.size()) {
            items.remove(position);
            notifyItemRemoved(position);
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvNotificationText;
        ImageButton btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNotificationText = itemView.findViewById(R.id.tvNotificationText);
            btnDelete = itemView.findViewById(R.id.btnDeleteNotification);
        }

        public void bind(Notification item, int position) {
            tvNotificationText.setText("🔔 " + item.getDisplayText());

            btnDelete.setOnClickListener(v -> {
                if (deleteListener != null) {
                    deleteListener.onDelete(item, position);
                }
            });
        }
    }
}