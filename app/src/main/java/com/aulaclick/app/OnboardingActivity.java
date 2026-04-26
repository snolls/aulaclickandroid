package com.aulaclick.app;

import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.aulaclick.app.utils.SessionManager;
import com.google.android.material.button.MaterialButton;

public class OnboardingActivity extends AppCompatActivity {

    private static final int[] ICONS = {
            R.drawable.ic_school,
            R.drawable.ic_dashboard,
            R.drawable.ic_calendar,
            R.drawable.ic_person
    };
    private String[] TITLES;
    private String[] DESCS;

    private ViewPager2 viewPager;
    private LinearLayout dotsContainer;
    private MaterialButton btnSiguiente;
    private TextView btnSaltar;
    private int totalSlides;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        TITLES = new String[]{
                getString(R.string.onboarding_title_1),
                getString(R.string.onboarding_title_2),
                getString(R.string.onboarding_title_3),
                getString(R.string.onboarding_title_4)
        };
        DESCS = new String[]{
                getString(R.string.onboarding_desc_1),
                getString(R.string.onboarding_desc_2),
                getString(R.string.onboarding_desc_3),
                getString(R.string.onboarding_desc_4)
        };
        totalSlides = TITLES.length;
        viewPager    = findViewById(R.id.viewPagerOnboarding);
        dotsContainer = findViewById(R.id.dotsContainer);
        btnSiguiente  = findViewById(R.id.btnSiguiente);
        btnSaltar     = findViewById(R.id.btnSaltar);

        viewPager.setAdapter(new SlideAdapter());

        crearDots(0);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                crearDots(position);
                boolean esUltima = position == totalSlides - 1;
                btnSiguiente.setText(esUltima ? getString(R.string.onboarding_start) : getString(R.string.onboarding_next));
                btnSaltar.setVisibility(esUltima ? View.INVISIBLE : View.VISIBLE);
            }
        });

        btnSiguiente.setOnClickListener(v -> {
            int actual = viewPager.getCurrentItem();
            if (actual < totalSlides - 1) {
                viewPager.setCurrentItem(actual + 1);
            } else {
                terminarOnboarding();
            }
        });

        btnSaltar.setOnClickListener(v -> terminarOnboarding());
    }

    private void terminarOnboarding() {
        new SessionManager(this).setOnboardingDone();
        startActivity(new Intent(this, DashboardActivity.class));
        finish();
    }

    private void crearDots(int posicionActual) {
        dotsContainer.removeAllViews();
        int dotSize  = (int) (8 * getResources().getDisplayMetrics().density);
        int dotMargin = (int) (4 * getResources().getDisplayMetrics().density);

        for (int i = 0; i < totalSlides; i++) {
            View dot = new View(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dotSize, dotSize);
            params.setMargins(dotMargin, 0, dotMargin, 0);
            dot.setLayoutParams(params);

            GradientDrawable shape = new GradientDrawable();
            shape.setShape(GradientDrawable.OVAL);
            shape.setColor(ContextCompat.getColor(this,
                    i == posicionActual ? R.color.colorPrimary : R.color.colorTextSecondary));
            dot.setBackground(shape);
            dotsContainer.addView(dot);
        }
    }

    private class SlideAdapter extends RecyclerView.Adapter<SlideAdapter.SlideVH> {

        @NonNull
        @Override
        public SlideVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_onboarding_slide, parent, false);
            return new SlideVH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull SlideVH h, int position) {
            h.icon.setImageResource(ICONS[position]);
            h.title.setText(TITLES[position]);
            h.desc.setText(DESCS[position]);
        }

        @Override
        public int getItemCount() { return totalSlides; }

        class SlideVH extends RecyclerView.ViewHolder {
            ImageView icon;
            TextView title, desc;
            SlideVH(@NonNull View v) {
                super(v);
                icon  = v.findViewById(R.id.ivSlideIcon);
                title = v.findViewById(R.id.tvSlideTitle);
                desc  = v.findViewById(R.id.tvSlideDesc);
            }
        }
    }
}
