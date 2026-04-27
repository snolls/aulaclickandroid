package com.aulaclick.app;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import java.util.Locale;

/**
 * Actividad que maneja el formulario de creación de una nueva reserva.
 * Implementa validaciones en el lado del cliente basadas en las propiedades del recurso,
 * tales como horario de apertura, cierre y permisos para fines de semana.
 * Se eligió utilizar MaterialTimePicker y MaterialDatePicker por coherencia de diseño (Material UI)
 * y para prevenir errores de entrada del usuario.
 */
public class NuevaReservaActivity extends AppCompatActivity {

    private TextInputEditText etHoraInicio, etHoraFin, etFechaReserva, etMotivo;
    private com.google.android.material.textfield.TextInputLayout tilHoraInicio, tilHoraFin, tilFechaReserva;
    private long selectedDateMillis;
    private com.aulaclick.app.network.models.Recurso recursoSeleccionado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nueva_reserva);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        findViewById(R.id.ivClose).setOnClickListener(v -> finish());

        etHoraInicio = findViewById(R.id.etHoraInicio);
        etHoraFin = findViewById(R.id.etHoraFin);
        tilHoraInicio = findViewById(R.id.tilHoraInicio);
        tilHoraFin = findViewById(R.id.tilHoraFin);
        etFechaReserva = findViewById(R.id.etFechaReserva);
        tilFechaReserva = findViewById(R.id.tilFechaReserva);
        etMotivo = findViewById(R.id.etReason);
        
        selectedDateMillis = com.google.android.material.datepicker.MaterialDatePicker.todayInUtcMilliseconds();
        if (etFechaReserva != null) {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());
            sdf.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
            etFechaReserva.setText(sdf.format(new java.util.Date(selectedDateMillis)));
            etFechaReserva.setOnClickListener(v -> showDatePicker());
        }
        MaterialButton btnConfirmar = findViewById(R.id.btnConfirmarReserva);
        MaterialButton btnReservingIndicator = findViewById(R.id.btnReservingIndicator);

        String nombreRecurso = getIntent().getStringExtra("nombre");
        if (nombreRecurso != null) {
            btnReservingIndicator.setText(getString(R.string.label_reserving_prefix, nombreRecurso));
        }

        String recursoJson = getIntent().getStringExtra("recurso_json");
        if (recursoJson != null) {
            recursoSeleccionado = new com.google.gson.Gson().fromJson(recursoJson, com.aulaclick.app.network.models.Recurso.class);
        }

        etHoraInicio.setOnClickListener(v -> showTimePicker(true));
        etHoraFin.setOnClickListener(v -> showTimePicker(false));

        /**
         * Listener para el botón de confirmar.
         * Pasos realizados:
         * 1. Valida que los campos no estén vacíos.
         * 2. Revisa que no haya errores de formato (validación de UI).
         * 3. Comprueba el permiso de fines de semana.
         * 4. Transforma los datos y construye un ReservaRequestDTO para enviar al servidor.
         */
        btnConfirmar.setOnClickListener(v -> {
            String startStr = etHoraInicio.getText() != null ? etHoraInicio.getText().toString() : "";
            String endStr = etHoraFin.getText() != null ? etHoraFin.getText().toString() : "";

            if (startStr.isEmpty() || endStr.isEmpty()) {
                com.google.android.material.snackbar.Snackbar.make(v, "Por favor, selecciona horas de inicio y fin.", com.google.android.material.snackbar.Snackbar.LENGTH_SHORT).show();
                return;
            }

            validarHorasSeleccionadas();
            if ((tilHoraInicio != null && tilHoraInicio.getError() != null) || 
                (tilHoraFin != null && tilHoraFin.getError() != null)) {
                com.google.android.material.snackbar.Snackbar.make(v, "Corrige los errores antes de continuar.", com.google.android.material.snackbar.Snackbar.LENGTH_LONG).show();
                return;
            }

            java.util.Calendar cal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"));
            cal.setTimeInMillis(selectedDateMillis);
            int dayOfWeek = cal.get(java.util.Calendar.DAY_OF_WEEK);
            boolean isWeekend = dayOfWeek == java.util.Calendar.SATURDAY || dayOfWeek == java.util.Calendar.SUNDAY;

            if (isWeekend && recursoSeleccionado != null && Boolean.FALSE.equals(recursoSeleccionado.getPermiteFinesSemana())) {
                com.google.android.material.snackbar.Snackbar.make(v, "El recurso no permite reservas en fines de semana.", com.google.android.material.snackbar.Snackbar.LENGTH_LONG).show();
                return;
            }

            try {
                String[] startParts = startStr.split(":");
                String[] endParts = endStr.split(":");

                int hInicio = Integer.parseInt(startParts[0]);
                int mInicio = Integer.parseInt(startParts[1]);
                int hFin = Integer.parseInt(endParts[0]);
                int mFin = Integer.parseInt(endParts[1]);
                
                com.aulaclick.app.network.models.ReservaRequestDTO dto = new com.aulaclick.app.network.models.ReservaRequestDTO();
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
                sdf.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
                dto.setFecha(sdf.format(new java.util.Date(selectedDateMillis)));

                dto.setHoraInicio(String.format(java.util.Locale.getDefault(), "%02d:%02d:00", hInicio, mInicio));
                dto.setHoraFin(String.format(java.util.Locale.getDefault(), "%02d:%02d:00", hFin, mFin));

                if (recursoSeleccionado != null && recursoSeleccionado.getId() != null) {
                    dto.setIdRecurso(recursoSeleccionado.getId().longValue());
                }

                com.aulaclick.app.utils.SessionManager sm = new com.aulaclick.app.utils.SessionManager(NuevaReservaActivity.this);
                Integer storedUserId = sm.getUserId();
                Long uId = 1L; // Default admin fallback
                if (storedUserId != null && storedUserId != -1) {
                    uId = storedUserId.longValue();
                }
                dto.setIdUsuario(uId);

                String textoMotivo = etMotivo != null && etMotivo.getText() != null ? etMotivo.getText().toString().trim() : "";
                if (!textoMotivo.isEmpty()) {
                    dto.setMotivo(textoMotivo);
                } else {
                    dto.setMotivo(null);
                }

                Toast.makeText(NuevaReservaActivity.this, R.string.msg_validating_server, Toast.LENGTH_SHORT).show();

                android.util.Log.d("DEBUG_CREAR_RESERVA", "Enviando DTO -> RecursoID: " + dto.getIdRecurso()
                        + ", UsuarioID: " + dto.getIdUsuario()
                        + ", Fecha: " + dto.getFecha()
                        + ", Inicio: " + dto.getHoraInicio()
                        + ", Fin: " + dto.getHoraFin());

                com.aulaclick.app.network.ApiClient.getApiService().crearReserva(dto).enqueue(new retrofit2.Callback<com.aulaclick.app.network.models.ReservaDTO>() {
                    @Override
                    public void onResponse(retrofit2.Call<com.aulaclick.app.network.models.ReservaDTO> call, retrofit2.Response<com.aulaclick.app.network.models.ReservaDTO> response) {
                        if (!response.isSuccessful()) {
                            android.util.Log.e("DEBUG_CREAR_RESERVA", "Fallo HTTP Código: " + response.code());
                            try {
                                if (response.errorBody() != null)
                                    android.util.Log.e("DEBUG_CREAR_RESERVA", "Motivo del servidor: " + response.errorBody().string());
                            } catch (Exception ignored) {}
                            String err = "Error desconocido";
                            if (response.errorBody() != null) {
                                try { err = response.errorBody().string(); } catch(Exception ignored) {}
                            }
                            com.google.android.material.snackbar.Snackbar.make(v, "Rechazo: " + err, com.google.android.material.snackbar.Snackbar.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(NuevaReservaActivity.this, "¡Reserva Creada!", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                    @Override
                    public void onFailure(retrofit2.Call<com.aulaclick.app.network.models.ReservaDTO> call, Throwable t) {
                        android.util.Log.e("DEBUG_CREAR_RESERVA", "Excepción de Red o Parseo: ", t);
                        com.google.android.material.snackbar.Snackbar.make(v, "Error de Red: " + t.getMessage(), com.google.android.material.snackbar.Snackbar.LENGTH_LONG).show();
                    }
                });

            } catch (Exception e) {
                com.google.android.material.snackbar.Snackbar.make(v, "Error procesando variables", com.google.android.material.snackbar.Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    private void showDatePicker() {
        java.util.Locale locale = new java.util.Locale("es", "ES");
        java.util.Locale.setDefault(locale);
        android.content.res.Configuration config = new android.content.res.Configuration();
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());

        com.google.android.material.datepicker.CalendarConstraints.Builder constraintsBuilder = new com.google.android.material.datepicker.CalendarConstraints.Builder();
        constraintsBuilder.setValidator(new com.google.android.material.datepicker.CalendarConstraints.DateValidator() {
            @Override
            public boolean isValid(long date) {
                java.util.Calendar hoy = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"));
                hoy.set(java.util.Calendar.HOUR_OF_DAY, 0);
                hoy.set(java.util.Calendar.MINUTE, 0);
                hoy.set(java.util.Calendar.SECOND, 0);
                hoy.set(java.util.Calendar.MILLISECOND, 0);

                if (date < hoy.getTimeInMillis()) {
                    return false; 
                }

                java.util.Calendar cal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"));
                cal.setTimeInMillis(date);
                int dayOfWeek = cal.get(java.util.Calendar.DAY_OF_WEEK);
                boolean isWeekend = (dayOfWeek == java.util.Calendar.SATURDAY || dayOfWeek == java.util.Calendar.SUNDAY);

                if (isWeekend && (recursoSeleccionado == null || Boolean.FALSE.equals(recursoSeleccionado.getPermiteFinesSemana()))) {
                    return false;
                }

                return true;
            }
            
            @Override
            public int describeContents() { return 0; }
            @Override
            public void writeToParcel(android.os.Parcel dest, int flags) {}
        });

        com.google.android.material.datepicker.MaterialDatePicker<Long> datePicker = com.google.android.material.datepicker.MaterialDatePicker.Builder.datePicker()
                .setTitleText("Selecciona una fecha")
                .setCalendarConstraints(constraintsBuilder.build())
                .setSelection(selectedDateMillis)
                .build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            selectedDateMillis = selection;
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());
            sdf.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
            if (etFechaReserva != null) {
                etFechaReserva.setText(sdf.format(new java.util.Date(selection)));
            }
            validarHorasSeleccionadas();
        });

        datePicker.show(getSupportFragmentManager(), "DATE_PICKER");
    }

    private void showTimePicker(boolean isInicio) {
        MaterialTimePicker picker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(12)
                .setMinute(0)
                .setTitleText(R.string.title_select_time)
                .build();

        picker.addOnPositiveButtonClickListener(v -> {
            String time = String.format(Locale.getDefault(), "%02d:%02d", picker.getHour(), picker.getMinute());
            if (isInicio) {
                etHoraInicio.setText(time);
            } else {
                etHoraFin.setText(time);
            }
            validarHorasSeleccionadas();
        });

        picker.show(getSupportFragmentManager(), "TIME_PICKER");
    }

    /**
     * Valida dinámicamente las horas seleccionadas en los campos de entrada frente
     * a las restricciones del recurso (apertura/cierre, horarios pasados).
     * Muestra los errores utilizando `TextInputLayout` para un feedback visual inmediato.
     */
    private void validarHorasSeleccionadas() {
        if (tilHoraInicio == null || tilHoraFin == null || recursoSeleccionado == null) return;
        tilHoraInicio.setError(null);
        tilHoraFin.setError(null);

        String startStr = etHoraInicio.getText() != null ? etHoraInicio.getText().toString() : "";
        String endStr = etHoraFin.getText() != null ? etHoraFin.getText().toString() : "";

        if (startStr.isEmpty()) return;

        try {
            java.time.LocalTime horaInicioSeleccionada = java.time.LocalTime.parse(startStr);
            
            // 1. Validar si es hoy y hora antigua
            java.util.Calendar cNow = java.util.Calendar.getInstance();
            java.util.Calendar cSel = java.util.Calendar.getInstance();
            cSel.setTimeInMillis(selectedDateMillis);
            boolean fechaSeleccionadaEsHoy = cNow.get(java.util.Calendar.YEAR) == cSel.get(java.util.Calendar.YEAR) &&
                                             cNow.get(java.util.Calendar.DAY_OF_YEAR) == cSel.get(java.util.Calendar.DAY_OF_YEAR);
            
            if (fechaSeleccionadaEsHoy && horaInicioSeleccionada.isBefore(java.time.LocalTime.now())) {
                tilHoraInicio.setError("No puedes reservar en el pasado");
            }

            // 2. Validar Apertura
            if (recursoSeleccionado.getHoraApertura() != null && !recursoSeleccionado.getHoraApertura().isEmpty()) {
                java.time.LocalTime apertura = java.time.LocalTime.parse(recursoSeleccionado.getHoraApertura());
                if (horaInicioSeleccionada.isBefore(apertura)) {
                    tilHoraInicio.setError("La sala abre a las " + recursoSeleccionado.getHoraApertura());
                }
            }

            if (!endStr.isEmpty()) {
                java.time.LocalTime horaFinSeleccionada = java.time.LocalTime.parse(endStr);
                
                // Cierre
                if (recursoSeleccionado.getHoraCierre() != null && !recursoSeleccionado.getHoraCierre().isEmpty()) {
                    java.time.LocalTime cierre = java.time.LocalTime.parse(recursoSeleccionado.getHoraCierre());
                    if (horaFinSeleccionada.isAfter(cierre)) {
                        tilHoraFin.setError("La sala cierra a las " + recursoSeleccionado.getHoraCierre());
                    }
                }

                // Coherencia
                if (horaInicioSeleccionada.isAfter(horaFinSeleccionada) || horaInicioSeleccionada.equals(horaFinSeleccionada)) {
                    tilHoraFin.setError("La hora de fin debe ser posterior al inicio");
                }
            }
        } catch (Exception e) {
            // Ignorar errores parciales de parseo
        }
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // Vuelve a la pantalla anterior
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
