package com.aulaclick.app;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aulaclick.app.network.ApiClient;
import com.aulaclick.app.network.models.ReservaDTO;
import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MisReservasAdapter extends RecyclerView.Adapter<MisReservasAdapter.ViewHolder> {

    private List<Reserva> reservas;

    public MisReservasAdapter(List<Reserva> reservas) {
        this.reservas = reservas;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mis_reservas, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Reserva reserva = reservas.get(position);
        holder.tvNombre.setText(reserva.getNombreRecurso());
        holder.tvFecha.setText(reserva.getFecha());
        holder.tvHora.setText(reserva.getHora());
        if (holder.tvUsuario != null) {
            String usuario = reserva.getNombreUsuario();
            holder.tvUsuario.setText(usuario != null && !usuario.isEmpty() ? usuario : "—");
        }

        String url = reserva.getImagenUrl();
        android.util.Log.d("DEBUG_IMAGEN_RESERVA", "Reserva [" + reserva.getNombreRecurso() + "] -> URL recibida: [" + url + "]");
        if (url != null && !url.isEmpty()) {
            holder.ivIcono.clearColorFilter();
            holder.ivIcono.setPadding(0, 0, 0, 0);
            Glide.with(holder.itemView.getContext())
                    .load(url)
                    .centerCrop()
                    .placeholder(R.drawable.ic_image)
                    .error(R.drawable.ic_image)
                    .into(holder.ivIcono);
        } else {
            holder.ivIcono.setImageResource(R.drawable.ic_school);
            int p = (int) (14 * holder.itemView.getContext().getResources().getDisplayMetrics().density);
            holder.ivIcono.setPadding(p, p, p, p);
            holder.ivIcono.setColorFilter(
                    androidx.core.content.ContextCompat.getColor(holder.itemView.getContext(), R.color.colorPrimary));
        }

        String estadoOriginal = (reserva.getEstado() != null) ? reserva.getEstado() : "";
        String estadoCheck = estadoOriginal.toUpperCase();

        int colorFondo;
        int colorTexto = android.graphics.Color.WHITE;

        if (estadoCheck.contains("CURSO")) {
            colorFondo = android.graphics.Color.parseColor("#9C27B0");
        } else if (estadoCheck.contains("PRONTO") || estadoCheck.contains("EMPIEZA")) {
            colorFondo = android.graphics.Color.parseColor("#FFC107");
            colorTexto = android.graphics.Color.BLACK;
        } else if (estadoCheck.contains("ACTIV") || estadoCheck.contains("CONFIRMAD")) {
            colorFondo = android.graphics.Color.parseColor("#4CAF50");
        } else if (estadoCheck.contains("CANCEL")) {
            colorFondo = android.graphics.Color.parseColor("#F44336");
        } else {
            colorFondo = android.graphics.Color.parseColor("#9E9E9E");
        }

        if (holder.viewEstadoLateral != null) {
            holder.viewEstadoLateral.setBackgroundTintList(android.content.res.ColorStateList.valueOf(colorFondo));
        }
        if (holder.tvEstado != null) {
            holder.tvEstado.setText(estadoOriginal.isEmpty() ? "—" : estadoOriginal);
            holder.tvEstado.setTextColor(colorTexto);
            holder.tvEstado.setBackgroundTintList(android.content.res.ColorStateList.valueOf(colorFondo));
        }

        if (holder.btnCancelar != null) {
            if (estadoCheck.contains("CANCEL") || estadoCheck.contains("FINALIZAD") || estadoCheck.contains("HISTORIAL") || estadoCheck.contains("CURSO")) {
                holder.btnCancelar.setVisibility(View.GONE);
            } else {
                holder.btnCancelar.setVisibility(View.VISIBLE);
                holder.btnCancelar.setOnClickListener(v -> {
                    new MaterialAlertDialogBuilder(holder.itemView.getContext())
                            .setTitle("Confirmar cancelación")
                            .setMessage("¿Estás seguro de que deseas cancelar esta reserva? Esta acción no se puede deshacer y el recurso quedará libre.")
                            .setPositiveButton("Sí, cancelar", (dialog, which) -> {
                                int pos = holder.getBindingAdapterPosition();
                                if (pos == RecyclerView.NO_ID) return;
                                com.google.gson.JsonObject dummyBody = new com.google.gson.JsonObject();
                                ApiClient.getApiService()
                                        .cancelarReserva(reserva.getIdReserva(), dummyBody)
                                        .enqueue(new Callback<ReservaDTO>() {
                                            @Override
                                            public void onResponse(@NonNull Call<ReservaDTO> call,
                                                                   @NonNull Response<ReservaDTO> response) {
                                                if (response.isSuccessful()) {
                                                    int p2 = holder.getBindingAdapterPosition();
                                                    if (p2 != RecyclerView.NO_ID) {
                                                        reservas.remove(p2);
                                                        notifyItemRemoved(p2);
                                                        notifyItemRangeChanged(p2, reservas.size());
                                                    }
                                                    Toast.makeText(holder.itemView.getContext(),
                                                            "Reserva cancelada con éxito", Toast.LENGTH_SHORT).show();
                                                } else {
                                                    android.util.Log.e("DEBUG_CANCELAR", "Error del servidor: " + response.code());
                                                    Toast.makeText(holder.itemView.getContext(),
                                                            "Error al cancelar (" + response.code() + ")", Toast.LENGTH_SHORT).show();
                                                }
                                            }

                                            @Override
                                            public void onFailure(@NonNull Call<ReservaDTO> call,
                                                                  @NonNull Throwable t) {
                                                android.util.Log.e("DEBUG_CANCELAR", "Fallo de red real: ", t);
                                                Toast.makeText(holder.itemView.getContext(),
                                                        "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                                            }
                                        });
                            })
                            .setNegativeButton("No", null)
                            .show();
                });
            }
        }
    }

    @Override
    public int getItemCount() {
        return reservas.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvFecha, tvHora, tvEstado, tvUsuario;
        MaterialButton btnCancelar;
        ImageView ivIcono;
        View viewEstadoLateral;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre          = itemView.findViewById(R.id.tvReservaNombre);
            tvFecha           = itemView.findViewById(R.id.tvReservaFecha);
            tvHora            = itemView.findViewById(R.id.tvReservaHora);
            tvEstado          = itemView.findViewById(R.id.tvReservaEstado);
            tvUsuario         = itemView.findViewById(R.id.tvReservaUsuario);
            btnCancelar       = itemView.findViewById(R.id.btnCancelarReserva);
            ivIcono           = itemView.findViewById(R.id.ivIconoRecurso);
            viewEstadoLateral = itemView.findViewById(R.id.viewEstadoLateral);
        }
    }
}
