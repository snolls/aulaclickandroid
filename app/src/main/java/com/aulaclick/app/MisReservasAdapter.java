package com.aulaclick.app;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import java.util.List;

public class MisReservasAdapter extends RecyclerView.Adapter<MisReservasAdapter.ViewHolder> {

    private List<Reserva> reservas;
    private Context context;

    public MisReservasAdapter(List<Reserva> reservas) {
        this.reservas = reservas;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_mis_reservas, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Reserva reserva = reservas.get(position);
        holder.tvNombre.setText(reserva.getNombreRecurso());
        holder.tvFecha.setText(reserva.getFecha());
        holder.tvHora.setText(reserva.getHora());
        holder.tvEstado.setText(reserva.getEstado());

        // Personalizar chip de estado
        if ("Confirmada".equalsIgnoreCase(reserva.getEstado())) {
            holder.tvEstado.setTextColor(0xFF10B981);
            holder.tvEstado.setBackgroundTintList(ColorStateList.valueOf(0x2210B981));
        } else if ("Pendiente".equalsIgnoreCase(reserva.getEstado())) {
            holder.tvEstado.setTextColor(0xFF1A73E8);
            holder.tvEstado.setBackgroundTintList(ColorStateList.valueOf(0x221A73E8));
        }

        holder.btnCancelar.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Confirmar cancelación")
                    .setMessage("¿Estás seguro de que deseas cancelar esta reserva?")
                    .setPositiveButton("Sí", (dialog, which) -> {
                        Toast.makeText(context, "Reserva cancelada con éxito", Toast.LENGTH_SHORT).show();
                        // Aquí se podría eliminar el ítem de la lista y notificar al adapter
                    })
                    .setNegativeButton("No", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return reservas.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvFecha, tvHora, tvEstado;
        MaterialButton btnCancelar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvReservaNombre);
            tvFecha = itemView.findViewById(R.id.tvReservaFecha);
            tvHora = itemView.findViewById(R.id.tvReservaHora);
            tvEstado = itemView.findViewById(R.id.tvReservaEstado);
            btnCancelar = itemView.findViewById(R.id.btnCancelarReserva);
        }
    }
}
