package app.convencao.cadier.view.fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import app.convencao.cadier.R;
import app.convencao.cadier.modelo.DocumentoPFisica;
import app.convencao.cadier.modelo.User;
import app.convencao.cadier.util.ApiConfig;
import app.convencao.cadier.util.ConectWebService;
import app.convencao.cadier.util.Enums.StatusDocumentoEnum;
import app.convencao.cadier.util.Enums.StatusEnum;
import app.convencao.cadier.util.Enums.TipoDocumentoEnum;
import app.convencao.cadier.view.activity.FotoCropActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Tela de envio dos 6 documentos de filiação, com status de aprovação - mesma mecânica que já
 * existe no site (DocumentosFiliado.js), até então ausente no app. Reusa os mesmos endpoints
 * (DocumentoPFisica) já consumidos por ProfileEditActivity/LoginActivity.GetImage.
 */
public class FragmentDocumentos extends Fragment {
    private static final int REQUEST_ESCOLHER_FOTO = 10;
    private static final int REQUEST_CROP_FOTO = 11;
    private static final int REQUEST_ESCOLHER_ARQUIVO = 12;

    private User user;
    private LinearLayout containerDocumentos;
    private TipoDocumentoEnum tipoEmEnvio; // tipo escolhido no momento de abrir a galeria/seletor
    private ProgressDialog progressDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        user = (User) getActivity().getIntent().getSerializableExtra("usuario");
        View view = inflater.inflate(R.layout.fragment_documentos, container, false);
        containerDocumentos = view.findViewById(R.id.containerDocumentos);
        new CarregarDocumentosTask().execute();
        return view;
    }

    private boolean podeEnviar(TipoDocumentoEnum tipo) {
        return tipo == TipoDocumentoEnum.Foto3x4 || user.getStatus() == StatusEnum.AguardandoAprovacaoDocumentos;
    }

    private int corDoStatus(StatusDocumentoEnum status) {
        switch (status) {
            case Aprovado: return R.color.status_success;
            case Enviado: return R.color.status_warning;
            case Rejeitado: return R.color.status_error;
            case Pendente:
            case NaoDigitalizado:
            default:
                return R.color.cadier_text_secondary;
        }
    }

    private void renderizarLista(List<DocumentoPFisica> documentos) {
        if (getContext() == null) return;
        containerDocumentos.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(getContext());
        for (DocumentoPFisica doc : documentos) {
            View itemView = inflater.inflate(R.layout.item_documento, containerDocumentos, false);
            bindItem(itemView, doc);
            containerDocumentos.addView(itemView);
        }
    }

    private void bindItem(View itemView, DocumentoPFisica doc) {
        TipoDocumentoEnum tipo = doc.getTipoDocumento();

        TextView textViewNome = itemView.findViewById(R.id.textViewNomeItemDocumento);
        textViewNome.setText(tipo.getLabelPtBr());

        TextView textViewStatus = itemView.findViewById(R.id.textViewStatusItemDocumento);
        textViewStatus.setText(doc.getStatus().getLabelPtBr());
        GradientDrawable chip = new GradientDrawable();
        chip.setShape(GradientDrawable.RECTANGLE);
        chip.setCornerRadius(getResources().getDimension(R.dimen.cadier_radius_chip));
        chip.setColor(ContextCompat.getColor(requireContext(), corDoStatus(doc.getStatus())));
        textViewStatus.setBackground(chip);

        TextView textViewMotivo = itemView.findViewById(R.id.textViewMotivoRejeicaoItemDocumento);
        if (doc.getStatus() == StatusDocumentoEnum.Rejeitado && doc.getMotivoRejeicao() != null && !doc.getMotivoRejeicao().isEmpty()) {
            textViewMotivo.setVisibility(View.VISIBLE);
            textViewMotivo.setText("Motivo da rejeição: " + doc.getMotivoRejeicao());
        } else {
            textViewMotivo.setVisibility(View.GONE);
        }

        Button buttonEnviar = itemView.findViewById(R.id.buttonEnviarItemDocumento);
        boolean habilitado = podeEnviar(tipo);
        if (!habilitado) {
            // Documento travado (ex.: "Não Digitalizado" de cadastro legado, ou os 5 documentos
            // não-foto fora do período de filiação) - texto de "Enviar" ficaria enganoso num botão
            // que não faz nada, mesmo já cinza.
            buttonEnviar.setText("Enviado");
        } else {
            buttonEnviar.setText(doc.getIdDocumento() > 0 ? "Reenviar" : "Enviar");
        }
        buttonEnviar.setEnabled(habilitado);
        buttonEnviar.setOnClickListener(v -> iniciarEnvio(tipo));

        if (tipo == TipoDocumentoEnum.Foto3x4) {
            View frameAvatar = itemView.findViewById(R.id.frameAvatarItemDocumento);
            View containerObs = itemView.findViewById(R.id.containerObsItemDocumento);
            frameAvatar.setVisibility(View.VISIBLE);
            containerObs.setVisibility(View.VISIBLE);

            ImageView imageViewAvatar = itemView.findViewById(R.id.imageViewAvatarItemDocumento);
            Bitmap aux = user.getPhoto() != null ? BitmapFactory.decodeFile(user.getPhoto()) : null;
            if (aux != null) {
                imageViewAvatar.setImageBitmap(aux);
            } else {
                imageViewAvatar.setImageResource(R.drawable.perfil);
                imageViewAvatar.setColorFilter(ContextCompat.getColor(requireContext(), R.color.cadier_teal_light));
            }
        }
    }

    private void iniciarEnvio(TipoDocumentoEnum tipo) {
        tipoEmEnvio = tipo;
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        if (tipo == TipoDocumentoEnum.Foto3x4) {
            intent.setType("image/*");
            startActivityForResult(intent, REQUEST_ESCOLHER_FOTO);
        } else {
            intent.setType("*/*");
            intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"image/*", "application/pdf"});
            startActivityForResult(intent, REQUEST_ESCOLHER_ARQUIVO);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ESCOLHER_FOTO && resultCode == android.app.Activity.RESULT_OK && data != null) {
            Uri imagemEscolhida = extrairUriEscolhida(data);
            if (imagemEscolhida == null) return;
            // O Uri devolvido pelo seletor só vem com permissão de leitura garantida pra esta
            // Activity - repassar pra FotoCropActivity/uCrop sem isso derruba com
            // SecurityException assim que tentam ler o conteúdo (a tela "pisca" e volta).
            requireContext().grantUriPermission(requireContext().getPackageName(), imagemEscolhida, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Intent intentCrop = new Intent(getContext(), FotoCropActivity.class);
            intentCrop.putExtra(FotoCropActivity.EXTRA_IMAGEM_URI, imagemEscolhida);
            intentCrop.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivityForResult(intentCrop, REQUEST_CROP_FOTO);
        } else if (requestCode == REQUEST_CROP_FOTO && resultCode == android.app.Activity.RESULT_OK && data != null) {
            String caminho = data.getStringExtra(FotoCropActivity.EXTRA_CAMINHO_FOTO_FINAL);
            if (caminho != null) {
                new EnviarDocumentoTask(tipoEmEnvio, new File(caminho), "image/jpeg").execute();
            }
        } else if (requestCode == REQUEST_ESCOLHER_ARQUIVO && resultCode == android.app.Activity.RESULT_OK && data != null) {
            Uri arquivoEscolhido = extrairUriEscolhida(data);
            if (arquivoEscolhido != null) {
                new EnviarDocumentoTask(tipoEmEnvio, arquivoEscolhido).execute();
            }
        }
    }

    /**
     * Alguns seletores (o Photo Picker do Android moderno, quando permite seleção múltipla e
     * mostra um botão "Concluído"/"Done") devolvem o Uri escolhido só em getClipData(), deixando
     * getData() nulo - sem esse fallback, o clique em "Concluído" não fazia nada.
     */
    private Uri extrairUriEscolhida(Intent data) {
        if (data.getData() != null) return data.getData();
        if (data.getClipData() != null && data.getClipData().getItemCount() > 0) {
            return data.getClipData().getItemAt(0).getUri();
        }
        return null;
    }

    private class CarregarDocumentosTask extends AsyncTask<Void, Void, List<DocumentoPFisica>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (getContext() != null) {
                progressDialog = ProgressDialog.show(getContext(), "Carregando", "Buscando seus documentos...", false, false);
            }
        }

        @Override
        protected List<DocumentoPFisica> doInBackground(Void... voids) {
            List<DocumentoPFisica> resultado = new ArrayList<>();
            try {
                ConectWebService cW = new ConectWebService();
                String resposta = cW.get(ApiConfig.BASE_URL + "DocumentoPFisica/PorFiliado/" + user.getPhysicalId(), user.getToken());
                JSONArray documentos = resposta != null ? new JSONArray(resposta) : new JSONArray();

                for (TipoDocumentoEnum tipo : TipoDocumentoEnum.values()) {
                    DocumentoPFisica encontrado = null;
                    for (int i = 0; i < documentos.length(); i++) {
                        JSONObject doc = documentos.getJSONObject(i);
                        if (doc.optInt("tipoDocumento") == tipo.getId()) {
                            encontrado = DocumentoPFisica.fromJson(doc);
                            break;
                        }
                    }
                    resultado.add(encontrado != null ? encontrado : DocumentoPFisica.pendenteSintetico(tipo));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return resultado;
        }

        @Override
        protected void onPostExecute(List<DocumentoPFisica> documentos) {
            super.onPostExecute(documentos);
            if (progressDialog != null) progressDialog.dismiss();
            if (getContext() == null) return;
            if (documentos.isEmpty()) {
                Toast.makeText(getContext(), "Não foi possível carregar seus documentos. Verifique sua conexão.", Toast.LENGTH_SHORT).show();
                return;
            }
            renderizarLista(documentos);
        }
    }

    private class EnviarDocumentoTask extends AsyncTask<Void, Void, Boolean> {
        private final TipoDocumentoEnum tipo;
        private File arquivoPronto;
        private final Uri uriParaCopiar;
        private String mimeType;

        /** Foto3x4 - arquivo já recortado/melhorado por FotoCropActivity. */
        EnviarDocumentoTask(TipoDocumentoEnum tipo, File arquivoPronto, String mimeType) {
            this.tipo = tipo;
            this.arquivoPronto = arquivoPronto;
            this.uriParaCopiar = null;
            this.mimeType = mimeType;
        }

        /** Demais documentos - copia o conteúdo do Uri escolhido pro cache antes de enviar. */
        EnviarDocumentoTask(TipoDocumentoEnum tipo, Uri uriParaCopiar) {
            this.tipo = tipo;
            this.arquivoPronto = null;
            this.uriParaCopiar = uriParaCopiar;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (getContext() != null) {
                progressDialog = ProgressDialog.show(getContext(), "Enviando", "Aguarde um instante...", false, false);
            }
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                if (arquivoPronto == null && uriParaCopiar != null) {
                    if (getContext() == null) return false;
                    mimeType = getContext().getContentResolver().getType(uriParaCopiar);
                    arquivoPronto = copiarUriParaCache(uriParaCopiar, mimeType);
                    if (arquivoPronto == null) return false;
                }
                ConectWebService cW = new ConectWebService();
                int status = cW.uploadArquivo(
                        ApiConfig.BASE_URL + "DocumentoPFisica/" + user.getPhysicalId() + "/" + tipo.getId(),
                        user.getToken(), "arquivo", arquivoPronto, mimeType);
                return status >= 200 && status < 300;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        private File copiarUriParaCache(Uri uri, String mime) {
            String extensao = mime != null && mime.contains("pdf") ? ".pdf" : ".jpg";
            File destino = new File(getContext().getCacheDir(), "doc_" + System.currentTimeMillis() + extensao);
            try (InputStream is = getContext().getContentResolver().openInputStream(uri);
                 FileOutputStream fos = new FileOutputStream(destino)) {
                if (is == null) return null;
                byte[] buffer = new byte[8192];
                int lidos;
                while ((lidos = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, lidos);
                }
                return destino;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Boolean sucesso) {
            super.onPostExecute(sucesso);
            if (progressDialog != null) progressDialog.dismiss();
            if (getContext() == null) return;
            if (Boolean.TRUE.equals(sucesso)) {
                Toast.makeText(getContext(), "Documento enviado com sucesso!", Toast.LENGTH_SHORT).show();
                new CarregarDocumentosTask().execute();
            } else {
                Toast.makeText(getContext(), "Erro ao enviar o documento, verifique sua conexão com a internet!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
