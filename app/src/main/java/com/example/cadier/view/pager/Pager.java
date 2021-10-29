package com.example.cadier.view.pager;


import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

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
                TabScheduled tabScheduled = new TabScheduled();
                return tabScheduled;
            case 1:
                TabPrevious tabPrevious = new TabPrevious();
                return tabPrevious;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return tabCount;
    }
}