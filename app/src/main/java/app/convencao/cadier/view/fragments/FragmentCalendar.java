package app.convencao.cadier.view.fragments;

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

import app.convencao.cadier.R;
import app.convencao.cadier.modelo.OcorrenciaEventoCalendario;
import app.convencao.cadier.modelo.User;
import app.convencao.cadier.util.ApiConfig;
import app.convencao.cadier.util.ConectWebService;
import app.convencao.cadier.util.EventoCalendarioParser;
import app.convencao.cadier.view.adapter.AdapterEventoCalendario;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

/**
 * Calendário nativo do app, consumindo o backend novo (Cadier.API) em vez de redirecionar pro
 * site antigo. Combina dois endpoints - o backend já aplica toda a regra de visibilidade, o
 * cliente só une as duas listas e ordena por data:
 * - EventoCalendario/Ocorrencias: eventos institucionais (sem curso vinculado) de um período fixo
 *   (hoje até +6 meses).
 * - EventoCalendario/MinhasAulas: aulas dos cursos em que o usuário logado está matriculado -
 *   nunca aparecem no endpoint acima, e só aparecem aqui se a pessoa estiver de fato matriculada
 *   no curso daquele evento (filtragem feita inteiramente no backend).
 */
public class FragmentCalendar extends ListFragment {
    User user;
    ProgressDialog progressDialog;
    ArrayList<OcorrenciaEventoCalendario> eventosList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        user = (User) getActivity().getIntent().getSerializableExtra("usuario");
        View view = inflater.inflate(R.layout.fragment_calendario, container, false);

        Context context = getContext();
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        boolean connected = connectivityManager.getActiveNetworkInfo() != null
                && connectivityManager.getActiveNetworkInfo().isAvailable()
                && connectivityManager.getActiveNetworkInfo().isConnected();

        if (connected) {
            new SearchEventos().execute();
        } else {
            Toast.makeText(context, "Você não está conectado à internet!!", Toast.LENGTH_LONG).show();
        }

        return view;
    }

    public class SearchEventos extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(getContext(), "Procurando eventos!", "Aguarde um pouco...", false, false);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            ConectWebService cW = new ConectWebService();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());

            Calendar inicio = Calendar.getInstance();
            inicio.set(Calendar.HOUR_OF_DAY, 0);
            inicio.set(Calendar.MINUTE, 0);
            inicio.set(Calendar.SECOND, 0);

            Calendar fim = (Calendar) inicio.clone();
            fim.add(Calendar.MONTH, 6);

            String urlOcorrencias = ApiConfig.BASE_URL + "EventoCalendario/Ocorrencias?inicio="
                    + sdf.format(inicio.getTime()) + "&fim=" + sdf.format(fim.getTime());

            eventosList = new ArrayList<>();
            adicionarOcorrencias(cW.get(urlOcorrencias, user.getToken()));
            adicionarOcorrencias(cW.get(ApiConfig.BASE_URL + "EventoCalendario/MinhasAulas", user.getToken()));

            Collections.sort(eventosList, new Comparator<OcorrenciaEventoCalendario>() {
                @Override
                public int compare(OcorrenciaEventoCalendario a, OcorrenciaEventoCalendario b) {
                    Date dataA = a.getDataHoraOcorrencia();
                    Date dataB = b.getDataHoraOcorrencia();
                    if (dataA == null || dataB == null) return 0;
                    return dataA.compareTo(dataB);
                }
            });

            return null;
        }

        private void adicionarOcorrencias(String resultado) {
            if (resultado == null) return;
            try {
                JSONArray jsonArray = new JSONArray(resultado);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject ocorrencia = jsonArray.getJSONObject(i);
                    eventosList.add(EventoCalendarioParser.paraOcorrencia(ocorrencia));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            AdapterEventoCalendario adapter = new AdapterEventoCalendario(getActivity(), R.layout.adapter_evento_calendario, eventosList);
            setListAdapter(adapter);
            if (progressDialog != null) progressDialog.dismiss();
        }
    }
}
