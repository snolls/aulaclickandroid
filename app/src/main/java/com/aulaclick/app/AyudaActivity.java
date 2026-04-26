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

    private String[][] buildFaq() {
        return new String[][]{
                {getString(R.string.faq_q1), getString(R.string.faq_a1)},
                {getString(R.string.faq_q2), getString(R.string.faq_a2)},
                {getString(R.string.faq_q3), getString(R.string.faq_a3)},
                {getString(R.string.faq_q4), getString(R.string.faq_a4)},
                {getString(R.string.faq_q5), getString(R.string.faq_a5)},
                {getString(R.string.faq_q6), getString(R.string.faq_a6)},
                {getString(R.string.faq_q7), getString(R.string.faq_a7)},
                {getString(R.string.faq_q8), getString(R.string.faq_a8)},
                {getString(R.string.faq_q9), getString(R.string.faq_a9)}
        };
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ayuda);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        RecyclerView rv = findViewById(R.id.rvFaq);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(new FaqAdapter(Arrays.asList(buildFaq())));
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
