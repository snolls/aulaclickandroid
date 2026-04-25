package com.aulaclick.app;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aulaclick.app.network.models.Recurso;
import com.bumptech.glide.Glide;

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

        // Carga de imagen
        String urlParaMostrar = null;
        if (recurso.getImagenUrl() != null && !recurso.getImagenUrl().isEmpty()) {
            urlParaMostrar = recurso.getImagenUrl();
        } else if (recurso.getTipoRecurso() != null
                && recurso.getTipoRecurso().getImagenUrl() != null
                && !recurso.getTipoRecurso().getImagenUrl().isEmpty()) {
            urlParaMostrar = recurso.getTipoRecurso().getImagenUrl();
        }

        if (urlParaMostrar != null) {
            Glide.with(holder.itemView.getContext())
                    .load(urlParaMostrar)
                    .centerCrop()
                    .placeholder(R.drawable.ic_image)
                    .error(R.drawable.ic_image)
                    .into(holder.ivRecursoIcon);
        } else {
            holder.ivRecursoIcon.setImageResource(R.drawable.ic_image);
        }

        holder.tvNombre.setText(recurso.getNombre());
        holder.tvCapacidad.setText(recurso.getCapacidad() + " personas");

        // Lógica de colores según el estado
        String estadoOriginal = (recurso.getEstado() != null) ? recurso.getEstado() : "";
        String estadoCheck = estadoOriginal.toLowerCase();

        int colorFondo;
        int colorTexto = android.graphics.Color.WHITE;

        if (estadoCheck.contains("pronto") || estadoCheck.contains("empieza")) {
            colorFondo = android.graphics.Color.parseColor("#FFC107");
            colorTexto = android.graphics.Color.BLACK;
        } else if (estadoCheck.contains("activ") || (estadoCheck.contains("disponible") && !estadoCheck.contains("no"))) {
            colorFondo = android.graphics.Color.parseColor("#4CAF50");
        } else if (estadoCheck.contains("no disponible") || estadoCheck.contains("mantenimiento") || estadoCheck.contains("cancel")) {
            colorFondo = android.graphics.Color.parseColor("#F44336");
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

        holder.itemView.setOnClickListener(v -> listener.onRecursoClick(recurso));
    }

    @Override
    public int getItemCount() {
        return recursos.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvCapacidad, tvBadgeEstado;
        View viewEstadoLateral;
        ImageView ivRecursoIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre          = itemView.findViewById(R.id.tvRecursoNombre);
            tvCapacidad       = itemView.findViewById(R.id.tvRecursoCapacidad);
            tvBadgeEstado     = itemView.findViewById(R.id.tvBadgeEstado);
            viewEstadoLateral = itemView.findViewById(R.id.viewEstadoLateral);
            ivRecursoIcon     = itemView.findViewById(R.id.ivRecursoIcon);
        }
    }
}
