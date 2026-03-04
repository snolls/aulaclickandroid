package com.aulaclick.app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aulaclick.app.network.models.Equipamiento;

import java.util.List;

public class EquipamientoAdapter extends RecyclerView.Adapter<EquipamientoAdapter.ViewHolder> {

    private List<Equipamiento> equipamientos;
    private OnEquipamientoClickListener listener;

    public interface OnEquipamientoClickListener {
        void onDeleteClick(Equipamiento equipamiento);
    }

    public EquipamientoAdapter(List<Equipamiento> equipamientos, OnEquipamientoClickListener listener) {
        this.equipamientos = equipamientos;
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
        Equipamiento equip = equipamientos.get(position);
        holder.tvNombre.setText(equip.getNombre());
        holder.btnDelete.setOnClickListener(v -> listener.onDeleteClick(equip));
    }

    @Override
    public int getItemCount() {
        return equipamientos.size();
    }

    public void updateData(List<Equipamiento> newEquipamientos) {
        this.equipamientos = newEquipamientos;
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
