package app.convencao.cadier.view.pager;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.ListFragment;

import app.convencao.cadier.util.Enums.ServiceKindEnum;
import app.convencao.cadier.view.adapter.AdapterPrevious;
import app.convencao.cadier.R;
import app.convencao.cadier.modelo.ServiceOrder;
import app.convencao.cadier.modelo.User;
import app.convencao.cadier.util.CnpjCpfDataMask;
import app.convencao.cadier.util.ConectWebService;

import org.json.JSONArray;
import org.json.JSONException;

import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by DrGreend on 24/03/2018.
 */

public class TabPrevious extends ListFragment {
    User user;
    ServiceOrder serviceOrder;
    ImageButton buttonSearch;
    ProgressDialog progressDialog;
    ArrayList<ServiceOrder> orderList;
    String date;

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
                date = input.getDayOfMonth() + "/" + (input.getMonth() + 1) + "/" + input.getYear();
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
            String result = null;
            ConectWebService cW = new ConectWebService();

            Map<String,String> arguments = new HashMap<>();
            arguments.put("IdPFisica", String.valueOf(user.getPhysicalId()));
            arguments.put("Date", date);
            result = cW.send("http://cadier.com.br/api/ordersByDate", "POST", arguments);

            return result;
        }

        @Override
        protected void onPostExecute(String result){
            super.onPostExecute(result);
            orderList = new ArrayList<>();
            try {
                if (result != null) {
                    JSONArray jsonArray = new JSONArray(result);
                    for(int i = 0; i < jsonArray.length(); i++) {
                        serviceOrder = new ServiceOrder(jsonArray.getJSONObject(i).getInt("IdOrdem"), jsonArray.getJSONObject(i).getInt("IdPFisica"), jsonArray.getJSONObject(i).getInt("IdAtendente"), jsonArray.getJSONObject(i).getString("Servico"),
                                jsonArray.getJSONObject(i).getString("Obs").contains("null") ? null : jsonArray.getJSONObject(i).getString("Obs"), Date.valueOf(jsonArray.getJSONObject(i).getString("DataPedido")), Date.valueOf(jsonArray.getJSONObject(i).getString("DataFeito")), Date.valueOf(jsonArray.getJSONObject(i).getString("DataEntregue")),
                                jsonArray.getJSONObject(i).getString("QuemLevou"), Float.parseFloat(jsonArray.getJSONObject(i).getString("Valor")), Float.parseFloat(jsonArray.getJSONObject(i).getString("Pago")), Float.parseFloat(jsonArray.getJSONObject(i).getString("CreditoAnterior")),
                                Float.parseFloat(jsonArray.getJSONObject(i).getString("Deposito")), ServiceKindEnum.fromInteger(jsonArray.getJSONObject(i).getInt("TipoServico")), Date.valueOf(jsonArray.getJSONObject(i).getString("Mensalidade")));

                        orderList.add(serviceOrder);
                    }
                    AdapterPrevious adapterPrevious = new AdapterPrevious(getActivity(), R.layout.adapter_anteriores, orderList);
                    setListAdapter(adapterPrevious);

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