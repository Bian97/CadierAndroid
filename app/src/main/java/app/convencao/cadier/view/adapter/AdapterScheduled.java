package app.convencao.cadier.view.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import app.convencao.cadier.R;
import app.convencao.cadier.modelo.ServiceOrder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Created by DrGreend on 01/04/2018.
 */

public class AdapterScheduled extends ArrayAdapter<ServiceOrder>{

    private ArrayList<ServiceOrder> orderList = new ArrayList<>();
    private Context context;
    private Integer resourceId;
    private ServiceOrder serviceOrder;

    public AdapterScheduled(@NonNull Context context, int resource, @NonNull ArrayList<ServiceOrder> list) {
        super(context, resource, list);
        orderList = list;
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

            holder.textViewAgendedWish = row.findViewById(R.id.textViewPedidoAgendados);
            holder.textViewAgendedValue = row.findViewById(R.id.textViewValorAgendados);
            holder.textViewAgendedDate = row.findViewById(R.id.textViewDataAgendados);
            holder.textViewAgendedPayed = row.findViewById(R.id.textViewPagoAgendados);
            holder.textViewAgendedStillPay = row.findViewById(R.id.textViewRestaAgendados);
            holder.textViewAgendedObs = row.findViewById(R.id.textViewObsAgendados);
        } else {
            holder = (ViewHolder) row.getTag();
        }
        serviceOrder = orderList.get(position);
        if(serviceOrder != null && holder != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            holder.textViewAgendedWish.setText(serviceOrder.getServiceKind().toString());
            holder.textViewAgendedValue.setText("R$ "+ String.format("%.02f", serviceOrder.getServicePrice()));
            holder.textViewAgendedDate.setText(sdf.format(serviceOrder.getDeliveryDate()));
            holder.textViewAgendedPayed.setText("R$ "+ String.format("%.02f", serviceOrder.getPayedToday()));
            holder.textViewAgendedStillPay.setText("R$ "+ String.format("%.02f", serviceOrder.getRemains()));
            holder.textViewAgendedObs.setText(serviceOrder.getObs());
        }
        return row;
    }

    static class ViewHolder {
        TextView textViewAgendedWish, textViewAgendedValue, textViewAgendedDate, textViewAgendedPayed, textViewAgendedStillPay, textViewAgendedObs;
    }
}