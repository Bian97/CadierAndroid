package app.convencao.cadier.view.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import app.convencao.cadier.R;
import app.convencao.cadier.modelo.User;
import app.convencao.cadier.util.Enums.StatusEnum;
import app.convencao.cadier.view.activity.MenuActivity;
import app.convencao.cadier.view.activity.ProfileEditActivity;

import java.text.SimpleDateFormat;

/**
 * Created by DrGreend on 07/03/2018.
 */

public class FragmentProfile extends Fragment {
    private static final int REQUEST_EDITAR_PERFIL = 1;

    User user;
    ImageView imageViewProfile;
    TextView textViewProfileName, textViewIdProfile, textViewAddressProfile,
            textViewChurch, textViewCheckDate, textViewStatus;
    Button buttonEditarPerfilAtalho;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        user = (User) getActivity().getIntent().getSerializableExtra("usuario");
        View view = inflater.inflate(R.layout.fragment_perfil, container, false);

        /*if ((getResources().getConfiguration().screenLayout &      Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE) {
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
        }*/

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int densityDpi = (int)(metrics.density * 160f);
        //Log.e("XAMPSONDENSITY", String.valueOf(densityDpi));

        imageViewProfile = view.findViewById(R.id.imageViewPerfil);
        textViewProfileName = view.findViewById(R.id.textViewNomePerfil);
        textViewIdProfile = view.findViewById(R.id.textViewRolPerfil);
        textViewAddressProfile = view.findViewById(R.id.textViewEnderecoPerfil);
        textViewChurch = view.findViewById(R.id.textViewIgreja);
        textViewCheckDate = view.findViewById(R.id.textViewDataPresente);
        textViewStatus = view.findViewById(R.id.textViewStatus);
        buttonEditarPerfilAtalho = view.findViewById(R.id.buttonEditarPerfilAtalho);

        buttonEditarPerfilAtalho.setOnClickListener(v -> {
            // Trava o botão no primeiro toque - sem isso, um duplo toque (comum no nosso público,
            // majoritariamente idoso) abre a tela de edição duas vezes empilhadas; ao voltar da
            // segunda, cai na primeira (parece que "não saiu da tela"/perdeu os dados editados).
            v.setEnabled(false);
            startActivityForResult(new Intent(getContext(), ProfileEditActivity.class).putExtra("usuario", user), REQUEST_EDITAR_PERFIL);
        });

        preencherTela();

        return view;
    }

    /** Popula as views a partir de `user` - separado do onCreateView pra poder re-executar depois
     *  de voltar de ProfileEditActivity com dados novos, sem depender de recriar o Fragment inteiro
     *  (antes disso, editar a foto - ou qualquer outro dado - só aparecia atualizado ao navegar pra
     *  outro item do menu e voltar). */
    private void preencherTela() {
        textViewProfileName.setText(user.getName());
        textViewIdProfile.setText("Rol nº " + user.getPhysicalId());
        if(user.getStreet() != null && user.getCity() != null && user.getCode() != null) {
            textViewAddressProfile.setText((user.getStreet() != null ? user.getStreet() + ". " : "") + (user.getCity() != null ? user.getCity() + ". " : "") + (user.getCode() != null ? user.getCode() : ""));
        } else {
            textViewAddressProfile.setText("Não há endereço cadastrado!");
        }
        textViewChurch.setText(user.getChurch() != null && user.getChurch() != "" ? user.getChurch() : "Não pertence à nenhuma igreja");
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        textViewCheckDate.setText("Última Reunião: " + (user.getLastVisit() != null ? sdf.format(user.getLastVisit()) : "Não há registro"));

        StatusEnum status = user.getStatus();
        textViewStatus.setText(status != null ? status.getLabelPtBr() : "Não informado");
        GradientDrawable chip = new GradientDrawable();
        chip.setShape(GradientDrawable.RECTANGLE);
        chip.setCornerRadius(getResources().getDimension(R.dimen.cadier_radius_chip));
        chip.setColor(ContextCompat.getColor(getContext(), corDoStatus(status)));
        textViewStatus.setBackground(chip);

        Bitmap aux = user.getPhoto() != null ? BitmapFactory.decodeFile(user.getPhoto()) : null;
        if (aux != null) {
            imageViewProfile.setPadding(0, 0, 0, 0);
            imageViewProfile.clearColorFilter();
            imageViewProfile.setImageBitmap(aux);
        } else {
            // Sem foto cadastrada - ícone de silhueta em vez de um círculo branco vazio.
            int padding = (int) (getResources().getDisplayMetrics().density * 16);
            imageViewProfile.setPadding(padding, padding, padding, padding);
            imageViewProfile.setImageResource(R.drawable.perfil);
            imageViewProfile.setColorFilter(ContextCompat.getColor(getContext(), R.color.cadier_teal_light));
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // ProfileEditActivity devolve o User atualizado - repassa pro MenuActivity, que guarda a
        // cópia "oficial" (ver comentário em MenuActivity.updateUser), e já atualiza esta tela na
        // hora (preencherTela()), sem precisar navegar pra outro item do menu e voltar.
        if (requestCode == REQUEST_EDITAR_PERFIL) {
            buttonEditarPerfilAtalho.setEnabled(true); // reabilita ao voltar, mesmo sem salvar nada
            if (resultCode == android.app.Activity.RESULT_OK && data != null) {
                User usuarioAtualizado = (User) data.getSerializableExtra("usuario");
                if (usuarioAtualizado != null) {
                    user = usuarioAtualizado;
                    ((MenuActivity) getActivity()).updateUser(usuarioAtualizado);
                    preencherTela();
                }
            }
        }
    }

    /** Mesma semântica de cor usada em Pedidos (verde=ok, laranja=atenção, vermelho=bloqueado). */
    private int corDoStatus(StatusEnum status) {
        if (status == null) return R.color.cadier_text_secondary;
        switch (status) {
            case Ativo:
                return R.color.status_success;
            case Inadimplente:
            case AguardandoAprovacaoDocumentos:
            case ProcessoInterno:
                return R.color.status_warning;
            case Falecido:
                return R.color.cadier_text_secondary;
            case Inativo:
            case Desligado:
            case Excluido:
            default:
                return R.color.status_error;
        }
    }
}