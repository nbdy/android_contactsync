package io.eberlein.btcontactsync.viewholders;

import android.bluetooth.BluetoothDevice;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.eberlein.btcontactsync.R;
import io.eberlein.btcontactsync.events.EventSyncWithDevice;

public class VHDevice extends RecyclerView.ViewHolder {
    private BluetoothDevice device;

    @BindView(R.id.tv_name) TextView name;
    @BindView(R.id.tv_addr) TextView addr;

    @OnClick
    void onClick(){
        EventBus.getDefault().post(new EventSyncWithDevice(device));
    }

    public VHDevice(View v){
        super(v);
        ButterKnife.bind(this, v);
    }

    public void setDevice(BluetoothDevice device){
        this.device = device;
        name.setText(device.getName());
        addr.setText(device.getAddress());
    }
}
