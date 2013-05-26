package de.nethold.lib.jcyradm;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.net.Socket;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import javax.net.ssl.SSLSocket;

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

    
    
}// Ende class
