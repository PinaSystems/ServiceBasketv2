package com.pinasystems.servicebasketv2;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

class ProviderCardAdapter extends RecyclerView.Adapter<ProviderCardAdapter.ViewHolder> {

    private List<Providers> providers;

    ProviderCardAdapter(List<Providers> provider){
        super();
        this.providers = provider;
    }

    @Override
    public ProviderCardAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.cardview_providers_list, parent, false);
        return new ProviderCardAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ProviderCardAdapter.ViewHolder holder, int position) {

        Providers provider =  providers.get(position);
        holder.textViewDate.setText(provider.getEst_date() + " at " + provider.getEst_time());
        holder.textViewName.setText(provider.getName());
        holder.textViewPrice.setText(provider.getMin_price() + " to " + provider.getMax_price());
        String rating = provider.getRating() + "/5";
        holder.textViewrating.setText(rating);
    }

    @Override
    public int getItemCount() {
        return providers.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        TextView textViewDate , textViewName , textViewPrice , textViewrating;

        ViewHolder(View itemView) {
            super(itemView);
            textViewDate= (TextView) itemView.findViewById(R.id.date);
            textViewName= (TextView) itemView.findViewById(R.id.name);
            textViewPrice= (TextView) itemView.findViewById(R.id.price_range);
            textViewrating = (TextView) itemView.findViewById(R.id.viewrating);
        }
    }
}
