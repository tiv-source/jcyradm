package de.nethold.lib.jcyradm.exception;

/**
 * Die Klasse NoServerResponse ist die Ausnahme die geworfen wird, wenn von dem
 * benutzen Server keine Daten empfangen werden.
 *
 * @author Marc Michele
 *
 */
public class NoServerResponse extends Exception {

    /**
     * SerialVersionUID der Klasse NoServerResponse.
     */
	private static final long serialVersionUID = -5297334996803926809L;

	/**
     * Konstruktor der Klasse NoServerResponse.
     */
    public NoServerResponse() {
        super("No Server Response.");
    }

} // Ende class