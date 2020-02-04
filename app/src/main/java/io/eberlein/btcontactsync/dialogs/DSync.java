package io.eberlein.btcontactsync.dialogs;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;

import androidx.appcompat.app.AlertDialog;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.eberlein.btcontactsync.R;
import io.eberlein.btcontactsync.events.EventSyncWithDevice;

public class DSync {
    @BindView(R.id.cb_all) CheckBox allContacts;

    public void show(final Context ctx, final BluetoothDevice device){
        View v = LayoutInflater.from(ctx).inflate(R.layout.dialog_sync, null, false);
        ButterKnife.bind(this, v);
        new AlertDialog.Builder(ctx).setTitle(device.getName()).setView(v).setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                EventBus.getDefault().post(new EventSyncWithDevice(device, allContacts.isChecked()));
            }
        }).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).show();
    }
}
