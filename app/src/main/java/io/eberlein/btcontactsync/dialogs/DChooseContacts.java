package io.eberlein.btcontactsync.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.tamir7.contacts.Contact;
import com.github.tamir7.contacts.Contacts;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import io.eberlein.btcontactsync.R;
import io.eberlein.btcontactsync.events.EventSyncContacts;

public class DChooseContacts {
    @BindView(R.id.rv_contacts)
    RecyclerView recyclerView;

    private List<Contact> chosenContacts = new ArrayList<>();

    class ContactHolder extends RecyclerView.ViewHolder {
        private Contact contact;

        @BindView(R.id.tv_name)
        TextView name;
        @BindView(R.id.cb_sync)
        CheckBox sync;

        @OnCheckedChanged(R.id.cb_sync)
        void onCbSyncChanged(){
            if(sync.isChecked()) chosenContacts.add(contact);
            else chosenContacts.remove(contact);
        }

        ContactHolder(View v){
            super(v);
            ButterKnife.bind(this, v);
        }

        void setContact(Contact contact){
            this.contact = contact;
            name.setText(contact.getDisplayName());
        }
    }

    public void show(final Context ctx){
        final List<Contact> contacts = Contacts.getQuery().find();
        recyclerView.setAdapter(new RecyclerView.Adapter<ContactHolder>() {
            @NonNull
            @Override
            public ContactHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new ContactHolder(LayoutInflater.from(ctx).inflate(R.layout.item_contact, null, false));
            }

            @Override
            public void onBindViewHolder(@NonNull ContactHolder holder, int position) {
                holder.setContact(contacts.get(position));
            }

            @Override
            public int getItemCount() {
                return contacts.size();
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(ctx));
        recyclerView.addItemDecoration(new DividerItemDecoration(ctx, DividerItemDecoration.VERTICAL));
        View v = LayoutInflater.from(ctx).inflate(R.layout.item_contact, null, false);
        ButterKnife.bind(this, v);
        new AlertDialog.Builder(ctx).setTitle(R.string.choose_contacts).setView(v).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                EventBus.getDefault().post(new EventSyncContacts(chosenContacts));
            }
        }).show();
    }
}
