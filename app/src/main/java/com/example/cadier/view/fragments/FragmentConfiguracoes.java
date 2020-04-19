package com.example.cadier.view.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.example.cadier.R;
import com.example.cadier.modelo.Usuario;
import com.example.cadier.view.activity.EditarEnderecoActivity;
import com.example.cadier.view.activity.EditarPerfilActivity;

/**
 * Created by DrGreend on 18/03/2018.
 */

public class FragmentConfiguracoes extends Fragment {
    LinearLayout linearLayoutEditarDados, linearLayoutEnderecoEditar;
    Usuario usuario;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        usuario = (Usuario) getActivity().getIntent().getSerializableExtra("usuario");
        View view = inflater.inflate(R.layout.fragment_configuracoes, container, false);

        linearLayoutEditarDados = view.findViewById(R.id.linearLayoutEditarDados);
        linearLayoutEnderecoEditar = view.findViewById(R.id.linearLayoutEnderecoEditar);

        linearLayoutEditarDados.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(), EditarPerfilActivity.class).putExtra("usuario", usuario));
            }
        });

        linearLayoutEnderecoEditar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(), EditarEnderecoActivity.class).putExtra("usuario", usuario));
            }
        });

        return view;
    }
}
