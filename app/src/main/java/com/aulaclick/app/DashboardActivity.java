package com.aulaclick.app;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aulaclick.app.network.ApiClient;
import com.aulaclick.app.utils.SessionManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.aulaclick.app.network.models.Recurso;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardActivity extends AppCompatActivity {

    private RecursoAdapter adapter;
    private final List<Recurso> listaCompleta = new ArrayList<>();
    private final List<Recurso> listaFiltrada = new ArrayList<>();
    private com.google.android.material.tabs.TabLayout tabLayoutFiltros;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Configurar la barra de herramientas
        com.google.android.material.appbar.MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setOverflowIcon(ContextCompat.getDrawable(this, R.drawable.ic_menu));

        tabLayoutFiltros = findViewById(R.id.tabLayoutFiltrosRecursos);

        // Configurar el RecyclerView
        RecyclerView rvRecursos = findViewById(R.id.rvRecursos);
        rvRecursos.setLayoutManager(new LinearLayoutManager(this));

        adapter = new RecursoAdapter(listaFiltrada, recurso -> {
            Intent intent = new Intent(this, DetalleRecursoActivity.class);
            intent.putExtra("recurso_json", new com.google.gson.Gson().toJson(recurso));
            startActivity(intent);
        });
        rvRecursos.setAdapter(adapter);

        // Configurar el botón flotante de acción
        FloatingActionButton fabAddRecurso = findViewById(R.id.fabAddRecurso);
        fabAddRecurso.setOnClickListener(v -> {
            startActivity(new Intent(this, AnadirRecursoActivity.class));
        });

        // Configurar deslizar para eliminar y visibilidad del FAB (solo para ADMIN)
        SessionManager sessionManager = new SessionManager(this);
        ApiClient.setToken(sessionManager.getToken());
        String rol = sessionManager.getUserRole();
        
        boolean esAdminOAdminSede = rol != null &&
                (rol.equalsIgnoreCase("ADMIN") || rol.equals("1") || rol.equalsIgnoreCase("ADMIN_SEDE"));

        if (esAdminOAdminSede) {
            fabAddRecurso.setVisibility(View.VISIBLE);
            ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
                @Override
                public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                    return false;
                }

                @Override
                public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

                    View itemView = viewHolder.itemView;
                    
                    // Configurar la brocha para el fondo rojo
                    Paint paint = new Paint();
                    paint.setColor(Color.parseColor("#F44336"));
                    paint.setAntiAlias(true); // Suaviza los bordes curvos

                    // Radio de esquinas en píxeles (12dp; ajustar si cambia el radio de las tarjetas)
                    float cornerRadius = 12 * itemView.getResources().getDisplayMetrics().density;

                    Drawable icon = ContextCompat.getDrawable(recyclerView.getContext(), android.R.drawable.ic_menu_delete);
                    if (icon == null) return;

                    int iconMargin = (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
                    int iconTop = itemView.getTop() + (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
                    int iconBottom = iconTop + icon.getIntrinsicHeight();

                    RectF backgroundRect = new RectF();

                    if (dX > 0) { // Deslizamiento hacia la derecha
                        int iconLeft = itemView.getLeft() + iconMargin;
                        int iconRight = itemView.getLeft() + iconMargin + icon.getIntrinsicWidth();
                        icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);

                        backgroundRect.set(itemView.getLeft(), itemView.getTop(), itemView.getLeft() + dX, itemView.getBottom());
                    } else if (dX < 0) { // Deslizamiento hacia la izquierda
                        int iconLeft = itemView.getRight() - iconMargin - icon.getIntrinsicWidth();
                        int iconRight = itemView.getRight() - iconMargin;
                        icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);

                        backgroundRect.set(itemView.getRight() + dX, itemView.getTop(), itemView.getRight(), itemView.getBottom());
                    } else {
                        backgroundRect.set(0, 0, 0, 0);
                    }

                    // Dibujar fondo rojo con esquinas redondeadas
                    c.drawRoundRect(backgroundRect, cornerRadius, cornerRadius, paint);
                    
                    // Mostrar el icono solo si el deslizamiento supera el margen mínimo
                    if (Math.abs(dX) > iconMargin) {
                        icon.draw(c);
                    }
                }

                @Override
                public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                    int position = viewHolder.getAdapterPosition();
                    Recurso recurso = listaFiltrada.get(position);

                    new MaterialAlertDialogBuilder(DashboardActivity.this)
                            .setTitle("¿Eliminar Recurso?")
                            .setMessage("¿Estás seguro de que deseas eliminar " + recurso.getNombre() + "?")
                            .setPositiveButton("Eliminar", (dialog, which) -> {
                                ApiClient.getApiService().eliminarRecurso(rol, recurso.getId()).enqueue(new Callback<>() {
                                    @Override
                                    public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                                        if (response.isSuccessful()) {
                                            cargarRecursos();
                                        } else if (response.code() == 403) {
                                            Toast.makeText(DashboardActivity.this, "No tienes permisos", Toast.LENGTH_SHORT).show();
                                            adapter.notifyItemChanged(position);
                                        } else if (response.code() == 409) {
                                            Toast.makeText(DashboardActivity.this, "El recurso está en uso", Toast.LENGTH_SHORT).show();
                                            adapter.notifyItemChanged(position);
                                        } else {
                                            Toast.makeText(DashboardActivity.this, "Error al eliminar recurso", Toast.LENGTH_SHORT).show();
                                            adapter.notifyItemChanged(position);
                                        }
                                    }

                                    @Override
                                    public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                                        Toast.makeText(DashboardActivity.this, "Error de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                                        adapter.notifyItemChanged(position);
                                    }
                                });
                            })
                            .setNegativeButton("Cancelar", (dialog, which) -> adapter.notifyItemChanged(position))
                            .setOnCancelListener(dialog -> adapter.notifyItemChanged(position))
                            .show();
                }
            };
            new ItemTouchHelper(simpleCallback).attachToRecyclerView(rvRecursos);
        } else {
            fabAddRecurso.setVisibility(View.GONE);
        }

        // Configurar la navegación inferior
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setSelectedItemId(R.id.nav_recursos);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_recursos) {
                return true;
            } else if (id == R.id.nav_reservas) {
                Intent intent = new Intent(this, MisReservasActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                return true;
            } else if (id == R.id.nav_perfil) {
                Intent intent = new Intent(this, PerfilActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                return true;
            }
            return false;
        });

        cargarRecursos();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.clear();
        getMenuInflater().inflate(R.menu.menu_opciones_superior, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem adminItem = menu.findItem(R.id.action_panel_admin);
        if (adminItem != null) {
            String rol = new SessionManager(this).getUserRole().toUpperCase();
            adminItem.setVisible(rol.contains("ADMIN"));
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_ayuda) {
            startActivity(new Intent(this, AyudaActivity.class));
            return true;
        } else if (id == R.id.action_cerrar_sesion) {
            new SessionManager(this).logout();
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return true;
        } else if (id == R.id.action_panel_admin) {
            startActivity(new Intent(this, AdminPanelActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        if (bottomNav != null) bottomNav.getMenu().findItem(R.id.nav_recursos).setChecked(true);
        cargarRecursos();
    }

    private void cargarRecursos() {
        ApiClient.getApiService().getRecursos().enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<Recurso>> call, @NonNull Response<List<Recurso>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listaCompleta.clear();
                    listaCompleta.addAll(response.body());
                    configurarTabsFiltros();
                } else {
                    Toast.makeText(DashboardActivity.this, R.string.error_load_recursos, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Recurso>> call, @NonNull Throwable t) {
                boolean esTimeout = t instanceof java.net.SocketTimeoutException
                        || t instanceof java.net.ConnectException
                        || t instanceof java.io.InterruptedIOException;

                String mensaje = esTimeout
                        ? "El servidor se estaba despertando. Desliza hacia abajo o pulsa reintentar."
                        : getString(R.string.error_network_prefix, t.getMessage());

                RecyclerView rv = findViewById(R.id.rvRecursos);
                View anchor = rv != null ? rv : findViewById(android.R.id.content);
                com.google.android.material.snackbar.Snackbar
                        .make(anchor, mensaje, com.google.android.material.snackbar.Snackbar.LENGTH_INDEFINITE)
                        .setAction("Reintentar", v -> cargarRecursos())
                        .show();
            }
        });
    }

    private void configurarTabsFiltros() {
        if (tabLayoutFiltros == null) return;
        tabLayoutFiltros.removeAllTabs();
        tabLayoutFiltros.clearOnTabSelectedListeners();

        List<String> tiposUnicos = new ArrayList<>();
        for (Recurso r : listaCompleta) {
            if (r.getTipoRecurso() != null && r.getTipoRecurso().getNombre() != null) {
                String tipo = r.getTipoRecurso().getNombre();
                if (!tiposUnicos.contains(tipo)) tiposUnicos.add(tipo);
            }
        }

        tabLayoutFiltros.addTab(tabLayoutFiltros.newTab().setText("Todos"));
        tabLayoutFiltros.addTab(tabLayoutFiltros.newTab().setText("Disponible"));
        tabLayoutFiltros.addTab(tabLayoutFiltros.newTab().setText("No disponible"));
        for (String tipo : tiposUnicos) {
            tabLayoutFiltros.addTab(tabLayoutFiltros.newTab().setText(tipo));
        }

        tabLayoutFiltros.addOnTabSelectedListener(new com.google.android.material.tabs.TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(com.google.android.material.tabs.TabLayout.Tab tab) {
                if (tab.getText() != null) filtrarRecursos(tab.getText().toString());
            }
            @Override public void onTabUnselected(com.google.android.material.tabs.TabLayout.Tab tab) {}
            @Override public void onTabReselected(com.google.android.material.tabs.TabLayout.Tab tab) {}
        });

        filtrarRecursos("Todos");
    }

    @SuppressLint("NotifyDataSetChanged")
    private void filtrarRecursos(String filtroSeleccionado) {
        listaFiltrada.clear();
        String filtro = filtroSeleccionado != null ? filtroSeleccionado.toLowerCase() : "todos";
        for (Recurso r : listaCompleta) {
            String estadoDB = r.getEstado() != null ? r.getEstado().toLowerCase() : "";
            String tipoDB   = (r.getTipoRecurso() != null && r.getTipoRecurso().getNombre() != null)
                              ? r.getTipoRecurso().getNombre().toLowerCase() : "";
            if (filtro.equals("todos")) {
                listaFiltrada.add(r);
            } else if (filtro.equals("disponible")) {
                if (estadoDB.equals("disponible") || estadoDB.equals("activo")) listaFiltrada.add(r);
            } else if (filtro.equals("no disponible")) {
                if (estadoDB.contains("no disponible") || estadoDB.contains("mantenimiento")
                        || estadoDB.equals("no disponible")) listaFiltrada.add(r);
            } else {
                if (tipoDB.equals(filtro)) listaFiltrada.add(r);
            }
        }
        if (adapter != null) adapter.notifyDataSetChanged();
    }
}
