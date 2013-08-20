package de.tivsource.lib.jcyradm.exception;

/**
 * Die Klasse NoServerAnswerFile ist die Ausnahme die geworfen wird, wenn die
 * Server Antwort Datei nicht gefunden wurde.
 *
 * @author Marc Michele
 *
 */
public class NoServerAnswerFile extends Exception {

    /**
     * SerialVersionUID der Klasse NoServerAnswerFile.
     */
    private static final long serialVersionUID = -8000816274189914156L;

    /**
     * Konstruktor der Klasse NoServerAnswerFile.
     */
    public NoServerAnswerFile() {
        super("Server Answer File not exists");
    }

} // Ende class
