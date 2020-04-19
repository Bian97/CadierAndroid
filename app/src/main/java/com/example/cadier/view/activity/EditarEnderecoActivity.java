package com.example.cadier.view.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.cadier.R;
import com.example.cadier.modelo.Usuario;
import com.example.cadier.util.ConectaWebService;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by DrGreend on 26/03/2018.
 */

public class EditarEnderecoActivity extends AppCompatActivity {
    EditText editTextEditarRua, editTextEditarBairro, editTextEditarCep, editTextEditarCidade, editTextEditarEstado, editTextEditarPais;
    Button buttonGuardarEndereco;
    ProgressDialog progressDialog;
    Usuario usuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editarendereco);

        Intent intent = getIntent();
        if(intent != null){
            usuario = (Usuario) intent.getSerializableExtra("usuario");
        }

        editTextEditarRua = findViewById(R.id.editTextEditarRua);
        editTextEditarBairro = findViewById(R.id.editTextEditarBairro);
        editTextEditarCep = findViewById(R.id.editTextEditarCep);
        editTextEditarCidade = findViewById(R.id.editTextEditarCidade);
        editTextEditarEstado = findViewById(R.id.editTextEditarEstado);
        editTextEditarPais = findViewById(R.id.editTextEditarPais);
        buttonGuardarEndereco = findViewById(R.id.buttonGuardarEndereco);

        editTextEditarRua.setText(usuario.getRua());
        editTextEditarBairro.setText(usuario.getBairro());
        editTextEditarCep.setText(usuario.getCep());
        editTextEditarCidade.setText(usuario.getCidade());
        editTextEditarEstado.setText(usuario.getEstado());
        editTextEditarPais.setText(usuario.getPais());

        buttonGuardarEndereco.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    usuario.setRua(editTextEditarRua.getText().toString());
                    usuario.setBairro(editTextEditarBairro.getText().toString());
                    usuario.setCep(editTextEditarCep.getText().toString());
                    usuario.setCidade(editTextEditarCidade.getText().toString());
                    usuario.setEstado(editTextEditarEstado.getText().toString());
                    usuario.setPais(editTextEditarPais.getText().toString());

                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("rua", editTextEditarRua.getText().toString());
                    jsonObject.put("bairro", editTextEditarBairro.getText().toString());
                    jsonObject.put("cep", editTextEditarCep.getText().toString());
                    jsonObject.put("cidade", editTextEditarCidade.getText().toString());
                    jsonObject.put("estado", editTextEditarEstado.getText().toString());
                    jsonObject.put("pais", editTextEditarPais.getText().toString());
                    jsonObject.put("rol", usuario.getRol());

                    String json = jsonObject.toString();

                    EditarEnderecoTask editarEnderecoTask = new EditarEnderecoTask();
                    editarEnderecoTask.execute("http://cadier.com.br/WS/wsPutEndereco.php", json);

                } catch (JSONException e){
                    e.printStackTrace();
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(EditarEnderecoActivity.this, MenuActivity.class).putExtra("usuario", usuario));
    }
    public class EditarEnderecoTask extends AsyncTask<String,String,String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(EditarEnderecoActivity.this, "Alterando Dados", "Aguarde um instante...", false, false);
        }

        @Override
        protected String doInBackground(String... strings) {
            String result;
            ConectaWebService cW = new ConectaWebService();
            result = cW.send(strings[0], "PUT", strings[1]);
            Log.d("POST", result);
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