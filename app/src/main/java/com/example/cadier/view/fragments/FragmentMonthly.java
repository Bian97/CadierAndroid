package com.example.cadier.view.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.cadier.R;
import com.example.cadier.modelo.ServiceOrder;
import com.example.cadier.modelo.User;
import com.example.cadier.util.ConectWebService;
import com.example.cadier.util.Enums.ServiceKindEnum;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by DrGreend on 07/03/2018.
 */

public class FragmentMonthly extends Fragment {
    TextView textViewMonthly, textViewPayedMonthly, textViewValue, textViewObs;
    ProgressDialog progressDialog;
    User user;
    ServiceOrder serviceOrder;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        user = (User) getActivity().getIntent().getSerializableExtra("usuario");
        View view = inflater.inflate(R.layout.fragment_mensalidades, container, false);

        textViewMonthly = view.findViewById(R.id.textViewMens);
        textViewPayedMonthly = view.findViewById(R.id.textViewMesPago);
        textViewValue = view.findViewById(R.id.textViewValor);
        textViewObs = view.findViewById(R.id.textViewObs);

        GetMonthly getMonthly = new GetMonthly();
        Context context = getContext();
        boolean connected;
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isAvailable() && connectivityManager.getActiveNetworkInfo().isConnected()) {
            connected = true;
        } else {
            connected = false;
        }
        if(connected) {
            getMonthly.execute();
        } else {
            Toast.makeText(context, "Você não está conectado à internet!!", Toast.LENGTH_LONG).show();
        }

        return view;
    }

    public class GetMonthly extends AsyncTask<String,String,String>{
        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            progressDialog = ProgressDialog.show(getContext(), "Retornando Mensalidades!", "Aguarde um instante...", false, false);
        }

        @Override
        protected String doInBackground(String... strings) {
            String result = null;
            ConectWebService cW = new ConectWebService();

            //result = cW.request("http://cadier.com.br/WS/wsGetMensalidade.php?rol=" + user.getPhysicalId());

            Map<String,String> arguments = new HashMap<>();
            arguments.put("IdPFisica", String.valueOf(user.getPhysicalId()));
            result = cW.send("http://cadier.com.br/api/lastMonthly", "POST", arguments);

            return result;
        }

        @Override
        protected void onPostExecute(String result){
            super.onPostExecute(result);

            progressDialog.dismiss();
            try {
                if (result != null) {
                    JSONObject jsonObject = new JSONObject(result);

                    serviceOrder = new ServiceOrder(jsonObject.getInt("IdOrdem"), jsonObject.getInt("IdPFisica"), jsonObject.getInt("IdAtendente"), jsonObject.getString("Servico"),
                            jsonObject.getString("Obs").contains("null") ? null : jsonObject.getString("Obs") , Date.valueOf(jsonObject.getString("DataPedido")), Date.valueOf(jsonObject.getString("DataFeito")), Date.valueOf(jsonObject.getString("DataEntregue")),
                            jsonObject.getString("QuemLevou").contains("null") ? null : jsonObject.getString("QuemLevou"), Float.parseFloat(jsonObject.getString("Valor")), Float.parseFloat(jsonObject.getString("Pago")), Float.parseFloat(jsonObject.getString("CreditoAnterior")),
                            Float.parseFloat(jsonObject.getString("Deposito")), ServiceKindEnum.fromInteger(jsonObject.getInt("TipoServico")), Date.valueOf(jsonObject.getString("Mensalidade")));

                    textViewMonthly.setText(serviceOrder.getService());
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                    textViewPayedMonthly.setText(sdf.format(serviceOrder.getDeliveryDate()));

                    textViewValue.setText("R$ " + String.format("%.02f", serviceOrder.getServicePrice()));
                    textViewObs.setText(serviceOrder.getObs() == null ? "Não há!" : serviceOrder.getObs());
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