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

public class AdapterAnteriores extends ArrayAdapter<OrdemServico>{

    private ArrayList<OrdemServico> listaServicos = new ArrayList<>();
    private Context context;
    private Integer resourceId;
    private OrdemServico ordemServico;

    public AdapterAnteriores(@NonNull Context context, int resource, @NonNull ArrayList<OrdemServico> lista) {
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
            holder.textViewPedido = row.findViewById(R.id.textViewPedido);
            holder.textViewDataPedido = row.findViewById(R.id.textViewDataPedido);
            holder.textViewDataEntrega = row.findViewById(R.id.textViewDataEntrega);
            holder.textViewStatus = row.findViewById(R.id.textViewStatus);
            holder.textViewObservacoes = row.findViewById(R.id.textViewObservacoes);
        } else {
            holder = (ViewHolder) row.getTag();
        }
        ordemServico = listaServicos.get(position);
        if(ordemServico != null && holder != null) {
            holder.textViewPedido.setText(ordemServico.getTipoServ());
            holder.textViewDataPedido.setText(ordemServico.getDataSoli());
            holder.textViewDataEntrega.setText(ordemServico.getDataEnt());
            holder.textViewObservacoes.setText(ordemServico.getObs());

            if ((ordemServico.getDataEnt().equalsIgnoreCase("") || ordemServico.getDataEnt().equalsIgnoreCase("  /  /    ")) || ordemServico.getResta() > 0) {
                holder.textViewStatus.setText("Pendência!");
            } else {
                holder.textViewStatus.setText("Finalizado!");
            }
        }
        return row;
    }

    static class ViewHolder {
        TextView textViewPedido, textViewDataPedido, textViewDataEntrega, textViewStatus, textViewObservacoes;
    }
}