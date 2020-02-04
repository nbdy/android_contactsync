package io.eberlein.btcontactsync.viewholders;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import butterknife.ButterKnife;

public class VH extends RecyclerView.ViewHolder {
    public VH(View v){
        super(v);
        ButterKnife.bind(this, v);
    }
}
