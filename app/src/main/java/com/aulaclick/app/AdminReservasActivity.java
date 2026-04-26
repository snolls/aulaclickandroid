package com.aulaclick.app;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class AdminReservasActivity extends AppCompatActivity {

    public static final String EXTRA_TAB     = "tab_index";
    public static final String EXTRA_SEDE_ID = "sede_id";
    public static final int TAB_ACTIVAS    = 0;
    public static final int TAB_HISTORIAL  = 1;
    public static final int TAB_CANCELADAS = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_reservas);

        int tabInicial   = getIntent().getIntExtra(EXTRA_TAB, TAB_ACTIVAS);
        long sedeIdRaw   = getIntent().getLongExtra(EXTRA_SEDE_ID, -1L);
        Long sedeId      = sedeIdRaw == -1L ? null : sedeIdRaw;

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(tabInicial == TAB_CANCELADAS
                ? getString(R.string.title_reservas_canceladas)
                : getString(R.string.title_reservas_activas));
        toolbar.setNavigationOnClickListener(v -> finish());

        ViewPager2 viewPager = findViewById(R.id.viewPager);
        viewPager.setAdapter(new AdminReservasPagerAdapter(this, sedeId));

        TabLayout tabLayout = findViewById(R.id.tabLayout);
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0: tab.setText(R.string.tab_activas);    break;
                case 1: tab.setText(R.string.tab_historial);  break;
                case 2: tab.setText(R.string.tab_canceladas); break;
            }
        }).attach();

        viewPager.setCurrentItem(tabInicial, false);

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_perfil);
            bottomNav.setOnItemSelectedListener(item -> {
                int id = item.getItemId();
                if (id == R.id.nav_recursos) {
                    Intent intent = new Intent(this, DashboardActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    return true;
                } else if (id == R.id.nav_reservas) {
                    Intent intent = new Intent(this, MisReservasActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    return true;
                } else if (id == R.id.nav_perfil) {
                    return true;
                }
                return false;
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        if (bottomNav != null) bottomNav.getMenu().findItem(R.id.nav_perfil).setChecked(true);
    }

    private static class AdminReservasPagerAdapter extends FragmentStateAdapter {
        private final Long sedeId;

        AdminReservasPagerAdapter(@NonNull FragmentActivity fa, Long sedeId) {
            super(fa);
            this.sedeId = sedeId;
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 1:  return AdminListaReservasFragment.newInstance(AdminListaReservasFragment.ESTADO_HISTORIAL,  sedeId);
                case 2:  return AdminListaReservasFragment.newInstance(AdminListaReservasFragment.ESTADO_CANCELADAS, sedeId);
                default: return AdminListaReservasFragment.newInstance(AdminListaReservasFragment.ESTADO_ACTIVAS,    sedeId);
            }
        }

        @Override
        public int getItemCount() { return 3; }
    }
}
