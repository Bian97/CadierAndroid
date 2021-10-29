package com.example.cadier.view.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.example.cadier.R;
import com.example.cadier.modelo.User;
import com.github.chrisbanes.photoview.PhotoView;
import com.squareup.picasso.Picasso;

/**
 * Created by DrGreend on 07/03/2018.
 */

public class FragmentCalendar extends Fragment {
    PhotoView calendario;
    User user;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        user = (User) getActivity().getIntent().getSerializableExtra("usuario");
        View view = inflater.inflate(R.layout.fragment_calendario, container, false);

        calendario = view.findViewById(R.id.imageViewCalendario);
        String link = "http://cadier.com.br/CalendarioCadier/CalendarioCadier.png";
        Picasso.get().load(link).into(calendario);

        return view;
    }
}