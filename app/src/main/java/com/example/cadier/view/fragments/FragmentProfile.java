package com.example.cadier.view.fragments;

import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.cadier.R;
import com.example.cadier.modelo.User;
import com.example.cadier.util.Enums.StatusEnum;

import java.text.SimpleDateFormat;

/**
 * Created by DrGreend on 07/03/2018.
 */

public class FragmentProfile extends Fragment {
    User user;
    ImageView imageViewAnnounce, imageViewProfile;
    TextView textViewProfileName, textViewIdProfile, textViewAddressProfile,
            textViewChurch, textViewCheckDate, textViewStatus;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        user = (User) getActivity().getIntent().getSerializableExtra("usuario");
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
        //Log.e("XAMPSONDENSITY", String.valueOf(densityDpi));

        imageViewAnnounce = view.findViewById(R.id.imageViewAnuncio);
        imageViewProfile = view.findViewById(R.id.imageViewPerfil);
        textViewProfileName = view.findViewById(R.id.textViewNomePerfil);
        textViewIdProfile = view.findViewById(R.id.textViewRolPerfil);
        textViewAddressProfile = view.findViewById(R.id.textViewEnderecoPerfil);
        textViewChurch = view.findViewById(R.id.textViewIgreja);
        textViewCheckDate = view.findViewById(R.id.textViewDataPresente);
        textViewStatus = view.findViewById(R.id.textViewStatus);

        textViewProfileName.setText(user.getName());
        textViewIdProfile.setText(String.valueOf(user.getPhysicalId()));
        if(user.getStreet() != null && user.getCity() != null && user.getCode() != null) {
            textViewAddressProfile.setText((user.getStreet() != null ? user.getStreet() + ". " : "") + (user.getCity() != null ? user.getCity() + ". " : "") + (user.getCode() != null ? user.getCode() : ""));
        } else {
            textViewAddressProfile.setText("Não há endereço cadastrado!");
        }
        textViewChurch.setText(user.getChurch() != null && user.getChurch() != "" ? user.getChurch() : "Não pertence à nenhuma igreja");
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        textViewCheckDate.setText("Última Reunião: " + sdf.format(user.getLastVisit()));
        textViewStatus.setText(user.getStatus().toString());
        Bitmap aux = BitmapFactory.decodeFile(user.getPhoto());
        imageViewProfile.setImageBitmap(aux);

        return view;
    }
}