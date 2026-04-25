package com.aulaclick.app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.Arrays;
import java.util.List;

public class AyudaActivity extends AppCompatActivity {

    private static final String[][] FAQ = {
            {
                    "¿Cómo hago una reserva?",
                    "Desde la pantalla principal, pulsa en cualquier recurso para ver sus detalles. Toca el botón de calendario, elige la fecha y el horario disponible y confirma tu reserva."
            },
            {
                    "¿Puedo cancelar una reserva?",
                    "Sí. Ve a 'Mis Reservas', localiza la reserva que deseas cancelar en la pestaña ACTIVAS y pulsa 'Cancelar'. La cancelación es inmediata."
            },
            {
                    "¿Qué tipos de recursos puedo reservar?",
                    "Puedes reservar aulas, laboratorios, salas de reuniones, equipamiento audiovisual y cualquier otro recurso que el centro tenga configurado en la plataforma."
            },
            {
                    "¿Con cuánta antelación puedo reservar?",
                    "Puedes reservar cualquier día desde hoy hacia el futuro, siempre que el recurso esté disponible en ese horario."
            },
            {
                    "¿Qué pasa si el horario que quiero ya está ocupado?",
                    "El sistema te avisará al intentar confirmar la reserva. Elige otro horario o consulta la disponibilidad del recurso en el calendario de detalles."
            },
            {
                    "¿Se pueden hacer reservas en fin de semana?",
                    "Depende del recurso. Cada recurso tiene configurado si permite o no reservas en fines de semana. Si no está disponible, el sistema te lo indicará."
            },
            {
                    "¿Dónde veo mis reservas pasadas?",
                    "En 'Mis Reservas', pestaña HISTORIAL. Ahí encontrarás todas las reservas finalizadas."
            },
            {
                    "¿Qué significa cada estado de reserva?",
                    "• ACTIVA: confirmada y pendiente de realizar.\n• EN CURSO: el recurso está siendo utilizado en este momento.\n• EMPIEZA PRONTO: comienza en menos de 30 minutos.\n• FINALIZADA: ya ha concluido.\n• CANCELADA: fue cancelada por ti o por un administrador."
            },
            {
                    "¿Cómo contacto con el administrador?",
                    "Si tienes algún problema técnico o necesitas ayuda, contacta con el administrador de tu sede a través de los canales que tu institución haya habilitado."
            }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ayuda);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        RecyclerView rv = findViewById(R.id.rvFaq);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(new FaqAdapter(Arrays.asList(FAQ)));
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) { finish(); return true; }
        return super.onOptionsItemSelected(item);
    }

    private static class FaqAdapter extends RecyclerView.Adapter<FaqAdapter.FaqVH> {

        private final List<String[]> items;
        private final boolean[] expandido;

        FaqAdapter(List<String[]> items) {
            this.items = items;
            this.expandido = new boolean[items.size()];
        }

        @NonNull
        @Override
        public FaqVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_faq, parent, false);
            return new FaqVH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull FaqVH h, int position) {
            String[] faq = items.get(position);
            h.tvPregunta.setText(faq[0]);
            h.tvRespuesta.setText(faq[1]);

            boolean estaExpandido = expandido[position];
            h.tvRespuesta.setVisibility(estaExpandido ? View.VISIBLE : View.GONE);
            h.ivChevron.setRotation(estaExpandido ? 90f : -90f);

            h.header.setOnClickListener(v -> {
                boolean nuevoEstado = !expandido[position];
                expandido[position] = nuevoEstado;

                RotateAnimation rotate = new RotateAnimation(
                        nuevoEstado ? -90f : 90f,
                        nuevoEstado ? 90f : -90f,
                        Animation.RELATIVE_TO_SELF, 0.5f,
                        Animation.RELATIVE_TO_SELF, 0.5f);
                rotate.setDuration(200);
                rotate.setFillAfter(true);
                h.ivChevron.startAnimation(rotate);

                h.tvRespuesta.setVisibility(nuevoEstado ? View.VISIBLE : View.GONE);
            });
        }

        @Override
        public int getItemCount() { return items.size(); }

        static class FaqVH extends RecyclerView.ViewHolder {
            LinearLayout header;
            TextView tvPregunta, tvRespuesta;
            ImageView ivChevron;
            FaqVH(@NonNull View v) {
                super(v);
                header      = v.findViewById(R.id.headerFaq);
                tvPregunta  = v.findViewById(R.id.tvPregunta);
                tvRespuesta = v.findViewById(R.id.tvRespuesta);
                ivChevron   = v.findViewById(R.id.ivChevron);
            }
        }
    }
}
