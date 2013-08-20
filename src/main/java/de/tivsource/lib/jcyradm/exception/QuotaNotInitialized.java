package de.tivsource.lib.jcyradm.exception;

/**
 * Die Klasse QuotaNotInitialized ist die Ausnahme die geworfen wird, wenn beim
 * abfragen des belegten Speicherplatzes oder der aktuellen
 * Speicherplatzbegrenzung die benötigen Daten noch nicht vom Server empfangen
 * wurden.
 *
 * @author Marc Michele
 *
 */
public class QuotaNotInitialized extends Exception {

    /**
     * SerialVersionUID der Klasse QuotaNotInitialized.
     */
    private static final long serialVersionUID = 3382327488288130452L;

    /**
     * Konstruktor der Klasse QuotaNotInitialized.
     */
    public QuotaNotInitialized() {
        super("Quota not initialized");
    }

} // Ende class
