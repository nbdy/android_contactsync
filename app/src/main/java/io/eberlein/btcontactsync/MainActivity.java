package io.eberlein.btcontactsync;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.GsonUtils;
import com.github.tamir7.contacts.Contact;
import com.github.tamir7.contacts.Contacts;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import io.eberlein.abt.BT;
import io.eberlein.btcontactsync.dialogs.DChooseContacts;
import io.eberlein.btcontactsync.dialogs.DSync;
import io.eberlein.btcontactsync.events.EventReceivedContact;
import io.eberlein.btcontactsync.events.EventSentContacts;
import io.eberlein.btcontactsync.events.EventSyncContacts;
import io.eberlein.btcontactsync.events.EventSyncContactsCancelled;
import io.eberlein.btcontactsync.events.EventSyncDone;
import io.eberlein.btcontactsync.events.EventSyncWithDevice;
import io.eberlein.btcontactsync.viewholders.VHDevice;

public class MainActivity extends AppCompatActivity {
    private static final String SERVICE_NAME = "contactSync";
    private static final UUID SERVICE_UUID = UUID.fromString("a1a0e0f4-9c6e-4b97-8107-1c7b092ef95f");

    @BindView(R.id.rv_devices) RecyclerView recyclerView;
    @BindView(R.id.btn_search) FloatingActionButton searchBtn;
    @BindView(R.id.cb_host) CheckBox hostCb;
    @BindView(R.id.tv_status) TextView statusTv;
    @BindView(R.id.pb_search) ProgressBar searchPb;

    private IServer server = null;
    private IClient client = null;
    private Handler handler = new Handler();
    private List<Contact> syncContacts;
    private List<Contact> receivedContacts = new ArrayList<>();
    private DSync dialogSync = new DSync();
    private boolean btWasEnabled = false;
    private BluetoothDevice syncDevice;
    private boolean remoteDoneSending = false;

    private Runnable stopServerRunnable = new Runnable() {
        @Override
        public void run() {
            server.cancel(true);
            dialogSync.dismiss();
            hostCb.setChecked(false);
        }
    };

    private BT.ClassicScanner.OnEventListener eventListener = new BT.ClassicScanner.OnEventListener() {
        @Override
        public void onDeviceFound(BluetoothDevice device) {}

        @Override
        public void onDiscoveryFinished(List<BluetoothDevice> devices) {
            deviceList.addAll(devices);
            searchBtn.show();
            searchPb.setVisibility(View.INVISIBLE);
            hostCb.setVisibility(View.VISIBLE);
        }

        @Override
        public void onDiscoveryStarted() {
            searchBtn.hide();
            searchPb.setVisibility(View.VISIBLE);
            hostCb.setVisibility(View.INVISIBLE);
        }
    };

    @OnClick(R.id.btn_search)
    void onBtnSearchClicked(){
        if(!BT.isDiscovering()){
            BT.ClassicScanner.startDiscovery(eventListener);
        }
    }

    @OnCheckedChanged(R.id.cb_host)
    void onCbHostChanged(){
        if(hostCb.isChecked()) {
            searchBtn.hide();
            server = new IServer();
            new DChooseContacts().show(this);
        } else {
            searchBtn.show();
            handler.removeCallbacks(stopServerRunnable);
            server.cancel(true);
        }
    }

    private BT.OnDataReceivedInterface onDataReceivedInterface = new BT.OnDataReceivedInterface() {
        @Override
        public void onReceived(String data) {
            Log.d(data, String.valueOf(client.getSendDataQueueSize()));
            if(data.equals("DONE")) remoteDoneSending = true;
            if(remoteDoneSending && client.getSendDataQueueSize() == 0) EventBus.getDefault().post(new EventSyncDone());
            if(!data.equals("DONE") && !data.isEmpty()) {
                Log.d("onReceived", data);
                EventBus.getDefault().post(new EventReceivedContact(GsonUtils.fromJson(data, Contact.class)));
            }
        }
    };

