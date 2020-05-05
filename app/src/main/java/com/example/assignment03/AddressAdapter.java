package com.example.assignment03;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

/* Recycler Adapter for formatting and storing address data */
public class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.AddressViewHolder> {

    private Context context; //activity context
    private ArrayList<AddressItem> addressList; //list to hold address items
    private OnItemClickListener listener; //onclicklistener instance

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener lsnr) {
        listener = lsnr;
    }

    public AddressAdapter(Context cntx, ArrayList<AddressItem> list) {
        context = cntx;
        addressList = list;
    }

    @NonNull
    @Override
    public AddressViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v  = LayoutInflater.from(context).inflate(R.layout.address_item, parent, false);
        return new AddressViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull AddressViewHolder holder, int position) {
        AddressItem currentItem = addressList.get(position);

        String address = currentItem.getAddress();

        holder.number.setText("[" + (position+1) + "]");
        holder.address.setText(address);
        if(position%2==1) {
            holder.background.setBackgroundResource(R.color.mapboxGrayLight);
        }
    }

    @Override
    public int getItemCount() {
        return addressList.size();
    }

    public class AddressViewHolder extends RecyclerView.ViewHolder {

        public TextView number; //number seen in address recycler
        public TextView address; //address text seen in address recycler
        public LinearLayout background; //layout instance to change background color

        public AddressViewHolder(@NonNull View itemView) {
            super(itemView);
            number = itemView.findViewById(R.id.address_item_number);
            address = itemView.findViewById(R.id.address_item_address);
            background = itemView.findViewById(R.id.background);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onItemClick(position);
                        }
                    }
                }
            });
        }
    }
}
