package com.aulaclick.app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.aulaclick.app.network.ApiClient;
import com.aulaclick.app.network.models.EstadisticasDTO;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminFragment extends Fragment {

    private TextView tvFragTotalReservas;
    private TextView tvFragEnCurso;
    private TextView tvFragUsuarios;
    private TextView tvFragCanceladas;
    private ProgressBar progressAdmin;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvFragTotalReservas = view.findViewById(R.id.tvFragTotalReservas);
        tvFragEnCurso = view.findViewById(R.id.tvFragEnCurso);
        tvFragUsuarios = view.findViewById(R.id.tvFragUsuarios);
        tvFragCanceladas = view.findViewById(R.id.tvFragCanceladas);
        progressAdmin = view.findViewById(R.id.progressAdmin);

        cargarEstadisticas();
    }

    private void cargarEstadisticas() {
        progressAdmin.setVisibility(View.VISIBLE);

        ApiClient.getApiService().getEstadisticas().enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<EstadisticasDTO> call,
                                   @NonNull Response<EstadisticasDTO> response) {
                if (!isAdded()) return;
                progressAdmin.setVisibility(View.GONE);

                if (response.code() == 403) {
                    Toast.makeText(requireContext(),
                            getString(R.string.error_no_admin_permission),
                            Toast.LENGTH_LONG).show();
                    requireActivity().finish();
                    return;
                }

                if (response.isSuccessful() && response.body() != null) {
                    EstadisticasDTO e = response.body();
                    tvFragTotalReservas.setText(String.valueOf(e.getTotalReservas()));
                    tvFragEnCurso.setText(String.valueOf(e.getReservasEnCurso()));
                    tvFragUsuarios.setText(String.valueOf(e.getTotalUsuarios()));
                    tvFragCanceladas.setText(String.valueOf(e.getTotalReservasCanceladas()));
                } else {
                    Toast.makeText(requireContext(),
                            "Error " + response.code() + ": " + response.message(),
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<EstadisticasDTO> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                progressAdmin.setVisibility(View.GONE);
                Toast.makeText(requireContext(),
                        "Fallo de conexión: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}
