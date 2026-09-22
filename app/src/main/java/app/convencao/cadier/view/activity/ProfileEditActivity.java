package app.convencao.cadier.view.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Patterns;
import android.view.View;
import android.webkit.MimeTypeMap;
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

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import androidx.appcompat.app.AppCompatActivity;

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
    boolean find;
    Uri selectedImage;
    static int RESULT_LOAD_IMAGE = 2;
    private static final int REQUEST_EDITAR_ENDERECO = 3;
    User user;
    EditText textViewEditPhone, textViewEditPhone2, textViewEditSpouse, textViewEditProfession, textViewEditEmail;
    Button buttonSaveProfile;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editarperfil);

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
                find = true;
                startActivityForResult(new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI), RESULT_LOAD_IMAGE);
            }
        });
        textViewEditPhone.setText(user.getPhone1() != null ? user.getPhone1() : "");
        textViewEditPhone2.setText(user.getPhone2() != null ? user.getPhone2() : "");
        textViewEditSpouse.setText(user.getSpouse() != null ? user.getSpouse() : "");
        textViewEditProfession.setText(user.getJob() != null ? user.getJob() : "");
        textViewEditEmail.setText(user.getEmail() != null ? user.getEmail() : "");
        if (user.getPhoto() != null) {
            Bitmap aux = BitmapFactory.decodeFile(user.getPhoto());
            if (aux != null) imageViewEditProfile.setImageBitmap(aux);
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
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && data != null) {

            selectedImage = data.getData();

            imageViewEditProfile.setImageURI(selectedImage);

            user.setPhoto(getRealPathFromURI(selectedImage));
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

    public String getRealPathFromURI(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        @SuppressWarnings("deprecation")
        Cursor cursor = getApplicationContext().getContentResolver().query(uri, projection, null, null, null);
        int column_index = cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    public class EditProfileTask extends AsyncTask<String,String,Boolean> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(ProfileEditActivity.this, "Alterando Dados", "Aguarde um instante...", false, false);
        }

        private String getMimeType(String path) {
            String extension = MimeTypeMap.getFileExtensionFromUrl(path);

            return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
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

                if (selectedImage != null) {
                    File file = new File(getRealPathFromURI(selectedImage));
                    String content_type = getMimeType(file.getPath());

                    OkHttpClient client = new OkHttpClient.Builder()
                            .connectTimeout(30, TimeUnit.SECONDS)
                            .writeTimeout(30, TimeUnit.SECONDS)
                            .readTimeout(30, TimeUnit.SECONDS)
                            .build();

                    RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                            .addFormDataPart("arquivo", file.getName(), RequestBody.create(file, MediaType.parse(content_type)))
                            .build();
                    Request request = new Request.Builder()
                            .url(ApiConfig.BASE_URL + "DocumentoPFisica/" + user.getPhysicalId() + "/" + ID_TIPO_DOCUMENTO_FOTO_3X4)
                            .header("x-api-key", ApiConfig.API_KEY)
                            .header("Authorization", "Bearer " + user.getToken())
                            .post(body)
                            .build();
                    try (Response response = client.newCall(request).execute()) {
                        sucesso = sucesso && response.isSuccessful();
                    }
                }
            } catch (IOException e){
                e.printStackTrace();
                sucesso = false;
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
