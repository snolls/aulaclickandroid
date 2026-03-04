package com.aulaclick.app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aulaclick.app.network.models.Departamento;

import java.util.List;

public class DepartamentoAdapter extends RecyclerView.Adapter<DepartamentoAdapter.ViewHolder> {

    private List<Departamento> departamentos;
    private OnDepartamentoClickListener listener;

    public interface OnDepartamentoClickListener {
        void onDeleteClick(Departamento departamento);
    }

    public DepartamentoAdapter(List<Departamento> departamentos, OnDepartamentoClickListener listener) {
        this.departamentos = departamentos;
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
        Departamento depto = departamentos.get(position);
        holder.tvNombre.setText(depto.getNombre());
        holder.btnDelete.setOnClickListener(v -> listener.onDeleteClick(depto));
    }

    @Override
    public int getItemCount() {
        return departamentos.size();
    }

    public void updateData(List<Departamento> newDepartamentos) {
        this.departamentos = newDepartamentos;
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
