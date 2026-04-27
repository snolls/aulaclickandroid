package com.aulaclick.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aulaclick.app.network.ApiClient;
import com.aulaclick.app.network.models.Departamento;
import com.aulaclick.app.network.models.Equipamiento;
import com.aulaclick.app.network.models.Recurso;
import com.aulaclick.app.network.models.ImagenGaleriaDTO;
import com.aulaclick.app.network.models.RecursoRequestDTO;
import com.aulaclick.app.network.models.TipoRecurso;
import com.aulaclick.app.utils.SessionManager;
import com.bumptech.glide.Glide;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.UploadCallback;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AnadirRecursoActivity extends AppCompatActivity {

    // ── Vistas ──────────────────────────────────────────────────────────────
    private TextInputEditText etNombre, etCapacidad;
    private ChipGroup cgTipo, cgDepartamento, cgEquipamiento;
    private MaterialSwitch switchEstado;
    private MaterialSwitch switchFinesSemana;
    private TextInputEditText etHoraApertura, etHoraCierre;
    private MaterialButton btnGuardar;
    private MaterialButton btnSeleccionarFoto;
    private ImageView ivMiniaturaFoto;

    // ── Estado edición ───────────────────────────────────────────────────────
    private Long recursoId = -1L;
    private Recurso recursoEnEdicion = null;

    // ── Sede (solo ADMIN global) ─────────────────────────────────────────────
    private List<com.aulaclick.app.network.models.SedeDTO> sedesDisponibles = new ArrayList<>();
    private android.widget.Spinner spinnerSedeAdmin;
    private boolean esAdminGlobal = false;

    // ── Cloudinary / imagen ──────────────────────────────────────────────────
    private String urlImagenSubida = null;
    private Long idImagenActual = null;
    private Uri uriImagenLocal = null;
    private AlertDialog progressDialog;

    // ── Galería nube (estado para borrado) ───────────────────────────────────
    private List<ImagenGaleriaDTO> listaImagenesGaleria;
    private GaleriaAdapter galeriaAdapterActual;

    // ── Selector de galería ──────────────────────────────────────────────────
    private final ActivityResultLauncher<PickVisualMediaRequest> selectorImagenLauncher =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) {
                    uriImagenLocal = uri;
                    urlImagenSubida = null;
                    ivMiniaturaFoto.setVisibility(android.view.View.VISIBLE);
                    Glide.with(this).load(uri).centerCrop().into(ivMiniaturaFoto);
                } else {
                    Snackbar.make(btnSeleccionarFoto, "No se seleccionó ninguna imagen", Snackbar.LENGTH_LONG).show();
                }
            });

    // ────────────────────────────────────────────────────────────────────────
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anadir_recurso);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        inicializarCloudinary();

        // Detectar rol
        SessionManager sm = new SessionManager(this);
        esAdminGlobal = "ADMIN".equalsIgnoreCase(sm.getUserRole());

        // Inicializar vistas
        etNombre            = findViewById(R.id.etNombreRecurso);
        cgTipo              = findViewById(R.id.cgTipo);
        etCapacidad         = findViewById(R.id.etCapacidad);
        cgDepartamento      = findViewById(R.id.cgDepartamento);
        cgEquipamiento      = findViewById(R.id.chipGroupEquipamiento);
        switchEstado        = findViewById(R.id.switchEstado);
        switchFinesSemana   = findViewById(R.id.switchFinesSemana);
        etHoraApertura      = findViewById(R.id.etHoraApertura);
        etHoraCierre        = findViewById(R.id.etHoraCierre);
        btnGuardar          = findViewById(R.id.btnGuardarRecurso);
        btnSeleccionarFoto  = findViewById(R.id.btnSeleccionarFoto);
        ivMiniaturaFoto     = findViewById(R.id.ivMiniaturaFoto);
        spinnerSedeAdmin    = findViewById(R.id.spinnerSedeAdmin);

        if (esAdminGlobal) {
            android.view.View llSede = findViewById(R.id.llSedeAdmin);
            if (llSede != null) llSede.setVisibility(android.view.View.VISIBLE);
        }

        if (etHoraApertura != null) {
            etHoraApertura.setOnClickListener(v -> showTimePicker(etHoraApertura, "Hora Apertura"));
        }
        if (etHoraCierre != null) {
            etHoraCierre.setOnClickListener(v -> showTimePicker(etHoraCierre, "Hora Cierre"));
        }

        android.view.View ivBack = findViewById(R.id.ivBack);
        if (ivBack != null) {
            ivBack.setOnClickListener(v -> finish());
        }

        // Botón seleccionar foto
        btnSeleccionarFoto.setOnClickListener(v -> {
            String[] opciones = {"Subir nueva", "Galería en la nube", "Sin foto / Eliminar actual"};
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Seleccionar Imagen")
                    .setItems(opciones, (dialog, which) -> {
                        if (which == 0) {
                            abrirGaleriaLocal();
                        } else if (which == 1) {
                            cargarYMostrarGaleriaNube();
                        } else {
                            uriImagenLocal = null;
                            urlImagenSubida = null;
                            idImagenActual = null;
                            ivMiniaturaFoto.setImageResource(R.drawable.ic_image);
                            ivMiniaturaFoto.setVisibility(android.view.View.VISIBLE);
                        }
                    })
                    .show();
        });

        // Leer ID de edición del Intent
        if (getIntent().hasExtra("RECURSO_ID") && getIntent().getExtras() != null) {
            Object idObj = getIntent().getExtras().get("RECURSO_ID");
            if (idObj instanceof Integer) {
                recursoId = ((Integer) idObj).longValue();
            } else if (idObj instanceof Long) {
                recursoId = (Long) idObj;
            } else if (idObj instanceof String) {
                recursoId = Long.parseLong((String) idObj);
            }
        }

        if (recursoId != -1L) {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Editar Recurso");
            }
            android.widget.TextView tvTitle = findViewById(R.id.tvTitle);
            if (tvTitle != null) tvTitle.setText("Editar Recurso");

            android.widget.TextView tvSubtitle = findViewById(R.id.tvSubtitle);
            if (tvSubtitle != null) tvSubtitle.setText("Modifica los detalles del recurso existente.");

            btnGuardar.setText("ACTUALIZAR");
            cargarRecursoAEditar(recursoId);
        } else {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Añadir Recurso");
            }
        }

        cargarDatosDinámicos();

        btnGuardar.setOnClickListener(v -> guardarRecurso());
    }

    private void showTimePicker(TextInputEditText editText, String title) {
        int hour = 8;
        int minute = 0;
        if (editText.getText() != null && !editText.getText().toString().isEmpty()) {
            try {
                String[] parts = editText.getText().toString().split(":");
                hour = Integer.parseInt(parts[0]);
                minute = Integer.parseInt(parts[1]);
            } catch (Exception ignored) {}
        }
        com.google.android.material.timepicker.MaterialTimePicker picker = new com.google.android.material.timepicker.MaterialTimePicker.Builder()
                .setTimeFormat(com.google.android.material.timepicker.TimeFormat.CLOCK_24H)
                .setHour(hour)
                .setMinute(minute)
                .setTitleText(title)
                .build();

        picker.addOnPositiveButtonClickListener(v -> {
            String time = String.format(java.util.Locale.getDefault(), "%02d:%02d", picker.getHour(), picker.getMinute());
            editText.setText(time);
        });

        picker.show(getSupportFragmentManager(), "TIME_PICKER_RECURSO");
    }

    // ── Cloudinary ───────────────────────────────────────────────────────────

    private void inicializarCloudinary() {
        try {
            Map<String, String> config = new HashMap<>();
            config.put("cloud_name", BuildConfig.CLOUDINARY_CLOUD_NAME);
            MediaManager.init(this, config);
        } catch (Exception e) {
            // Ya estaba inicializado; ignorar
        }
    }

    private void abrirGaleriaLocal() {
        abrirSelectorImagen();
    }

    private void abrirSelectorImagen() {
        selectorImagenLauncher.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build());
    }

    private void cargarYMostrarGaleriaNube() {
        ApiClient.getApiService().getImagenesExistentes().enqueue(new Callback<List<ImagenGaleriaDTO>>() {
            @Override
            public void onResponse(@NonNull Call<List<ImagenGaleriaDTO>> call, @NonNull Response<List<ImagenGaleriaDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ImagenGaleriaDTO> imagenes = response.body();
                    if (imagenes.isEmpty()) {
                        Snackbar.make(btnSeleccionarFoto, "La galería está vacía. Sube una foto primero.", Snackbar.LENGTH_SHORT).show();
                        return;
                    }

                    BottomSheetDialog dialog = new BottomSheetDialog(AnadirRecursoActivity.this);

                    RecyclerView rv = new RecyclerView(AnadirRecursoActivity.this);
                    rv.setLayoutManager(new GridLayoutManager(AnadirRecursoActivity.this, 3));
                    int padding = (int) (8 * getResources().getDisplayMetrics().density);
                    rv.setPadding(padding, padding, padding, padding);
                    rv.setClipToPadding(false);

                    listaImagenesGaleria = imagenes;
                    galeriaAdapterActual = new GaleriaAdapter(imagenes, imagen -> {
                        idImagenActual = imagen.getIdImagen();
                        urlImagenSubida = imagen.getUrl();
                        uriImagenLocal = null;
                        ivMiniaturaFoto.setVisibility(android.view.View.VISIBLE);
                        Glide.with(AnadirRecursoActivity.this)
                                .load(imagen.getUrl())
                                .centerCrop()
                                .placeholder(R.drawable.ic_image)
                                .into(ivMiniaturaFoto);
                        dialog.dismiss();
                        Snackbar.make(btnSeleccionarFoto, "Imagen seleccionada", Snackbar.LENGTH_SHORT).show();
                    });
                    rv.setAdapter(galeriaAdapterActual);

                    dialog.setContentView(rv);
                    dialog.show();
                } else {
                    try {
                        String error = response.errorBody() != null ? response.errorBody().string() : "Sin detalles";
                        Log.e("GALERIA_ERROR", "Error HTTP " + response.code() + ": " + error);
                    } catch (Exception e) {
                        Log.e("GALERIA_ERROR", "No se pudo leer el errorBody", e);
                    }
                    Snackbar.make(btnSeleccionarFoto, "Error al conectar con la galería (" + response.code() + ")", Snackbar.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<ImagenGaleriaDTO>> call, @NonNull Throwable t) {
                Log.e("GALERIA_ERROR", "Fallo CRÍTICO de red o parseo: ", t);
                Snackbar.make(btnSeleccionarFoto, "Error al cargar galería: " + t.getMessage(), Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void subirYGuardar(Uri uri, RecursoRequestDTO dto) {
        MediaManager.get().upload(uri)
                .unsigned(BuildConfig.CLOUDINARY_UPLOAD_PRESET)
                .callback(new UploadCallback() {
                    @Override public void onStart(String requestId) {}
                    @Override public void onProgress(String requestId, long bytes, long totalBytes) {}
                    @Override public void onReschedule(String requestId, com.cloudinary.android.callback.ErrorInfo error) {}

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        urlImagenSubida = (String) resultData.get("secure_url");

                        com.aulaclick.app.network.models.ImagenRequestDTO imagenDto = new com.aulaclick.app.network.models.ImagenRequestDTO();
                        imagenDto.setUrl(urlImagenSubida);
                        ApiClient.getApiService().registrarImagenEnGaleria(imagenDto).enqueue(new Callback<ImagenGaleriaDTO>() {
                            @Override
                            public void onResponse(@NonNull Call<ImagenGaleriaDTO> call, @NonNull Response<ImagenGaleriaDTO> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    idImagenActual = response.body().getIdImagen();
                                }
                                dto.setIdImagen(idImagenActual);
                                crearOActualizarRecurso(dto);
                            }
                            @Override
                            public void onFailure(@NonNull Call<ImagenGaleriaDTO> call, @NonNull Throwable t) {
                                android.util.Log.w("GALERIA", "No se pudo registrar la imagen: " + t.getMessage());
                                crearOActualizarRecurso(dto);
                            }
                        });
                    }

                    @Override
                    public void onError(String requestId, com.cloudinary.android.callback.ErrorInfo error) {
                        runOnUiThread(() -> {
                            ocultarProgreso();
                            Snackbar.make(btnGuardar, "Error al subir imagen: " + error.getDescription(), Snackbar.LENGTH_LONG).show();
                        });
                    }
                })
                .dispatch();
    }

    // ── Carga y edición ──────────────────────────────────────────────────────

    private void cargarRecursoAEditar(Long id) {
        ApiClient.getApiService().getRecurso(id).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<Recurso> call, @NonNull Response<Recurso> response) {
                if (response.isSuccessful() && response.body() != null) {
                    recursoEnEdicion = response.body();
                    idImagenActual = recursoEnEdicion.getIdImagen();
                    aplicarDatosEdicion();
                } else {
                    Toast.makeText(AnadirRecursoActivity.this, "Error al cargar recurso para edición", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Recurso> call, @NonNull Throwable t) {
                Toast.makeText(AnadirRecursoActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

    private void aplicarDatosEdicion() {
        if (recursoEnEdicion == null) return;

        if (etNombre.getText().toString().isEmpty()) {
            etNombre.setText(recursoEnEdicion.getNombre());
            if (recursoEnEdicion.getCapacidad() != null) {
                etCapacidad.setText(String.valueOf(recursoEnEdicion.getCapacidad()));
            }
        }

        boolean estaDisponible = recursoEnEdicion.getEstado() != null && 
            (recursoEnEdicion.getEstado().equalsIgnoreCase("Disponible") || recursoEnEdicion.getEstado().equalsIgnoreCase("Activo"));
        switchEstado.setChecked(estaDisponible);

        // Cargar imagen existente (sólo si el usuario aún no ha elegido una nueva)
        if (uriImagenLocal == null && recursoEnEdicion.getImagenUrl() != null && !recursoEnEdicion.getImagenUrl().isEmpty()) {
            if (urlImagenSubida == null) urlImagenSubida = recursoEnEdicion.getImagenUrl();
            ivMiniaturaFoto.setVisibility(android.view.View.VISIBLE);
            Glide.with(this)
                    .load(urlImagenSubida)
                    .centerCrop()
                    .placeholder(R.drawable.ic_image)
                    .into(ivMiniaturaFoto);
        }

        if (switchFinesSemana != null && recursoEnEdicion.getPermiteFinesSemana() != null) {
            switchFinesSemana.setChecked(recursoEnEdicion.getPermiteFinesSemana());
        }
        if (etHoraApertura != null && recursoEnEdicion.getHoraApertura() != null && !recursoEnEdicion.getHoraApertura().isEmpty()) {
            etHoraApertura.setText(recortarHora(recursoEnEdicion.getHoraApertura()));
        }
        if (etHoraCierre != null && recursoEnEdicion.getHoraCierre() != null && !recursoEnEdicion.getHoraCierre().isEmpty()) {
            etHoraCierre.setText(recortarHora(recursoEnEdicion.getHoraCierre()));
        }

        if (recursoEnEdicion.getDepartamento() != null && recursoEnEdicion.getDepartamento().getId() != null) {
            seleccionarChipPorTag(cgDepartamento, recursoEnEdicion.getDepartamento().getId());
        }

        if (recursoEnEdicion.getTipoRecurso() != null && recursoEnEdicion.getTipoRecurso().getId() != null) {
            seleccionarChipPorTag(cgTipo, recursoEnEdicion.getTipoRecurso().getId());
        }

        if (recursoEnEdicion.getEquipamientos() != null) {
            for (Equipamiento eq : recursoEnEdicion.getEquipamientos()) {
                if (eq.getId() != null) {
                    seleccionarChipPorTag(cgEquipamiento, eq.getId());
                }
            }
        }

        // Pre-seleccionar sede en modo edición (solo ADMIN)
        if (esAdminGlobal && recursoEnEdicion.getSedeId() != null && !sedesDisponibles.isEmpty()) {
            for (int i = 0; i < sedesDisponibles.size(); i++) {
                if (sedesDisponibles.get(i).getId() != null &&
                        sedesDisponibles.get(i).getId().equals(recursoEnEdicion.getSedeId())) {
                    spinnerSedeAdmin.setSelection(i);
                    break;
                }
            }
        }
    }

    private void seleccionarChipPorTag(ChipGroup chipGroup, Object idBaseDatos) {
        if (idBaseDatos == null) return;
        String idObjetivo = String.valueOf(idBaseDatos);

        for (int i = 0; i < chipGroup.getChildCount(); i++) {
            Chip chip = (Chip) chipGroup.getChildAt(i);
            if (chip.getTag() != null && String.valueOf(chip.getTag()).equals(idObjetivo)) {
                chip.setChecked(true);
                break;
            }
        }
    }

    // ── Guardar recurso ──────────────────────────────────────────────────────

    private void guardarRecurso() {
        String nombre       = etNombre.getText() != null ? etNombre.getText().toString().trim() : "";
        String capacidadStr = etCapacidad.getText() != null ? etCapacidad.getText().toString().trim() : "";

        if (nombre.isEmpty() || capacidadStr.isEmpty()
                || cgDepartamento.getCheckedChipId() == -1
                || cgTipo.getCheckedChipId() == -1) {
            Toast.makeText(this, R.string.error_fill_fields, Toast.LENGTH_SHORT).show();
            return;
        }

        if (esAdminGlobal && sedesDisponibles.isEmpty()) {
            Toast.makeText(this, "Por favor, espera a que carguen las sedes", Toast.LENGTH_SHORT).show();
            return;
        }

        RecursoRequestDTO dto = new RecursoRequestDTO();
        dto.setNombre(nombre);
        dto.setCapacidad(Integer.parseInt(capacidadStr));
        dto.setEstado(switchEstado.isChecked() ? "Disponible" : "No disponible");
        dto.setIdDepartamento(obtenerIdDeChipGroup(cgDepartamento));
        dto.setIdTipoRecurso(obtenerIdDeChipGroup(cgTipo));
        dto.setIdsEquipamientos(obtenerIdsMultiplesDeChipGroup(cgEquipamiento));
        if (switchFinesSemana != null) dto.setPermiteFinesSemana(switchFinesSemana.isChecked());
        if (etHoraApertura != null && etHoraApertura.getText() != null)
            dto.setHoraApertura(etHoraApertura.getText().toString());
        if (etHoraCierre != null && etHoraCierre.getText() != null)
            dto.setHoraCierre(etHoraCierre.getText().toString());

        if (esAdminGlobal && !sedesDisponibles.isEmpty()) {
            int pos = spinnerSedeAdmin.getSelectedItemPosition();
            if (pos >= 0 && pos < sedesDisponibles.size()) {
                dto.setIdSede(sedesDisponibles.get(pos).getId());
            }
        }

        if (uriImagenLocal != null) {
            // Caso A: imagen local nueva → subir a Cloudinary → registrar → guardar
            mostrarProgreso("Subiendo imagen y guardando...");
            subirYGuardar(uriImagenLocal, dto);
        } else {
            // Caso B (galería nube) y C (sin imagen)
            dto.setIdImagen(idImagenActual);
            mostrarProgreso("Guardando...");
            crearOActualizarRecurso(dto);
        }
    }

    private void borrarImagenDeLaNube(Long id, int position) {
        AlertDialog pd = crearDialogoProgreso("Eliminando de la nube...");
        pd.show();

        ApiClient.getApiService().eliminarImagenGaleria(id).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                pd.dismiss();
                if (response.isSuccessful()) {
                    listaImagenesGaleria.remove(position);
                    galeriaAdapterActual.notifyItemRemoved(position);
                    galeriaAdapterActual.notifyItemRangeChanged(position, listaImagenesGaleria.size());
                    Snackbar snackbar = Snackbar.make(btnSeleccionarFoto, "Imagen eliminada correctamente", Snackbar.LENGTH_SHORT);
                    snackbar.getView().setBackgroundColor(android.graphics.Color.parseColor("#388E3C"));
                    snackbar.show();
                } else {
                    Snackbar.make(btnSeleccionarFoto, "Error al eliminar imagen (" + response.code() + ")", Snackbar.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                pd.dismiss();
                Snackbar.make(btnSeleccionarFoto, "Error de red: " + t.getMessage(), Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    private void crearOActualizarRecurso(RecursoRequestDTO dto) {
        SessionManager sessionManager = new SessionManager(this);
        String rol = sessionManager.getUserRole();

        Callback<Recurso> callback = new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<Recurso> call, @NonNull Response<Recurso> response) {
                ocultarProgreso();
                if (response.isSuccessful()) {
                    Toast.makeText(AnadirRecursoActivity.this,
                            recursoId != -1L ? "Recurso actualizado con éxito" : getString(R.string.msg_recurso_saved),
                            Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(AnadirRecursoActivity.this,
                            getString(R.string.error_server_prefix, response.code()),
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Recurso> call, @NonNull Throwable t) {
                ocultarProgreso();
                Toast.makeText(AnadirRecursoActivity.this,
                        getString(R.string.error_network_prefix, t.getMessage()),
                        Toast.LENGTH_SHORT).show();
            }
        };

        if (recursoId != -1L) {
            ApiClient.getApiService().actualizarRecurso(rol, recursoId, dto).enqueue(callback);
        } else {
            ApiClient.getApiService().crearRecurso(rol, dto).enqueue(callback);
        }
    }

    private AlertDialog crearDialogoProgreso(String mensaje) {
        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.HORIZONTAL);
        layout.setPadding(64, 48, 64, 48);
        layout.setGravity(android.view.Gravity.CENTER_VERTICAL);
        com.google.android.material.progressindicator.CircularProgressIndicator progress =
                new com.google.android.material.progressindicator.CircularProgressIndicator(this);
        progress.setIndeterminate(true);
        layout.addView(progress);
        android.widget.TextView tv = new android.widget.TextView(this);
        tv.setText(mensaje);
        tv.setPadding(32, 0, 0, 0);
        layout.addView(tv);
        return new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setView(layout)
                .setCancelable(false)
                .create();
    }

    private void mostrarProgreso(String mensaje) {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        progressDialog = crearDialogoProgreso(mensaje);
        progressDialog.show();
        btnGuardar.setEnabled(false);
        btnGuardar.setText(R.string.btn_saving);
    }

    private void ocultarProgreso() {
        runOnUiThread(() -> {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            btnGuardar.setEnabled(true);
            btnGuardar.setText(recursoId != -1L ? "ACTUALIZAR" : getString(R.string.btn_save_recurso));
        });
    }

    // ── Datos dinámicos ──────────────────────────────────────────────────────

    private void cargarDatosDinámicos() {
        if (esAdminGlobal) cargarSedes();
        cargarDepartamentos();
        cargarTiposRecurso();
        cargarEquipamiento();
    }

    private void cargarSedes() {
        ApiClient.getApiService().getSedes().enqueue(new Callback<List<com.aulaclick.app.network.models.SedeDTO>>() {
            @Override
            public void onResponse(@NonNull Call<List<com.aulaclick.app.network.models.SedeDTO>> call,
                                   @NonNull Response<List<com.aulaclick.app.network.models.SedeDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    sedesDisponibles = response.body();
                    android.widget.ArrayAdapter<com.aulaclick.app.network.models.SedeDTO> adapter =
                            new android.widget.ArrayAdapter<>(AnadirRecursoActivity.this,
                                    android.R.layout.simple_spinner_item, sedesDisponibles);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerSedeAdmin.setAdapter(adapter);
                    aplicarDatosEdicion();
                }
            }
            @Override
            public void onFailure(@NonNull Call<List<com.aulaclick.app.network.models.SedeDTO>> call,
                                  @NonNull Throwable t) {
                Toast.makeText(AnadirRecursoActivity.this, "Error al cargar sedes", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cargarDepartamentos() {
        ApiClient.getApiService().getDepartamentos().enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<Departamento>> call, @NonNull Response<List<Departamento>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    cgDepartamento.removeAllViews();
                    for (Departamento d : response.body()) {
                        if (d.getNombre() != null) {
                            cgDepartamento.addView(createChip(d.getNombre(), d.getId()));
                        }
                    }
                    aplicarDatosEdicion();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Departamento>> call, @NonNull Throwable t) {
                Toast.makeText(AnadirRecursoActivity.this, R.string.error_load_deptos, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cargarTiposRecurso() {
        ApiClient.getApiService().getTiposRecurso().enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<TipoRecurso>> call, @NonNull Response<List<TipoRecurso>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    cgTipo.removeAllViews();
                    for (TipoRecurso t : response.body()) {
                        if (t.getNombre() != null) {
                            cgTipo.addView(createChip(t.getNombre(), t.getId()));
                        }
                    }
                    aplicarDatosEdicion();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<TipoRecurso>> call, @NonNull Throwable t) {
                Toast.makeText(AnadirRecursoActivity.this, R.string.error_load_tipos, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cargarEquipamiento() {
        ApiClient.getApiService().getEquipamientos().enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<Equipamiento>> call, @NonNull Response<List<Equipamiento>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    cgEquipamiento.removeAllViews();
                    for (Equipamiento e : response.body()) {
                        if (e.getNombre() != null) {
                            cgEquipamiento.addView(createChip(e.getNombre(), e.getId()));
                        }
                    }
                    aplicarDatosEdicion();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Equipamiento>> call, @NonNull Throwable t) {
                Toast.makeText(AnadirRecursoActivity.this, R.string.error_load_equipamiento, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ── Utilidades ───────────────────────────────────────────────────────────

    private Chip createChip(String text, Integer id) {
        Chip chip = new Chip(this);
        chip.setId(android.view.View.generateViewId());
        chip.setText(text);
        chip.setCheckable(true);
        chip.setTag(id);
        chip.setClickable(true);
        return chip;
    }

    private String recortarHora(String hora) {
        if (hora != null && hora.length() >= 5) return hora.substring(0, 5);
        return hora;
    }

    private Long obtenerIdDeChipGroup(ChipGroup chipGroup) {
        int checkedId = chipGroup.getCheckedChipId();
        if (checkedId == -1) return null;
        Chip chip = findViewById(checkedId);
        if (chip != null && chip.getTag() != null) {
            return Long.parseLong(String.valueOf(chip.getTag()));
        }
        return null;
    }

    private List<Long> obtenerIdsMultiplesDeChipGroup(ChipGroup chipGroup) {
        List<Long> ids = new ArrayList<>();
        for (int id : chipGroup.getCheckedChipIds()) {
            Chip chip = findViewById(id);
            if (chip != null && chip.getTag() != null) {
                ids.add(Long.parseLong(String.valueOf(chip.getTag())));
            }
        }
        return ids;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // ── Galería nube adapter ─────────────────────────────────────────────────

    private class GaleriaAdapter extends RecyclerView.Adapter<GaleriaAdapter.VH> {

        interface OnImageClickListener {
            void onClick(ImagenGaleriaDTO imagen);
        }

        private final List<ImagenGaleriaDTO> imagenes;
        private final OnImageClickListener listener;

        GaleriaAdapter(List<ImagenGaleriaDTO> imagenes, OnImageClickListener listener) {
            this.imagenes = imagenes;
            this.listener = listener;
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull android.view.ViewGroup parent, int viewType) {
            int size = (int) (100 * parent.getContext().getResources().getDisplayMetrics().density);
            ImageView iv = new ImageView(parent.getContext());
            iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
            RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(size, size);
            int margin = (int) (4 * parent.getContext().getResources().getDisplayMetrics().density);
            lp.setMargins(margin, margin, margin, margin);
            iv.setLayoutParams(lp);
            return new VH(iv);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            ImagenGaleriaDTO imagen = imagenes.get(position);
            Glide.with(holder.imageView.getContext())
                    .load(imagen.getUrl())
                    .centerCrop()
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .into(holder.imageView);
            holder.imageView.setOnClickListener(v -> listener.onClick(imagen));
            holder.imageView.setOnLongClickListener(v -> {
                int pos = holder.getBindingAdapterPosition();
                if (pos == RecyclerView.NO_ID) return true;
                new MaterialAlertDialogBuilder(AnadirRecursoActivity.this)
                        .setTitle("¿Eliminar imagen?")
                        .setMessage("Esta imagen se borrará de la nube y de cualquier recurso que la esté usando. Esta acción no se puede deshacer.")
                        .setPositiveButton("Eliminar", (dialogInterface, i) -> borrarImagenDeLaNube(imagen.getIdImagen(), pos))
                        .setNegativeButton("Cancelar", null)
                        .show();
                return true;
            });
        }

        @Override
        public int getItemCount() {
            return imagenes.size();
        }

        static class VH extends RecyclerView.ViewHolder {
            final ImageView imageView;
            VH(ImageView iv) {
                super(iv);
                imageView = iv;
            }
        }
    }
}
