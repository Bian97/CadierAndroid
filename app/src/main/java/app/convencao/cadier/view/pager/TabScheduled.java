package app.convencao.cadier.view.pager;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.ListFragment;

import app.convencao.cadier.util.Enums.ServiceKindEnum;
import app.convencao.cadier.view.adapter.AdapterScheduled;
import app.convencao.cadier.R;
import app.convencao.cadier.modelo.ServiceOrder;
import app.convencao.cadier.modelo.User;
import app.convencao.cadier.util.ConectWebService;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.sql.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by DrGreend on 24/03/2018.
 */

public class TabScheduled extends ListFragment {
    User user;
    ServiceOrder serviceOrder;
    ProgressDialog progressDialog;
    ArrayList<ServiceOrder> ordersList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        user = (User) getActivity().getIntent().getSerializableExtra("usuario");
        View view = inflater.inflate(R.layout.tab_agendados, container, false);
        SearchAgended searchAgended = new SearchAgended();
        Context context = getContext();
        boolean connected;
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isAvailable() && connectivityManager.getActiveNetworkInfo().isConnected()) {
            connected = true;
        } else {
            connected = false;
        }
        if(connected) {
            searchAgended.execute();
        } else {
            Toast.makeText(context, "Você não está conectado à internet!!", Toast.LENGTH_LONG).show();
        }
        return view;
    }

    public class SearchAgended extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            progressDialog = ProgressDialog.show(getContext(), "Procurando informações!", "Aguarde um pouco...", false, false);
        }

        @Override
        protected String doInBackground(String... strings) {
            String result = null;
            ConectWebService cW = new ConectWebService();

            Map<String,String> arguments = new HashMap<>();
            arguments.put("IdPFisica", String.valueOf(user.getPhysicalId()));
            result = cW.send("http://cadier.com.br/api/pendingOrders", "POST", arguments);

            return result;
        }

        @Override
        protected void onPostExecute(String result){
            super.onPostExecute(result);
            ordersList = new ArrayList<>();
            try {
                if (result != null && !result.equalsIgnoreCase("vazio")) {
                    JSONArray jsonArray = new JSONArray(result);
                    for(int i = 0; i < jsonArray.length(); i++) {
                        serviceOrder = new ServiceOrder(jsonArray.getJSONObject(i).getInt("IdOrdem"), jsonArray.getJSONObject(i).getInt("IdPFisica"), jsonArray.getJSONObject(i).getInt("IdAtendente"), jsonArray.getJSONObject(i).getString("Servico"),
                                jsonArray.getJSONObject(i).getString("Obs"), Date.valueOf(jsonArray.getJSONObject(i).getString("DataPedido")), Date.valueOf(jsonArray.getJSONObject(i).getString("DataFeito")), Date.valueOf(jsonArray.getJSONObject(i).getString("DataEntregue")),
                                jsonArray.getJSONObject(i).getString("QuemLevou"), Float.parseFloat(jsonArray.getJSONObject(i).getString("Valor")), Float.parseFloat(jsonArray.getJSONObject(i).getString("Pago")), Float.parseFloat(jsonArray.getJSONObject(i).getString("CreditoAnterior")),
                                Float.parseFloat(jsonArray.getJSONObject(i).getString("Deposito")), ServiceKindEnum.fromInteger(jsonArray.getJSONObject(i).getInt("TipoServico")), Date.valueOf(jsonArray.getJSONObject(i).getString("Mensalidade")));

                        ordersList.add(serviceOrder);
                    }
                    AdapterScheduled adapterScheduled = new AdapterScheduled(getActivity(), R.layout.adapter_agendados, ordersList);
                    setListAdapter(adapterScheduled);
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