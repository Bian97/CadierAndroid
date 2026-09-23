package app.convencao.cadier.view.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import app.convencao.cadier.R;
import app.convencao.cadier.util.WhatsApp;

/**
 * Created by DrGreend on 07/03/2018.
 */

/**
 * Canais de contato - mesmos do "Fale Conosco" do site (src/components/FaleConosco.js): WhatsApp
 * da Secretaria como canal principal, e-mails de Secretaria/Ouvidoria e Facebook como canais
 * complementares. Os telefones fixos que essa tela mostrava antes (Ouvidoria/Secretaria, sistema
 * antigo) foram substituídos por esses canais oficiais atuais.
 */
public class FragmentContacts extends Fragment {
    private static final String EMAIL_SECRETARIA = "atendimentocadier@gmail.com";
    private static final String EMAIL_OUVIDORIA = "jjrio2015@gmail.com";
    private static final String URL_FACEBOOK = "https://www.facebook.com/cadier.convencaoassembleiadedeus";

    TextView textViewLink, textViewEmailSecretaria, textViewEmailOuvidoria, textViewFacebook;
    Button buttonWhatsappSecretaria;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contatos, container, false);

        textViewLink = view.findViewById(R.id.textViewLink);
        textViewEmailSecretaria = view.findViewById(R.id.textViewEmailSecretaria);
        textViewEmailOuvidoria = view.findViewById(R.id.textViewEmailOuvidoria);
        textViewFacebook = view.findViewById(R.id.textViewFacebook);
        buttonWhatsappSecretaria = view.findViewById(R.id.buttonWhatsappSecretaria);

        // Clique na linha inteira (ícone + texto), não só no texto - alvo de toque maior.
        view.findViewById(R.id.layoutSite).setOnClickListener(v -> abrirUrl("https://cadier.com.br"));
        view.findViewById(R.id.layoutFacebook).setOnClickListener(v -> abrirUrl(URL_FACEBOOK));
        view.findViewById(R.id.layoutEmailSecretaria).setOnClickListener(v -> enviarEmail(EMAIL_SECRETARIA));
        view.findViewById(R.id.layoutEmailOuvidoria).setOnClickListener(v -> enviarEmail(EMAIL_OUVIDORIA));
        buttonWhatsappSecretaria.setOnClickListener(v -> WhatsApp.abrirChatSecretaria(getContext(), null));

        return view;
    }

    private void abrirUrl(String url) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
    }

    private void enviarEmail(String endereco) {
        startActivity(new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + endereco)));
    }
}