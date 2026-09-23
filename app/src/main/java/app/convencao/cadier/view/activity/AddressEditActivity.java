package app.convencao.cadier.view.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import app.convencao.cadier.R;
import app.convencao.cadier.modelo.User;
import app.convencao.cadier.util.ApiConfig;
import app.convencao.cadier.util.CnpjCpfDataMask;
import app.convencao.cadier.util.ConectWebService;

import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by DrGreend on 26/03/2018.
 * Reescrito pro backend novo (PessoaFisica/MeusDadosDocumento). Telefone1/idPessoaJuridica são
 * sobrescritos incondicionalmente por esse endpoint (ver comentário em ProfileEditActivity), por
 * isso sempre reenvia os valores atuais do usuário nesses 2 campos mesmo só editando endereço
 * aqui - senão apaga telefone/igreja sem querer.
 * CEP virou o primeiro campo: ao completar os 8 dígitos, busca automaticamente no ViaCEP (API
 * pública, sem x-api-key/token da CADIER - por isso usa OkHttp direto aqui, não ConectWebService)
 * e preenche Rua/Bairro/Cidade/Estado/País. Cidade/Estado/País ficam travados (só o CEP os define -
 * um CEP válido sempre traz os três certos; Rua/Bairro continuam editáveis porque o ViaCEP às vezes
 * não tem esse detalhe pra CEPs de cidades pequenas).
 */

public class AddressEditActivity extends AppCompatActivity {
    EditText editTextEditStreet, editTextEditDistrict, editTextEditCode, editTextEditCity, editTextEditState, editTextEditCountry;
    Button buttonSaveAddress;
    ProgressDialog progressDialog;
    User user;
    String ultimoCepBuscado = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editarendereco);

        Intent intent = getIntent();
        if(intent != null){
            user = (User) intent.getSerializableExtra("usuario");
        }

        TextView textViewVoltar = findViewById(R.id.textViewVoltarEditarEndereco);
        editTextEditStreet = findViewById(R.id.editTextEditStreet);
        editTextEditDistrict = findViewById(R.id.editTextEditDistrict);
        editTextEditCode = findViewById(R.id.editTextEditCode);
        editTextEditCity = findViewById(R.id.editTextEditCity);
        editTextEditState = findViewById(R.id.editTextEditState);
        editTextEditCountry = findViewById(R.id.editTextEditCountry);
        buttonSaveAddress = findViewById(R.id.buttonSaveAddress);

        textViewVoltar.setOnClickListener(v -> finish());

        // Só o CEP alimenta esses 3 - nunca digitados à mão.
        editTextEditCity.setEnabled(false);
        editTextEditState.setEnabled(false);
        editTextEditCountry.setEnabled(false);

        if(user.getCountry() != null && user.getState() != null){

            editTextEditStreet.setText(user.getStreet());
            editTextEditDistrict.setText(user.getDistrict());
            editTextEditCode.setText(user.getCode());
            editTextEditCity.setText(user.getCity());
            editTextEditState.setText(user.getState());
            editTextEditCountry.setText(user.getCountry());
            ultimoCepBuscado = CnpjCpfDataMask.unmask(user.getCode() != null ? user.getCode() : "");
        }

        editTextEditCode.addTextChangedListener(CnpjCpfDataMask.dataInsert("#####-###", editTextEditCode));
        editTextEditCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String cep = CnpjCpfDataMask.unmask(s.toString());
                if (cep.length() == 8 && !cep.equals(ultimoCepBuscado)) {
                    ultimoCepBuscado = cep;
                    new BuscarCepTask().execute(cep);
                }
            }
        });

        buttonSaveAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    user.setStreet(editTextEditStreet.getText().toString());
                    user.setDistrict(editTextEditDistrict.getText().toString());
                    // Sem tirar o traço da máscara ("00000-000"), o backend recebe um CEP em
                    // formato inválido e ignora a alteração inteira (endereço todo, não só o CEP)
                    // silenciosamente - continua respondendo 200 mesmo sem salvar nada.
                    user.setCode(CnpjCpfDataMask.unmask(editTextEditCode.getText().toString()));
                    user.setCity(editTextEditCity.getText().toString());
                    user.setState(editTextEditState.getText().toString());
                    user.setCountry(editTextEditCountry.getText().toString());

                    EditarEnderecoTask editarEnderecoTask = new EditarEnderecoTask();
                    editarEnderecoTask.execute();

                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
    }

    public class BuscarCepTask extends AsyncTask<String, Void, JSONObject> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(AddressEditActivity.this, "Buscando CEP", "Aguarde um instante...", false, false);
        }

        @Override
        protected JSONObject doInBackground(String... params) {
            try {
                OkHttpClient client = new OkHttpClient.Builder()
                        .connectTimeout(15, TimeUnit.SECONDS)
                        .readTimeout(15, TimeUnit.SECONDS)
                        .build();
                Request request = new Request.Builder()
                        .url("https://viacep.com.br/ws/" + params[0] + "/json/")
                        .get()
                        .build();
                try (Response response = client.newCall(request).execute()) {
                    if (!response.isSuccessful() || response.body() == null) return null;
                    return new JSONObject(response.body().string());
                }
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(JSONObject resultado) {
            super.onPostExecute(resultado);
            progressDialog.dismiss();

            if (resultado == null) {
                Toast.makeText(AddressEditActivity.this, "Não foi possível buscar o CEP. Verifique sua conexão.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (resultado.optBoolean("erro", false)) {
                Toast.makeText(AddressEditActivity.this, "CEP não encontrado.", Toast.LENGTH_SHORT).show();
                return;
            }

            editTextEditStreet.setText(resultado.optString("logradouro", ""));
            editTextEditDistrict.setText(resultado.optString("bairro", ""));
            editTextEditCity.setText(resultado.optString("localidade", ""));
            editTextEditState.setText(resultado.optString("estado", resultado.optString("uf", "")));
            editTextEditCountry.setText("Brasil");
        }
    }

    public class EditarEnderecoTask extends AsyncTask<Void,String,Boolean> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(AddressEditActivity.this, "Alterando Dados", "Aguarde um instante...", false, false);
        }

        @Override
        protected Boolean doInBackground(Void... nada) {
            try {
                ConectWebService cW = new ConectWebService();

                JSONObject corpo = new JSONObject();
                corpo.put("telefone1", user.getPhone1());
                corpo.put("idPessoaJuridica", user.getIdPessoaJuridica() == null ? JSONObject.NULL : user.getIdPessoaJuridica());
                corpo.put("rua", user.getStreet());
                corpo.put("bairro", user.getDistrict());
                corpo.put("cidade", user.getCity());
                corpo.put("estado", user.getState());
                corpo.put("cep", user.getCode());
                corpo.put("pais", user.getCountry());

                int status = cW.sendJsonStatus(ApiConfig.BASE_URL + "PessoaFisica/MeusDadosDocumento", "PATCH", corpo.toString(), user.getToken());
                return status >= 200 && status < 300;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean sucesso) {
            super.onPostExecute(sucesso);
            progressDialog.dismiss();
            if(Boolean.TRUE.equals(sucesso)) {
                Toast.makeText(getApplicationContext(), "Alterado com Sucesso!", Toast.LENGTH_SHORT).show();
                // Devolve o User atualizado pra quem abriu essa tela - sem isso, o endereço novo
                // só existe nessa cópia local e some ao sair (ver comentário em
                // MenuActivity.updateUser).
                setResult(RESULT_OK, new Intent().putExtra("usuario", user));
            } else {
                Toast.makeText(getApplicationContext(), "Erro na Alteração, verifique sua conexão com a internet!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
