package com.example.cadier.view.adapter;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.cadier.R;
import com.example.cadier.modelo.OrdemServico;

import java.util.ArrayList;

/**
 * Created by DrGreend on 01/04/2018.
 */

public class AdapterAgendados extends ArrayAdapter<OrdemServico>{

    private ArrayList<OrdemServico> listaServicos = new ArrayList<>();
    private Context context;
    private Integer resourceId;
    private OrdemServico ordemServico;

    public AdapterAgendados(@NonNull Context context, int resource, @NonNull ArrayList<OrdemServico> lista) {
        super(context, resource, lista);
        listaServicos = lista;
        this.context = context;
        this.resourceId = resource;
    }

    public View getView(final int position, View convertView, ViewGroup parent){
        View row = convertView;
        ViewHolder holder = null;

        if(row == null){
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(resourceId, parent, false);
            holder = new ViewHolder();

            holder.textViewPedidoAgendados = row.findViewById(R.id.textViewPedidoAgendados);
            holder.textViewValorAgendados = row.findViewById(R.id.textViewValorAgendados);
            holder.textViewDataAgendados = row.findViewById(R.id.textViewDataAgendados);
            holder.textViewPagoAgendados = row.findViewById(R.id.textViewPagoAgendados);
            holder.textViewRestaAgendados = row.findViewById(R.id.textViewRestaAgendados);
            holder.textViewObsAgendados = row.findViewById(R.id.textViewObsAgendados);
        } else {
            holder = (ViewHolder) row.getTag();
        }
        ordemServico = listaServicos.get(position);
        if(ordemServico != null && holder != null) {
            holder.textViewPedidoAgendados.setText(ordemServico.getTipoServ());
            holder.textViewValorAgendados.setText("R$ "+ String.format("%.02f",ordemServico.getValorServ()));
            holder.textViewDataAgendados.setText(ordemServico.getDataEnt());
            holder.textViewPagoAgendados.setText("R$ "+ String.format("%.02f",ordemServico.getPgHj()));
            holder.textViewRestaAgendados.setText("R$ "+ String.format("%.02f",ordemServico.getResta()));
            holder.textViewObsAgendados.setText(ordemServico.getObs());
        }
        return row;
    }

    static class ViewHolder {
        TextView   textViewPedidoAgendados, textViewValorAgendados, textViewDataAgendados, textViewPagoAgendados, textViewRestaAgendados, textViewObsAgendados;
    }
}