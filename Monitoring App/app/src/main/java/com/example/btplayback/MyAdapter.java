package com.example.btplayback;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

    private static final String TAG = "MyAdapter";
    private ArrayList<BluetoothDevice> btDevices;
    private Context mContext;
    private DeviceSelector deviceSelector;

    public MyAdapter(ArrayList<BluetoothDevice> btDevices, Context mContext,DeviceSelector deviceSelector) {
        this.btDevices = btDevices;
        this.mContext = mContext;
        this.deviceSelector = deviceSelector;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.list_item,parent,false);
        return new ViewHolder(view,deviceSelector);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ((ViewHolder)holder).deviceName.setText(btDevices.get(position).getName());
    }

    @Override
    public int getItemCount() {
        return btDevices.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView deviceName;
        private DeviceSelector deviceSelector;

        public ViewHolder(@NonNull View itemView,DeviceSelector deviceSelector) {
            super(itemView);
            deviceName = itemView.findViewById(R.id.deviceName);
            itemView.setOnClickListener(this);
            this.deviceSelector = deviceSelector;
        }

        @Override
        public void onClick(View v) {
            deviceSelector.onDeviceSelected(getAdapterPosition());
        }
    }

    public interface DeviceSelector{
        void onDeviceSelected(int position);
    }
}
