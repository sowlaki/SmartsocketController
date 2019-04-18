package com.example.smartsocketcontroller;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.RecyclerView;

public class DeviceListAdapter extends RecyclerView.Adapter<DeviceListAdapter.DeviceViewHolder> {


    public ArrayList<String> DeviceName;
    public ArrayList<String> DeviceAddress;

    View.OnClickListener listener;

    private onItemClicked onClick;

    public interface onItemClicked {

        void onItemClick(int position);

    }

    public DeviceListAdapter(ArrayList<String> DeviceName, ArrayList<String> DeviceAddress, onItemClicked onClick) {
        this.DeviceName = DeviceName;
        this.DeviceAddress = DeviceAddress;

        this.onClick = onClick;
    }


    @NonNull
    @Override
    public DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.rowlayout, parent, false);
        DeviceViewHolder vh = new DeviceViewHolder(v, null);
        return vh;
    }

    @Override
    public void onBindViewHolder(DeviceViewHolder holder, final int position) {

        holder.devicename.setText(DeviceName.get(position));
        holder.deviceaddress.setText(DeviceAddress.get(position));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onClick.onItemClick(position);
            }
        });

    }

    @Override
    public int getItemCount() {
        return (DeviceName.size());
    }


    public static class DeviceViewHolder extends RecyclerView.ViewHolder {


        TextView devicename;
        TextView deviceaddress;


        public DeviceViewHolder(View itemView, View.OnClickListener listener) {
            super(itemView);

            devicename = itemView.findViewById(R.id.name);
            deviceaddress = itemView.findViewById(R.id.address);

            itemView.setOnClickListener(listener);

        }


    }

    public void setOnClick(onItemClicked onClick){

        this.onClick = onClick;
    }
}
