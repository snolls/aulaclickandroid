package com.aulaclick.app;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aulaclick.app.network.ApiClient;
import com.aulaclick.app.network.models.RolDTO;
import com.aulaclick.app.network.models.SedeDTO;
import com.aulaclick.app.network.models.UsuarioDTO;
import com.aulaclick.app.network.models.UsuarioRequestDTO;
import com.aulaclick.app.network.models.AdminCambiarPasswordDTO;
import com.aulaclick.app.network.models.UsuarioRolSedeDTO;
import com.aulaclick.app.utils.SessionManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GestionUsuariosActivity extends AppCompatActivity {

    private RecyclerView rvUsuarios;
    private ProgressBar progressUsuarios;
    private TextView tvEmpty;

    private List<RolDTO>     rolesDisponibles  = new ArrayList<>();
    private List<SedeDTO>    sedesDisponibles  = new ArrayList<>();
    private List<UsuarioDTO> todosLosUsuarios  = new ArrayList<>();
    private Long             sedeFiltroCurrent = null;
    private int              ordenActual       = 0; // 0=NombreAZ 1=NombreZA 2=Rol 3=Sede
    private int pendingLoads = 0;
    private AlertDialog dialogActivo;
    private String miRol;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gestion_usuarios);

        miRol = new SessionManager(this).getUserRole();

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        rvUsuarios       = findViewById(R.id.rvUsuarios);
        progressUsuarios = findViewById(R.id.progressUsuarios);
        tvEmpty          = findViewById(R.id.tvEmptyUsuarios);

        rvUsuarios.setLayoutManager(new LinearLayoutManager(this));

        // Sede filter visible solo para ADMIN global
        if ("ADMIN".equalsIgnoreCase(miRol)) {
            android.view.View ivSede = findViewById(R.id.ivSedeIconUsuarios);
            android.widget.Spinner spSede = findViewById(R.id.spinnerFiltroSedeUsuarios);
            android.view.View spacer = findViewById(R.id.spacerFiltroUsuarios);
            if (ivSede != null)  ivSede.setVisibility(android.view.View.VISIBLE);
            if (spSede != null)  spSede.setVisibility(android.view.View.VISIBLE);
            if (spacer != null)  spacer.setVisibility(android.view.View.GONE);
        } else {
            // ADMIN_SEDE: solo mostrar spacer para empujar el icono a la derecha
            android.view.View spacer = findViewById(R.id.spacerFiltroUsuarios);
            if (spacer != null) spacer.setVisibility(android.view.View.VISIBLE);
        }

        // Botón ordenar (siempre visible para cualquier admin)
        android.widget.ImageButton btnOrdenar = findViewById(R.id.btnOrdenarUsuarios);
        if (btnOrdenar != null) {
            btnOrdenar.setOnClickListener(v -> mostrarDialogoOrden());
        }

        FloatingActionButton fab = findViewById(R.id.fabNuevoUsuario);
        fab.setVisibility(View.VISIBLE);
        fab.setOnClickListener(v -> mostrarDialogoUsuario(null));

        cargarDatosIniciales();

        FloatingActionButton fabForce = findViewById(R.id.fabNuevoUsuario);
        if (fabForce != null) {
            fabForce.show();
            fabForce.setAlpha(1.0f);
        }
    }

    private void cargarDatosIniciales() {
        pendingLoads = 2;

        ApiClient.getApiService().getSedes().enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<SedeDTO>> call, @NonNull Response<List<SedeDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    sedesDisponibles = response.body();
                    if ("ADMIN".equalsIgnoreCase(miRol)) {
                        configurarSpinnerFiltroSede();
                    }
                }
                decrementAndCheck();
            }
            @Override
            public void onFailure(@NonNull Call<List<SedeDTO>> call, @NonNull Throwable t) {
                decrementAndCheck();
            }
        });

        ApiClient.getApiService().getRoles().enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<RolDTO>> call, @NonNull Response<List<RolDTO>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    rolesDisponibles = new ArrayList<>(response.body());
                    filtrarRoles();
                }
                decrementAndCheck();
            }
            @Override
            public void onFailure(@NonNull Call<List<RolDTO>> call, @NonNull Throwable t) {
                decrementAndCheck();
            }
        });
    }

    private void filtrarRoles() {
        if (miRol != null && miRol.equalsIgnoreCase("ADMIN_SEDE")) {
            rolesDisponibles.removeIf(r -> "ADMIN".equalsIgnoreCase(r.getNombre()));
        }
    }

    private synchronized void decrementAndCheck() {
        pendingLoads--;
        if (pendingLoads == 0) runOnUiThread(this::cargarUsuarios);
    }

    private void cargarUsuarios() {
        progressUsuarios.setVisibility(View.VISIBLE);
        rvUsuarios.setVisibility(View.GONE);
        tvEmpty.setVisibility(View.GONE);

        ApiClient.getApiService().getUsuarios().enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<UsuarioDTO>> call,
                                   @NonNull Response<List<UsuarioDTO>> response) {
                progressUsuarios.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    todosLosUsuarios = response.body();
                    aplicarFiltroSede();
                } else if (response.code() == 403) {
                    tvEmpty.setVisibility(View.VISIBLE);
                    Toast.makeText(GestionUsuariosActivity.this,
                            "Acceso denegado (403): sin permisos de administrador", Toast.LENGTH_LONG).show();
                } else {
                    tvEmpty.setVisibility(View.VISIBLE);
                    Toast.makeText(GestionUsuariosActivity.this,
                            "Error " + response.code() + ": " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(@NonNull Call<List<UsuarioDTO>> call, @NonNull Throwable t) {
                progressUsuarios.setVisibility(View.GONE);
                tvEmpty.setVisibility(View.VISIBLE);
                Toast.makeText(GestionUsuariosActivity.this,
                        "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostrarDialogoOrden() {
        String[] opciones = {
                getString(R.string.sort_nombre_az),
                getString(R.string.sort_nombre_za),
                getString(R.string.sort_rol),
                getString(R.string.sort_sede)
        };
        new android.app.AlertDialog.Builder(this)
                .setTitle(R.string.dialog_title_sort)
                .setSingleChoiceItems(opciones, ordenActual, (dialog, which) -> {
                    ordenActual = which;
                    aplicarFiltroSede();
                    dialog.dismiss();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void configurarSpinnerFiltroSede() {
        android.widget.Spinner spinner = findViewById(R.id.spinnerFiltroSedeUsuarios);
        if (spinner == null) return;

        List<SedeDTO> opciones = new ArrayList<>();
        SedeDTO todas = new SedeDTO();
        todas.setNombre(getString(R.string.label_todas_las_sedes));
        opciones.add(todas);
        opciones.addAll(sedesDisponibles);

        android.widget.ArrayAdapter<SedeDTO> adapter = new android.widget.ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, opciones);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                sedeFiltroCurrent = position == 0 ? null : opciones.get(position).getId();
                aplicarFiltroSede();
            }
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
    }

    @android.annotation.SuppressLint("NotifyDataSetChanged")
    private void aplicarFiltroSede() {
        List<UsuarioDTO> filtrados = new ArrayList<>();
        for (UsuarioDTO u : todosLosUsuarios) {
            if (sedeFiltroCurrent == null) {
                filtrados.add(u);
            } else {
                if ("ADMIN".equalsIgnoreCase(u.getRol())) continue;
                if (sedeFiltroCurrent.equals(u.getSedeId())) filtrados.add(u);
            }
        }
        ordenarLista(filtrados);
        if (filtrados.isEmpty()) {
            rvUsuarios.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            rvUsuarios.setAdapter(new UsuariosAdapter(filtrados,
                    GestionUsuariosActivity.this::mostrarDialogoUsuario));
            rvUsuarios.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.GONE);
        }
    }

    private void ordenarLista(List<UsuarioDTO> lista) {
        java.util.Comparator<UsuarioDTO> comparator;
        switch (ordenActual) {
            case 1: // Nombre Z→A
                comparator = (a, b) -> {
                    String na = a.getNombreCompleto() != null ? a.getNombreCompleto() : "";
                    String nb = b.getNombreCompleto() != null ? b.getNombreCompleto() : "";
                    return nb.compareToIgnoreCase(na);
                };
                break;
            case 2: // Rol (ADMIN → ADMIN_SEDE → resto, luego nombre A→Z)
                comparator = (a, b) -> {
                    int pa = pesoRol(a.getRol()), pb = pesoRol(b.getRol());
                    if (pa != pb) return pa - pb;
                    String na = a.getNombreCompleto() != null ? a.getNombreCompleto() : "";
                    String nb = b.getNombreCompleto() != null ? b.getNombreCompleto() : "";
                    return na.compareToIgnoreCase(nb);
                };
                break;
            case 3: // Sede A→Z, luego nombre A→Z
                comparator = (a, b) -> {
                    String sa = nombreSede(a.getSedeId());
                    String sb = nombreSede(b.getSedeId());
                    int cmp = sa.compareToIgnoreCase(sb);
                    if (cmp != 0) return cmp;
                    String na = a.getNombreCompleto() != null ? a.getNombreCompleto() : "";
                    String nb = b.getNombreCompleto() != null ? b.getNombreCompleto() : "";
                    return na.compareToIgnoreCase(nb);
                };
                break;
            default: // 0: Nombre A→Z
                comparator = (a, b) -> {
                    String na = a.getNombreCompleto() != null ? a.getNombreCompleto() : "";
                    String nb = b.getNombreCompleto() != null ? b.getNombreCompleto() : "";
                    return na.compareToIgnoreCase(nb);
                };
        }
        lista.sort(comparator);
    }

    private int pesoRol(String rol) {
        if (rol == null) return 99;
        switch (rol.toUpperCase()) {
            case "ADMIN":      return 0;
            case "ADMIN_SEDE": return 1;
            default:           return 2;
        }
    }

    private String nombreSede(Long sedeId) {
        if (sedeId == null) return "";
        for (SedeDTO s : sedesDisponibles) {
            if (sedeId.equals(s.getId())) return s.getNombre() != null ? s.getNombre() : "";
        }
        return "";
    }

    private void mostrarDialogoUsuario(@Nullable UsuarioDTO usuarioAEditar) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_usuario, null);

        TextInputLayout   tilNombre         = dialogView.findViewById(R.id.tilDialogNombre);
        TextInputEditText etNombre          = dialogView.findViewById(R.id.etDialogNombre);
        TextInputLayout   tilEmail          = dialogView.findViewById(R.id.tilDialogEmail);
        TextInputEditText etEmail           = dialogView.findViewById(R.id.etDialogEmail);
        TextInputLayout   tilPass           = dialogView.findViewById(R.id.tilDialogPassword);
        TextInputEditText etPassword        = dialogView.findViewById(R.id.etDialogPassword);
        TextInputLayout   tilCambiarPass    = dialogView.findViewById(R.id.tilDialogCambiarPassword);
        TextInputEditText etCambiarPassword = dialogView.findViewById(R.id.etDialogCambiarPassword);
        Spinner spinnerRol   = dialogView.findViewById(R.id.spinnerRol);
        Spinner spinnerSede  = dialogView.findViewById(R.id.spinnerSede);
        TextView tvSedeLabel = dialogView.findViewById(R.id.tvSedeLabel);

        boolean isNew = (usuarioAEditar == null);
        boolean esAdminSede = "ADMIN_SEDE".equalsIgnoreCase(miRol);

        tilPass.setVisibility(isNew ? View.VISIBLE : View.GONE);
        tilCambiarPass.setVisibility(isNew ? View.GONE : View.VISIBLE);

        if (esAdminSede) {
            tvSedeLabel.setVisibility(View.GONE);
            spinnerSede.setVisibility(View.GONE);
        }

        // Cuando el rol seleccionado es ADMIN, la sede no aplica
        if (!esAdminSede) {
            spinnerRol.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                    RolDTO rolSeleccionado = rolesDisponibles.get(position);
                    boolean esAdmin = "ADMIN".equalsIgnoreCase(rolSeleccionado.getNombre());
                    tvSedeLabel.setVisibility(esAdmin ? View.GONE : View.VISIBLE);
                    spinnerSede.setVisibility(esAdmin ? View.GONE : View.VISIBLE);
                }
                @Override
                public void onNothingSelected(android.widget.AdapterView<?> parent) {}
            });
        }

        ArrayAdapter<RolDTO> rolAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, rolesDisponibles);
        rolAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRol.setAdapter(rolAdapter);

        ArrayAdapter<SedeDTO> sedeAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, sedesDisponibles);
        sedeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSede.setAdapter(sedeAdapter);

        if (!isNew) {
            etNombre.setText(usuarioAEditar.getNombreCompleto());
            etEmail.setText(usuarioAEditar.getEmail());

            for (int i = 0; i < rolesDisponibles.size(); i++) {
                RolDTO r = rolesDisponibles.get(i);
                boolean matchById   = usuarioAEditar.getIdRol() != null && usuarioAEditar.getIdRol().equals(r.getId());
                boolean matchByName = usuarioAEditar.getRol() != null && usuarioAEditar.getRol().equalsIgnoreCase(r.getNombre());
                if (matchById || matchByName) {
                    spinnerRol.setSelection(i);
                    break;
                }
            }

            if (!esAdminSede && usuarioAEditar.getSedeId() != null) {
                for (int i = 0; i < sedesDisponibles.size(); i++) {
                    if (sedesDisponibles.get(i).getId().equals(usuarioAEditar.getSedeId())) {
                        spinnerSede.setSelection(i);
                        break;
                    }
                }
            }
        }

        // Clear field errors when user starts typing (same pattern as login screen)
        android.text.TextWatcher clearErrors = new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                tilNombre.setError(null);
                tilEmail.setError(null);
                tilPass.setError(null);
                tilCambiarPass.setError(null);
            }
            @Override public void afterTextChanged(android.text.Editable s) {}
        };
        etNombre.addTextChangedListener(clearErrors);
        etEmail.addTextChangedListener(clearErrors);
        etPassword.addTextChangedListener(clearErrors);
        etCambiarPassword.addTextChangedListener(clearErrors);

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(isNew ? getString(R.string.dialog_nuevo_usuario) : getString(R.string.dialog_editar_usuario))
                .setView(dialogView)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Guardar", null);

        if (!isNew) {
            builder.setNeutralButton("Eliminar", null);
        }

        dialogActivo = builder.create();
        dialogActivo.show();

        if (!isNew) {
            dialogActivo.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(v ->
                    confirmarEliminar(usuarioAEditar));
        }

        dialogActivo.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            tilNombre.setError(null);
            tilEmail.setError(null);
            tilPass.setError(null);
            tilCambiarPass.setError(null);

            String nombre = etNombre.getText() != null ? etNombre.getText().toString().trim() : "";
            String email  = etEmail.getText()  != null ? etEmail.getText().toString().trim()  : "";

            Long idRolSeleccionado  = null;
            Long idSedeSeleccionada = null;

            int posRol = spinnerRol.getSelectedItemPosition();
            boolean rolEsAdmin = false;
            if (posRol >= 0 && posRol < rolesDisponibles.size()) {
                idRolSeleccionado = rolesDisponibles.get(posRol).getId();
                rolEsAdmin = "ADMIN".equalsIgnoreCase(rolesDisponibles.get(posRol).getNombre());
            }

            if (rolEsAdmin) {
                // ADMIN global → sin sede
                idSedeSeleccionada = null;
            } else if (esAdminSede) {
                if (!isNew && usuarioAEditar != null) {
                    idSedeSeleccionada = usuarioAEditar.getSedeId();
                }
            } else {
                int posSede = spinnerSede.getSelectedItemPosition();
                if (posSede >= 0 && posSede < sedesDisponibles.size()) {
                    idSedeSeleccionada = sedesDisponibles.get(posSede).getId();
                }
            }

            if (nombre.isEmpty()) {
                tilNombre.setError(getString(R.string.error_nombre_obligatorio));
                etNombre.requestFocus();
                return;
            }
            if (email.isEmpty()) {
                tilEmail.setError(getString(R.string.error_email_obligatorio));
                etEmail.requestFocus();
                return;
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                tilEmail.setError(getString(R.string.error_formato_email));
                etEmail.requestFocus();
                return;
            }
            if (idRolSeleccionado == null) {
                Toast.makeText(this, R.string.error_selecciona_rol, Toast.LENGTH_SHORT).show();
                return;
            }
            if (!esAdminSede && !rolEsAdmin && idSedeSeleccionada == null) {
                Toast.makeText(this, R.string.error_selecciona_sede, Toast.LENGTH_SHORT).show();
                return;
            }

            if (isNew) {
                // Step 1: check email uniqueness, then validate password, then create
                Long finalIdRolNuevo  = idRolSeleccionado;
                Long finalIdSedeNuevo = idSedeSeleccionada;

                ApiClient.getApiService().verificarEmail(email).enqueue(new Callback<>() {
                    @Override
                    public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                        if (response.code() == 409) {
                            tilEmail.setError(getString(R.string.error_email_en_uso));
                            etEmail.requestFocus();
                            return;
                        }

                        // Email OK — now validate password
                        String pass = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";
                        if (pass.isEmpty()) {
                            tilPass.setError(getString(R.string.error_password_required));
                            etPassword.requestFocus();
                            return;
                        }
                        String passError = validarPassword(pass, nombre, email);
                        if (passError != null) {
                            tilPass.setError(passError);
                            etPassword.requestFocus();
                            return;
                        }

                        // Email and password OK — create user
                        UsuarioRequestDTO dto = new UsuarioRequestDTO();
                        dto.setNombreCompleto(nombre);
                        dto.setEmail(email);
                        dto.setPassword(pass);
                        dto.setIdRol(finalIdRolNuevo);
                        dto.setIdSede(finalIdSedeNuevo);

                        ApiClient.getApiService().crearUsuario(dto).enqueue(new Callback<>() {
                            @Override
                            public void onResponse(@NonNull Call<UsuarioDTO> call2,
                                                   @NonNull Response<UsuarioDTO> response2) {
                                if (response2.isSuccessful()) {
                                    Toast.makeText(GestionUsuariosActivity.this,
                                            getString(R.string.msg_usuario_creado), Toast.LENGTH_SHORT).show();
                                    dialogActivo.dismiss();
                                    cargarUsuarios();
                                } else if (response2.code() == 409) {
                                    tilEmail.setError(getString(R.string.error_email_en_uso));
                                    etEmail.requestFocus();
                                } else if (response2.code() == 400) {
                                    String msg = parseErrorMessage(response2.errorBody());
                                    tilPass.setError(msg != null ? msg : "Datos no válidos");
                                    etPassword.requestFocus();
                                } else {
                                    Toast.makeText(GestionUsuariosActivity.this,
                                            "Error " + response2.code() + ": " + response2.message(),
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                            @Override
                            public void onFailure(@NonNull Call<UsuarioDTO> call2, @NonNull Throwable t) {
                                Toast.makeText(GestionUsuariosActivity.this,
                                        "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    @Override
                    public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                        Toast.makeText(GestionUsuariosActivity.this,
                                "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                String nuevaPass = etCambiarPassword.getText() != null
                        ? etCambiarPassword.getText().toString().trim() : "";

                Long finalIdRol  = idRolSeleccionado;
                Long finalIdSede = idSedeSeleccionada;

                if (!nuevaPass.isEmpty()) {
                    String passError = validarPassword(nuevaPass, nombre, email);
                    if (passError != null) {
                        tilCambiarPass.setError(passError);
                        etCambiarPassword.requestFocus();
                        return;
                    }
                    ApiClient.getApiService()
                            .adminCambiarPassword(usuarioAEditar.getId(),
                                    new AdminCambiarPasswordDTO(nuevaPass))
                            .enqueue(new Callback<>() {
                                @Override
                                public void onResponse(@NonNull Call<Void> call,
                                                       @NonNull Response<Void> response) {
                                    if (response.isSuccessful()) {
                                        actualizarRolSede(usuarioAEditar.getId(), finalIdRol, finalIdSede, nombre, email);
                                    } else if (response.code() == 400) {
                                        String msg = parseErrorMessage(response.errorBody());
                                        tilCambiarPass.setError(msg != null ? msg : "Contraseña no válida");
                                        etCambiarPassword.requestFocus();
                                    } else {
                                        Toast.makeText(GestionUsuariosActivity.this,
                                                "Error al cambiar contraseña (" + response.code() + ")",
                                                Toast.LENGTH_LONG).show();
                                    }
                                }
                                @Override
                                public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                                    Toast.makeText(GestionUsuariosActivity.this,
                                            "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                } else {
                    actualizarRolSede(usuarioAEditar.getId(), finalIdRol, finalIdSede, nombre, email);
                }
            }
        });
    }

    private void confirmarEliminar(UsuarioDTO usuario) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar usuario")
                .setMessage(getString(R.string.dialog_eliminar_usuario_msg, usuario.getNombreCompleto()))
                .setPositiveButton("Eliminar", (d, w) -> {
                    ApiClient.getApiService().eliminarUsuario(usuario.getId()).enqueue(new Callback<>() {
                        @Override
                        public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(GestionUsuariosActivity.this,
                                        R.string.msg_usuario_eliminado, Toast.LENGTH_SHORT).show();
                                if (dialogActivo != null) dialogActivo.dismiss();
                                cargarUsuarios();
                            } else {
                                Toast.makeText(GestionUsuariosActivity.this,
                                        "Error " + response.code() + ": " + response.message(),
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                        @Override
                        public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                            Toast.makeText(GestionUsuariosActivity.this,
                                    "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void actualizarRolSede(Long idUsuario, Long idRol, Long idSede, String nombre, String email) {
        ApiClient.getApiService()
                .actualizarRolSede(idUsuario, new UsuarioRolSedeDTO(idRol, idSede, nombre, email))
                .enqueue(new Callback<>() {
                    @Override
                    public void onResponse(@NonNull Call<UsuarioDTO> call,
                                           @NonNull Response<UsuarioDTO> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(GestionUsuariosActivity.this,
                                    getString(R.string.msg_usuario_actualizado), Toast.LENGTH_SHORT).show();
                            if (dialogActivo != null) dialogActivo.dismiss();
                            cargarUsuarios();
                        } else if (response.code() == 409) {
                            Toast.makeText(GestionUsuariosActivity.this,
                                    "El correo ya está en uso por otro usuario", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(GestionUsuariosActivity.this,
                                    "Error " + response.code() + ": " + response.message(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onFailure(@NonNull Call<UsuarioDTO> call, @NonNull Throwable t) {
                        Toast.makeText(GestionUsuariosActivity.this,
                                "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private String validarPassword(String pass, String nombre, String email) {
        if (pass.length() < 12)
            return getString(R.string.val_pass_min_length);
        if (!pass.matches(".*[0-9].*"))
            return getString(R.string.val_pass_needs_digit);
        if (!pass.matches(".*[a-zA-Z].*"))
            return getString(R.string.val_pass_needs_letter);
        if (pass.contains("@") || pass.contains("\""))
            return getString(R.string.val_pass_forbidden_chars);
        if (!pass.matches(".*[!#$%&'()*+,\\-./:;<=>?\\[\\]^_`{|}~].*"))
            return getString(R.string.val_pass_needs_special);
        String passLower = pass.toLowerCase();
        if (email != null && !email.isEmpty()) {
            String localPart = email.contains("@") ? email.split("@")[0].toLowerCase() : email.toLowerCase();
            if (passLower.contains(localPart))
                return getString(R.string.val_pass_contains_email);
        }
        if (nombre != null && !nombre.isEmpty()) {
            for (String parte : nombre.toLowerCase().split(" ")) {
                if (parte.length() > 2 && passLower.contains(parte))
                    return getString(R.string.val_pass_contains_name);
            }
        }
        return null;
    }

    private String parseErrorMessage(okhttp3.ResponseBody errorBody) {
        if (errorBody == null) return null;
        try {
            String body = errorBody.string();
            int idx = body.indexOf("\"message\":\"");
            if (idx >= 0) {
                int start = idx + 11;
                int end = body.indexOf("\"", start);
                if (end > start) return body.substring(start, end);
            }
            return body.isEmpty() ? null : body;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
