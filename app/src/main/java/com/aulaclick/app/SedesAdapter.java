package com.aulaclick.app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aulaclick.app.network.models.SedeDTO;

import java.util.List;

public class SedesAdapter extends RecyclerView.Adapter<SedesAdapter.SedeViewHolder> {

    private final List<SedeDTO> sedes;
    private final OnSedeClickListener listener;

    public interface OnSedeClickListener {
        void onSedeClick(SedeDTO sede);
    }

    public SedesAdapter(List<SedeDTO> sedes, OnSedeClickListener listener) {
        this.sedes = sedes;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SedeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_sede, parent, false);
        return new SedeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SedeViewHolder holder, int position) {
        SedeDTO sede = sedes.get(position);
        holder.tvNombre.setText(sede.getNombre());
        
        String detalle = "";
        if (sede.getCiudad() != null && !sede.getCiudad().isEmpty()) {
            detalle += "Ciudad: " + sede.getCiudad();
        }
        if (sede.getDireccion() != null && !sede.getDireccion().isEmpty()) {
            if (!detalle.isEmpty()) detalle += " - ";
            detalle += "Dirección: " + sede.getDireccion();
        }

        if (!detalle.isEmpty()) {
            holder.tvDetalle.setText(detalle);
            holder.tvDetalle.setVisibility(View.VISIBLE);
        } else {
            holder.tvDetalle.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> listener.onSedeClick(sede));
    }

    @Override
    public int getItemCount() {
        return sedes != null ? sedes.size() : 0;
    }

    static class SedeViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre;
        TextView tvDetalle;

        public SedeViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvSedeNombre);
            tvDetalle = itemView.findViewById(R.id.tvDetalle);
        }
    }
}
