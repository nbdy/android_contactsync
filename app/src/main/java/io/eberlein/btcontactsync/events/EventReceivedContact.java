package io.eberlein.btcontactsync.events;

import com.github.tamir7.contacts.Contact;

public class EventReceivedContact extends EventWithObject<Contact> {
    public EventReceivedContact(Contact contact){super(contact);}
}
