package com.aulaclick.app;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class MisReservasActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mis_reservas);

        findViewById(R.id.ivBack).setOnClickListener(v -> finish());

        RecyclerView rvMisReservas = findViewById(R.id.rvMisReservas);
        rvMisReservas.setLayoutManager(new LinearLayoutManager(this));

        List<Reserva> listaPrueba = new ArrayList<>();
        listaPrueba.add(new Reserva("Aula de Informática 3", "15 Oct, 2024", "09:00 - 11:00", "Confirmada"));
        listaPrueba.add(new Reserva("Laboratorio de Química", "18 Oct, 2024", "12:00 - 14:00", "Pendiente"));
        listaPrueba.add(new Reserva("Sala de Conferencias", "20 Oct, 2024", "10:00 - 12:00", "Confirmada"));

        MisReservasAdapter adapter = new MisReservasAdapter(listaPrueba);
        rvMisReservas.setAdapter(adapter);
    }
}
