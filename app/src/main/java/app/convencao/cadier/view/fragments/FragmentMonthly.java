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
 * Se a pessoa for presidente/cadastrante de alguma igreja (Loja/pessoas-juridicas-vinculadas, mesmo
 * endpoint que "Meus Pedidos" usa pra montar a aba PJ), busca também a última mensalidade (tipo 2 -
 * só esse, Filiação/Reativação são conceitos exclusivos de pessoa física) quitada da primeira igreja
 * vinculada, via OrdemServico/PorPessoaJuridica/{id}.
 */

public class FragmentMonthly extends Fragment {
    private static final Set<Integer> IDS_TIPO_SERVICO_MENSALIDADE_OU_EQUIVALENTE = new HashSet<>();
    static {
        IDS_TIPO_SERVICO_MENSALIDADE_OU_EQUIVALENTE.add(2);  // Mensalidade
        IDS_TIPO_SERVICO_MENSALIDADE_OU_EQUIVALENTE.add(3);  // Filiação
        IDS_TIPO_SERVICO_MENSALIDADE_OU_EQUIVALENTE.add(36); // Reativação de Filiação
    }
    private static final int ID_TIPO_SERVICO_MENSALIDADE = 2;

    TextView textViewMens, textViewMensVazio, textViewPayedMonthly, textViewValue;
    View layoutStatsMens;
    View cardMensalidadeIgreja, layoutStatsMensIgreja;
    TextView textViewNomeIgreja, textViewMensIgreja, textViewMesPagoIgreja, textViewValorIgreja, textViewMensIgrejaVazio;
    ProgressDialog progressDialog;
    User user;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        user = (User) getActivity().getIntent().getSerializableExtra("usuario");
        View view = inflater.inflate(R.layout.fragment_mensalidades, container, false);

        textViewMens = view.findViewById(R.id.textViewMens);
        textViewMensVazio = view.findViewById(R.id.textViewMensVazio);
        layoutStatsMens = view.findViewById(R.id.layoutStatsMens);
        textViewPayedMonthly = view.findViewById(R.id.textViewMesPago);
        textViewValue = view.findViewById(R.id.textViewValor);

        cardMensalidadeIgreja = view.findViewById(R.id.cardMensalidadeIgreja);
        layoutStatsMensIgreja = view.findViewById(R.id.layoutStatsMensIgreja);
        textViewNomeIgreja = view.findViewById(R.id.textViewNomeIgreja);
        textViewMensIgreja = view.findViewById(R.id.textViewMensIgreja);
        textViewMesPagoIgreja = view.findViewById(R.id.textViewMesPagoIgreja);
        textViewValorIgreja = view.findViewById(R.id.textViewValorIgreja);
        textViewMensIgrejaVazio = view.findViewById(R.id.textViewMensIgrejaVazio);

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

    /** Resultado combinado - pessoa física sempre buscada, igreja só se houver vínculo. */
    private static class Resultado {
        ServiceOrder pessoaFisica;
        String nomeIgreja;
        ServiceOrder pessoaJuridica;
        boolean temIgrejaVinculada;
    }

    public class GetMonthly extends AsyncTask<String,String,Resultado>{
        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            progressDialog = ProgressDialog.show(getContext(), "Retornando Mensalidades!", "Aguarde um instante...", false, false);
        }

        private ServiceOrder pegarUltimaMensalidadeQuitada(JSONArray pedidos, Set<Integer> idsAceitos) throws Exception {
            // A lista já vem mais recente primeiro (ver OrdemServicoController), então o primeiro
            // pedido quitado que bater o filtro já é o mais recente.
            for (int i = 0; i < pedidos.length(); i++) {
                JSONObject pedido = pedidos.getJSONObject(i);
                double quitado = pedido.optDouble("pago", 0) + pedido.optDouble("deposito", 0);
                if (quitado < pedido.optDouble("valor", 0)) continue;
                if (pedido.isNull("tipoServico")) continue;

                int idTipoServico = pedido.getJSONObject("tipoServico").optInt("idTipoServico", -1);
                if (!idsAceitos.contains(idTipoServico)) continue;

                return OrdemServicoParser.paraServiceOrder(pedido);
            }
            return null;
        }

