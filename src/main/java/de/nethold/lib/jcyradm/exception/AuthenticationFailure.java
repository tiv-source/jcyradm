package de.nethold.lib.jcyradm.exception;

/**
 * Die Klasse AuthenticationFailure ist die Ausnahme die geworfen wird wenn der
 * Benutzername oder das Passwort nicht gültig sind.
 *
 * @author Marc Michele
 *
 */
public class AuthenticationFailure extends Exception {

    /**
     * SerialVersionUID der Klasse AuthenticationFailure.
     */
	private static final long serialVersionUID = 5655198435093474377L;

	/**
     * Konstruktor der Klasse AuthenticationFailure.
     */
    public AuthenticationFailure() {
        super("Wrong User or Password.");
    }

} // Ende class
