package com.example.cadier.view.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.cadier.R;

/**
 * Created by DrGreend on 07/03/2018.
 */

public class FragmentContatos extends Fragment {
    TextView textViewLink;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contatos, container, false);

        textViewLink = view.findViewById(R.id.textViewLink);

        textViewLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent viewIntent =
                        new Intent("android.intent.action.VIEW",
                                Uri.parse("http://cadier.yolasite.com"));
                startActivity(viewIntent);
            }
        });

        return view;
    }
}