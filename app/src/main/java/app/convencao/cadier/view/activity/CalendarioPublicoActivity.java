package app.convencao.cadier.view.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import app.convencao.cadier.R;
import app.convencao.cadier.modelo.OcorrenciaEventoCalendario;
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
import java.util.Locale;

/**
 * Calendário público, acessível direto da tela de login (sem precisar entrar na conta) - quem só
 * quer conferir dia/local de uma reunião presencial ou o link de uma reunião online antes de se
 * cadastrar/logar. Chama só EventoCalendario/Ocorrencias (eventos institucionais); esse endpoint
 * aceita a x-api-key sozinha, sem JWT (ver EventoCalendarioController) - por isso não precisa de
 * usuário logado. MinhasAulas não entra aqui porque depende de matrícula de uma pessoa
 * identificada.
 */
public class CalendarioPublicoActivity extends AppCompatActivity {
    ListView listViewEventos;
    TextView textViewVazio;
    ProgressDialog progressDialog;
    ArrayList<OcorrenciaEventoCalendario> eventosList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendario_publico);

        listViewEventos = findViewById(R.id.listViewEventosPublicos);
        textViewVazio = findViewById(R.id.textViewEventosPublicosVazio);
        findViewById(R.id.textViewVoltar).setOnClickListener(v -> finish());

        Context context = this;
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        boolean connected = connectivityManager.getActiveNetworkInfo() != null
                && connectivityManager.getActiveNetworkInfo().isAvailable()
                && connectivityManager.getActiveNetworkInfo().isConnected();

        if (connected) {
            new SearchEventosPublicos().execute();
        } else {
            Toast.makeText(context, "Você não está conectado à internet!!", Toast.LENGTH_LONG).show();
        }
    }

    public class SearchEventosPublicos extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(CalendarioPublicoActivity.this, "Procurando eventos!", "Aguarde um pouco...", false, false);
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

            String url = ApiConfig.BASE_URL + "EventoCalendario/Ocorrencias?inicio="
                    + sdf.format(inicio.getTime()) + "&fim=" + sdf.format(fim.getTime());

            eventosList = new ArrayList<>();
            String resultado = cW.get(url, null);
            if (resultado != null) {
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
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if (progressDialog != null) progressDialog.dismiss();

            if (eventosList.isEmpty()) {
                textViewVazio.setVisibility(View.VISIBLE);
            } else {
                AdapterEventoCalendario adapter = new AdapterEventoCalendario(CalendarioPublicoActivity.this, R.layout.adapter_evento_calendario, eventosList);
                listViewEventos.setAdapter(adapter);
            }
        }
    }
}
