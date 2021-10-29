package com.example.cadier.view.activity;

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
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.cadier.R;
import com.example.cadier.modelo.User;
import com.example.cadier.util.ConectWebService;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
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
 */

public class ProfileEditActivity extends AppCompatActivity {
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

        imageViewEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                find = true;
                startActivityForResult(new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI), RESULT_LOAD_IMAGE);
            }
        });
        textViewEditPhone.setText(!user.getPhone1().contains("null") ? user.getPhone1() : "");
        textViewEditPhone2.setText(!user.getPhone2().contains("null") ? user.getPhone2() : "");
        textViewEditSpouse.setText(!user.getSpouse().contains("null") ? user.getSpouse() : "");
        textViewEditProfession.setText(!user.getJob().contains("null") ? user.getJob() : "");
        textViewEditEmail.setText(!user.getEmail().contains("null") ? user.getEmail() : "");
        Bitmap aux = BitmapFactory.decodeFile(user.getPhoto());
        imageViewEditProfile.setImageBitmap(aux);
        buttonSaveProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if(!textViewEditPhone.getText().toString().equals("")){

                        EditProfileTask editTask = new EditProfileTask();
                        user.setPhone1(textViewEditPhone.getText().toString());
                        user.setPhone2(textViewEditPhone2.getText().toString());
                        user.setSpouse(textViewEditSpouse.getText().toString());
                        user.setJob(textViewEditProfession.getText().toString());
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
    public class EditProfileTask extends AsyncTask<String,String,String> {
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
        protected String doInBackground(String... strings) {
            String result = "";

            try {
                OkHttpClient client = new OkHttpClient.Builder()
                        .connectTimeout(30, TimeUnit.SECONDS)
                        .writeTimeout(30, TimeUnit.SECONDS)
                        .readTimeout(30, TimeUnit.SECONDS)
                        .build();

                RequestBody body;

                if(selectedImage != null) {
                    File file = new File(getRealPathFromURI(selectedImage));
                    String content_type = getMimeType(file.getPath());
                    Log.e("POST", "com foto");

                    ConectWebService cW = new ConectWebService();

                    body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                            .addFormDataPart("url_da_foto", file.getName(), RequestBody.create(MediaType.parse(content_type), file))
                            .addFormDataPart("telefone", user.getPhone1())
                            .addFormDataPart("telefone2", user.getPhone2())
                            .addFormDataPart("conjuge", user.getSpouse())
                            .addFormDataPart("profissao", user.getJob())
                            .addFormDataPart("email", user.getEmail())
                            .addFormDataPart("IdPFisica", String.valueOf(user.getPhysicalId()))
                            //.addFormDataPart("achou", "sim")
                            .build();
                    Request request = new Request.Builder().url("http://cadier.com.br/api/basicUpdateProfileApp").post(body).build();
                    Response response = client.newCall(request).execute();
                    result = response.body().string();
                } else {
                    Log.e("POST", "sem foto");
                    body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                            .addFormDataPart("telefone", user.getPhone1())
                            .addFormDataPart("telefone2", user.getPhone2())
                            .addFormDataPart("conjuge", user.getSpouse())
                            .addFormDataPart("profissao", user.getJob())
                            .addFormDataPart("email", user.getEmail())
                            .addFormDataPart("IdPFisica", String.valueOf(user.getPhysicalId()))
                            //.addFormDataPart("achou", "nao")
                            .build();
                    Request request = new Request.Builder().url("http://cadier.com.br/api/basicUpdateProfileApp").post(body).build();
                    Response response = client.newCall(request).execute();
                    result = response.body().string();
                }
            /*String result;
            ConectaWebService cW = new ConectaWebService();
            result = cW.send(strings[0], "PUT", strings[1]);*/
                Log.d("POST", result);
            } catch (IOException e){
                e.printStackTrace();
            } catch (Exception e){
                e.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            progressDialog.dismiss();
            if(result.equalsIgnoreCase("Alterado")) {
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
                Log.e("", "permission denied, show dialog");
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