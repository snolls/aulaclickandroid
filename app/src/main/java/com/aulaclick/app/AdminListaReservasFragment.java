package com.aulaclick.app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aulaclick.app.network.ApiClient;
import com.aulaclick.app.network.models.ReservaDTO;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminListaReservasFragment extends Fragment {

    public static final String ESTADO_ACTIVAS    = "activas";
    public static final String ESTADO_HISTORIAL  = "historial";
    public static final String ESTADO_CANCELADAS = "canceladas";

    private static final String ARG_ESTADO = "estado";

    private String estado;
    private RecyclerView rvReservas;
    private ProgressBar progressBar;
    private TextView tvEmpty;

    public static AdminListaReservasFragment newInstance(String estado) {
        AdminListaReservasFragment f = new AdminListaReservasFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ESTADO, estado);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            estado = getArguments().getString(ARG_ESTADO);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_lista_reservas, container, false);
        rvReservas  = v.findViewById(R.id.rvReservas);
        progressBar = v.findViewById(R.id.progressBar);
        tvEmpty     = v.findViewById(R.id.tvEmpty);
        rvReservas.setLayoutManager(new LinearLayoutManager(requireContext()));
        cargarReservas();
        return v;
    }

    private void cargarReservas() {
        progressBar.setVisibility(View.VISIBLE);
        rvReservas.setVisibility(View.GONE);
        tvEmpty.setVisibility(View.GONE);

        ApiClient.getApiService().getReservasAdmin().enqueue(new Callback<List<ReservaDTO>>() {
            @Override
            public void onResponse(@NonNull Call<List<ReservaDTO>> call,
                                   @NonNull Response<List<ReservaDTO>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    List<ReservaDTO> filtradas = filtrarPorEstado(response.body());
                    filtradas.sort((r1, r2) -> {
                        String f1 = r1.getFecha() != null ? r1.getFecha() : "";
                        String f2 = r2.getFecha() != null ? r2.getFecha() : "";
                        int cmp = f2.compareTo(f1); // más recientes primero
                        if (cmp != 0) return cmp;
                        String h1 = r1.getHoraInicio() != null ? r1.getHoraInicio() : "";
                        String h2 = r2.getHoraInicio() != null ? r2.getHoraInicio() : "";
                        return h2.compareTo(h1);
                    });
                    if (filtradas.isEmpty()) {
                        tvEmpty.setText("No hay reservas");
                        tvEmpty.setVisibility(View.VISIBLE);
                    } else {
                        rvReservas.setAdapter(new MisReservasAdapter(convertir(filtradas)));
                        rvReservas.setVisibility(View.VISIBLE);
                    }
                } else {
                    tvEmpty.setText("Error al cargar reservas (" + response.code() + ")");
                    tvEmpty.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<ReservaDTO>> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                tvEmpty.setText("Error de red");
                tvEmpty.setVisibility(View.VISIBLE);
            }
        });
    }

    private List<ReservaDTO> filtrarPorEstado(List<ReservaDTO> todas) {
        List<ReservaDTO> resultado = new ArrayList<>();
        for (ReservaDTO r : todas) {
            String est = r.getEstado() != null ? r.getEstado().toUpperCase() : "";
            switch (estado) {
                case ESTADO_ACTIVAS:
                    if (est.contains("ACTIV") || est.contains("CONFIRMAD") || est.contains("PENDIENT")
                            || est.contains("PRONTO") || est.contains("CURSO") || est.isEmpty())
                        resultado.add(r);
                    break;
                case ESTADO_CANCELADAS:
                    if (est.contains("CANCELAD"))
                        resultado.add(r);
                    break;
                case ESTADO_HISTORIAL:
                    if (est.contains("HISTORIAL") || est.contains("PASAD") || est.contains("COMPLETAD")
                            || est.contains("FINALIZ"))
                        resultado.add(r);
                    break;
            }
        }
        return resultado;
    }

    private List<Reserva> convertir(List<ReservaDTO> dtos) {
        List<Reserva> lista = new ArrayList<>();
        for (ReservaDTO dto : dtos) {
            String nombre = dto.getNombreRecurso() != null ? dto.getNombreRecurso() : "Recurso";
            String fecha  = dto.getFecha() != null ? dto.getFecha() : "";
            String hora   = formatHora(dto.getHoraInicio()) + " – " + formatHora(dto.getHoraFin());
            String est    = dto.getEstado() != null ? dto.getEstado() : "Activa";
            Reserva r = new Reserva(dto.getId(), nombre, fecha, hora, est, dto.getImagenUrl());
            r.setNombreUsuario(dto.getNombreUsuario());
            lista.add(r);
        }
        return lista;
    }

    private String formatHora(String h) {
        if (h != null && h.length() >= 5) return h.substring(0, 5);
        return h != null ? h : "";
    }
}
