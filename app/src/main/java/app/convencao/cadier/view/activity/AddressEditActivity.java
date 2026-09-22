package app.convencao.cadier.view.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import app.convencao.cadier.R;
import app.convencao.cadier.modelo.User;
import app.convencao.cadier.util.ApiConfig;
import app.convencao.cadier.util.ConectWebService;

import org.json.JSONObject;

/**
 * Created by DrGreend on 26/03/2018.
 * Reescrito pro backend novo (PessoaFisica/MeusDadosDocumento). Telefone1/idPessoaJuridica são
 * sobrescritos incondicionalmente por esse endpoint (ver comentário em ProfileEditActivity), por
 * isso sempre reenvia os valores atuais do usuário nesses 2 campos mesmo só editando endereço
 * aqui - senão apaga telefone/igreja sem querer.
 */

public class AddressEditActivity extends AppCompatActivity {
    EditText editTextEditStreet, editTextEditDistrict, editTextEditCode, editTextEditCity, editTextEditState, editTextEditCountry;
    Button buttonSaveAddress;
    ProgressDialog progressDialog;
    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editarendereco);

        Intent intent = getIntent();
        if(intent != null){
            user = (User) intent.getSerializableExtra("usuario");
        }

        editTextEditStreet = findViewById(R.id.editTextEditStreet);
        editTextEditDistrict = findViewById(R.id.editTextEditDistrict);
        editTextEditCode = findViewById(R.id.editTextEditCode);
        editTextEditCity = findViewById(R.id.editTextEditCity);
        editTextEditState = findViewById(R.id.editTextEditState);
        editTextEditCountry = findViewById(R.id.editTextEditCountry);
        buttonSaveAddress = findViewById(R.id.buttonSaveAddress);

        if(user.getCountry() != null && user.getState() != null){

            editTextEditStreet.setText(user.getStreet());
            editTextEditDistrict.setText(user.getDistrict());
            editTextEditCode.setText(user.getCode());
            editTextEditCity.setText(user.getCity());
            editTextEditState.setText(user.getState());
            editTextEditCountry.setText(user.getCountry());
        }

        buttonSaveAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    user.setStreet(editTextEditStreet.getText().toString());
                    user.setDistrict(editTextEditDistrict.getText().toString());
                    user.setCode(editTextEditCode.getText().toString());
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(AddressEditActivity.this, MenuActivity.class).putExtra("usuario", user));
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
            } else {
                Toast.makeText(getApplicationContext(), "Erro na Alteração, verifique sua conexão com a internet!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
