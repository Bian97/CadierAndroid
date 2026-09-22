package app.convencao.cadier.view.fragments;

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

import app.convencao.cadier.R;
import app.convencao.cadier.modelo.ServiceOrder;
import app.convencao.cadier.modelo.User;
import app.convencao.cadier.util.ApiConfig;
import app.convencao.cadier.util.ConectWebService;
import app.convencao.cadier.util.OrdemServicoParser;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by DrGreend on 07/03/2018.
 * O antigo endpoint dedicado "lastMonthly" não existe mais no backend novo (o equivalente mais
 * próximo, PessoaFisica/{id}/UltimoPagamento, só devolve uma data - sem serviço/valor/obs). Em vez
 * disso busca a lista completa de OrdemServico/PorPessoaFisica/{id} (mesma chamada das abas de
 * Pedidos) e pega o pedido quitado mais recente cujo tipo de serviço seja Mensalidade, Filiação ou
 * Reativação de Filiação (ids 2, 3 e 36 - mesmo conjunto que o back considera "em dia" pra fins de
 * inadimplência). "Quitado" usa o mesmo critério do back (ver
 * Cadier.DB/Scripts/PessoaFisica/MarcarFiliadosComoInadimplentes.sql: (Pago + Deposito) >= Valor) -
 * não dá pra exigir "pago > 0" porque uma mensalidade abonada (cortesia, Valor = Pago = 0) também
 * conta como quitada e nunca teria pago > 0.
 */

public class FragmentMonthly extends Fragment {
    private static final Set<Integer> IDS_TIPO_SERVICO_MENSALIDADE_OU_EQUIVALENTE = new HashSet<>();
    static {
        IDS_TIPO_SERVICO_MENSALIDADE_OU_EQUIVALENTE.add(2);  // Mensalidade
        IDS_TIPO_SERVICO_MENSALIDADE_OU_EQUIVALENTE.add(3);  // Filiação
        IDS_TIPO_SERVICO_MENSALIDADE_OU_EQUIVALENTE.add(36); // Reativação de Filiação
    }

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

    public class GetMonthly extends AsyncTask<String,String,ServiceOrder>{
        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            progressDialog = ProgressDialog.show(getContext(), "Retornando Mensalidades!", "Aguarde um instante...", false, false);
        }

        @Override
        protected ServiceOrder doInBackground(String... strings) {
            try {
                ConectWebService cW = new ConectWebService();
                String result = cW.get(ApiConfig.BASE_URL + "OrdemServico/PorPessoaFisica/" + user.getPhysicalId(), user.getToken());
                if (result == null) return null;

                JSONArray jsonArray = new JSONArray(result);
                // A lista já vem mais recente primeiro (ver OrdemServicoController.ListarPorPessoaFisica),
                // então o primeiro pedido quitado que bater o filtro já é o mais recente.
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject pedido = jsonArray.getJSONObject(i);
                    double quitado = pedido.optDouble("pago", 0) + pedido.optDouble("deposito", 0);
                    if (quitado < pedido.optDouble("valor", 0)) continue;
                    if (pedido.isNull("tipoServico")) continue;

                    int idTipoServico = pedido.getJSONObject("tipoServico").optInt("idTipoServico", -1);
                    if (!IDS_TIPO_SERVICO_MENSALIDADE_OU_EQUIVALENTE.contains(idTipoServico)) continue;

                    return OrdemServicoParser.paraServiceOrder(pedido);
                }
                return null;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(ServiceOrder resultado){
            super.onPostExecute(resultado);

            progressDialog.dismiss();
            try {
                if (resultado != null) {
                    serviceOrder = resultado;
                    textViewMonthly.setText(serviceOrder.getService());
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                    textViewPayedMonthly.setText(serviceOrder.getDeliveryDate() != null
                            ? sdf.format(serviceOrder.getDeliveryDate())
                            : (serviceOrder.getOrderDate() != null ? sdf.format(serviceOrder.getOrderDate()) : ""));

                    textViewValue.setText("R$ " + String.format("%.02f", serviceOrder.getServicePrice()));
                    textViewObs.setText(serviceOrder.getObs() == null ? "Não há!" : serviceOrder.getObs());
                } else {
                    Toast.makeText(getContext(), "Não foram encontrados registros. Por favor, verifique se você já pagou alguma mensalidade ou verifique sua conexão com a internet!", Toast.LENGTH_LONG).show();
                }
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
