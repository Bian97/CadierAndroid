package app.convencao.cadier.view.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import app.convencao.cadier.R;
import app.convencao.cadier.modelo.ServiceOrder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

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
            holder.textViewDateWish.setText(serviceOrder.getOrderDate() != null ? sdf.format(serviceOrder.getOrderDate()) : "-");
            holder.textViewDateGive.setText(serviceOrder.getDeliveryDate() != null ? sdf.format(serviceOrder.getDeliveryDate()) : "-");
            holder.textViewObs.setText(serviceOrder.getService());

            GradientDrawable chip = new GradientDrawable();
            chip.setShape(GradientDrawable.RECTANGLE);
            chip.setCornerRadius(context.getResources().getDimension(R.dimen.cadier_radius_chip));
            if (serviceOrder.isPendente()) {
                holder.textViewStatus.setText("Pendência!");
                chip.setColor(ContextCompat.getColor(context, R.color.status_warning));
            } else {
                holder.textViewStatus.setText("Finalizado!");
                chip.setColor(ContextCompat.getColor(context, R.color.status_success));
            }
            holder.textViewStatus.setBackground(chip);
        }
        return row;
    }

    static class ViewHolder {
        TextView textViewWish, textViewDateWish, textViewDateGive, textViewStatus, textViewObs;
    }
}