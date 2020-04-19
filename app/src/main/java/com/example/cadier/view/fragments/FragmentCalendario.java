package com.example.cadier.view.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.cadier.R;
import com.example.cadier.modelo.Usuario;
import com.example.cadier.view.activity.MenuActivity;
import com.github.chrisbanes.photoview.PhotoView;
import com.github.chrisbanes.photoview.PhotoViewAttacher;
import com.squareup.picasso.Picasso;

/**
 * Created by DrGreend on 07/03/2018.
 */

public class FragmentCalendario extends Fragment {
    PhotoView calendario;
    Usuario usuario;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        usuario = (Usuario) getActivity().getIntent().getSerializableExtra("usuario");
        View view = inflater.inflate(R.layout.fragment_calendario, container, false);

        calendario = view.findViewById(R.id.imageViewCalendario);
        String link = "http://cadier.com.br/CalendarioCadier/CalendarioCadier.png";
        Picasso.get().load(link).into(calendario);

        return view;
    }
}