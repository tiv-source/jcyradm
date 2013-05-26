package de.nethold.lib.jcyradm.exception;

/**
 * Die Klasse UnexpectedExtraArguments ist eine Exception die geworfen wird,
 * wenn an den benutzen Server ein Kommando mit einem zusätzlichen Parameter
 * gesendet wird das der Server nicht kennt.
 *
 * @author Marc Michele
 *
 */
public class UnexpectedExtraArguments extends Exception {

    /**
     * SerialVersionUID der Klasse UnexpectedExtraArguments.
     */
	private static final long serialVersionUID = 2801608929489543738L;

	/**
     * Konstruktor der Klasse UnexpectedExtraArguments.
     */
    public UnexpectedExtraArguments() {
        super("Unexpected Extra Arguments");
    }

} // Ende class