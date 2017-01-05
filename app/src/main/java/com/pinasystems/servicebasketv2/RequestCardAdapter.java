package com.pinasystems.servicebasketv2;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by admin on 11/2/2016.
 */
public class RequestCardAdapter extends RecyclerView.Adapter<RequestCardAdapter.ViewHolder> {

    private Context context;

    List<Requests> requests;

    public RequestCardAdapter(List<Requests> requests, Context context){
        super();
        this.requests = requests;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.cardview_requests_list, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        Requests request =  requests.get(position);
        holder.textViewName.setText(request.getName());
        holder.textViewDate.setText(request.getDate());
        holder.textViewSubcategory.setText(request.getSubcategory());
        holder.textViewDescription.setText(request.getDescription());
    }

    @Override
    public int getItemCount() {
        return requests.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        public TextView textViewName;
        public TextView textViewDate;
        public TextView textViewSubcategory;
        public TextView textViewDescription;

        public ViewHolder(View itemView) {
            super(itemView);
            textViewName = (TextView) itemView.findViewById(R.id.name);
            textViewDate= (TextView) itemView.findViewById(R.id.date);
            textViewSubcategory= (TextView) itemView.findViewById(R.id.category);
            textViewDescription= (TextView) itemView.findViewById(R.id.description);
        }
    }
}