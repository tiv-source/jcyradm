package de.nethold.lib.jcyradm.exception;

/**
 * Die Klasse NoPropertiesFile ist die Ausnahme die geworfen wird, wenn es die
 * angegebene Eigenschaftsdatei nicht gibt.
 *
 * @author Marc Michele
 *
 */
public class NoPropertiesFile extends Exception {

    /**
     * SerialVersionUID der Klasse NoPropertiesFile.
     */
    private static final long serialVersionUID = -9132279462449363816L;

    /**
     * Konstruktor der Klasse NoPropertiesFile.
     */
    public NoPropertiesFile() {
        super("Properties File not exists");
    }

}// Ende class