    @SuppressLint("StaticFieldLeak")
    private class IClient extends BT.Client {
        IClient(BluetoothSocket socket){
            super(socket, onDataReceivedInterface);
        }

        @Override
        public void onReady() {
            for(Contact c : syncContacts) addSendData(GsonUtils.toJson(c));
            EventBus.getDefault().post(new EventSentContacts());
            addSendData("DONE");
        }

        @Override
        public void onFinished() {
            EventBus.getDefault().post(new EventSyncDone());
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class IServer extends BT.Server {
        IServer(){
            super(SERVICE_NAME, SERVICE_UUID);
        }

        @Override
        public void manageSocket(BluetoothSocket socket) {
            startClient(socket);
        }
    }

    private void startClient(BluetoothSocket socket){
        client = new IClient(socket);
        client.execute();
    }

    private BT.ConnectionInterface connectionInterface = new BT.ConnectionInterface() {
        @Override
        public void onConnected() {
            startClient(BT.Connector.getSocket());
        }

        @Override
        public void onDisconnected() {
            if(client != null) client.stop();
        }
    };

    private List<BluetoothDevice> deviceList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        for(String p : new String[]{
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.WRITE_CONTACTS,
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.ACCESS_FINE_LOCATION
        }){
            if(ContextCompat.checkSelfPermission(this, p) != PackageManager.PERMISSION_GRANTED) ActivityCompat.requestPermissions(this, new String[]{p}, 420);
        }

        BT.create(this);

        if(!BT.supported()){
            new AlertDialog.Builder(this).setTitle(R.string.warning).setMessage(R.string.msg_no_bt_support).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    AppUtils.exitApp();
                }
            }).show();
        } else {
            btWasEnabled = BT.isEnabled();
            if(!btWasEnabled) BT.enable();
            BT.Connector.register(this, connectionInterface);
        }

        Contacts.initialize(this);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(new RecyclerView.Adapter<VHDevice>() {
            @NonNull
            @Override
            public VHDevice onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new VHDevice(LayoutInflater.from(getApplicationContext()).inflate(R.layout.item_device, parent, false));
            }

            @Override
            public void onBindViewHolder(@NonNull VHDevice holder, int position) {
                holder.setDevice(deviceList.get(position));
            }

            @Override
            public int getItemCount() {
                return deviceList.size();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(!btWasEnabled) BT.disable();
        BT.destroy(this);
        BT.Connector.unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventSyncWithDevice(EventSyncWithDevice e){
        syncDevice = e.getObject();
        if(!BT.isDeviceBonded(syncDevice)) {
            syncDevice.createBond();
        } else {
            new DChooseContacts().show(this);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventSyncContacts(EventSyncContacts e){
        syncContacts = e.getObject();
        searchBtn.hide();
        dialogSync.show(this);
        if(hostCb.isChecked()) {
            server.execute();
            BT.setDiscoverable(this, 120);
            handler.postDelayed(stopServerRunnable, 120 * 1000);
        } else {
            BT.Connector.connect(syncDevice, SERVICE_UUID);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventSyncContactsCancelled(EventSyncContactsCancelled e){
        server.cancel(true);
        hostCb.setChecked(false);
        handler.removeCallbacks(stopServerRunnable);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventSyncDone(EventSyncDone e){
        client.stop();
        searchBtn.show();
        for(Contact c : receivedContacts) {
            Log.d("inserting", GsonUtils.toJson(c));
            Contacts.getQuery().updateContact(c);
        }
        dialogSync.done();
        hostCb.setChecked(false);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventReceivedContact(EventReceivedContact e){
        receivedContacts.add(e.getObject());
        dialogSync.setReceived(receivedContacts.size());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventSentContacts(EventSentContacts e){
        dialogSync.setSent(syncContacts.size());
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }
}
