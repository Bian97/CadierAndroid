package com.example.cadier.view.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.cadier.R;
import com.example.cadier.modelo.ServiceOrder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Created by DrGreend on 01/04/2018.
 */

public class AdapterPrevious extends ArrayAdapter<ServiceOrder>{

    private ArrayList<ServiceOrder> ordersList = new ArrayList<>();
    private Context context;
    private Integer resourceId;
    private ServiceOrder serviceOrder;

    public AdapterPrevious(@NonNull Context context, int resource, @NonNull ArrayList<ServiceOrder> list) {
        super(context, resource, list);
        ordersList = list;
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
            holder.textViewWish = row.findViewById(R.id.textViewPedido);
            holder.textViewDateWish = row.findViewById(R.id.textViewDataPedido);
            holder.textViewDateGive = row.findViewById(R.id.textViewDataEntrega);
            holder.textViewStatus = row.findViewById(R.id.textViewStatus);
            holder.textViewObs = row.findViewById(R.id.textViewObservacoes);
        } else {
            holder = (ViewHolder) row.getTag();
        }
        serviceOrder = ordersList.get(position);
        if(serviceOrder != null && holder != null) {
            holder.textViewWish.setText(serviceOrder.getService());
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            holder.textViewDateWish.setText(sdf.format(serviceOrder.getOrderDate()));
            holder.textViewDateGive.setText(sdf.format(serviceOrder.getDeliveryDate()));
            holder.textViewObs.setText(serviceOrder.getObs() == null ? "Não há!" : serviceOrder.getObs());

            if ((serviceOrder.getDeliveryDate() == null || serviceOrder.getDeliveryDate().before(new GregorianCalendar(2000, Calendar.JANUARY,01).getTime())) || serviceOrder.getRemains() > 0) {
                holder.textViewStatus.setText("Pendência!");
            } else {
                holder.textViewStatus.setText("Finalizado!");
            }
        }
        return row;
    }

    static class ViewHolder {
        TextView textViewWish, textViewDateWish, textViewDateGive, textViewStatus, textViewObs;
    }
}