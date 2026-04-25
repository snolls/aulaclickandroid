package com.aulaclick.app;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aulaclick.app.network.ApiClient;
import com.aulaclick.app.network.models.ImagenGaleriaDTO;
import com.aulaclick.app.network.models.ImagenRequestDTO;
import com.bumptech.glide.Glide;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.UploadCallback;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GestionGaleriaActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private RecyclerView rvGaleriaAdmin;
    private ProgressBar progressBar;
    private TextView tvGaleriaVacia;
    private MenuItem menuItemEliminar;

    private final List<ImagenGaleriaDTO> listaImagenes = new ArrayList<>();
    private GaleriaAdminAdapter adapter;

    private final ActivityResultLauncher<Intent> selectorImagen = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() != Activity.RESULT_OK || result.getData() == null) return;
                List<Uri> urisSeleccionadas = new ArrayList<>();
                if (result.getData().getClipData() != null) {
                    int count = result.getData().getClipData().getItemCount();
                    for (int i = 0; i < count; i++) {
                        urisSeleccionadas.add(result.getData().getClipData().getItemAt(i).getUri());
                    }
                } else if (result.getData().getData() != null) {
                    urisSeleccionadas.add(result.getData().getData());
                }
                if (!urisSeleccionadas.isEmpty()) {
                    procesarSubidaMasiva(urisSeleccionadas);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gestion_galeria);

        toolbar        = findViewById(R.id.toolbar);
        rvGaleriaAdmin = findViewById(R.id.rvGaleriaAdmin);
        progressBar    = findViewById(R.id.progressBar);
        tvGaleriaVacia = findViewById(R.id.tvGaleriaVacia);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        inicializarCloudinary();

        adapter = new GaleriaAdminAdapter(count -> actualizarModoSeleccion(count));
        rvGaleriaAdmin.setLayoutManager(new GridLayoutManager(this, 3));
        rvGaleriaAdmin.setAdapter(adapter);

        FloatingActionButton fabSubir = findViewById(R.id.fabSubirFoto);
        fabSubir.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            selectorImagen.launch(intent);
        });

        cargarImagenes();
    }

    // ── Toolbar menu ─────────────────────────────────────────────────────────

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_galeria, menu);
        menuItemEliminar = menu.findItem(R.id.action_eliminar_seleccion);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (adapter.isSelectionMode()) {
                actualizarModoSeleccion(0);
            } else {
                finish();
            }
            return true;
        }
        if (item.getItemId() == R.id.action_eliminar_seleccion) {
            confirmarEliminarSeleccion();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (adapter.isSelectionMode()) {
            actualizarModoSeleccion(0);
        } else {
            super.onBackPressed();
        }
    }

    // ── Modo selección ────────────────────────────────────────────────────────

    private void actualizarModoSeleccion(int cantidad) {
        if (cantidad > 0) {
            toolbar.setTitle(cantidad + " seleccionadas");
            if (menuItemEliminar != null) menuItemEliminar.setVisible(true);
        } else {
            adapter.clearSelection();
            toolbar.setTitle("Galería Multimedia");
            if (menuItemEliminar != null) menuItemEliminar.setVisible(false);
        }
    }

    // ── Carga ─────────────────────────────────────────────────────────────────

    private void cargarImagenes() {
        progressBar.setVisibility(View.VISIBLE);
        rvGaleriaAdmin.setVisibility(View.GONE);
        tvGaleriaVacia.setVisibility(View.GONE);

        ApiClient.getApiService().getImagenesExistentes().enqueue(new Callback<List<ImagenGaleriaDTO>>() {
            @Override
            public void onResponse(@NonNull Call<List<ImagenGaleriaDTO>> call, @NonNull Response<List<ImagenGaleriaDTO>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    listaImagenes.clear();
                    listaImagenes.addAll(response.body());
                    adapter.notifyDataSetChanged();
                    boolean vacia = listaImagenes.isEmpty();
                    tvGaleriaVacia.setVisibility(vacia ? View.VISIBLE : View.GONE);
                    rvGaleriaAdmin.setVisibility(vacia ? View.GONE : View.VISIBLE);
                } else {
                    tvGaleriaVacia.setVisibility(View.VISIBLE);
                    Snackbar.make(rvGaleriaAdmin, "Error al cargar galería (" + response.code() + ")", Snackbar.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<ImagenGaleriaDTO>> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                tvGaleriaVacia.setVisibility(View.VISIBLE);
                Snackbar.make(rvGaleriaAdmin, "Error de red: " + t.getMessage(), Snackbar.LENGTH_LONG).show();
            }
        });
    }

    // ── Borrado individual ────────────────────────────────────────────────────

    private void borrarImagen(Long id, int position) {
        android.app.ProgressDialog pd = new android.app.ProgressDialog(this);
        pd.setMessage("Eliminando de la nube...");
        pd.setCancelable(false);
        pd.show();

        ApiClient.getApiService().eliminarImagenGaleria(id).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                pd.dismiss();
                if (response.isSuccessful()) {
                    listaImagenes.remove(position);
                    adapter.notifyItemRemoved(position);
                    adapter.notifyItemRangeChanged(position, listaImagenes.size());
                    if (listaImagenes.isEmpty()) {
                        rvGaleriaAdmin.setVisibility(View.GONE);
                        tvGaleriaVacia.setVisibility(View.VISIBLE);
                    }
                    mostrarSnackbarVerde("Imagen eliminada correctamente");
                } else {
                    Snackbar.make(rvGaleriaAdmin, "Error al eliminar imagen (" + response.code() + ")", Snackbar.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                pd.dismiss();
                Snackbar.make(rvGaleriaAdmin, "Error de red: " + t.getMessage(), Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    // ── Borrado masivo ────────────────────────────────────────────────────────

    private void confirmarEliminarSeleccion() {
        Set<Long> ids = adapter.getSeleccionadas();
        if (ids.isEmpty()) return;
        new MaterialAlertDialogBuilder(this)
                .setTitle("¿Eliminar " + ids.size() + " imágenes?")
                .setMessage("Se borrarán definitivamente de Cloudinary y de la base de datos. Esta acción no se puede deshacer.")
                .setPositiveButton("Eliminar", (dialog, i) -> eliminarMasivo(new ArrayList<>(ids)))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void eliminarMasivo(List<Long> ids) {
        android.app.ProgressDialog pd = new android.app.ProgressDialog(this);
        pd.setMessage("Eliminando " + ids.size() + " imágenes...");
        pd.setCancelable(false);
        pd.show();

        ApiClient.getApiService().eliminarImagenesMasivo(ids).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                pd.dismiss();
                if (response.isSuccessful()) {
                    listaImagenes.removeIf(img -> ids.contains(img.getIdImagen()));
                    actualizarModoSeleccion(0);
                    adapter.notifyDataSetChanged();
                    if (listaImagenes.isEmpty()) {
                        rvGaleriaAdmin.setVisibility(View.GONE);
                        tvGaleriaVacia.setVisibility(View.VISIBLE);
                    }
                    mostrarSnackbarVerde(ids.size() + " imágenes eliminadas");
                } else {
                    Snackbar.make(rvGaleriaAdmin, "Error al eliminar (" + response.code() + ")", Snackbar.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                pd.dismiss();
                Snackbar.make(rvGaleriaAdmin, "Error de red: " + t.getMessage(), Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    // ── Cloudinary ────────────────────────────────────────────────────────────

    private void inicializarCloudinary() {
        try {
            Map<String, String> config = new HashMap<>();
            config.put("cloud_name", BuildConfig.CLOUDINARY_CLOUD_NAME);
            MediaManager.init(this, config);
        } catch (Exception ignored) {}
    }

    private void procesarSubidaMasiva(List<Uri> uris) {
        int total = uris.size();
        android.app.ProgressDialog pd = new android.app.ProgressDialog(this);
        pd.setMessage("Subiendo " + total + " imagen(es) a la nube...");
        pd.setCancelable(false);
        pd.show();

        List<ImagenRequestDTO> dtosListos = Collections.synchronizedList(new ArrayList<>());
        int[] completadas = {0};

        for (Uri uri : uris) {
            MediaManager.get().upload(uri)
                    .unsigned(BuildConfig.CLOUDINARY_UPLOAD_PRESET)
                    .callback(new UploadCallback() {
                        @Override public void onStart(String requestId) {}
                        @Override public void onProgress(String requestId, long bytes, long totalBytes) {}
                        @Override public void onReschedule(String requestId, com.cloudinary.android.callback.ErrorInfo error) {}

                        @Override
                        public void onSuccess(String requestId, Map resultData) {
                            String url = (String) resultData.get("secure_url");
                            ImagenRequestDTO dto = new ImagenRequestDTO();
                            dto.setUrl(url);
                            dtosListos.add(dto);
                            synchronized (completadas) {
                                completadas[0]++;
                                if (completadas[0] == total) {
                                    runOnUiThread(() -> registrarEnBatch(dtosListos, pd));
                                }
                            }
                        }

                        @Override
                        public void onError(String requestId, com.cloudinary.android.callback.ErrorInfo error) {
                            synchronized (completadas) {
                                completadas[0]++;
                                if (completadas[0] == total) {
                                    runOnUiThread(() -> {
                                        if (!dtosListos.isEmpty()) {
                                            registrarEnBatch(dtosListos, pd);
                                        } else {
                                            pd.dismiss();
                                            Snackbar.make(rvGaleriaAdmin, "No se pudo subir ninguna imagen", Snackbar.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }
                        }
                    })
                    .dispatch();
        }
    }

    private void registrarEnBatch(List<ImagenRequestDTO> dtos, android.app.ProgressDialog pd) {
        pd.setMessage("Registrando en la galería...");
        ApiClient.getApiService().registrarImagenesMasivo(new ArrayList<>(dtos)).enqueue(new Callback<List<ImagenGaleriaDTO>>() {
            @Override
            public void onResponse(@NonNull Call<List<ImagenGaleriaDTO>> call, @NonNull Response<List<ImagenGaleriaDTO>> response) {
                pd.dismiss();
                if (response.isSuccessful() && response.body() != null) {
                    List<ImagenGaleriaDTO> listaDevuelta = response.body();
                    listaImagenes.addAll(0, listaDevuelta);
                    adapter.notifyItemRangeInserted(0, listaDevuelta.size());
                    rvGaleriaAdmin.scrollToPosition(0);
                    rvGaleriaAdmin.setVisibility(View.VISIBLE);
                    tvGaleriaVacia.setVisibility(View.GONE);
                    mostrarSnackbarVerde(listaDevuelta.size() + " imagen(es) subidas correctamente");
                } else {
                    Snackbar.make(rvGaleriaAdmin, "Error al registrar imágenes (" + response.code() + ")", Snackbar.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(@NonNull Call<List<ImagenGaleriaDTO>> call, @NonNull Throwable t) {
                pd.dismiss();
                Snackbar.make(rvGaleriaAdmin, "Error de red: " + t.getMessage(), Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    private void subirImagenACloudinary(Uri uri) {
        android.app.ProgressDialog pd = new android.app.ProgressDialog(this);
        pd.setMessage("Subiendo imagen...");
        pd.setCancelable(false);
        pd.show();

        MediaManager.get().upload(uri)
                .unsigned(BuildConfig.CLOUDINARY_UPLOAD_PRESET)
                .callback(new UploadCallback() {
                    @Override public void onStart(String requestId) {}
                    @Override public void onProgress(String requestId, long bytes, long totalBytes) {}
                    @Override public void onReschedule(String requestId, com.cloudinary.android.callback.ErrorInfo error) {}

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        String url = (String) resultData.get("secure_url");
                        ImagenRequestDTO dto = new ImagenRequestDTO();
                        dto.setUrl(url);
                        ApiClient.getApiService().registrarImagenEnGaleria(dto).enqueue(new Callback<ImagenGaleriaDTO>() {
                            @Override
                            public void onResponse(@NonNull Call<ImagenGaleriaDTO> call, @NonNull Response<ImagenGaleriaDTO> response) {
                                pd.dismiss();
                                if (response.isSuccessful() && response.body() != null) {
                                    ImagenGaleriaDTO nuevaImagen = response.body();
                                    listaImagenes.add(0, nuevaImagen);
                                    adapter.notifyItemInserted(0);
                                    rvGaleriaAdmin.scrollToPosition(0);
                                    rvGaleriaAdmin.setVisibility(View.VISIBLE);
                                    tvGaleriaVacia.setVisibility(View.GONE);
                                    mostrarSnackbarVerde("Imagen subida correctamente");
                                } else {
                                    Snackbar.make(rvGaleriaAdmin, "Error al registrar imagen (" + response.code() + ")", Snackbar.LENGTH_SHORT).show();
                                }
                            }
                            @Override
                            public void onFailure(@NonNull Call<ImagenGaleriaDTO> call, @NonNull Throwable t) {
                                pd.dismiss();
                                Snackbar.make(rvGaleriaAdmin, "Error de red: " + t.getMessage(), Snackbar.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onError(String requestId, com.cloudinary.android.callback.ErrorInfo error) {
                        runOnUiThread(() -> {
                            pd.dismiss();
                            Snackbar.make(rvGaleriaAdmin, "Error al subir: " + error.getDescription(), Snackbar.LENGTH_LONG).show();
                        });
                    }
                })
                .dispatch();
    }

    private void mostrarSnackbarVerde(String mensaje) {
        Snackbar snackbar = Snackbar.make(rvGaleriaAdmin, mensaje, Snackbar.LENGTH_SHORT);
        snackbar.getView().setBackgroundColor(android.graphics.Color.parseColor("#388E3C"));
        snackbar.show();
    }

    // ── Adapter ───────────────────────────────────────────────────────────────

    private class GaleriaAdminAdapter extends RecyclerView.Adapter<GaleriaAdminAdapter.VH> {

        interface OnSelectionChangedListener {
            void onSelectionChanged(int count);
        }

        private boolean selectionMode = false;
        private final Set<Long> seleccionadas = new HashSet<>();
        private final OnSelectionChangedListener selectionListener;

        GaleriaAdminAdapter(OnSelectionChangedListener listener) {
            this.selectionListener = listener;
        }

        boolean isSelectionMode() { return selectionMode; }

        Set<Long> getSeleccionadas() { return new HashSet<>(seleccionadas); }

        void toggleSelection(Long id) {
            if (seleccionadas.contains(id)) {
                seleccionadas.remove(id);
            } else {
                seleccionadas.add(id);
            }
            if (seleccionadas.isEmpty()) {
                selectionMode = false;
            }
            selectionListener.onSelectionChanged(seleccionadas.size());
        }

        void clearSelection() {
            seleccionadas.clear();
            selectionMode = false;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull android.view.ViewGroup parent, int viewType) {
            int size = (int) (110 * parent.getContext().getResources().getDisplayMetrics().density);
            ImageView iv = new ImageView(parent.getContext());
            iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
            RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(size, size);
            int margin = (int) (2 * parent.getContext().getResources().getDisplayMetrics().density);
            lp.setMargins(margin, margin, margin, margin);
            iv.setLayoutParams(lp);
            return new VH(iv);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            ImagenGaleriaDTO imagen = listaImagenes.get(position);
            Glide.with(holder.imageView.getContext())
                    .load(imagen.getUrl())
                    .centerCrop()
                    .placeholder(R.drawable.ic_image)
                    .error(R.drawable.ic_image)
                    .into(holder.imageView);

            boolean isSelected = seleccionadas.contains(imagen.getIdImagen());
            if (isSelected) {
                holder.imageView.setColorFilter(
                        new PorterDuffColorFilter(0x990D47A1, PorterDuff.Mode.SRC_ATOP));
            } else {
                holder.imageView.clearColorFilter();
            }

            holder.imageView.setOnClickListener(v -> {
                int pos = holder.getAdapterPosition();
                if (pos == RecyclerView.NO_ID) return;
                ImagenGaleriaDTO img = listaImagenes.get(pos);
                if (selectionMode) {
                    toggleSelection(img.getIdImagen());
                    notifyItemChanged(pos);
                }
            });

            holder.imageView.setOnLongClickListener(v -> {
                int pos = holder.getAdapterPosition();
                if (pos == RecyclerView.NO_ID) return true;
                ImagenGaleriaDTO img = listaImagenes.get(pos);
                if (!selectionMode) {
                    selectionMode = true;
                    seleccionadas.add(img.getIdImagen());
                    selectionListener.onSelectionChanged(seleccionadas.size());
                    notifyDataSetChanged();
                } else {
                    // Pulsación larga en modo selección → eliminar elemento individual
                    new MaterialAlertDialogBuilder(GestionGaleriaActivity.this)
                            .setTitle("¿Eliminar imagen?")
                            .setMessage("Esta imagen se borrará definitivamente de Cloudinary y de la base de datos. Esta acción no se puede deshacer.")
                            .setPositiveButton("Eliminar", (dialog, i) -> borrarImagen(img.getIdImagen(), pos))
                            .setNegativeButton("Cancelar", null)
                            .show();
                }
                return true;
            });
        }

        @Override
        public int getItemCount() { return listaImagenes.size(); }

        class VH extends RecyclerView.ViewHolder {
            final ImageView imageView;
            VH(ImageView iv) {
                super(iv);
                imageView = iv;
            }
        }
    }
}
