package io.eberlein.btcontactsync.events;

import com.github.tamir7.contacts.Contact;

import java.util.List;

public class EventSyncContacts extends EventWithObject<List<Contact>> {
    public EventSyncContacts(List<Contact> c){super(c);}
}
