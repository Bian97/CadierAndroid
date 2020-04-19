package com.example.cadier.view.fragments;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.cadier.R;
import com.example.cadier.modelo.Usuario;
import com.example.cadier.view.activity.MenuActivity;

/**
 * Created by DrGreend on 07/03/2018.
 */

public class FragmentPerfil extends Fragment {
    Usuario usuario;
    ImageView imageViewAnuncio, imageViewPerfil, imageViewMenu;
    TextView textViewNomePerfil, textViewRolPerfil, textViewEnderecoPerfil,
            textViewIgreja, textViewDataPresente, textViewStatus;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        usuario = (Usuario) getActivity().getIntent().getSerializableExtra("usuario");
        View view = inflater.inflate(R.layout.fragment_perfil, container, false);

        if ((getResources().getConfiguration().screenLayout &      Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE) {
            Log.e("XAMPSON", "Large screen");
        }
        else if ((getResources().getConfiguration().screenLayout &      Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_NORMAL) {
            Log.e("XAMPSON", "Normal screen");
        }
        else if ((getResources().getConfiguration().screenLayout &      Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_SMALL) {
            Log.e("XAMPSON", "Small screen");
        }
        else {
            Log.e("XAMPSON", "Nenhuma das screens");
        }

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int densityDpi = (int)(metrics.density * 160f);
        Log.e("XAMPSONDENSITY", String.valueOf(densityDpi));

        imageViewAnuncio = view.findViewById(R.id.imageViewAnuncio);
        imageViewPerfil = view.findViewById(R.id.imageViewPerfil);
        textViewNomePerfil = view.findViewById(R.id.textViewNomePerfil);
        textViewRolPerfil = view.findViewById(R.id.textViewRolPerfil);
        textViewEnderecoPerfil = view.findViewById(R.id.textViewEnderecoPerfil);
        textViewIgreja = view.findViewById(R.id.textViewIgreja);
        textViewDataPresente = view.findViewById(R.id.textViewDataPresente);
        textViewStatus = view.findViewById(R.id.textViewStatus);

        textViewNomePerfil.setText(usuario.getNome());
        textViewRolPerfil.setText(String.valueOf(usuario.getRol()));
        textViewEnderecoPerfil.setText(usuario.getRua() + ". " + usuario.getCidade() + ". " + usuario.getCep());
        textViewIgreja.setText(usuario.getIgreja());
        textViewDataPresente.setText("Última Reunião: "+usuario.getUltViz());
        textViewStatus.setText(usuario.getStatus());
        Bitmap aux = BitmapFactory.decodeFile(usuario.getFoto());
        imageViewPerfil.setImageBitmap(aux);

        return view;
    }
}