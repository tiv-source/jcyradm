package de.nethold.lib.jcyradm.exception;

/**
 * Die Klasse NoServerStream ist die Ausnahme die geworfen wird, wenn beim
 * zugriff auf den angegebenen Server kein Daten-Strom-Objekt vorhanden ist.
 *
 * @author Marc Michele
 *
 */
public class NoServerStream extends Exception {

    /**
     * SerialVersionUID der Klasse NoServerStream.
     */
    private static final long serialVersionUID = -1062981640943227217L;

    /**
     * Konstruktor der Klasse NoServerStream.
     */
    public NoServerStream() {
        super("No Server Stream open.");
    }

} // Ende class