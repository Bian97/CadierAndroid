package com.example.cadier.view.pager;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.cadier.R;
import com.example.cadier.modelo.OrdemServico;
import com.example.cadier.modelo.Usuario;
import com.example.cadier.util.ConectaWebService;
import com.example.cadier.view.adapter.AdapterAgendados;
import com.example.cadier.view.adapter.AdapterAnteriores;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

/**
 * Created by DrGreend on 24/03/2018.
 */

public class TabAgendados extends ListFragment {
    Usuario usuario;
    OrdemServico ordemServico;
    ProgressDialog progressDialog;
    ArrayList<OrdemServico> listaServicos;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        usuario = (Usuario) getActivity().getIntent().getSerializableExtra("usuario");
        View view = inflater.inflate(R.layout.tab_agendados, container, false);
        BuscarAgendados buscarAgendados = new BuscarAgendados();
        Context context = getContext();
        boolean conectado;
        ConnectivityManager conectivtyManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (conectivtyManager.getActiveNetworkInfo() != null && conectivtyManager.getActiveNetworkInfo().isAvailable() && conectivtyManager.getActiveNetworkInfo().isConnected()) {
            conectado = true;
        } else {
            conectado = false;
        }
        if(conectado) {
            buscarAgendados.execute();
        } else {
            Toast.makeText(context, "Você não está conectado à internet!!", Toast.LENGTH_LONG).show();
        }


        return view;
    }

    public class BuscarAgendados extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            progressDialog = ProgressDialog.show(getContext(), "Procurando informações!", "Aguarde um pouco...", false, false);
        }

        @Override
        protected String doInBackground(String... strings) {
            String result = null;
            ConectaWebService cW = new ConectaWebService();

            result = cW.request("http://cadier.com.br/WS/wsListAgendados.php?rol=" + usuario.getRol());


            return result;
        }

        @Override
        protected void onPostExecute(String result){
            super.onPostExecute(result);
            listaServicos = new ArrayList<>();
            try {
                if (result != null && !result.equalsIgnoreCase("vazio")) {
                    JSONArray jsonArray = new JSONArray(result);
                    for(int i = 0; i < jsonArray.length(); i++) {
                        ordemServico = new OrdemServico(jsonArray.getJSONObject(i).getInt("codord"), jsonArray.getJSONObject(i).getInt("fk_rolfis"), jsonArray.getJSONObject(i).getInt("fk_atend"), jsonArray.getJSONObject(i).getString("tiposerv")
                                , jsonArray.getJSONObject(i).getString("obs"), jsonArray.getJSONObject(i).getString("datasoli"), jsonArray.getJSONObject(i).getString("datafez"), jsonArray.getJSONObject(i).getString("dataent"),
                                jsonArray.getJSONObject(i).getString("nomelevou"), Float.parseFloat(jsonArray.getJSONObject(i).getString("valorserv")), Float.parseFloat(jsonArray.getJSONObject(i).getString("pghj")), Float.parseFloat(jsonArray.getJSONObject(i).getString("creditoant")),
                                Float.parseFloat(jsonArray.getJSONObject(i).getString("resta")), Float.parseFloat(jsonArray.getJSONObject(i).getString("deposito")));

                        listaServicos.add(ordemServico);
                    }
                    AdapterAgendados adapterAgendados = new AdapterAgendados(getActivity(), R.layout.adapter_agendados, listaServicos);
                    setListAdapter(adapterAgendados);
                    //listviewAnteriores.setAdapter(adapterAnteriores);
                } else {
                    Toast.makeText(getContext(), "Aviso: Você não possui pedidos agendados!", Toast.LENGTH_LONG).show();
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