package app.convencao.cadier.view.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import app.convencao.cadier.R;

/**
 * Created by DrGreend on 07/03/2018.
 */

public class FragmentHelp extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ajuda, container, false);

        return view;
    }
}