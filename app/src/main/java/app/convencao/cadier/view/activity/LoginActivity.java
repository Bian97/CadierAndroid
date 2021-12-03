package app.convencao.cadier.view.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import app.convencao.cadier.R;
import app.convencao.cadier.modelo.User;
import app.convencao.cadier.util.CnpjCpfDataMask;
import app.convencao.cadier.util.ConectWebService;
import app.convencao.cadier.util.Enums.StatusEnum;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by DrGreend on 07/03/2018.
 */

public class LoginActivity extends AppCompatActivity {
    EditText login, password;
    Button enter;
    ProgressDialog progressDialog;
    User user;
    ImageView imageViewLogin;
    TextView txtViewLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        login = findViewById(R.id.editTextLogin);
        password = findViewById(R.id.editTextPassword);
        enter = findViewById(R.id.buttonLogin);
        imageViewLogin= findViewById(R.id.imageViewLogin);
        txtViewLink = findViewById(R.id.textViewLink);

        txtViewLink.setOnClickListener(view -> {
            Intent viewIntent =
                    new Intent("android.intent.action.VIEW",
                            Uri.parse("https://cadier.yolasite.com"));
            startActivity(viewIntent);
        });

        imageViewLogin.setImageResource(R.drawable.logo);
        login.addTextChangedListener(CnpjCpfDataMask.insert(login, CnpjCpfDataMask.MaskType.CPF));

