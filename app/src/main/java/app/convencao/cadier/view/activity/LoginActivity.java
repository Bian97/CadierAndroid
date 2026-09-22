package app.convencao.cadier.view.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import app.convencao.cadier.util.ApiConfig;
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
import java.sql.Date;

/**
 * Created by DrGreend on 07/03/2018.
 * Login reescrito pro backend novo: o campo antigo "password" (já era só o número do Rol, com
 * ícone de cadeado só por estética - nunca foi senha de verdade, veja hint="ROL" e
 * inputType="number" em activity_login.xml) agora vira literalmente o campo "numero" do
 * autenticacao/login novo (documento + numero, sem senha, mesmo padrão do site). Como o XML já
 * pedia exatamente isso, a tela em si não precisou mudar - só o que é feito com esses 2 valores.
 */

public class LoginActivity extends AppCompatActivity {
    EditText login, password;
    Button enter, buttonCalendario;
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
        buttonCalendario = findViewById(R.id.buttonCalendario);

        buttonCalendario.setOnClickListener(view ->
                startActivity(new Intent(LoginActivity.this, CalendarioPublicoActivity.class)));

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
                    loginTask.execute(login.getText().toString(), password.getText().toString());
                } else {
                    Toast.makeText(getApplicationContext(), "PREENCHA O CPF E O NÚMERO DO ROL!", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e){
                e.printStackTrace();
            }
        });
    }

    // Resultado consolidado da sequência login -> ficha completa -> foto de perfil, tudo feito
    // dentro de um único AsyncTask (3 chamadas sequenciais) em vez de encadear vários AsyncTasks -
    // mais simples de acompanhar e continua sob um só ProgressDialog, como a tela já fazia antes.
    private static class ResultadoLogin {
        boolean sucesso;
        boolean naoAutorizado;
        User user;
    }

    public class LoginTask extends AsyncTask<String, String, ResultadoLogin> {
        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            progressDialog = ProgressDialog.show(LoginActivity.this, "Procurando Filiado", "Aguarde um instante...", false, false);
        }

        @Override
        protected ResultadoLogin doInBackground(String... strings) {
            ResultadoLogin resultado = new ResultadoLogin();
            String documento = strings[0].replaceAll("[^0-9]", "");
            String numeroTexto = strings[1];

            try {
                ConectWebService cW = new ConectWebService();

                JSONObject corpoLogin = new JSONObject();
                corpoLogin.put("documento", documento);
                corpoLogin.put("numero", Integer.parseInt(numeroTexto));
                corpoLogin.put("atendente", false);

                int statusLogin = cW.sendJsonStatus(ApiConfig.BASE_URL + "autenticacao/login", "POST", corpoLogin.toString(), null);
                if (statusLogin == 401 || statusLogin == 400) {
                    resultado.naoAutorizado = true;
                    return resultado;
                }
                if (statusLogin != 200) {
                    return resultado;
                }

                String respostaLogin = cW.sendJson(ApiConfig.BASE_URL + "autenticacao/login", "POST", corpoLogin.toString(), null);
                JSONObject login = new JSONObject(respostaLogin);
                String token = login.getString("token");
                int numero = login.getInt("numero");

                // Ficha completa (endereço, telefone, cargo etc.) - o login em si só devolve o
                // essencial (token, nome, email, situação).
                String respostaFicha = cW.get(ApiConfig.BASE_URL + "PessoaFisica/Detalhes/" + numero, token);
                if (respostaFicha == null) {
                    return resultado;
                }
                JSONObject ficha = new JSONObject(respostaFicha);

                User user = new User(
                        numero,
                        ficha.optString("nome", null),
                        ficha.optString("telefone1", null),
                        ficha.isNull("cargo") ? null : nomeCargo(ficha.optInt("cargo")),
                        parseDataOuNull(ficha.optString("dataNascimento", null)),
                        ficha.optString("conjuge", null),
                        ficha.optString("filiacao", null),
                        ficha.optString("profissao", null),
                        null, // "presidentName" não existe na ficha do backend novo - sem equivalente.
                        ficha.optString("email", null),
                        ficha.optString("indicacao", null),
                        ficha.optString("telefone2", null),
                        null // foto é buscada à parte, ver GetImage abaixo.
                );
                user.setToken(token);

                user.UserAddress(ficha.optString("logradouro", null), ficha.optString("bairro", null),
                        ficha.optString("cidade", null), ficha.optString("estado", null),
                        ficha.optString("cep", null), ficha.optString("pais", null));

                user.UserInfos(ficha.optString("rg", null), ficha.optString("cpf", null));

                user.UserSituations(parseDataOuNull(ficha.optString("dataEntrada", null)), null,
                        ficha.isNull("condicao") ? null : StatusEnum.fromInteger(ficha.optInt("condicao")),
                        parseDataOuNull(ficha.optString("dataUltimaVisita", null)), ficha.optString("obs", null));

                user.UserChurch(ficha.optString("nomePessoaJuridica", null), null);
                if (!ficha.isNull("idPessoaJuridica")) {
                    user.setIdPessoaJuridica(ficha.optInt("idPessoaJuridica"));
                }

                resultado.sucesso = true;
                resultado.user = user;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return resultado;
        }

        private Date parseDataOuNull(String iso) {
            if (iso == null || iso.isEmpty()) return null;
            try {
                // Datas do backend novo vêm em ISO 8601 (ex: "2020-05-10T00:00:00") - Date.valueOf
                // só entende "yyyy-MM-dd", por isso corta o resto antes de converter.
                return Date.valueOf(iso.length() >= 10 ? iso.substring(0, 10) : iso);
            } catch (Exception e) {
                return null;
            }
        }

        // CargosEnum (Cadier.Model.Enums) - o backend novo devolve o código numérico, a tela
        // antiga esperava o nome já em texto (era assim que o sistema legado guardava).
        private String nomeCargo(int cargo) {
            switch (cargo) {
                case 0: return "Membro";
                case 1: return "Auxiliar";
                case 2: return "Obreiro";
                case 3: return "Diácono";
                case 4: return "Presbítero";
                case 5: return "Missionário";
                case 6: return "Evangelista";
                case 7: return "Pastor";
                default: return null;
            }
        }

        @Override
        protected void onPostExecute(ResultadoLogin resultado){
            super.onPostExecute(resultado);
            progressDialog.dismiss();

            if (resultado.naoAutorizado) {
                Toast.makeText(LoginActivity.this, "Erro! Seus dados estão incorretos!", Toast.LENGTH_LONG).show();
                return;
            }
            if (!resultado.sucesso || resultado.user == null) {
                Toast.makeText(LoginActivity.this, "Verifique sua conexão com a internet e tente novamente!", Toast.LENGTH_LONG).show();
                return;
            }

            user = resultado.user;
            Toast.makeText(getApplicationContext(), "Seja Bem-Vindo " + user.getName(), Toast.LENGTH_SHORT).show();
            GetImage getImage = new GetImage();
            getImage.execute();
        }
    }

    @Override
    public void onBackPressed(){
        //super.onBackPressed();
    }

    // Baixa a foto de perfil (documento tipo "Foto3x4", id 3) via
    // DocumentoPFisica/PorFiliado/{id} + DocumentoPFisica/{id}/Download - o antigo
    // wsBaixaImagem.php não existe mais, essa é a rota equivalente no backend novo. Sem bloquear o
    // login: se não encontrar/baixar a foto (filiado nunca enviou uma, por exemplo), segue pro
    // menu mesmo assim - MenuActivity já trata user.getPhoto() nulo mostrando um ícone padrão.
    public class GetImage extends AsyncTask<String, String, String> {
        private static final int ID_TIPO_DOCUMENTO_FOTO_3X4 = 3;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(LoginActivity.this, "Aguarde um pouco.", "Baixando Imagem de perfil...", false, false);
        }

        @Override
        protected String doInBackground(String... usu) {
            try {
                ConectWebService cW = new ConectWebService();
                String respostaLista = cW.get(ApiConfig.BASE_URL + "DocumentoPFisica/PorFiliado/" + user.getPhysicalId(), user.getToken());
                if (respostaLista == null) return null;

                JSONArray documentos = new JSONArray(respostaLista);
                int idDocumentoFoto = -1;
                for (int i = 0; i < documentos.length(); i++) {
                    JSONObject doc = documentos.getJSONObject(i);
                    if (doc.optInt("tipoDocumento") == ID_TIPO_DOCUMENTO_FOTO_3X4) {
                        idDocumentoFoto = doc.optInt("idDocumento");
                    }
                }
                if (idDocumentoFoto == -1) return null;

                byte[] bytes = cW.getBytes(ApiConfig.BASE_URL + "DocumentoPFisica/" + idDocumentoFoto + "/Download", user.getToken());
                if (bytes == null) return null;

                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                if (bitmap == null) return null;

                File direct = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + File.separator + "CADIER");
                if(!direct.exists()){
                    direct.mkdirs();
                }
                String fileName = "perfil_" + user.getPhysicalId() + ".jpg";
                File file = new File(direct, fileName);
                if(file.exists()){
                    file.delete();
                }

                try (FileOutputStream out = new FileOutputStream(file)) {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                }

                return file.getAbsolutePath();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        @Override
        protected void onPostExecute(String caminhoFoto){
            super.onPostExecute(caminhoFoto);
            progressDialog.dismiss();
            user.setPhoto(caminhoFoto);
            startActivity(new Intent(getApplicationContext(), MenuActivity.class).putExtra("usuario", user));
        }
    }
}
