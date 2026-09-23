package app.convencao.cadier.view.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.fragment.app.Fragment;

import app.convencao.cadier.R;
import app.convencao.cadier.modelo.User;
import app.convencao.cadier.view.activity.AddressEditActivity;
import app.convencao.cadier.view.activity.MenuActivity;
import app.convencao.cadier.view.activity.ProfileEditActivity;

/**
 * Created by DrGreend on 18/03/2018.
 */

public class FragmentConfigurations extends Fragment {
    private static final int REQUEST_EDITAR_PERFIL = 1;
    private static final int REQUEST_EDITAR_ENDERECO = 2;

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
                // Trava no primeiro toque - duplo toque abriria a tela de edição duas vezes
                // empilhadas (ver mesmo comentário em FragmentProfile).
                view.setEnabled(false);
                startActivityForResult(new Intent(getContext(), ProfileEditActivity.class).putExtra("usuario", user), REQUEST_EDITAR_PERFIL);
            }
        });

        linearLayoutEnderecoEditar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.setEnabled(false);
                startActivityForResult(new Intent(getContext(), AddressEditActivity.class).putExtra("usuario", user), REQUEST_EDITAR_ENDERECO);
            }
        });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // ProfileEditActivity/AddressEditActivity devolvem o User atualizado - repassa pro
        // MenuActivity, que é quem guarda a cópia "oficial" reaproveitada nas outras telas (ver
        // comentário em MenuActivity.updateUser; sem isso, editar e voltar mostrava dados antigos).
        if (requestCode == REQUEST_EDITAR_PERFIL) {
            linearLayoutEditarDados.setEnabled(true);
        } else if (requestCode == REQUEST_EDITAR_ENDERECO) {
            linearLayoutEnderecoEditar.setEnabled(true);
        }

        if ((requestCode == REQUEST_EDITAR_PERFIL || requestCode == REQUEST_EDITAR_ENDERECO)
                && resultCode == android.app.Activity.RESULT_OK && data != null) {
            User usuarioAtualizado = (User) data.getSerializableExtra("usuario");
            if (usuarioAtualizado != null) {
                user = usuarioAtualizado;
                ((MenuActivity) getActivity()).updateUser(usuarioAtualizado);
            }
        }
    }
}
