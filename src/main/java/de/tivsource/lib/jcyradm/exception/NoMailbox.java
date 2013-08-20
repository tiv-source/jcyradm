package de.tivsource.lib.jcyradm.exception;

/**
 * Die Klasse NoMailbox ist die Ausnahme die geworfen wird, wenn es die benutze
 * Mailbox nicht gibt.
 *
 * @author Marc Michele
 *
 */
public class NoMailbox extends Exception {

    /**
     * SerialVersionUID der Klasse NoMailbox.
     */
    private static final long serialVersionUID = -642162588858173904L;

    /**
     * Konstruktor der Klasse NoMailbox.
     */
    public NoMailbox() {
        super("Mailbox not exists");
    }

} // Ende class
