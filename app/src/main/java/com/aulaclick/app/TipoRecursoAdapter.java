package com.aulaclick.app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aulaclick.app.network.models.TipoRecurso;

import java.util.List;

public class TipoRecursoAdapter extends RecyclerView.Adapter<TipoRecursoAdapter.ViewHolder> {

    private List<TipoRecurso> tipos;
    private OnTipoClickListener listener;

    public interface OnTipoClickListener {
        void onDeleteClick(TipoRecurso tipo);
    }

    public TipoRecursoAdapter(List<TipoRecurso> tipos, OnTipoClickListener listener) {
        this.tipos = tipos;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_departamento, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TipoRecurso tipo = tipos.get(position);
        holder.tvNombre.setText(tipo.getNombre());
        holder.btnDelete.setOnClickListener(v -> listener.onDeleteClick(tipo));
    }

    @Override
    public int getItemCount() {
        return tipos.size();
    }

    public void updateData(List<TipoRecurso> newTipos) {
        this.tipos = newTipos;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre;
        ImageButton btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombre);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
