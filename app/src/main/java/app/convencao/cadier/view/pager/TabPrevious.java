package app.convencao.cadier.view.pager;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.ListFragment;

import app.convencao.cadier.util.ApiConfig;
import app.convencao.cadier.util.OrdemServicoParser;
import app.convencao.cadier.view.adapter.AdapterPrevious;
import app.convencao.cadier.R;
import app.convencao.cadier.modelo.ServiceOrder;
import app.convencao.cadier.modelo.User;
import app.convencao.cadier.util.ConectWebService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by DrGreend on 24/03/2018.
 * O antigo endpoint dedicado "ordersByDate" (filtro por data no servidor) não existe mais no
 * backend novo - busca a lista completa de OrdemServico/PorPessoaFisica/{id} e filtra pela data
 * escolhida no DatePicker aqui mesmo, comparando só a parte "yyyy-MM-dd" de dataPedido.
 */

public class TabPrevious extends ListFragment {
    User user;
    ServiceOrder serviceOrder;
    ImageButton buttonSearch;
    ProgressDialog progressDialog;
    ArrayList<ServiceOrder> orderList;
    String dataEscolhida; // yyyy-MM-dd

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        user = (User) getActivity().getIntent().getSerializableExtra("usuario");
        View view = inflater.inflate(R.layout.tab_anteriores, container, false);

        buttonSearch = view.findViewById(R.id.buttonProcurar);

        buttonSearch.setOnClickListener(view1 -> {
            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            final DatePicker input = new DatePicker(getActivity());
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);
            input.setLayoutParams(lp);
            builder.setTitle("Escolha a data da reunião ou do pedido abaixo!");
            builder.setView(input);

            builder.setPositiveButton(android.R.string.ok, (dialog, which) -> {
                Calendar calendario = Calendar.getInstance();
                calendario.set(input.getYear(), input.getMonth(), input.getDayOfMonth());
                dataEscolhida = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(calendario.getTime());
                dialog.dismiss();
                SearchPrevious searchPrevious = new SearchPrevious();
                Context context = getContext();
                boolean connected = false;
                ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                if (connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isAvailable() && connectivityManager.getActiveNetworkInfo().isConnected()) {
                    connected = true;
                }
                if(connected) {
                    searchPrevious.execute();
                } else {
                    Toast.makeText(context, "Você não está conectado à internet!!", Toast.LENGTH_LONG).show();
                }
            });

            builder.setNegativeButton(android.R.string.cancel, (dialogInterface, i) -> dialogInterface.dismiss());

            builder.create();
            builder.show();
        });

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
                        String dataPedido = pedido.optString("dataPedido", "");
                        if (!dataPedido.startsWith(dataEscolhida)) continue;

                        serviceOrder = OrdemServicoParser.paraServiceOrder(pedido);
                        orderList.add(serviceOrder);
                    }
                }
                if (orderList.isEmpty()) {
                    Toast.makeText(getContext(), "Não foram encontrados registros. Confira se a data está correta ou verifique sua conexão com a internet!", Toast.LENGTH_LONG).show();
                } else {
                    AdapterPrevious adapterPrevious = new AdapterPrevious(getActivity(), R.layout.adapter_anteriores, orderList);
                    setListAdapter(adapterPrevious);
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
