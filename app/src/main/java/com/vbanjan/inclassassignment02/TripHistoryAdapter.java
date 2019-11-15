package com.vbanjan.inclassassignment02;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class TripHistoryAdapter extends RecyclerView.Adapter<TripHistoryAdapter.ViewHolder> {

    ArrayList<TripHistory> tripHistories;

    public TripHistoryAdapter(ArrayList<TripHistory> tripHistories) {
        this.tripHistories=tripHistories;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.trip_history_detail,parent,false);
        ViewHolder viewHolder=new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
      TripHistory tripHistory=tripHistories.get(position);
      holder.from.setText(tripHistory.From);
      holder.to.setText(tripHistory.To);
      holder.status.setText(tripHistory.Status);
    }

    @Override
    public int getItemCount() {
        return tripHistories.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        EditText from,to;
        TextView status;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            from=itemView.findViewById(R.id.from);
            status=itemView.findViewById(R.id.textView5);
            from.setEnabled(false);
            to=itemView.findViewById(R.id.to);
            to.setEnabled(false);
        }
    }
}
