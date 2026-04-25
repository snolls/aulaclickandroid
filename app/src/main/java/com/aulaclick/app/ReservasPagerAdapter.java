package com.aulaclick.app;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ReservasPagerAdapter extends FragmentStateAdapter {

    public ReservasPagerAdapter(@NonNull FragmentActivity fa) {
        super(fa);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 1:  return ListaReservasFragment.newInstance(ListaReservasFragment.ESTADO_HISTORIAL);
            case 2:  return ListaReservasFragment.newInstance(ListaReservasFragment.ESTADO_CANCELADAS);
            default: return ListaReservasFragment.newInstance(ListaReservasFragment.ESTADO_ACTIVAS);
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