        @Override
        protected Resultado doInBackground(String... strings) {
            Resultado resultado = new Resultado();
            try {
                ConectWebService cW = new ConectWebService();

                String resultPF = cW.get(ApiConfig.BASE_URL + "OrdemServico/PorPessoaFisica/" + user.getPhysicalId(), user.getToken());
                if (resultPF != null) {
                    resultado.pessoaFisica = pegarUltimaMensalidadeQuitada(new JSONArray(resultPF), IDS_TIPO_SERVICO_MENSALIDADE_OU_EQUIVALENTE);
                }

                String resultVinculadas = cW.get(ApiConfig.BASE_URL + "Loja/pessoas-juridicas-vinculadas", user.getToken());
                if (resultVinculadas != null) {
                    JSONArray vinculadas = new JSONArray(resultVinculadas);
                    if (vinculadas.length() > 0) {
                        JSONObject igreja = vinculadas.getJSONObject(0);
                        resultado.temIgrejaVinculada = true;
                        resultado.nomeIgreja = igreja.optString("nome", "Igreja");

                        String resultPJ = cW.get(ApiConfig.BASE_URL + "OrdemServico/PorPessoaJuridica/" + igreja.optInt("id"), user.getToken());
                        if (resultPJ != null) {
                            Set<Integer> soMensalidade = new HashSet<>();
                            soMensalidade.add(ID_TIPO_SERVICO_MENSALIDADE);
                            resultado.pessoaJuridica = pegarUltimaMensalidadeQuitada(new JSONArray(resultPJ), soMensalidade);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return resultado;
        }

        @Override
        protected void onPostExecute(Resultado resultado){
            super.onPostExecute(resultado);
            progressDialog.dismiss();
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

                if (resultado.pessoaFisica != null) {
                    ServiceOrder serviceOrder = resultado.pessoaFisica;
                    textViewMens.setText(serviceOrder.getService());
                    textViewMens.setVisibility(View.VISIBLE);
                    layoutStatsMens.setVisibility(View.VISIBLE);
                    textViewMensVazio.setVisibility(View.GONE);

                    textViewPayedMonthly.setText(serviceOrder.getDeliveryDate() != null
                            ? sdf.format(serviceOrder.getDeliveryDate())
                            : (serviceOrder.getOrderDate() != null ? sdf.format(serviceOrder.getOrderDate()) : "-"));
                    textViewValue.setText("R$ " + String.format("%.02f", serviceOrder.getServicePrice()));
                } else {
                    textViewMens.setVisibility(View.GONE);
                    layoutStatsMens.setVisibility(View.GONE);
                    textViewMensVazio.setVisibility(View.VISIBLE);
                }

                if (resultado.temIgrejaVinculada) {
                    cardMensalidadeIgreja.setVisibility(View.VISIBLE);
                    textViewNomeIgreja.setText("Última mensalidade paga - " + resultado.nomeIgreja);

                    if (resultado.pessoaJuridica != null) {
                        ServiceOrder pj = resultado.pessoaJuridica;
                        textViewMensIgreja.setVisibility(View.VISIBLE);
                        layoutStatsMensIgreja.setVisibility(View.VISIBLE);
                        textViewMensIgreja.setText(pj.getService());
                        textViewMesPagoIgreja.setText(pj.getDeliveryDate() != null
                                ? sdf.format(pj.getDeliveryDate())
                                : (pj.getOrderDate() != null ? sdf.format(pj.getOrderDate()) : "-"));
                        textViewValorIgreja.setText("R$ " + String.format("%.02f", pj.getServicePrice()));
                        textViewMensIgrejaVazio.setVisibility(View.GONE);
                    } else {
                        textViewMensIgreja.setVisibility(View.GONE);
                        layoutStatsMensIgreja.setVisibility(View.GONE);
                        textViewMensIgrejaVazio.setVisibility(View.VISIBLE);
                    }
                } else {
                    cardMensalidadeIgreja.setVisibility(View.GONE);
                }

                if (resultado.pessoaFisica == null && !resultado.temIgrejaVinculada) {
                    Toast.makeText(getContext(), "Não foram encontrados registros. Por favor, verifique se você já pagou alguma mensalidade ou verifique sua conexão com a internet!", Toast.LENGTH_LONG).show();
                }
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
