package de.nethold.lib.jcyradm.exception;

/**
 * Die Klasse MailboxExists ist die Ausnahme die geworfen wird wenn beim anlegen
 * einer Mailbox schon eine Mailbox mit gleichem Namen existiert.
 *
 * @author Marc Michele
 *
 */
public class MailboxExists extends Exception {

    /**
     * SerialVersionUID der Klasse MailboxExists.
     */
    private static final long serialVersionUID = -7391872070090203945L;

    /**
     * Konstruktor der Klasse MailboxExists.
     */
    public MailboxExists() {
        super("Mailbox already exists");
    }

} // Ende class
