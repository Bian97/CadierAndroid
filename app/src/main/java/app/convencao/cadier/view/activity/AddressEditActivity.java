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
import app.convencao.cadier.util.ConectWebService;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by DrGreend on 26/03/2018.
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

                    Map<String,String> arguments = new HashMap<>();

                    arguments.put("url", "http://cadier.com.br/api/changeAddress");
                    arguments.put("rua", editTextEditStreet.getText().toString());
                    arguments.put("bairro", editTextEditDistrict.getText().toString());
                    arguments.put("cep", editTextEditCode.getText().toString());
                    arguments.put("cidade", editTextEditCity.getText().toString());
                    arguments.put("estado", editTextEditState.getText().toString());
                    arguments.put("pais", editTextEditCountry.getText().toString());
                    arguments.put("IdPFisica", String.valueOf(user.getPhysicalId()));

                    EditarEnderecoTask editarEnderecoTask = new EditarEnderecoTask();
                    editarEnderecoTask.execute(arguments);

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
    public class EditarEnderecoTask extends AsyncTask<Map<String, String>,String,String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(AddressEditActivity.this, "Alterando Dados", "Aguarde um instante...", false, false);
        }

        @Override
        protected String doInBackground(Map<String, String>... strings) {
            String result;
            ConectWebService cW = new ConectWebService();
            String url = strings[0].get("url");
            strings[0].remove("url");

            result = cW.send(url, "POST", strings[0]);
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
}