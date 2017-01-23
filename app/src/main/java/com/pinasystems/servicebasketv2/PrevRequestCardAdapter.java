package com.pinasystems.servicebasketv2;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

class PrevRequestCardAdapter extends RecyclerView.Adapter<PrevRequestCardAdapter.ViewHolder> {

    private List<PrevRequests> requests;

    PrevRequestCardAdapter(List<PrevRequests> requests){
        super();
        this.requests = requests;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.cardview_prevrequests_list, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        PrevRequests request =  requests.get(position);
        holder.textViewDate.setText(request.getDate());
        holder.textViewSubcategory.setText(request.getSubcategory());
        holder.textViewDescription.setText(request.getDescription());
    }

    @Override
    public int getItemCount() {
        return requests.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        TextView textViewDate;
        TextView textViewSubcategory;
        TextView textViewDescription;

        ViewHolder(View itemView) {
            super(itemView);
            textViewDate= (TextView) itemView.findViewById(R.id.date);
            textViewSubcategory= (TextView) itemView.findViewById(R.id.category);
            textViewDescription= (TextView) itemView.findViewById(R.id.description);
        }
    }
}