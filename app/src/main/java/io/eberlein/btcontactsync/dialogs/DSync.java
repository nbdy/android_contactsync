package io.eberlein.btcontactsync.dialogs;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;


import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.eberlein.btcontactsync.R;

public class DSync {
    @BindView(R.id.tv_received) TextView received;
    @BindView(R.id.tv_sent) TextView sent;
    @BindView(R.id.btn_ok) Button btnOk;
    @BindView(R.id.pb_sync) ProgressBar syncPb;

    @OnClick(R.id.btn_ok)
    void onBtnOkClicked(){
        dialog.dismiss();

    }

    private AlertDialog dialog;

    public void show(final Context ctx){
        View v = LayoutInflater.from(ctx).inflate(R.layout.dialog_sync, null, false);
        ButterKnife.bind(this, v);
        dialog = new AlertDialog.Builder(ctx).setTitle(R.string.syncing_with).setView(v).setCancelable(false).show();
        setSent(0);
        setReceived(0);
    }

    public void done(){
        btnOk.setVisibility(View.VISIBLE);
        syncPb.setVisibility(View.INVISIBLE);
    }

    public void setReceived(int count){
        received.setText(String.valueOf(count));
    }

    public void setSent(int count){
        sent.setText(String.valueOf(count));
    }

    public void dismiss(){
        dialog.dismiss();
    }
}
