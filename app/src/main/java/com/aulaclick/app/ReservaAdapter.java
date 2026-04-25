package com.aulaclick.app;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.aulaclick.app.network.models.ReservaDTO;

import java.util.ArrayList;
import java.util.List;

public class ReservaAdapter extends RecyclerView.Adapter<ReservaAdapter.ViewHolder> {

    private List<ReservaDTO> reservas = new ArrayList<>();

    public void setReservas(List<ReservaDTO> nuevasReservas) {
        this.reservas.clear();
        if (nuevasReservas != null) this.reservas.addAll(nuevasReservas);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reserva, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ReservaDTO reserva = reservas.get(position);

        holder.tvHoras.setText(limpiarHora(reserva.getHoraInicio()) + " – " + limpiarHora(reserva.getHoraFin()));
        holder.tvUsuario.setText(reserva.getNombreUsuario());
        holder.tvMotivo.setText(reserva.getMotivo() != null ? reserva.getMotivo() : "Sin descripción");
        holder.tvFecha.setText(reserva.getFecha() != null ? reserva.getFecha() : "");

        String estadoOriginal = (reserva.getEstado() != null) ? reserva.getEstado() : "";
        String estadoCheck = estadoOriginal.toLowerCase();

        int colorFondo;
        int colorTexto = android.graphics.Color.WHITE;

        if (estadoCheck.contains("curso")) {
            colorFondo = android.graphics.Color.parseColor("#9C27B0");
        } else if (estadoCheck.contains("pronto") || estadoCheck.contains("empieza")) {
            colorFondo = android.graphics.Color.parseColor("#FFC107");
            colorTexto = android.graphics.Color.BLACK;
        } else if (estadoCheck.contains("activ") || estadoCheck.contains("confirmad")) {
            colorFondo = android.graphics.Color.parseColor("#4CAF50");
        } else if (estadoCheck.contains("cancel")) {
            colorFondo = android.graphics.Color.parseColor("#F44336");
        } else if (estadoCheck.contains("pendient")) {
            colorFondo = android.graphics.Color.parseColor("#1A73E8");
        } else {
            colorFondo = android.graphics.Color.parseColor("#9E9E9E");
        }

        if (holder.viewEstadoLateral != null) {
            holder.viewEstadoLateral.setBackgroundTintList(android.content.res.ColorStateList.valueOf(colorFondo));
        }
        if (holder.tvBadgeEstado != null) {
            holder.tvBadgeEstado.setText(estadoOriginal.isEmpty() ? "—" : estadoOriginal);
            holder.tvBadgeEstado.setTextColor(colorTexto);
            holder.tvBadgeEstado.setBackgroundTintList(android.content.res.ColorStateList.valueOf(colorFondo));
        }
    }

    @Override
    public int getItemCount() {
        return reservas != null ? reservas.size() : 0;
    }

    public String limpiarHora(String h) {
        return h != null && h.length() >= 5 ? h.substring(0, 5) : (h != null ? h : "");
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvHoras, tvUsuario, tvMotivo, tvFecha, tvBadgeEstado;
        View viewEstadoLateral;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvHoras           = itemView.findViewById(R.id.tvHoras);
            tvUsuario         = itemView.findViewById(R.id.tvUsuario);
            tvMotivo          = itemView.findViewById(R.id.tvMotivo);
            tvFecha           = itemView.findViewById(R.id.tvFecha);
            tvBadgeEstado     = itemView.findViewById(R.id.tvBadgeEstado);
            viewEstadoLateral = itemView.findViewById(R.id.viewEstadoLateral);
        }
    }
}
