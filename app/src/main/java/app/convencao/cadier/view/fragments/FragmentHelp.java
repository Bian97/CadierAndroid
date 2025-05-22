package app.convencao.cadier.view.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

import app.convencao.cadier.R;

/**
 * Created by DrGreend on 07/03/2018.
 */

public class FragmentHelp extends Fragment {
    Button buttonPolitica;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ajuda, container, false);
        buttonPolitica = view.findViewById(R.id.buttonPolitica);

        buttonPolitica.setOnClickListener(v -> {
            Intent viewIntent =
                    new Intent("android.intent.action.VIEW",
                            Uri.parse("https://cadier.com.br/politicaprivacidade.html"));
            startActivity(viewIntent);
        });
        return view;
    }
}