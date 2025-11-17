package es.fdi.ucm.pad.notnotion.ui.events;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import es.fdi.ucm.pad.notnotion.R;
import es.fdi.ucm.pad.notnotion.data.model.CalendarEvent;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private List<CalendarEvent> events = new ArrayList<>();
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event, parent, false);  // 👉 tu layout
        return new EventViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        CalendarEvent event = events.get(position);

        holder.eventTitle.setText(event.getTitle());
        holder.eventHour.setText(timeFormat.format(event.getStartDate().toDate()));

        // El punto y la línea ya son estáticos en el XML
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    public void setEvents(List<CalendarEvent> list) {
        this.events = list;
        notifyDataSetChanged();
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {

        TextView eventHour;
        TextView eventTitle;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            eventHour = itemView.findViewById(R.id.textHour);
            eventTitle = itemView.findViewById(R.id.tvEventTitle);
        }
    }
}
