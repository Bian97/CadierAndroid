package app.convencao.cadier.view.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import app.convencao.cadier.R;
import app.convencao.cadier.modelo.User;
import app.convencao.cadier.util.ApiConfig;
import app.convencao.cadier.util.CnpjCpfDataMask;
import app.convencao.cadier.util.ConectWebService;
import app.convencao.cadier.util.InsetsUtil;

import org.json.JSONObject;

import java.io.File;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

/**
 * Created by DrGreend on 26/03/2018.
 * Reescrito pro backend novo. Autoatendimento (PessoaFisica/MeusDadosDocumento) permite o filiado
 * alterar Telefone1/Telefone2/igreja/endereço/Cônjuge/Profissão. E-mail tem endpoint próprio
 * (PessoaFisica/MeuEmail). Foto de perfil agora é um documento (DocumentoPFisica, tipo Foto3x4)
 * em vez de um campo solto em basicUpdateProfileApp.
 */

public class ProfileEditActivity extends AppCompatActivity {
    private static final int ID_TIPO_DOCUMENTO_FOTO_3X4 = 3;

    ImageView imageViewEditProfile;
    File arquivoFotoFinal; // foto já recortada/melhorada (FotoCropActivity) - null se o usuário não trocou a foto
    static int REQUEST_ESCOLHER_FOTO = 2;
    private static final int REQUEST_CROP_FOTO = 4;
    private static final int REQUEST_EDITAR_ENDERECO = 3;
    User user;
    EditText textViewEditPhone, textViewEditPhone2, textViewEditSpouse, textViewEditProfession, textViewEditEmail;
    Button buttonSaveProfile;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editarperfil);
        InsetsUtil.aplicar(this, R.id.barraTopo);

        Intent intent = getIntent();
        if(intent != null){
            user = (User) intent.getSerializableExtra("usuario");
        }

        TextView textViewVoltar = findViewById(R.id.textViewVoltarEditarPerfil);
        Button buttonIrParaEndereco = findViewById(R.id.buttonIrParaEndereco);
        imageViewEditProfile = findViewById(R.id.imageViewEditarPerfil);
        textViewEditPhone = findViewById(R.id.textViewEditarTelefone);
        textViewEditPhone2 = findViewById(R.id.textViewEditarTelefone2);
        textViewEditSpouse = findViewById(R.id.textViewEditarConjuge);
        textViewEditProfession = findViewById(R.id.textViewEditarProfissao);
        textViewEditEmail = findViewById(R.id.textViewEditarEmail);
        buttonSaveProfile = findViewById(R.id.buttonGuardarPerfil);

        textViewVoltar.setOnClickListener(v -> onBackPressed());
        buttonIrParaEndereco.setOnClickListener(v -> {
            // Trava no primeiro toque - duplo toque abriria a tela de endereço duas vezes
            // empilhadas (mesmo racional das outras telas de navegação).
            v.setEnabled(false);
            startActivityForResult(new Intent(ProfileEditActivity.this, AddressEditActivity.class).putExtra("usuario", user), REQUEST_EDITAR_ENDERECO);
        });

        // Telefone2 continua só leitura (autoatendimento não altera - ver comentário da classe).
        // Telefone2/Cônjuge/Profissão agora são editáveis (PessoaFisica/MeusDadosDocumento passou
        // a aceitar os três campos).
        textViewEditPhone.addTextChangedListener(CnpjCpfDataMask.phoneInsert(textViewEditPhone));
        textViewEditPhone2.addTextChangedListener(CnpjCpfDataMask.phoneInsert(textViewEditPhone2));

        imageViewEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent escolherImagem = new Intent(Intent.ACTION_GET_CONTENT);
                escolherImagem.setType("image/*");
                startActivityForResult(escolherImagem, REQUEST_ESCOLHER_FOTO);
            }
        });
        textViewEditPhone.setText(user.getPhone1() != null ? user.getPhone1() : "");
        textViewEditPhone2.setText(user.getPhone2() != null ? user.getPhone2() : "");
        textViewEditSpouse.setText(user.getSpouse() != null ? user.getSpouse() : "");
        textViewEditProfession.setText(user.getJob() != null ? user.getJob() : "");
        textViewEditEmail.setText(user.getEmail() != null ? user.getEmail() : "");
        Bitmap fotoAtual = user.getPhoto() != null ? BitmapFactory.decodeFile(user.getPhoto()) : null;
        if (fotoAtual != null) {
            imageViewEditProfile.setPadding(0, 0, 0, 0);
            imageViewEditProfile.clearColorFilter();
            imageViewEditProfile.setImageBitmap(fotoAtual);
        } else {
            // Sem foto cadastrada - mesmo ícone de silhueta usado em FragmentProfile/MenuActivity,
            // em vez de deixar o círculo branco vazio.
            int padding = (int) (getResources().getDisplayMetrics().density * 16);
            imageViewEditProfile.setPadding(padding, padding, padding, padding);
            imageViewEditProfile.setImageResource(R.drawable.perfil);
            imageViewEditProfile.setColorFilter(ContextCompat.getColor(this, R.color.cadier_teal_light));
        }
        buttonSaveProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    String telefone1 = CnpjCpfDataMask.unmask(textViewEditPhone.getText().toString());
                    String email = textViewEditEmail.getText().toString().trim();

                    if (telefone1.isEmpty()) {
                        textViewEditPhone.setError("Informe pelo menos um telefone");
                        textViewEditPhone.requestFocus();
                        return;
                    }
                    if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        textViewEditEmail.setError("Informe um e-mail válido");
                        textViewEditEmail.requestFocus();
                        return;
                    }

                    EditProfileTask editTask = new EditProfileTask();
                    user.setPhone1(telefone1);
                    user.setPhone2(CnpjCpfDataMask.unmask(textViewEditPhone2.getText().toString()));
                    user.setEmail(email);
                    user.setSpouse(textViewEditSpouse.getText().toString().trim());
                    user.setJob(textViewEditProfession.getText().toString().trim());
                    editTask.execute();
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ESCOLHER_FOTO && resultCode == RESULT_OK && data != null) {
            Uri imagemEscolhida = extrairUriEscolhida(data);
            if (imagemEscolhida == null) return;
            // Abre o recorte+melhoria compartilhado (mesma tela usada em FragmentDocumentos pro
            // slot de Foto3x4) em vez de usar a imagem original direto.
            // O Uri devolvido pelo seletor só vem com permissão de leitura garantida pra esta
            // Activity - repassar pra FotoCropActivity/uCrop sem isso derruba com
            // SecurityException assim que tentam ler o conteúdo (a tela "pisca" e volta).
            grantUriPermission(getPackageName(), imagemEscolhida, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Intent intentCrop = new Intent(this, FotoCropActivity.class);
            intentCrop.putExtra(FotoCropActivity.EXTRA_IMAGEM_URI, imagemEscolhida);
            intentCrop.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivityForResult(intentCrop, REQUEST_CROP_FOTO);
        } else if (requestCode == REQUEST_CROP_FOTO && resultCode == RESULT_OK && data != null) {
            String caminho = data.getStringExtra(FotoCropActivity.EXTRA_CAMINHO_FOTO_FINAL);
            if (caminho != null) {
                arquivoFotoFinal = new File(caminho);
                Bitmap previa = BitmapFactory.decodeFile(caminho);
                if (previa != null) imageViewEditProfile.setImageBitmap(previa);
            }
        } else if (requestCode == REQUEST_EDITAR_ENDERECO) {
            findViewById(R.id.buttonIrParaEndereco).setEnabled(true);
            if (resultCode == RESULT_OK && data != null) {
                // AddressEditActivity devolve o User com o endereço já salvo - sem isso, ao voltar
                // pra cá o endereço editado lá sumiria (só existia numa cópia local dela).
                User usuarioComEnderecoNovo = (User) data.getSerializableExtra("usuario");
                if (usuarioComEnderecoNovo != null) {
                    user = usuarioComEnderecoNovo;
                    // Repassa na hora pra quem abriu essa tela (FragmentProfile/FragmentConfigurations),
                    // mesmo que o usuário só tenha editado o endereço e volte sem tocar em "Guardar
                    // Alterações" aqui - senão o endereço novo fica preso só nessa Activity e some.
                    setResult(RESULT_OK, new Intent().putExtra("usuario", user));
                }
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

    public class EditProfileTask extends AsyncTask<String,String,Boolean> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(ProfileEditActivity.this, "Alterando Dados", "Aguarde um instante...", false, false);
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            boolean sucesso = true;
            try {
                ConectWebService cW = new ConectWebService();

                // telefone1/idPessoaJuridica são sobrescritos incondicionalmente por esse endpoint
                // (null de verdade apaga) - por isso sempre manda o valor atual do usuário neles,
                // mesmo quando só outro campo está mudando aqui. Telefone2/Cônjuge/Profissão idem,
                // mas aqui vêm sempre dos campos da tela (não travam mais).
                JSONObject corpo = new JSONObject();
                corpo.put("telefone1", user.getPhone1());
                corpo.put("telefone2", user.getPhone2());
                corpo.put("idPessoaJuridica", user.getIdPessoaJuridica() == null ? JSONObject.NULL : user.getIdPessoaJuridica());
                corpo.put("conjuge", user.getSpouse());
                corpo.put("profissao", user.getJob());
                int statusDados = cW.sendJsonStatus(ApiConfig.BASE_URL + "PessoaFisica/MeusDadosDocumento", "PATCH", corpo.toString(), user.getToken());
                sucesso = sucesso && statusDados >= 200 && statusDados < 300;

                JSONObject corpoEmail = new JSONObject();
                corpoEmail.put("email", user.getEmail());
                int statusEmail = cW.sendJsonStatus(ApiConfig.BASE_URL + "PessoaFisica/MeuEmail", "PATCH", corpoEmail.toString(), user.getToken());
                sucesso = sucesso && statusEmail >= 200 && statusEmail < 300;

                if (arquivoFotoFinal != null) {
                    int statusFoto = cW.uploadArquivo(
                            ApiConfig.BASE_URL + "DocumentoPFisica/" + user.getPhysicalId() + "/" + ID_TIPO_DOCUMENTO_FOTO_3X4,
                            user.getToken(), "arquivo", arquivoFotoFinal, "image/jpeg");
                    boolean fotoEnviada = statusFoto >= 200 && statusFoto < 300;
                    sucesso = sucesso && fotoEnviada;
                    // Independe de telefone/e-mail terem salvado: se a foto foi pro servidor, o
                    // app precisa mostrar ela - antes ficava com a foto antiga na sessão.
                    if (fotoEnviada) user.setPhoto(arquivoFotoFinal.getAbsolutePath());
                }
            } catch (Exception e){
                e.printStackTrace();
                sucesso = false;
            }
            return sucesso;
        }

        @Override
        protected void onPostExecute(Boolean sucesso) {
            super.onPostExecute(sucesso);
            progressDialog.dismiss();
            if(Boolean.TRUE.equals(sucesso)) {
                Toast.makeText(getApplicationContext(), "Alterado com Sucesso!", Toast.LENGTH_SHORT).show();
                // Devolve o User atualizado pra quem abriu essa tela (ver comentário em
                // MenuActivity.updateUser) - senão os dados editados aqui somem ao voltar.
                setResult(RESULT_OK, new Intent().putExtra("usuario", user));
            } else {
                Toast.makeText(getApplicationContext(), "Erro na Alteração, verifique sua conexão com a internet!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
