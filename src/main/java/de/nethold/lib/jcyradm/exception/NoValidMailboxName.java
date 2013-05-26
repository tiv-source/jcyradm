package de.nethold.lib.jcyradm.exception;

/**
 * Die Klasse NoValidMailboxName ist eine Exception die geworfen wird, wenn der
 * übergebene Mailbox-Name nicht gültig ist.
 *
 * @author Marc Michele
 *
 */
public class NoValidMailboxName extends Exception {

    /**
     * SerialVersionUID der Klasse NoValidMailboxName.
     */
	private static final long serialVersionUID = -3773163689804829543L;

	/**
     * Konstruktor der Klasse NoValidMailboxName.
     */
    public NoValidMailboxName() {
        super("No valid Mailbox Name.");
    }

} // Ende class