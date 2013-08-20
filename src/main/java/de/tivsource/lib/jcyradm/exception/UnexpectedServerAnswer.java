package de.tivsource.lib.jcyradm.exception;

/**
 * Die Klasse UnexpectedServerAnswer ist eine Exception die geworfen wird,
 * wenn die Antwort des benutzen Servers nicht erwartet wurde.
 *
 * @author Marc Michele
 *
 */
public class UnexpectedServerAnswer extends Exception {

    /**
     * SerialVersionUID der Klasse UnexpectedServerAnswer.
     */
    private static final long serialVersionUID = 6807122799527025996L;

    /**
     * Konstruktor der Klasse UnexpectedServerAnswer.
     */
    public UnexpectedServerAnswer() {
        super("Unexpected Server Answer.");
    }

} // Ende class