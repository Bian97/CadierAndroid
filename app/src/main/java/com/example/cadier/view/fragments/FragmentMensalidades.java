package com.example.cadier.view.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cadier.R;
import com.example.cadier.modelo.OrdemServico;
import com.example.cadier.modelo.Usuario;
import com.example.cadier.util.ConectaWebService;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;

/**
 * Created by DrGreend on 07/03/2018.
 */

public class FragmentMensalidades extends Fragment {
    TextView textViewMens, textViewMesPago, textViewValor, textViewObs;
    ProgressDialog progressDialog;
    Usuario usuario;
    OrdemServico ordemServico;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        usuario = (Usuario) getActivity().getIntent().getSerializableExtra("usuario");
        View view = inflater.inflate(R.layout.fragment_mensalidades, container, false);

        textViewMens = view.findViewById(R.id.textViewMens);
        textViewMesPago = view.findViewById(R.id.textViewMesPago);
        textViewValor = view.findViewById(R.id.textViewValor);
        textViewObs = view.findViewById(R.id.textViewObs);

        PegaMensalidade pegaMensalidade = new PegaMensalidade();
        Context context = getContext();
        boolean conectado;
        ConnectivityManager conectivtyManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (conectivtyManager.getActiveNetworkInfo() != null && conectivtyManager.getActiveNetworkInfo().isAvailable() && conectivtyManager.getActiveNetworkInfo().isConnected()) {
            conectado = true;
        } else {
            conectado = false;
        }
        if(conectado) {
            pegaMensalidade.execute();
        } else {
            Toast.makeText(context, "Você não está conectado à internet!!", Toast.LENGTH_LONG).show();
        }

        return view;
    }

    public class PegaMensalidade extends AsyncTask<String,String,String>{
        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            progressDialog = ProgressDialog.show(getContext(), "Retornando Mensalidades!", "Aguarde um instante...", false, false);
        }

        @Override
        protected String doInBackground(String... strings) {
            String result = null;
            ConectaWebService cW = new ConectaWebService();

            result = cW.request("http://cadier.com.br/WS/wsGetMensalidade.php?rol=" + usuario.getRol());

            return result;
        }

        @Override
        protected void onPostExecute(String result){
            super.onPostExecute(result);
            Log.d("XAMPSON", result);
            progressDialog.dismiss();
            try {
                if (result != null) {
                    JSONObject jsonObject = new JSONObject(result);
                    ordemServico = new OrdemServico(jsonObject.getInt("codord"), jsonObject.getInt("fk_rolfis"), jsonObject.getInt("fk_atend"), jsonObject.getString("tiposerv")
                    ,jsonObject.getString("obs"), jsonObject.getString("datasoli"), jsonObject.getString("datafez"), jsonObject.getString("dataent"),
                            jsonObject.getString("nomelevou"), Float.parseFloat(jsonObject.getString("valorserv")), Float.parseFloat(jsonObject.getString("pghj")), Float.parseFloat(jsonObject.getString("creditoant")),
                            Float.parseFloat(jsonObject.getString("resta")), Float.parseFloat(jsonObject.getString("deposito")));
                    textViewMens.setText(ordemServico.getTipoServ());
                    textViewMesPago.setText(ordemServico.getDataEnt());

                    textViewValor.setText("R$ " + String.format("%.02f",ordemServico.getValorServ()));
                    textViewObs.setText(ordemServico.getObs());
                } else {
                    Toast.makeText(getContext(), "Não foram encontrados registros. Por favor, verifique se você já pagou alguma mensalidade ou verifique sua conexão com a internet!", Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e){
                e.printStackTrace();
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}