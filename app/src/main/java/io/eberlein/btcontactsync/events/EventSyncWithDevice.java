package io.eberlein.btcontactsync.events;

import android.bluetooth.BluetoothDevice;

public class EventSyncWithDevice extends EventWithObject<BluetoothDevice> {
    private boolean allContacts;

    public EventSyncWithDevice(BluetoothDevice device){super(device); allContacts = true;}
    public EventSyncWithDevice(BluetoothDevice device, boolean allContacts){super(device); allContacts = false;}

    public boolean syncAllContacts(){return allContacts;}
}
