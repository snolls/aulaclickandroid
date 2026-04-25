package com.aulaclick.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.aulaclick.app.utils.ImageUtils;
import com.aulaclick.app.utils.SessionManager;
import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class DetalleRecursoActivity extends AppCompatActivity {

    private Integer recursoId = null;
    private String recursoNombre = "";
    private com.aulaclick.app.network.models.Recurso recursoActual = null;
    private ReservaAdapter reservaAdapter;
    private androidx.recyclerview.widget.RecyclerView rvAgenda;
    private android.widget.TextView tvEmptyAgenda;
    private List<com.aulaclick.app.network.models.ReservaDTO> todasLasReservas = new ArrayList<>();
    private String fechaFiltroActual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_recurso);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        toolbar.setNavigationOnClickListener(v -> finish());

        String recursoJson = getIntent().getStringExtra("recurso_json");
        
        if (recursoJson != null) {
            com.aulaclick.app.network.models.Recurso recurso = new com.google.gson.Gson().fromJson(recursoJson, com.aulaclick.app.network.models.Recurso.class);
            if (recurso != null) {
                recursoId = recurso.getId();
                actualizarUI(recurso);
            }
        }

        // Inicializar fecha de hoy como filtro por defecto
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        fechaFiltroActual = sdf.format(new Date());

        rvAgenda = findViewById(R.id.rvAgenda);
        tvEmptyAgenda = findViewById(R.id.tvEmptyAgenda);

        if (rvAgenda != null) {
            rvAgenda.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));
            reservaAdapter = new ReservaAdapter();
            rvAgenda.setAdapter(reservaAdapter);
        }

        invalidateOptionsMenu();

        // Configurar botón de filtro de fecha
        MaterialButton btnFiltroFecha = findViewById(R.id.btnFiltroFecha);
        if (btnFiltroFecha != null) {
            btnFiltroFecha.setOnClickListener(v -> {
                Locale esLocale = new Locale("es", "ES");
                Locale.setDefault(esLocale);
                android.content.res.Configuration config = getResources().getConfiguration();
                config.setLocale(esLocale);
                getApplicationContext().createConfigurationContext(config);

                CalendarConstraints constraints = new CalendarConstraints.Builder()
                        .setValidator(DateValidatorPointForward.from(MaterialDatePicker.todayInUtcMilliseconds()))
                        .build();

                MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder
                        .datePicker()
                        .setTitleText("Seleccionar día de reserva")
                        .setCalendarConstraints(constraints)
                        .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                        .build();

                datePicker.addOnPositiveButtonClickListener(selection -> {
                    // Formatear a yyyy-MM-dd en UTC para evitar desfases de zona horaria
                    SimpleDateFormat sdfFilter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    sdfFilter.setTimeZone(TimeZone.getTimeZone("UTC"));
                    fechaFiltroActual = sdfFilter.format(new Date(selection));

                    // Texto del botón legible (ej. "18 Abr 2026")
                    SimpleDateFormat sdfDisplay = new SimpleDateFormat("dd MMM yyyy", new Locale("es", "ES"));
                    sdfDisplay.setTimeZone(TimeZone.getTimeZone("UTC"));
                    btnFiltroFecha.setText(sdfDisplay.format(new Date(selection)));

                    filtrarYMostrarReservas();
                });

                datePicker.show(getSupportFragmentManager(), "DATE_PICKER");
            });
        }

        FloatingActionButton fab = findViewById(R.id.fabNuevaReserva);
        fab.setOnClickListener(v -> {
            if (recursoActual != null) {
                Intent intent = new Intent(this, NuevaReservaActivity.class);
                intent.putExtra("nombre", recursoNombre);
                intent.putExtra("recurso_json", new com.google.gson.Gson().toJson(recursoActual));
                startActivity(intent);
            } else {
                android.widget.Toast.makeText(this, "Cargando datos...", android.widget.Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void actualizarUI(com.aulaclick.app.network.models.Recurso recurso) {
        if (recurso == null) return;
        this.recursoActual = recurso;
        
        recursoId = recurso.getId();
        recursoNombre = recurso.getNombre() != null ? recurso.getNombre() : "";
        
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        if (recursoNombre != null && !recursoNombre.isEmpty()) {
            toolbar.setTitle(recursoNombre);
            TextView tvNombre = findViewById(R.id.tvDetalleNombre);
            if (tvNombre != null) tvNombre.setText(recursoNombre);
        }
        
        TextView tvUbicacion = findViewById(R.id.tvDetalleUbicacion);
        if (tvUbicacion != null) {
            String ubicacionText = "";
            if (recurso.getDepartamento() != null) {
                ubicacionText = recurso.getDepartamento().getNombre();
            }
            if (recurso.getTipoRecurso() != null) {
                if (!ubicacionText.isEmpty()) ubicacionText += " - ";
                ubicacionText += recurso.getTipoRecurso().getNombre();
            }
            tvUbicacion.setText(ubicacionText);
        }
        
        TextView tvCapacidad = findViewById(R.id.tvDetalleCapacidad);
        if (tvCapacidad != null && recurso.getCapacidad() != null) {
            tvCapacidad.setText("Capacidad: " + recurso.getCapacidad());
        }
        
        TextView tvStatus = findViewById(R.id.tvDetalleStatus);
        android.view.View viewStatus = findViewById(R.id.viewDetalleStatus);
        com.google.android.material.floatingactionbutton.FloatingActionButton fabReservar = findViewById(R.id.fabNuevaReserva);

        if (tvStatus != null) {
            String estado = recurso.getEstado();
            boolean estaDisponible = estado != null && (estado.equalsIgnoreCase("Disponible") || estado.equalsIgnoreCase("Activo"));
            
            if (estaDisponible) {
                tvStatus.setText("Disponible");
                tvStatus.setTextColor(android.graphics.Color.parseColor("#4CAF50"));
                if (viewStatus != null) {
                    viewStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#4CAF50")));
                }
                if (fabReservar != null) {
                    fabReservar.setEnabled(true);
                    fabReservar.setBackgroundTintList(android.content.res.ColorStateList.valueOf(androidx.core.content.ContextCompat.getColor(this, R.color.colorPrimary)));
                }
            } else {
                tvStatus.setText("No disponible");
                tvStatus.setTextColor(android.graphics.Color.parseColor("#F44336"));
                if (viewStatus != null) {
                    viewStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#F44336")));
                }
                if (fabReservar != null) {
                    fabReservar.setEnabled(false);
                    fabReservar.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#9E9E9E")));
                }
            }
        }
        
        TextView tvEquipamiento = findViewById(R.id.tvDetalleEquipamiento);
        if (tvEquipamiento != null && recurso.getEquipamientos() != null && !recurso.getEquipamientos().isEmpty()) {
            java.util.List<String> equipNames = new ArrayList<>();
            for (com.aulaclick.app.network.models.Equipamiento e : recurso.getEquipamientos()) {
                if (e.getNombre() != null) equipNames.add(e.getNombre());
            }
            tvEquipamiento.setText(String.join(", ", equipNames));
        }

        com.google.android.material.card.MaterialCardView cardSede = findViewById(R.id.cardDetalleSede);
        TextView tvSede = findViewById(R.id.tvDetalleSede);
        if (cardSede != null && tvSede != null && recurso.getSedeNombre() != null && !recurso.getSedeNombre().isEmpty()) {
            tvSede.setText(recurso.getSedeNombre());
            cardSede.setVisibility(View.VISIBLE);
        }

        ImageView ivDetalleHeader = findViewById(R.id.ivDetalleHeader);
        if (ivDetalleHeader != null) {
            String urlParaMostrar = null;
            if (recurso.getImagenUrl() != null && !recurso.getImagenUrl().isEmpty()) {
                urlParaMostrar = recurso.getImagenUrl();
            } else if (recurso.getTipoRecurso() != null && recurso.getTipoRecurso().getImagenUrl() != null && !recurso.getTipoRecurso().getImagenUrl().isEmpty()) {
                urlParaMostrar = recurso.getTipoRecurso().getImagenUrl();
            }

            if (urlParaMostrar != null && !urlParaMostrar.isEmpty()) {
                Glide.with(this)
                     .load(urlParaMostrar)
                     .centerCrop()
                     .placeholder(R.drawable.ic_image)
                     .error(R.drawable.ic_image)
                     .into(ivDetalleHeader);
            } else {
                ivDetalleHeader.setImageResource(R.drawable.ic_image);
            }
        }

        TextView tvHorarioPermitido = findViewById(R.id.tvHorarioPermitido);
        if (tvHorarioPermitido != null) {
            String horaApertura = formatearHora(recurso.getHoraApertura() != null ? recurso.getHoraApertura() : "00:00");
            String horaCierre   = formatearHora(recurso.getHoraCierre()   != null ? recurso.getHoraCierre()   : "23:59");
            String textoHorario = "Horario: " + horaApertura + " - " + horaCierre;

            if (Boolean.TRUE.equals(recurso.getPermiteFinesSemana())) {
                textoHorario += " (Incluye fines de semana)";
            } else {
                textoHorario += " (Lunes a Viernes)";
            }
            tvHorarioPermitido.setText(textoHorario);
        }
    }

    private String formatearHora(String horaBackend) {
        if (horaBackend != null && horaBackend.length() >= 5) {
            return horaBackend.substring(0, 5);
        }
        return horaBackend;
    }

    private void cargarDetallesRecurso(Integer id) {
        if (id == null) return;
        com.aulaclick.app.network.ApiClient.getApiService().getRecurso(Long.valueOf(id)).enqueue(new retrofit2.Callback<com.aulaclick.app.network.models.Recurso>() {
            @Override
            public void onResponse(retrofit2.Call<com.aulaclick.app.network.models.Recurso> call, retrofit2.Response<com.aulaclick.app.network.models.Recurso> response) {
                if (response.isSuccessful() && response.body() != null) {
                    actualizarUI(response.body());
                }
            }

            @Override
            public void onFailure(retrofit2.Call<com.aulaclick.app.network.models.Recurso> call, Throwable t) {
                android.widget.Toast.makeText(DetalleRecursoActivity.this, "Error al refrescar detalle", android.widget.Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filtrarYMostrarReservas() {
        List<com.aulaclick.app.network.models.ReservaDTO> filtradas = new ArrayList<>();
        for (com.aulaclick.app.network.models.ReservaDTO r : todasLasReservas) {
            if (r.getFecha() == null || !r.getFecha().equals(fechaFiltroActual)) continue;
            String est = r.getEstado() != null ? r.getEstado().toUpperCase() : "";
            if (est.contains("CANCELAD")) continue;
            filtradas.add(r);
        }
        java.util.Collections.sort(filtradas, (r1, r2) -> {
            String h1 = r1.getHoraInicio() != null ? r1.getHoraInicio() : "";
            String h2 = r2.getHoraInicio() != null ? r2.getHoraInicio() : "";
            return h1.compareTo(h2);
        });
        if (reservaAdapter != null) {
            reservaAdapter.setReservas(filtradas);
            // notifyDataSetChanged() ya se llama dentro de setReservas()
        }
        if (tvEmptyAgenda != null) {
            tvEmptyAgenda.setVisibility(filtradas.isEmpty() ? View.VISIBLE : View.GONE);
        }
        if (rvAgenda != null) {
            rvAgenda.setVisibility(filtradas.isEmpty() ? View.GONE : View.VISIBLE);
        }
    }

    private void cargarReservas() {
        if (recursoId == null) return;
        com.aulaclick.app.network.ApiClient.getApiService().getReservasPorRecurso(Long.valueOf(recursoId)).enqueue(new retrofit2.Callback<java.util.List<com.aulaclick.app.network.models.ReservaDTO>>() {
            @Override
            public void onResponse(retrofit2.Call<java.util.List<com.aulaclick.app.network.models.ReservaDTO>> call, retrofit2.Response<java.util.List<com.aulaclick.app.network.models.ReservaDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    todasLasReservas = response.body();
                    android.util.Log.d("DEBUG_DETALLE_RECURSO", "El servidor ha enviado " + todasLasReservas.size() + " reservas para recursoId=" + recursoId);
                    for (com.aulaclick.app.network.models.ReservaDTO r : todasLasReservas) {
                        android.util.Log.d("DEBUG_DETALLE_RECURSO", " -> ID: " + r.getId() + " | Fecha: " + r.getFecha() + " | Estado: [" + r.getEstado() + "]");
                    }
                    filtrarYMostrarReservas();
                } else {
                    android.util.Log.e("API_ERROR", "Error descargando reservas: " + response.code());
                    try {
                        if (response.errorBody() != null)
                            android.util.Log.e("DEBUG_DETALLE_RECURSO", "ErrorBody: " + response.errorBody().string());
                    } catch (Exception ignored) {}
                }
            }
            @Override
            public void onFailure(retrofit2.Call<java.util.List<com.aulaclick.app.network.models.ReservaDTO>> call, Throwable t) {
                android.util.Log.e("API_ERROR", "onFailure cargarReservas: " + t.getMessage(), t);
                android.widget.Toast.makeText(DetalleRecursoActivity.this, "Error al cargar reservas", android.widget.Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (recursoId != null) {
            cargarDetallesRecurso(recursoId);
            cargarReservas();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detalle, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem itemEditar = menu.findItem(R.id.action_editar);
        if (itemEditar != null) {
            SessionManager sessionManager = new SessionManager(this);
            String rol = sessionManager.getUserRole();
            
            boolean puedeEditar = rol != null &&
                    (rol.equalsIgnoreCase("ADMIN") || rol.equals("1") || rol.equalsIgnoreCase("ADMIN_SEDE"));
            itemEditar.setVisible(puedeEditar);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_editar && recursoId != null) {
            Intent intent = new Intent(this, AnadirRecursoActivity.class);
            intent.putExtra("RECURSO_ID", recursoId.longValue());
            startActivity(intent);
            return true;
        } else if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
