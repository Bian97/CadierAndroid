package app.convencao.cadier.view.activity;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import app.convencao.cadier.R;
import app.convencao.cadier.modelo.User;
import app.convencao.cadier.util.ApiConfig;
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

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

/**
 * Created by DrGreend on 26/03/2018.
 * Reescrito pro backend novo. Autoatendimento (PessoaFisica/MeusDadosDocumento) só permite o
 * filiado alterar Telefone1/igreja/endereço - de propósito não inclui Telefone2, Cônjuge ou
 * Profissão (ver comentário no ViewModel do backend: esses campos só continuam editáveis pelo
 * atendente). Por isso esses 3 campos aqui viraram somente leitura (em vez de sumir da tela, pra
 * não perder o layout já pronto) - se o usuário tentar editar e salvar, o valor digitado é
 * simplesmente ignorado pelo back, então travar o campo evita a falsa impressão de que salvou.
 * E-mail tem endpoint próprio (PessoaFisica/MeuEmail). Foto de perfil agora é um documento
 * (DocumentoPFisica, tipo Foto3x4) em vez de um campo solto em basicUpdateProfileApp.
 */

public class ProfileEditActivity extends AppCompatActivity {
    private static final int ID_TIPO_DOCUMENTO_FOTO_3X4 = 3;

    ImageView imageViewEditProfile;
    boolean find;
    Uri selectedImage;
    static int RESULT_LOAD_IMAGE = 2;
    User user;
    EditText textViewEditPhone, textViewEditPhone2, textViewEditSpouse, textViewEditProfession, textViewEditEmail;
    Button buttonSaveProfile;
    ProgressDialog progressDialog;
    private static final int PERMISSION_REQUEST_CODE = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editarperfil);

        Intent intent = getIntent();
        if(intent != null){
            user = (User) intent.getSerializableExtra("usuario");
        }

        if (!checkPermission()) {
            openActivity();
        } else {
            if (checkPermission()) {
                requestPermissionAndContinue();
            } else {
                openActivity();
            }
        }

        imageViewEditProfile = findViewById(R.id.imageViewEditarPerfil);
        textViewEditPhone = findViewById(R.id.textViewEditarTelefone);
        textViewEditPhone2 = findViewById(R.id.textViewEditarTelefone2);
        textViewEditSpouse = findViewById(R.id.textViewEditarConjuge);
        textViewEditProfession = findViewById(R.id.textViewEditarProfissao);
        textViewEditEmail = findViewById(R.id.textViewEditarEmail);
        buttonSaveProfile = findViewById(R.id.buttonGuardarPerfil);

        // Telefone2/Cônjuge/Profissão: só leitura, ver comentário da classe.
        textViewEditPhone2.setEnabled(false);
        textViewEditSpouse.setEnabled(false);
        textViewEditProfession.setEnabled(false);

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
                    if(!textViewEditPhone.getText().toString().equals("")){

                        EditProfileTask editTask = new EditProfileTask();
                        user.setPhone1(textViewEditPhone.getText().toString());
                        user.setEmail(textViewEditEmail.getText().toString());
                        editTask.execute();
                    } else {
                        Toast.makeText(getApplicationContext(), "Precisamos de um Telefone 1!", Toast.LENGTH_SHORT).show();
                    }
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(ProfileEditActivity.this, MenuActivity.class).putExtra("usuario", user));
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

                // telefone1 e idPessoaJuridica são sobrescritos incondicionalmente por esse
                // endpoint (null de verdade apaga) - por isso sempre manda o valor atual do
                // usuário nesses dois, mesmo quando só o telefone está mudando aqui.
                JSONObject corpo = new JSONObject();
                corpo.put("telefone1", user.getPhone1());
                corpo.put("idPessoaJuridica", user.getIdPessoaJuridica() == null ? JSONObject.NULL : user.getIdPessoaJuridica());
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
            } else {
                Toast.makeText(getApplicationContext(), "Erro na Alteração, verifique sua conexão com a internet!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean checkPermission() {

        return ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                ;
    }

    private void requestPermissionAndContinue() {
        if (ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, WRITE_EXTERNAL_STORAGE)
                    && ActivityCompat.shouldShowRequestPermissionRationale(this, READ_EXTERNAL_STORAGE)) {
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
                alertBuilder.setCancelable(true);
                alertBuilder.setTitle("Permissão para manuseio de arquivo!");
                alertBuilder.setMessage("Você permite a leitura de imagens pelo aplicativo?");
                alertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(ProfileEditActivity.this, new String[]{WRITE_EXTERNAL_STORAGE
                                , READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
                    }
                });
                AlertDialog alert = alertBuilder.create();
                alert.show();
            } else {
                ActivityCompat.requestPermissions(ProfileEditActivity.this, new String[]{WRITE_EXTERNAL_STORAGE,
                        READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
            }
        } else {
            openActivity();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (permissions.length > 0 && grantResults.length > 0) {

                boolean flag = true;
                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        flag = false;
                    }
                }
                if (flag) {
                    openActivity();
                } else {
                    finish();
                }

            } else {
                finish();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void openActivity() {
        //add your further process after giving permission or to download images from remote server.
    }
}
