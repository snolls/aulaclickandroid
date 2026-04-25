package com.aulaclick.app;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aulaclick.app.network.models.UsuarioDTO;

import java.util.List;

public class UsuariosAdapter extends RecyclerView.Adapter<UsuariosAdapter.ViewHolder> {

    public interface OnUserClickListener {
        void onUserClick(UsuarioDTO usuario);
    }

    private final List<UsuarioDTO> lista;
    private final OnUserClickListener listener;

    public UsuariosAdapter(List<UsuarioDTO> lista, OnUserClickListener listener) {
        this.lista    = lista;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_usuario, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        UsuarioDTO u = lista.get(position);

        h.tvNombre.setText(u.getNombreCompleto() != null ? u.getNombreCompleto() : "—");
        h.tvEmail.setText(u.getEmail() != null ? u.getEmail() : "—");

        String rol = u.getRol() != null ? u.getRol().toUpperCase() : "—";
        h.tvRol.setText(rol);

        int chipColor;
        if (rol.contains("ADMIN") && !rol.contains("SEDE")) {
            chipColor = Color.parseColor("#1A73E8");
        } else if (rol.contains("SEDE")) {
            chipColor = Color.parseColor("#7B1FA2");
        } else if (rol.contains("USER") || rol.contains("USUARIO")) {
            chipColor = Color.parseColor("#374151");
        } else {
            chipColor = Color.parseColor("#4B5563");
        }
        h.tvRol.setBackgroundTintList(ColorStateList.valueOf(chipColor));

        h.itemView.setOnClickListener(v -> listener.onUserClick(u));
    }

    @Override
    public int getItemCount() { return lista.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvNombre, tvEmail, tvRol;

        ViewHolder(@NonNull View v) {
            super(v);
            tvNombre = v.findViewById(R.id.tvUsuarioNombre);
            tvEmail  = v.findViewById(R.id.tvUsuarioEmail);
            tvRol    = v.findViewById(R.id.tvUsuarioRol);
        }
    }
}
