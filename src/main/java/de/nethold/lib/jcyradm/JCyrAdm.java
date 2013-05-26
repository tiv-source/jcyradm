package de.nethold.lib.jcyradm;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.net.Socket;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.apache.log4j.Logger;

import de.nethold.lib.jcyradm.exception.NoPropertiesFile;

/**
 * JCyrAdm ist eine Libary die dazu dient eine Verbindung mit einem 
 * Cyrus-Imap-Server herzustellen und um dann Verwaltungsoperationen 
 * auszuführen (createMailbox, removeMailbox, etc. ).
 *
 * @author Marc Michele
 *
 */
public class JCyrAdm {

    /**
	 * Statischer Logger der Klasse JCyrAdm, zur Zeit gibt es Meldungen vom Type
	 * TRACE und DEBUG.
	 */
    private static final Logger LOGGER = Logger.getLogger(JCyrAdm.class);

    /**
     * Der Standard Imap-Port.
     */
    private static final int DEFAULT_IMAP_PORT = 143;

    /**
     * Der Standard Imap-SSL-Port.
     */
    private static final int DEFAULT_IMAP_SSL_PORT = 993;

    /**
     * Die Standard Properties Datei.
     */
    private static final String DEFAULT_PROPERTIES_FILE = "jcyradm.properties";

    /**
     * Cyrus Imap-Host zu dem die Verbindung aufgebaut werden soll.
     */
    private String host = "localhost";

    /**
     * Port auf dem der Cyrus Server lauscht.
     */
    private Integer port;

    /**
     * Default ACL.
     */
    private String allacl = "lrswipcda";

    /**
     * Administrator mit dem die Verbindung aufgebaut werden soll.
     */
    private String administrator;

    /**
     * Passwort des Administrators.
     */
    private String password;

    /**
     * Belegter Speicherplatz der Mailbox.
     */
    private BigDecimal used;


    /**
     * Zugeordneter Speicherplatz der Mailbox.
     */
    private BigDecimal quota;

    /**
     * Prozentuale Belegung der Mailbox.
     */
    private BigDecimal load;

    /**
     * Willkommens-Nachricht des Servers.
     */
    private String welcomeMsg;

    /**
     * SSL-Socket-Verbindungs-Objekt.
     */
    private SSLSocket sslRequestSocket;

    /**
     * Socket-Verbindungs-Objekt.
     */
    private Socket requestSocket;

    /**
     * Der Stream mit dem zu Server geschrieben wird.
     */
    private PrintStream out;

    /**
     * Der Stream mit dem vom Server gelesen wird.
     */
    private BufferedReader in;

    /**
     * Map mit den ACLs der aktuellen Mailbox (User/ACL).
     */
    private Map<String, String> alcs;

    /**
     * Map mit den Rückgabewerten des ID Kommandos.
     */
    private Map<String, String> idMap = new HashMap<String, String>();

    /**
     * Property Datei in der die Einstellungen gespeichert werden.
     */
    private Properties props;

    /**
     * Standard Konstruktor der Klasse JCyrAdm, dabei wird die interne
     * Properties-Datei benutzt.
     *
     * @throws NoPropertiesFile - Ausnahme wenn die Properties-Datei nicht
     *             gefunden wird.
     */
    public JCyrAdm() throws NoPropertiesFile {
        super();
        LOGGER.debug("Aktuelle Sprache: " + Locale.getDefault().getLanguage());
        props = new Properties();
        try {
            LOGGER.debug("Lade Standard Properties Datei.");
            InputStream inputStream = getClass().getClassLoader()
                    .getResourceAsStream(DEFAULT_PROPERTIES_FILE);
            props.load(inputStream);
            inputStream.close();
        } catch (Exception e1) {
            throw new NoPropertiesFile();
        }
    }// Ende JCyrAdm()

    /**
     * Konstruktor der Klasse JCyrAdm, es muss eine Properties-Datei angegeben
     * werden.
     *
     * @param properties - Properties-Datei
     * @throws NoPropertiesFile - Ausnahme wenn die Properties-Datei nicht
     *             gefunden wird.
     */
    public JCyrAdm(String properties) throws NoPropertiesFile {
        super();
        LOGGER.debug("Aktuelle Sprache: " + Locale.getDefault().getLanguage());
        props = new Properties();
        try {
            LOGGER.debug("Lade Properties Datei.");
            InputStream inputStream = new FileInputStream(properties);
            props.load(inputStream);
            inputStream.close();
        } catch (Exception e1) {
            throw new NoPropertiesFile();
        }
    }// Ende JCyrAdm(String properties)

    /**
     * Methode um eine Verbindung zum Server aufzubauen, es muss der Parameter
     * "ssl" gesetzt werden. Wenn TRUE übergeben wird dann wird eine
     * SSL-Verbindung zum angebenen Port aufgebaut.
     *
     * @param ssl - Boolean mit dem zwischen SSL und Plain umgeschaltet wird.
     * @throws IOException - Unbekannter Host oder Unmöglich den Stream zu
     *             öffnen
     */
    public final void connect(final Boolean ssl) throws IOException {
        LOGGER.trace(getText("logger.trace.connect"));
        if (ssl) {
        	LOGGER.trace("Verschlüsselte Verbindung");
            if (isNull(port)) {
                port = DEFAULT_IMAP_SSL_PORT;
            }
            SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory
                    .getDefault();
            sslRequestSocket = (SSLSocket) factory.createSocket(host, port);
            out = new PrintStream(sslRequestSocket.getOutputStream());
            out.flush();
            in = new BufferedReader(new InputStreamReader(
                    sslRequestSocket.getInputStream()));
        } else {
        	LOGGER.trace("Ungesicherte Verbindung");
            if (isNull(port)) {
                port = DEFAULT_IMAP_PORT;
            }
            requestSocket = new Socket(host, port);
            out = new PrintStream(requestSocket.getOutputStream());
            out.flush();
            in = new BufferedReader(new InputStreamReader(
                    requestSocket.getInputStream()));

        }
        welcomeMsg = in.readLine();
        LOGGER.debug("Server >| " + welcomeMsg);
    } // Ende connect()
    
    /**
     * Hilfs-Methode die prüft ob ein Object Null ist.
     *
     * @param isNull - Objekt das getestet werden soll.
     * @return Boolean - Wahrheitswert: True wenn das Objekt Null ist.
     */
    private Boolean isNull(final Object isNull) {
    	return isNull != null ? false : true;
    }// Ende isNull()

    /**
	 * Hilfs-Methode die dazu dient den String einer zu einem bestimmtem
	 * Schlüssel aus der Eigenschaftsdatei zu holen.
	 * 
	 * @param text - Schlüssel unter dem der String abgelegt ist.
	 * @return String - Der enthaltente String zum angegebenen Schlüssel oder
	 *         wenn der Schlüssel nicht gefunden wurde der übergebene String.
	 */
    private String getText(String text) {
        if ((props != null) && (props.getProperty(text) != null)) {
            return props.getProperty(text);
        }
        return text;
    }// Ende getText()

    
}// Ende class
