package com.example.cadier.view.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.fragment.app.Fragment;

import com.example.cadier.R;
import com.example.cadier.modelo.User;
import com.example.cadier.view.activity.AddressEditActivity;
import com.example.cadier.view.activity.ProfileEditActivity;

/**
 * Created by DrGreend on 18/03/2018.
 */

public class FragmentConfigurations extends Fragment {
    LinearLayout linearLayoutEditarDados, linearLayoutEnderecoEditar;
    User user;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        user = (User) getActivity().getIntent().getSerializableExtra("usuario");
        View view = inflater.inflate(R.layout.fragment_configuracoes, container, false);

        linearLayoutEditarDados = view.findViewById(R.id.linearLayoutEditarDados);
        linearLayoutEnderecoEditar = view.findViewById(R.id.linearLayoutEnderecoEditar);

        linearLayoutEditarDados.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(), ProfileEditActivity.class).putExtra("usuario", user));
            }
        });

        linearLayoutEnderecoEditar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(), AddressEditActivity.class).putExtra("usuario", user));
            }
        });

        return view;
    }
}
