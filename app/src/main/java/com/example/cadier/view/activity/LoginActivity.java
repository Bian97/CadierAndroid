package com.example.cadier.view.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cadier.R;
import com.example.cadier.modelo.Usuario;
import com.example.cadier.util.CnpjCpfDataMask;
import com.example.cadier.util.ConectaWebService;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by DrGreend on 07/03/2018.
 */

public class LoginActivity extends AppCompatActivity {
    EditText login, senha;
    Button entrar;
    ProgressDialog progressDialog;
    Usuario usuario;
    ImageView imageViewLogin;
    TextView txtViewLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        login = findViewById(R.id.editTextLogin);
        senha = findViewById(R.id.editTextSenha);
        entrar = findViewById(R.id.buttonEntrar);
        imageViewLogin= findViewById(R.id.imageViewLogin);
        txtViewLink = findViewById(R.id.textViewLink);

        txtViewLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent viewIntent =
                        new Intent("android.intent.action.VIEW",
                                Uri.parse("https://cadier.yolasite.com"));
                startActivity(viewIntent);
            }
        });

        imageViewLogin.setImageResource(R.drawable.logo);
        login.addTextChangedListener(CnpjCpfDataMask.insert(login, CnpjCpfDataMask.MaskType.CPF));

        entrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if((!login.getText().toString().equals("") && !senha.getText().toString().equals(""))){
                        LoginTask loginTask = new LoginTask();
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("cpf", login.getText().toString());
                        jsonObject.put("rol", String.valueOf(senha.getText().toString()));
                        String json = jsonObject.toString();
                        loginTask.execute("http://cadier.com.br/WS/wsLogin.php", json);
                    } else {
                        Toast.makeText(getApplicationContext(), "PREENCHA O LOGIN E A SENHA!", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e){
                    e.printStackTrace();
                }
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
            ConectaWebService cW = new ConectaWebService();
            result = cW.send(strings[0], "POST", strings[1]);
            return result;
        }

        @Override
        protected void onPostExecute(String result){
            super.onPostExecute(result);
            progressDialog.dismiss();
            try {
                if(result != null) {
                    if (!login.getText().toString().equals("") || !senha.getText().toString().equals("")) {
                        if (!result.equalsIgnoreCase("errocon") && !result.equalsIgnoreCase("erroli")) {
                            JSONObject jsonObject = new JSONObject(result);
                            usuario = new Usuario(jsonObject.getInt("rol"), jsonObject.getString("nome"), jsonObject.getString("rua"), jsonObject.getString("bairro"),
                                    jsonObject.getString("cidade"), jsonObject.getString("estado"), jsonObject.getString("cep"), jsonObject.getString("pais"), jsonObject.getString("rg"),
                                    jsonObject.getString("cpf"), jsonObject.getString("tel"), jsonObject.getString("cargo"), jsonObject.getString("nasc"), jsonObject.getString("dataentrou"),
                                    jsonObject.getString("conjuge"), jsonObject.getString("igreja"), jsonObject.getString("endigr"), jsonObject.getString("filiacao"), jsonObject.getString("titulo"),
                                    jsonObject.getString("profissao"), jsonObject.getString("nomepres"), jsonObject.getString("email"), jsonObject.getString("apres"), jsonObject.getString("atualizado"),
                                    jsonObject.getString("status"), jsonObject.getString("ultimaviz"), jsonObject.getString("obs"), jsonObject.getString("telop"), jsonObject.getString("foto"));

                            if (login.getText().toString().equals(usuario.getCpf()) && senha.getText().toString().equals(String.valueOf(usuario.getRol()))) {
                                Toast.makeText(getApplicationContext(), "Seja Bem-Vindo " + usuario.getNome(), Toast.LENGTH_SHORT).show();
                                PegaImagem pegaImagem = new PegaImagem();
                                pegaImagem.execute(usuario.getFoto());
                            } else {
                                if (!login.getText().toString().equals(usuario.getCpf()) && senha.getText().toString().equals(String.valueOf(usuario.getRol()))) {
                                    Toast.makeText(getApplicationContext(), "O CPF ESTÁ ERRADO!", Toast.LENGTH_SHORT).show();
                                } else if (login.getText().toString().equals(usuario.getCpf()) && !senha.getText().toString().equals(String.valueOf(usuario.getRol()))) {
                                    Toast.makeText(getApplicationContext(), "O NÚMERO DO ROL ESTÁ ERRADO!", Toast.LENGTH_SHORT).show();
                                } else if (!login.getText().toString().equals(usuario.getCpf()) && !senha.getText().toString().equals(String.valueOf(usuario.getRol()))) {
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
    public class PegaImagem extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(LoginActivity.this, "Aguarde um pouco.", "Baixando Imagem de perfil...", false, false);
        }

        @Override
        protected String doInBackground(String... usu) {
            String status = null;
            try {
                    Bitmap aux = null;
                    String nomeArquivo = usu[0];
                    nomeArquivo = nomeArquivo.substring(nomeArquivo.lastIndexOf("/") + 1);

                    URL url = new URL("http://cadier.com.br/WS/wsBaixaImagem.php?arquivo=" + nomeArquivo+"&rol="+usuario.getRol());
                    aux = BitmapFactory.decodeStream((InputStream) url.openStream());

                    File direct = new File(Environment.getExternalStorageDirectory() + File.separator + "CADIER");
                    if(!direct.exists()){
                        File diretorioImagem = new File(Environment.getExternalStorageDirectory() + File.separator + "/CADIER/");
                        diretorioImagem.mkdirs();
                    }
                    File file = new File(new File(Environment.getExternalStorageDirectory() + File.separator +"/CADIER/"), nomeArquivo);
                    if(file.exists()){
                        file.delete();
                    }

                    FileOutputStream out = new FileOutputStream(file);
                    String extensaoArquivo = nomeArquivo.substring(nomeArquivo.lastIndexOf(".")+1);
                    if(extensaoArquivo.equalsIgnoreCase("jpg") || extensaoArquivo.equalsIgnoreCase("jpeg")) {
                        aux.compress(Bitmap.CompressFormat.JPEG, 100, out);
                        out.flush();
                        out.close();
                    } else if(extensaoArquivo.equalsIgnoreCase("png")){
                        aux.compress(Bitmap.CompressFormat.PNG, 100, out);
                        out.flush();
                        out.close();
                    }

                    usuario.setFoto(file.getAbsolutePath());

                    if(usuario.getFoto() != null){
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
                Toast.makeText(getApplicationContext(), "As imagems não foram baixadas completamente!", Toast.LENGTH_SHORT).show();
            }
            startActivity(new Intent(getApplicationContext(), MenuActivity.class).putExtra("usuario", usuario));
        }
    }
}