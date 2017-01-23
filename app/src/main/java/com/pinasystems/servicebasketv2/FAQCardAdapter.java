package com.pinasystems.servicebasketv2;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;


class FAQCardAdapter extends RecyclerView.Adapter<FAQCardAdapter.ViewHolder> {

    private List<FAQ> faqs;

    FAQCardAdapter(List<FAQ> faqs) {
        super();
        this.faqs = faqs;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.cardview_faq_list, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        FAQ faq = faqs.get(position);
        holder.textViewquestion.setText(faq.getQuestion());
        holder.textViewanswer.setText(faq.getAnswer());
    }

    @Override
    public int getItemCount() {
        return faqs.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewquestion;
        TextView textViewanswer;

        ViewHolder(View itemView) {
            super(itemView);
            textViewquestion = (TextView) itemView.findViewById(R.id.question);
            textViewanswer = (TextView) itemView.findViewById(R.id.answer);
        }
    }
}