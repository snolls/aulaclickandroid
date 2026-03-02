package com.aulaclick.app;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import java.util.Locale;

public class NuevaReservaActivity extends AppCompatActivity {

    private TextInputEditText etHoraInicio, etHoraFin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nueva_reserva);

        findViewById(R.id.ivClose).setOnClickListener(v -> finish());

        etHoraInicio = findViewById(R.id.etHoraInicio);
        etHoraFin = findViewById(R.id.etHoraFin);
        MaterialButton btnConfirmar = findViewById(R.id.btnConfirmarReserva);

        etHoraInicio.setOnClickListener(v -> showTimePicker(true));
        etHoraFin.setOnClickListener(v -> showTimePicker(false));

        btnConfirmar.setOnClickListener(v -> {
            Toast.makeText(this, "Validando con el servidor...", Toast.LENGTH_SHORT).show();
            v.postDelayed(this::finish, 1500);
        });
    }

    private void showTimePicker(boolean isInicio) {
        MaterialTimePicker picker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(12)
                .setMinute(0)
                .setTitleText("Selecciona la hora")
                .build();

        picker.addOnPositiveButtonClickListener(v -> {
            String time = String.format(Locale.getDefault(), "%02d:%02d", picker.getHour(), picker.getMinute());
            if (isInicio) {
                etHoraInicio.setText(time);
            } else {
                etHoraFin.setText(time);
            }
        });

        picker.show(getSupportFragmentManager(), "TIME_PICKER");
    }
}
