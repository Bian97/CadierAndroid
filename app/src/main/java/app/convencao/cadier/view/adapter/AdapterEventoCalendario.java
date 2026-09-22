package app.convencao.cadier.view.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import app.convencao.cadier.R;
import app.convencao.cadier.modelo.OcorrenciaEventoCalendario;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class AdapterEventoCalendario extends ArrayAdapter<OcorrenciaEventoCalendario> {

    private final ArrayList<OcorrenciaEventoCalendario> eventos;
    private final Context context;
    private final Integer resourceId;
    private final SimpleDateFormat sdfInicio = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
    private final SimpleDateFormat sdfFim = new SimpleDateFormat("HH:mm", Locale.getDefault());

    public AdapterEventoCalendario(@NonNull Context context, int resource, @NonNull ArrayList<OcorrenciaEventoCalendario> list) {
        super(context, resource, list);
        this.eventos = list;
        this.context = context;
        this.resourceId = resource;
    }

    @NonNull
    @Override
    public View getView(final int position, View convertView, @NonNull ViewGroup parent) {
        View row = convertView;
        ViewHolder holder;

        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(resourceId, parent, false);
            holder = new ViewHolder();

            holder.textViewTitulo = row.findViewById(R.id.textViewTituloEvento);
            holder.textViewCurso = row.findViewById(R.id.textViewCursoEvento);
            holder.textViewData = row.findViewById(R.id.textViewDataEvento);
            holder.textViewLocal = row.findViewById(R.id.textViewLocalEvento);
            holder.textViewDescricao = row.findViewById(R.id.textViewDescricaoEvento);
            holder.textViewLink = row.findViewById(R.id.textViewLinkEvento);
            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }

        OcorrenciaEventoCalendario evento = eventos.get(position);
        if (evento != null) {
            holder.textViewTitulo.setText(evento.getTitulo());

            if (evento.isAula()) {
                holder.textViewCurso.setText("Aula: " + evento.getNomeCurso());
                holder.textViewCurso.setVisibility(View.VISIBLE);
            } else {
                holder.textViewCurso.setVisibility(View.GONE);
            }

            if (evento.getDataHoraOcorrencia() != null) {
                String texto = sdfInicio.format(evento.getDataHoraOcorrencia());
                if (evento.getDataHoraFimOcorrencia() != null) {
                    texto += " - " + sdfFim.format(evento.getDataHoraFimOcorrencia());
                }
                holder.textViewData.setText(texto);
                holder.textViewData.setVisibility(View.VISIBLE);
            } else {
                holder.textViewData.setVisibility(View.GONE);
            }

            if (evento.getLocal() != null && !evento.getLocal().isEmpty()) {
                holder.textViewLocal.setText(evento.getLocal());
                holder.textViewLocal.setVisibility(View.VISIBLE);
                final String local = evento.getLocal();
                holder.textViewLocal.setOnClickListener(v -> {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=" + Uri.encode(local)));
                    context.startActivity(intent);
                });
            } else {
                holder.textViewLocal.setVisibility(View.GONE);
                holder.textViewLocal.setOnClickListener(null);
            }

            if (evento.getDescricao() != null && !evento.getDescricao().isEmpty()) {
                holder.textViewDescricao.setText(evento.getDescricao());
                holder.textViewDescricao.setVisibility(View.VISIBLE);
            } else {
                holder.textViewDescricao.setVisibility(View.GONE);
            }

            if (evento.getLinkReuniao() != null && !evento.getLinkReuniao().isEmpty()) {
                holder.textViewLink.setVisibility(View.VISIBLE);
                final String link = evento.getLinkReuniao();
                holder.textViewLink.setOnClickListener(v -> {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
                    context.startActivity(intent);
                });
            } else {
                holder.textViewLink.setVisibility(View.GONE);
                holder.textViewLink.setOnClickListener(null);
            }
        }

        return row;
    }

    static class ViewHolder {
        TextView textViewTitulo, textViewCurso, textViewData, textViewLocal, textViewDescricao, textViewLink;
    }
}
