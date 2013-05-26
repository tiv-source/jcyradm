package de.nethold.lib.jcyradm.exception;

/**
 * Die Klasse NoQuota ist die Ausnahme die geworfen wird, wenn für die
 * angegebene Mailbox keine Quota angelegt wurde.
 *
 * @author Marc Michele
 *
 */
public class NoQuota extends Exception {

    /**
     * SerialVersionUID der Klasse NoQuota.
     */
	private static final long serialVersionUID = -3305938792286503828L;

	/**
     * Konstruktor der Klasse NoQuota.
     */
    public NoQuota() {
        super("No Mailbox Quota set.");
    }

} // Ende class
