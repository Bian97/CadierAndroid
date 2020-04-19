package com.example.cadier.view.pager;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 * Created by DrGreend on 24/03/2018.
 */

public class Pager extends FragmentStatePagerAdapter {

    int tabCount = 0;

    public Pager(FragmentManager fm, int tabCount) {
        super(fm);
        this.tabCount = tabCount;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                TabAgendados tabAgendados = new TabAgendados();
                return tabAgendados;
            case 1:
                TabAnteriores tabAnteriores = new TabAnteriores();
                return tabAnteriores;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return tabCount;
    }
}