package com.example.cadier.view.pager;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v7.app.AlertDialog;
import android.text.InputFilter;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.cadier.R;
import com.example.cadier.modelo.OrdemServico;
import com.example.cadier.modelo.Usuario;
import com.example.cadier.util.CnpjCpfDataMask;
import com.example.cadier.util.ConectaWebService;
import com.example.cadier.view.adapter.AdapterAnteriores;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

/**
 * Created by DrGreend on 24/03/2018.
 */

public class TabAnteriores extends ListFragment {
    Usuario usuario;
    OrdemServico ordemServico;
    //EditText textViewDataExib;
    ImageButton buttonProcurar;
    ProgressDialog progressDialog;
    ArrayList<OrdemServico> listaServicos;
    String data;

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        usuario = (Usuario) getActivity().getIntent().getSerializableExtra("usuario");
        View view = inflater.inflate(R.layout.tab_anteriores, container, false);

      //  textViewDataExib = view.findViewById(R.id.textViewDataExib);
        buttonProcurar = view.findViewById(R.id.buttonProcurar);

        buttonProcurar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
        //        data = textViewDataExib.getText().toString();

                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                final EditText input = new EditText(getActivity());

                input.addTextChangedListener(CnpjCpfDataMask.dataInsert("##/##/####", input));
                input.setHint("00/00/0000");
                input.setGravity(Gravity.CENTER);
                input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
                input.setLayoutParams(lp);
                builder.setTitle("Digite a data da reunião abaixo!");
                builder.setView(input);

                //builder.setMessage("Aviso: As senhas não coincidem!");
                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        data = input.getText().toString();
                        dialog.dismiss();
                        BuscarAnteriores buscarAnteriores = new BuscarAnteriores();
                        Context context = getContext();
                        boolean conectado;
                        ConnectivityManager conectivtyManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                        if (conectivtyManager.getActiveNetworkInfo() != null && conectivtyManager.getActiveNetworkInfo().isAvailable() && conectivtyManager.getActiveNetworkInfo().isConnected()) {
                            conectado = true;
                        } else {
                            conectado = false;
                        }
                        if(conectado) {
                            buscarAnteriores.execute();
                        } else {
                            Toast.makeText(context, "Você não está conectado à internet!!", Toast.LENGTH_LONG).show();
                        }
                    }
                });

                builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });

                builder.create();
                builder.show();
            }
        });

        return view;
    }

    public class BuscarAnteriores extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            progressDialog = ProgressDialog.show(getContext(), "Procurando informações!", "Aguarde um pouco...", false, false);
        }

        @Override
        protected String doInBackground(String... strings) {
            String result = null;
            ConectaWebService cW = new ConectaWebService();

            result = cW.request("http://cadier.com.br/WS/wsListAnteriores.php?rol=" + usuario.getRol() + "&data=" + data);


            return result;
        }

        @Override
        protected void onPostExecute(String result){
            super.onPostExecute(result);
            Log.d("XAMPSON", result);
            listaServicos = new ArrayList<>();
            try {
                if (result != null) {
                    JSONArray jsonArray = new JSONArray(result);
                    for(int i = 0; i < jsonArray.length(); i++) {
                        ordemServico = new OrdemServico(jsonArray.getJSONObject(i).getInt("codord"), jsonArray.getJSONObject(i).getInt("fk_rolfis"), jsonArray.getJSONObject(i).getInt("fk_atend"), jsonArray.getJSONObject(i).getString("tiposerv")
                                , jsonArray.getJSONObject(i).getString("obs"), jsonArray.getJSONObject(i).getString("datasoli"), jsonArray.getJSONObject(i).getString("datafez"), jsonArray.getJSONObject(i).getString("dataent"),
                                jsonArray.getJSONObject(i).getString("nomelevou"), Float.parseFloat(jsonArray.getJSONObject(i).getString("valorserv")), Float.parseFloat(jsonArray.getJSONObject(i).getString("pghj")), Float.parseFloat(jsonArray.getJSONObject(i).getString("creditoant")),
                                Float.parseFloat(jsonArray.getJSONObject(i).getString("resta")), Float.parseFloat(jsonArray.getJSONObject(i).getString("deposito")));

                        listaServicos.add(ordemServico);
                    }
                    AdapterAnteriores adapterAnteriores = new AdapterAnteriores(getActivity(), R.layout.adapter_anteriores, listaServicos);
                    setListAdapter(adapterAnteriores);
                    //listviewAnteriores.setAdapter(adapterAnteriores);
                } else {
                    Toast.makeText(getContext(), "Não foram encontrados registros. Confira se a data está correta ou verifique sua conexão com a internet!", Toast.LENGTH_LONG).show();
                }
                progressDialog.dismiss();
            } catch (JSONException e){
                e.printStackTrace();
                Toast.makeText(getContext(), "Não encontrado! Certifique-se da data, ou ligue para a CADIER!", Toast.LENGTH_LONG).show();
                progressDialog.dismiss();
            } catch (Exception e){
                e.printStackTrace();
                progressDialog.dismiss();
            }
        }
    }
}