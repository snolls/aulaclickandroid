package com.aulaclick.app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.aulaclick.app.network.models.Recurso;
import java.util.List;

public class RecursoAdapter extends RecyclerView.Adapter<RecursoAdapter.ViewHolder> {

    private List<Recurso> recursos;
    private OnRecursoClickListener listener;

    public interface OnRecursoClickListener {
        void onRecursoClick(Recurso recurso);
    }

    public RecursoAdapter(List<Recurso> recursos, OnRecursoClickListener listener) {
        this.recursos = recursos;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recurso, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Recurso recurso = recursos.get(position);
        holder.tvNombre.setText(recurso.getNombre());
        holder.tvCapacidad.setText("Capacidad: " + recurso.getCapacidad());
        
        int colorStatus;
        String estado = recurso.getEstado() != null ? recurso.getEstado() : "";
        
        if (estado.equalsIgnoreCase("Activo") || estado.equalsIgnoreCase("Disponible")) {
            colorStatus = 0xFF10B981; // Verde
        } else if (estado.equalsIgnoreCase("Ocupado") || estado.equalsIgnoreCase("Inactivo")) {
            colorStatus = 0xFFEF4444; // Rojo
        } else {
            colorStatus = 0xFFF59E0B; // Default/Mantenimiento (Amarillo/Naranja)
        }

        holder.viewStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(colorStatus));
        
        holder.itemView.setOnClickListener(v -> listener.onRecursoClick(recurso));
    }

    @Override
    public int getItemCount() {
        return recursos.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvCapacidad;
        View viewStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvRecursoNombre);
            tvCapacidad = itemView.findViewById(R.id.tvRecursoCapacidad);
            viewStatus = itemView.findViewById(R.id.viewStatusIndicator);
        }
    }
}