        if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(LoginActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PackageManager.PERMISSION_GRANTED);
        }

        enter.setOnClickListener(view -> {
            try {
                if((!login.getText().toString().equals("") && !password.getText().toString().equals(""))){
                    LoginTask loginTask = new LoginTask();
                    loginTask.execute("http://cadier.com.br/api/login", login.getText().toString(), password.getText().toString());
                } else {
                    Toast.makeText(getApplicationContext(), "PREENCHA O LOGIN E A SENHA!", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e){
                e.printStackTrace();
            }
        });
    }

    public class LoginTask extends AsyncTask<String,String,String>{
        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            progressDialog = ProgressDialog.show(LoginActivity.this, "Procurando Filiado", "Aguarde um instante...", false, false);
        }

        @Override
        protected String doInBackground(String... strings) {
            String result;
            ConectWebService cW = new ConectWebService();

            Map<String,String> arguments = new HashMap<>();
            arguments.put("IdPFisica", strings[2]);
            arguments.put("cpf", strings[1]);
            result = cW.send(strings[0], "POST", arguments);
            return result;
        }

        @Override
        protected void onPostExecute(String result){
            super.onPostExecute(result);
            progressDialog.dismiss();
            try {
                if(result != null) {
                    if (!login.getText().toString().equals("") || !password.getText().toString().equals("")) {
                        if (!result.equalsIgnoreCase("errocon") && !result.equalsIgnoreCase("erroli")) {
                            JSONArray jsonArray = new JSONArray(result);
                            JSONObject jsonObject = jsonArray.getJSONObject(0);

                            user = new User(jsonObject.getInt("IdPFisica"), jsonObject.getString("Nome"), jsonObject.getString("Telefone1"),
                                    jsonObject.getString("Cargo"), Date.valueOf(jsonObject.getString("DataNascimento")),
                                    jsonObject.getString("Conjuge"), jsonObject.getString("Filiacao"), jsonObject.getString("Profissao"),
                                    jsonObject.getString("p_fisica_presidente"), jsonObject.getString("Email"), jsonObject.getString("ApresentouConv"),
                                    jsonObject.getString("Telefone2"), jsonObject.getString("Foto"));

                            //MUDAR PARA STORED PROCEDURE

                            /*user.UserAddress(jsonObject.getString("Rua"), jsonObject.getString("Bairro"),
                                    jsonObject.getString("Cidade"), jsonObject.getString("Estado"), jsonObject.getString("Cep"), jsonObject.getString("Pais"));

                            user.UserInfos(jsonObject.getString("Rg"), jsonObject.getString("Cpf"));

                            user.UserSituations(Date.valueOf(jsonObject.getString("DataEntrou")), Date.valueOf(jsonObject.getString("DataAtualizado")),
                                    StatusEnum.fromInteger(jsonObject.getInt("Condicao")), Date.valueOf(jsonObject.getString("DataUltimaVisita")), jsonObject.getString("Obs"));

                            user.UserChurch(jsonObject.getString("NomeIgreja"),
                                    jsonObject.getString("EnderecoIgreja"));*/


                            if(!jsonObject.isNull("enderecos")){
                                user.UserAddress(jsonObject.getJSONObject("enderecos").getString("Rua"), jsonObject.getJSONObject("enderecos").getString("Bairro"),
                                        jsonObject.getJSONObject("enderecos").getString("Cidade"), jsonObject.getJSONObject("enderecos").getString("Estado"), jsonObject.getJSONObject("enderecos").getString("Cep"), jsonObject.getJSONObject("enderecos").getString("Pais"));
                            }

                            if(!jsonObject.isNull("infos")){
                                user.UserInfos(jsonObject.getJSONObject("infos").getString("Rg"), jsonObject.getJSONObject("infos").getString("Cpf"));
                            }

                            if(!jsonObject.isNull("situacoes")){
                                user.UserSituations(Date.valueOf(jsonObject.getJSONObject("situacoes").getString("DataEntrou")), Date.valueOf(jsonObject.getJSONObject("situacoes").getString("DataAtualizado")),
                                        StatusEnum.fromInteger(jsonObject.getJSONObject("situacoes").getInt("Condicao")), Date.valueOf(jsonObject.getJSONObject("situacoes").getString("DataUltimaVisita")), jsonObject.getJSONObject("situacoes").getString("Obs"));
                            }

                            if(!jsonObject.isNull("infos_temporarias")){
                                user.UserChurch(jsonObject.getJSONObject("infos_temporarias").getString("NomeIgreja"),
                                        jsonObject.getJSONObject("infos_temporarias").getString("EnderecoIgreja"));
                            }

                            if(!jsonObject.isNull("infos_temporarias")){
                                user.UserChurch(jsonObject.getJSONObject("infos_temporarias").getString("NomeIgreja"),
                                        null);
                            }

                            if(!jsonObject.isNull("p_juridica")){
                                user.UserChurch(jsonObject.getJSONObject("p_juridica").getString("Nome"), null);
                            }

                            if (login.getText().toString().equals(user.getCpf()) && password.getText().toString().equals(String.valueOf(user.getPhysicalId()))) {
                                Toast.makeText(getApplicationContext(), "Seja Bem-Vindo " + user.getName(), Toast.LENGTH_SHORT).show();
                                GetImage getImage = new GetImage();
                                getImage.execute(user.getPhoto());
                            } else {
                                if (!login.getText().toString().equals(user.getCpf()) && password.getText().toString().equals(String.valueOf(user.getPhysicalId()))) {
                                    Toast.makeText(getApplicationContext(), "O CPF ESTÁ ERRADO!", Toast.LENGTH_SHORT).show();
                                } else if (login.getText().toString().equals(user.getCpf()) && !password.getText().toString().equals(String.valueOf(user.getPhysicalId()))) {
                                    Toast.makeText(getApplicationContext(), "O NÚMERO DO ROL ESTÁ ERRADO!", Toast.LENGTH_SHORT).show();
                                } else if (!login.getText().toString().equals(user.getCpf()) && !password.getText().toString().equals(String.valueOf(user.getPhysicalId()))) {
                                    Toast.makeText(getApplicationContext(), "USUÁRIO NÃO EXISTE!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        } else if (result.equalsIgnoreCase("errocon")) {
                            Toast.makeText(LoginActivity.this, "Erro! Verifique se sua conexão com a internet!", Toast.LENGTH_LONG).show();
                        } else if (result == null) {
                            Toast.makeText(LoginActivity.this, "Erro! Seus dados estão incorretos!", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "PREENCHA O LOGIN E A SENHA!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "Verifique sua conexão com a internet e tente novamente!", Toast.LENGTH_LONG).show();
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
    @Override
    public void onBackPressed(){
        //super.onBackPressed();
    }
    public class GetImage extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(LoginActivity.this, "Aguarde um pouco.", "Baixando Imagem de perfil...", false, false);
        }

        @Override
        protected String doInBackground(String... usu) {
            String status = null;
            try {
                    Bitmap bitmap = null;
                    String fileName = usu[0];
                    fileName = fileName.substring(fileName.lastIndexOf("/") + 1);

                    URL url = new URL("http://cadier.com.br/WS/wsBaixaImagem.php?arquivo=" + fileName+"&rol="+ user.getPhysicalId());
                    bitmap = BitmapFactory.decodeStream((InputStream) url.openStream());

                    File direct = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + File.separator + "CADIER");
                    if(!direct.exists()){
                        File ImagePath = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + File.separator + "CADIER");
                        ImagePath.mkdirs();
                    }
                    File file = new File(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + File.separator + "CADIER"), fileName);
                    if(file.exists()){
                        file.delete();
                    }

                    FileOutputStream out = new FileOutputStream(file);
                    String fileExtension = fileName.substring(fileName.lastIndexOf(".")+1);
                    if(fileExtension.equalsIgnoreCase("jpg") || fileExtension.equalsIgnoreCase("jpeg")) {
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                        out.flush();
                        out.close();
                    } else if(fileExtension.equalsIgnoreCase("png")){
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                        out.flush();
                        out.close();
                    }

                    user.setPhoto(file.getAbsolutePath());

                    if(user.getPhoto() != null){
                        status = "cheio";
                    } else {
                        status = null;
                    }
            } catch (MalformedURLException e) {
                e.printStackTrace();
                status = null;
            } catch (Exception e) {
                e.printStackTrace();
                status = null;
            }
            return status;
        }
        @Override
        protected void onPostExecute(String status){
            super.onPostExecute(status);
            progressDialog.dismiss();
            if(status == null){
                Toast.makeText(getApplicationContext(), "As imagens não foram baixadas completamente!", Toast.LENGTH_SHORT).show();
            }
            startActivity(new Intent(getApplicationContext(), MenuActivity.class).putExtra("usuario", user));
        }
    }
}