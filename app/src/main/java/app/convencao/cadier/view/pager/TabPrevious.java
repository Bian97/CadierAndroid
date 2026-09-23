package app.convencao.cadier.view.pager;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.fragment.app.ListFragment;

import app.convencao.cadier.util.ApiConfig;
import app.convencao.cadier.util.OrdemServicoParser;
import app.convencao.cadier.util.WhatsApp;
import app.convencao.cadier.view.adapter.AdapterPrevious;
import app.convencao.cadier.R;
import app.convencao.cadier.modelo.ServiceOrder;
import app.convencao.cadier.modelo.User;
import app.convencao.cadier.util.ConectWebService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by DrGreend on 24/03/2018.
 * "Anteriores" = pedidos já concluídos (entregues e sem saldo em aberto - ver
 * ServiceOrder.isPendente()), filtrados client-side da mesma lista completa de
 * OrdemServico/PorPessoaFisica/{id} que a tela "Meus Pedidos" do site usa (sem filtro de data -
 * o antigo endpoint dedicado "ordersByDate" não existe mais no backend novo, e o filtro por data
 * escolhida no DatePicker foi removido: agora traz todos os pedidos anteriores do usuário de uma vez).
 */

public class TabPrevious extends ListFragment {
    User user;
    ServiceOrder serviceOrder;
    ProgressDialog progressDialog;
    ArrayList<ServiceOrder> orderList;

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        user = (User) getActivity().getIntent().getSerializableExtra("usuario");
        View view = inflater.inflate(R.layout.tab_anteriores, container, false);

        Button buttonFalarSecretaria = view.findViewById(R.id.buttonFalarSecretaria);
        buttonFalarSecretaria.setOnClickListener(v -> WhatsApp.abrirChatSecretaria(getContext(), "Olá! Tenho uma dúvida sobre meus pedidos na CADIER."));

        Context context = getContext();
        boolean connected = false;
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isAvailable() && connectivityManager.getActiveNetworkInfo().isConnected()) {
            connected = true;
        }
        if (connected) {
            new SearchPrevious().execute();
        } else {
            Toast.makeText(context, "Você não está conectado à internet!!", Toast.LENGTH_LONG).show();
        }

        return view;
    }

    public class SearchPrevious extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            progressDialog = ProgressDialog.show(getContext(), "Procurando informações!", "Aguarde um pouco...", false, false);
        }

        @Override
        protected String doInBackground(String... strings) {
            ConectWebService cW = new ConectWebService();
            return cW.get(ApiConfig.BASE_URL + "OrdemServico/PorPessoaFisica/" + user.getPhysicalId(), user.getToken());
        }

        @Override
        protected void onPostExecute(String result){
            super.onPostExecute(result);
            orderList = new ArrayList<>();
            try {
                if (result != null) {
                    JSONArray jsonArray = new JSONArray(result);
                    for(int i = 0; i < jsonArray.length(); i++) {
                        JSONObject pedido = jsonArray.getJSONObject(i);
                        serviceOrder = OrdemServicoParser.paraServiceOrder(pedido);
                        if (serviceOrder.isPendente()) continue; // só os já concluídos

                        orderList.add(serviceOrder);
                    }
                }
                if (orderList.isEmpty()) {
                    Toast.makeText(getContext(), "Aviso: Você não possui pedidos anteriores!", Toast.LENGTH_LONG).show();
                } else {
                    AdapterPrevious adapterPrevious = new AdapterPrevious(getActivity(), R.layout.adapter_anteriores, orderList);
                    setListAdapter(adapterPrevious);
                }
                progressDialog.dismiss();
            } catch (JSONException e){
                e.printStackTrace();
                Toast.makeText(getContext(), "Não encontrado! Verifique sua conexão ou ligue para a CADIER!", Toast.LENGTH_LONG).show();
                progressDialog.dismiss();
            } catch (Exception e){
                e.printStackTrace();
                progressDialog.dismiss();
            }
        }
    }
}
