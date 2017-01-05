package com.pinasystems.servicebasketv2;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by admin on 11/15/2016.
 */

public class AddressCardAdapter extends RecyclerView.Adapter<AddressCardAdapter.ViewHolder> {

    private Context context;

    List<Addresses> addresses;

    public AddressCardAdapter(List<Addresses> addresses, Context context) {
        super();
        this.addresses = addresses;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.cardview_address_list, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        Addresses address = addresses.get(position);
        String label = address.getLabel() + ":";
        holder.textViewLabel.setText(label);
        holder.textViewAddress.setText(address.getAddress());
    }

    @Override
    public int getItemCount() {
        return addresses.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewLabel;
        TextView textViewAddress;

        ViewHolder(View itemView) {
            super(itemView);
            textViewLabel = (TextView) itemView.findViewById(R.id.label);
            textViewAddress = (TextView) itemView.findViewById(R.id.address);
        }
    }
}