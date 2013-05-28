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
import java.util.regex.Pattern;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.apache.log4j.Logger;

import de.nethold.lib.jcyradm.exception.AuthenticationFailure;
import de.nethold.lib.jcyradm.exception.MailboxExists;
import de.nethold.lib.jcyradm.exception.NoMailbox;
import de.nethold.lib.jcyradm.exception.NoPropertiesFile;
import de.nethold.lib.jcyradm.exception.NoQuota;
import de.nethold.lib.jcyradm.exception.NoServerAnswerFile;
import de.nethold.lib.jcyradm.exception.NoServerResponse;
import de.nethold.lib.jcyradm.exception.NoServerStream;
import de.nethold.lib.jcyradm.exception.NoValidMailboxName;
import de.nethold.lib.jcyradm.exception.QuotaNotInitialized;
import de.nethold.lib.jcyradm.exception.UnexpectedExtraArguments;
import de.nethold.lib.jcyradm.exception.UnexpectedServerAnswer;

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
     * Die Standard Antwort Datei.
     */
    private static final String DEFAULT_ANSWER_FILE = "server.properties";

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
    private Map<String, String> acls;

    /**
     * Map mit den Rückgabewerten des ID Kommandos.
     */
    private Map<String, String> idMap = new HashMap<String, String>();

    /**
     * Property Datei in der die Einstellungen gespeichert werden.
     */
    private Properties props;

    /**
     * Datei mit den erwarteten Server-Anworten.
     */
    private Properties serverAnswers;

    /**
     * Standard Konstruktor der Klasse JCyrAdm, dabei wird die interne
     * Properties-Datei benutzt.
     *
     * @throws NoPropertiesFile - Ausnahme wenn die Properties-Datei nicht
     *             gefunden wird.
     * @throws NoServerAnswerFile 
     */
    public JCyrAdm() throws NoPropertiesFile, NoServerAnswerFile {
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
        try {
            LOGGER.debug("Lade Server Antworten Datei.");
            InputStream inputStream = getClass().getClassLoader()
                    .getResourceAsStream(DEFAULT_ANSWER_FILE);
            serverAnswers.load(inputStream);
            inputStream.close();
        } catch (Exception e1) {
            throw new NoServerAnswerFile();
        }
    }// Ende JCyrAdm()

    /**
     * Konstruktor der Klasse JCyrAdm, es muss eine Properties-Datei angegeben
     * werden.
     *
     * @param properties - Properties-Datei
     * @throws NoPropertiesFile - Ausnahme wenn die Properties-Datei nicht
     *             gefunden wird.
     * @throws NoServerAnswerFile 
     */
    public JCyrAdm(String properties) throws NoPropertiesFile, NoServerAnswerFile {
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
        try {
            LOGGER.debug("Lade Server Antworten Datei.");
            InputStream inputStream = getClass().getClassLoader()
                    .getResourceAsStream(DEFAULT_ANSWER_FILE);
            serverAnswers.load(inputStream);
            inputStream.close();
        } catch (Exception e1) {
            throw new NoServerAnswerFile();
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
        	LOGGER.trace("öffne Verschlüsselte Verbindung");
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
        	LOGGER.trace("öffne Ungesicherte Verbindung");
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
	 * Methode um die Verbindung zum Server zu trennen.
	 * 
	 * @throws IOException - wenn der Stream schon geschlossen ist oder
	 *             Verbindung abgelaufen ist.
	 */
    public final void disconnect() throws IOException {
        LOGGER.trace(getText("logger.trace.disconnect"));
        if (sslRequestSocket != null) {
        	LOGGER.trace("schließe Verschlüsselte Verbindung");
            sslRequestSocket.close();
        } else {
        	LOGGER.trace("öffne Ungesicherte Verbindung");
            requestSocket.close();
        }
    } // disconnect()

    /**
     * Unfertige Methode !!!! Diese Methode muss dringent überarbeitet werden.
     * Holt die Capability und setzt die Default-ACLs. .
     *
     * @throws IOException - InputStream/OutputStream geschlossen oder nicht
     *             vorhanden
     */
    public final void capability() throws IOException {
        LOGGER.trace("capability() aufgerufen.");
        sendCommand(". capability");
        String line = in.readLine();
        LOGGER.debug("Server >| " + line);
        //System.out.println("Server >| " + line);
        line = in.readLine();
        LOGGER.debug("Server >| " + line);
        //System.out.println("Server >| " + line);

        // // TODO Hier mus noch die Acl Abfrage hin ist jetzt von Hand gesetzt
        allacl = "lrswipkxtecda";
    }

    /**
     * Mit dieser Methode wird der Administrationsbenutzer am Server
     * angemeldet.
     *
     * @throws NoServerResponse - Keine Antwort vom Server erhalten.
     * @throws UnexpectedServerAnswer - Unerwartete Antwort vom Server.
     * @throws AuthenticationFailure 
     */
    public final void login() throws NoServerResponse, UnexpectedServerAnswer, AuthenticationFailure {
        LOGGER.trace("login() aufgerufen.");
        sendCommand(". login \"" + administrator + "\" \"" + password + "\"");
        try {
            // Lese Antwort vom Server
            String line = in.readLine();
            LOGGER.debug("Server >| " + line);

            // Wenn User oder Passwort falsch
            if(getText("server.answer.login.failed")
                    .contentEquals(new StringBuffer(line))) {
                LOGGER.error("Fehler >| " + line);
                throw new AuthenticationFailure();
            }
            // Wenn Benutzer erfolgreich angemeldet wurde
            else if(Pattern.matches(getText("server.answer.login"), line)) {
                LOGGER.info("Authen >| " + line);
            }
            // In allen anderen Fällen
            else {
                System.out.println(getText("server.answer.login"));
                LOGGER.error("Fehler >| " + line);
                throw new UnexpectedServerAnswer();
            }

        } catch (IOException e) {
            throw new NoServerResponse();
        }
    }// Ende login()

    /**
     * Mit dieser Methode meldet man sich vom Server ab, es werden auch alle
     * Streams geschlossen.
     *
     * @throws NoServerResponse - Keine Antwort vom Server.
     * @throws NoServerStream - Kein Stream vom Server vorhanden.
     * @throws UnexpectedServerAnswer - Unerwartete Server Antwort erhalten.
     */
    public final void logout() throws NoServerResponse, NoServerStream, UnexpectedServerAnswer {
        LOGGER.trace("logout() aufgerufen.");

        // Sende Logout Nachricht
        sendCommand(". logout");

        try {
            // Werte erste Server Antwort aus
            String line = in.readLine();
            LOGGER.debug("Server >| " + line);
            if(!getText("server.answer.logout")
                    .contentEquals(new StringBuffer(line))) {
                LOGGER.error("Fehler >| " + line);
                throw new UnexpectedServerAnswer();
            }
        } catch (IOException e) {
            LOGGER.error("Fehler >| Keine Antwort von Server erhalten");
            throw new NoServerResponse();
        }

        try {
            // Werte zweite Server Antwort aus
            String line = in.readLine();
            LOGGER.debug("Server >| " + line);
            if(!getText("server.answer.ok")
                    .contentEquals(new StringBuffer(line))) {
                LOGGER.error("Fehler >| " + line);
                throw new UnexpectedServerAnswer();
            }
        } catch (IOException e) {
            LOGGER.error("Fehler >| Keine Antwort von Server erhalten");
            throw new NoServerResponse();
        }

        try {
            // Schließe InputStream
            in.close();
        } catch (IOException e) {
            LOGGER.error("Fehler >| Keine Stream vom Server vorhanden");
            throw new NoServerStream();
        }
        // Schließe OutputStream
        out.close();
    }// Ende logout()

    /**
     * Mit dieser Methode können die ACLs einer bestimmten Mailbox abgefragt
     * werden.
     *
     * @param mailbox - Die Mailbox für die die ACLs abgefragt werden sollen
     * @throws NoValidMailboxName - // TODO Dokumentation
     * @throws NoServerResponse 
     * @throws UnexpectedServerAnswer 
     */
    public final void acl(final String mailbox) throws NoValidMailboxName,
            NoServerResponse, UnexpectedServerAnswer {
        /*
         * Prüfen ob der übergebene Mailboxname gültig ist.
         */
        if (!isValid(mailbox)) {
            LOGGER.error("Fehler >| Ungültiger Mailboxname");
            throw new NoValidMailboxName();
        }

        /*
         * Kommando absetzen.
         */
        sendCommand(". getacl \"user." + mailbox + "\"");

        /*
         * Erste Antwortzeile einlesen.
         */
        try {
            String line = in.readLine();
            LOGGER.debug("Server >| " + line);

            if(!Pattern.matches(getText("server.answer.acl"), line)) {
                LOGGER.error("Fehler >| " + line);
                throw new UnexpectedServerAnswer();
            }

            acls = new HashMap<String, String>();
            String keys[] = line.split(" ");
            for(int i=0; i < keys.length; i++) {
                if(i > 2) {
                    if(i % 2 == 1) {
                        acls.put(keys[i], keys[i+1]);
                    }
                }
            }

        } catch (IOException e) {
            LOGGER.error("Fehler >| Keine Antwort von Server erhalten");
            throw new NoServerResponse();
        }

        /*
         * Zweite Antwortzeile einlesen.
         */
        try {
            String line = in.readLine();
            LOGGER.debug("Server >| " + line);
            if(!getText("server.answer.ok")
                    .contentEquals(new StringBuffer(line))) {
                LOGGER.error("Fehler >| " + line);
                throw new UnexpectedServerAnswer();
            }
        } catch (IOException e) {
            LOGGER.error("Fehler >| Keine Antwort von Server erhalten");
            throw new NoServerResponse();
        }
    }// Ende acl(String)

    /**
     * Mit dieser Methode können für eine bestimmte Mailbox, Rechte für einen
     * bestimmten Benutzer gesetzt werden.
     *
     * @param mailbox - Die Mailbox für die die Rechte gesetzt werden sollen.
     * @param user - Benutzer für die die Rechte gelten sollen.
     * @param acl - Rechte die für den Benutzer gelten sollen.
     * @throws NoValidMailboxName -
     * @throws NoServerResponse 
     * @throws UnexpectedServerAnswer 
     */
    public final void setAcl(final String mailbox, final String user,
            final String acl) throws NoValidMailboxName, NoServerResponse, UnexpectedServerAnswer {
        /*
         * Prüfen ob der übergebene Mailboxname gültig ist.
         */
        if (!isValid(mailbox)) {
            LOGGER.error("Fehler >| Ungültiger Mailboxname");
            throw new NoValidMailboxName();
        }

        /*
         * Kommando absetzen.
         */
        sendCommand(". setacl \"user." + mailbox + "\" \"" + user + "\" " + acl);

        /*
         * Einlesen der Antwortzeile.
         */
        try {
            String line = in.readLine();
            LOGGER.debug("Server >| " + line);
            if(!getText("server.answer.ok")
                    .contentEquals(new StringBuffer(line))) {
                LOGGER.error("Fehler >| " + line);
                throw new UnexpectedServerAnswer();
            }
        } catch (IOException e) {
            LOGGER.error("Fehler >| Keine Antwort von Server erhalten");
            throw new NoServerResponse();
        }
    }// Ende setAcl(String, String, String)


    /**
     * Mit dieser Methode können die Rechte einer Mailbox die für einen
     * bestimmten Benutzer existieren gelöscht werden.
     *
     * @param mailbox - Mailbox für die die Rechte gelöscht werden sollen.
     * @param user - Benutzer für den die Rechte gelöscht werden sollen.
     * @throws NoValidMailboxName - // TODO Dokumentation.
     * @throws NoServerResponse 
     * @throws UnexpectedServerAnswer 
     */
    public final void deleteAcl(final String mailbox, final String user)
            throws NoValidMailboxName, NoServerResponse, UnexpectedServerAnswer {
        /*
         * Prüfen ob der übergebene Mailboxname gültig ist.
         */
        if (!isValid(mailbox)) {
            LOGGER.error("Fehler >| Ungültiger Mailboxname");
            throw new NoValidMailboxName();
        }

        /*
         * Kommando absetzen.
         */
        sendCommand(". deleteacl \"user." + mailbox + "\" \"" + user + "\"");

        /*
         * Antwortzeile auswerten.
         */
        try {
            String line = in.readLine();
            LOGGER.debug("Server >| " + line);
            if(!getText("server.answer.ok")
                    .contentEquals(new StringBuffer(line))) {
                LOGGER.error("Fehler >| " + line);
                throw new UnexpectedServerAnswer();
            }
        } catch (IOException e) {
            LOGGER.error("Fehler >| Keine Antwort von Server erhalten");
            throw new NoServerResponse();
        }

    }// Ende deleteAcl(String, String)

    /**
     * Methode zum berechnen der Quota der aktuellen Mailbox, die Werte können
     * über die entsprechenden Methoden abgerufen werden.
     *
     * @param mailbox - Mailbox für die die Quota berechnet werden soll.
     * @throws IOException - InputStream/OutputStream geschlossen oder nicht
     *             vorhanden
     * @throws NoMailbox - TODO doku
     * @throws NoQuota - TODO doku
     * @throws UnexpectedExtraArguments - TODO doku
     * @throws NoServerResponse - TODO doku
     * @throws NoValidMailboxName - TODO doku
     */
    public final void quota(final String mailbox) throws IOException,
            NoMailbox, NoQuota, UnexpectedExtraArguments, NoServerResponse,
            NoValidMailboxName {

        /*
         * Prüfen ob der übergebene Mailboxname gültig ist.
         */
        if (!isValid(mailbox)) {
            LOGGER.error("Fehler >| Ungültiger Mailboxname");
            throw new NoValidMailboxName();
        }

        /*
         * Absenden des Befehls und auslesen der ersten Ergebniszeile.
         */
        sendCommand(". getquota \"user." + mailbox + "\"");
        String line = in.readLine();
        LOGGER.debug("Server >| " + line);

        /*
         * Prüfen ob der Server eine Antwort geschickt hat.
         */
        if (isNull(line)) {
            LOGGER.warn("Kein Antwort vom Server.");
            throw new NoServerResponse();
        }

        /*
         * Wird geworfen wenn es die Mailbox nicht gibt.
         */
        if (line.startsWith(". NO Mailbox")) {
            LOGGER.warn("Mailbox existiert nicht.");
            throw new NoMailbox();
        }

        /*
         * Wird geworfen wenn keine Quota gesetzt worden ist .
         */
        if (line.startsWith(". NO Quota root does not exist")) {
            LOGGER.warn("Es wurde bis jetzt noch keine Quota gesetzt.");
            throw new NoQuota();
        }

        /*
         * Wird geworfen wenn der Methode unbekannte Parameter oder Zeichen
         * übergeben wurden.
         */
        if (line.startsWith(". BAD Unexpected extra arguments to Getquota")) {
            LOGGER.warn("Es wurden weiter Argumente dem Befehl hinzugefügt.");
            throw new UnexpectedExtraArguments();
        }

        /*
         * Wenn keine Quota exsistiert bzw. die Antwort nicht "* QUOTA enthält
         * dann wird eine Exception geworfen.
         */
        if (!line.startsWith("* QUOTA")) {
            LOGGER.warn("In der Server-Anwort war keine Quota enthalten.");
            // TODO Exception hier hin.
        }

        /*
         * Setzen der Index Elemente.
         */
        int start = line.lastIndexOf("(");
        int end = line.lastIndexOf(")");

        /*
         * Zerlegen des Ergebnisses und schreiben der Quota und des
         * Benutzten Platzes in die Variablen.
         */
        String[] storage = line.substring(start + 1, end).split(" ");
        used = new BigDecimal(storage[1]);
        quota = new BigDecimal(storage[2]);
        LOGGER.debug(line.substring(start + 1, end));


        /*
         * Errechnen der Load und befüllung der entsprechenden Variable.
         */
        load = used.multiply(new BigDecimal("100")).divide(quota, 2,
                BigDecimal.ROUND_UP);

        /*
         * Auslesen der zweiten Antwortzeile
         */
        line = in.readLine();
        LOGGER.debug("Server >| " + line);

        /*
         * Prüfen ob der Server eine Antwort geschickt hat.
         */
        if (isNull(line)) {
            LOGGER.warn("Kein Antwort vom Server.");
            throw new NoServerResponse();
        }

        /*
         * Exceptions je nach Antwortzeile.
         */
        if (!line.startsWith(". OK")) {
            LOGGER.warn(
                    "Der letzte Befehl wurde nicht erfolgreich ausgeführt."
                    );
            // TODO hier mus noch eine Exception hin
        }

    }// Ende quota(String mailbox)

    /**
     * Methode zum setzten der Quota einer Mailbox.
     *
     * @param mailbox - Hier. // TODO Doku hier
     * @param quotaToSet - Hier. // TODO Doku hier
     * @throws IOException - TODO doku
     * @throws NoValidMailboxName -
     */
    public final void setQuota(final String mailbox,
            final BigDecimal quotaToSet)
            throws IOException, NoValidMailboxName {
        /*
         * Prüfen ob der übergebene Mailboxname gültig ist.
         */
        if (!isValid(mailbox)) {
            LOGGER.warn("Ungültiger Mailboxname");
            throw new NoValidMailboxName();
        }

        /*
         * Sende Kommando.
         */
        sendCommand((new StringBuilder())
                .append(". setquota \"")
                .append("user.")
                .append(mailbox)
                .append("\" (STORAGE ")
                .append(quotaToSet)
                .append(")").toString());

        String line = in.readLine();
        LOGGER.debug("Server >| " + line);
        //System.out.println("Server >| " + line);

        // ". setquota \"$mb_name\" (STORAGE $quota)"
        // // TODO hier mus code hin
    }// Ende setQuota()

    /**
     * Methode zum erstellen einer Mailbox mit dem Namen "mailbox".
     *
     * @param mailbox - String mit dem Namen der Mailbox (i.e.
     *            "mailboxname" ohne [user.])
     * @throws IOException - InputStream/OutputStream geschlossen oder nicht
     *             vorhanden
     * @throws MailboxExists - Die Mailbox die erstellt werden soll exsistiert
     *             bereits.
     * @throws NoServerResponse - //TODO Dokumentation
     * @throws NoValidMailboxName - //TODO Dokumentation
     */
    public final void createMailBox(final String mailbox) throws IOException,
            MailboxExists, NoServerResponse, NoValidMailboxName {
        /*
         * Prüfen ob der übergebene Mailboxname gültig ist.
         */
        System.out.println(mailbox);
        if (!isValid(mailbox)) {

            LOGGER.warn("Ungültiger Mailboxname");
            throw new NoValidMailboxName();
        }

        /*
         * Kommando absetzen.
         */
        sendCommand((new StringBuilder())
                .append(". create \"")
                .append("user.")
                .append(mailbox)
                .append("\"")
                .toString());

        /*
         * Antwortzeile auslesen.
         */
        String line = in.readLine();
        LOGGER.debug("Server >| " + line);

        /*
         * Prüfen ob es eine Serverantwort gibt.
         */
        if (isNull(line)) {
            LOGGER.warn("Keine Antwort vom Server.");
            throw new NoServerResponse();
        }

        /*
         * Wirft Exception wenn es die Mailbox bereits gibt.
         */
        if (line.startsWith(". NO Mailbox already exists")) {
            LOGGER.warn("Die Mailbox existiert schon.");
            throw new MailboxExists();
        }
    }// Ende createMailBox()

    /**
     * Hier. // TODO Doku hier
     *
     * @param mailbox - Hier. // TODO Doku hier
     * @throws IOException - InputStream/OutputStream geschlossen oder nicht
     *             vorhanden
     * @throws NoValidMailboxName -
     */
    public final void deleteMailBox(final String mailbox) throws IOException,
            NoValidMailboxName {
        /*
         * Prüfen ob der übergebene Mailboxname gültig ist.
         */
        if (!isValid(mailbox)) {
            LOGGER.warn("Ungültiger Mailboxname");
            throw new NoValidMailboxName();
        }

        /*
         * Setzen der Rechte für den Administrationsbenutzer
         */
        try {
            setAcl(mailbox, administrator, allacl);
        } catch (NoServerResponse e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (UnexpectedServerAnswer e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        sendCommand(". delete \"user." + mailbox + "\"");
        String line = in.readLine();
        LOGGER.debug("Server >| " + line);
        //System.out.println("Server >| " + line);
    }// Ende deleteMailBox()

    /**
     * Methode zum setzen des Hostnamen oder der IP-Adresse des Servers mit dem
     * eine Verbindung aufgebaut werden soll. Falls der Host nicht gesetzt ist
     * wird localhost als hostname benutzt.
     *
     * @param hostname - Der Name oder die IP-Adresse des Servers zu dem eine
     *            Verbindung aufgebaut werden soll.
     */
    public final void setHost(final String hostname) {
        this.host = hostname;
    }// Ende setHost()

    /**
     * Methode um die Port-Nummer des Server zu verändern, normalerweise nicht
     * nötig, wenn der Server auf den Standard-Ports betrieben wird.
     *
     * @param portNumber - Port-Nummer die für die Verbindung zum Server
     *            benutzt werden soll.
     */
    public final void setPort(final Integer portNumber) {
        this.port = portNumber;
    }// Ende setPort()

    /**
     * Hier. // TODO Doku hier
     *
     * @param set - Hier. // TODO Doku hier
     */
    public final void setAdministrator(final String set) {
        this.administrator = set;
    }// Ende setAdministrator()

    /**
     * Hier. // TODO Doku hier
     *
     * @param set - Hier. // TODO Doku hier
     */
    public final void setPassword(final String set) {
        this.password = set;
    }// Ende setPasswort()

    /**
     * Liefert die Version des Server mit dem gerade eine Verbindung aufgebaut
     * ist.
     *
     * @return String - Cyrus Version
     * @throws IOException - InputStream/OutputStream geschlossen oder nicht
     *             vorhanden
     */
    public final String version() throws IOException {
    	sendCommand(". id NIL");
        String line = in.readLine();
        LOGGER.debug("Server >| " + line);

        if (isNull(line)) {
            // TODO Hier kommt noch Exception
            LOGGER.warn("Keine Server Antwort.");
        }

        int start = line.indexOf("(");
        int end = line.lastIndexOf(")");

        String[] storage = line.substring(start + 1, end).split("\" \"");

        for (int i = 0; i < storage.length; i++) {
            idMap.put(storage[i], storage[i + 1]);
            i++;
        }

        line = in.readLine();
        LOGGER.debug("Server >| " + line);
        //System.out.println("Server >| " + line);

        return idMap.get("version").split(" ")[0];
    }// Ende version()

    /**
     * Mit Hilfe dieser Methode kann man sich die Wilkommensnachricht des Server
     * abfragen, die nach dem aufruf der Methode connect(Boolean ssl) empfangen
     * wurde.
     *
     * @return String - Willkommensnachricht des Servers.
     */
    public final String getWelcomeMsg() {
        return welcomeMsg;
    }// Ende getWelcomeMsg()

    /**
     * TODO Doku
     * @return
     */
    public Map<String, String> getAcls() {
        return acls;
    }// Ende getAcls;

    /**
     * Bevor die Methode getUsed() aufgerufen werden kann, muss die Methode
     * quota(String mailbox) aufgrufen werden. Die Methode getUsed() liefert
     * dann den benutzen Teil der Quota der Mailbox.
     *
     * @return BigDecimal - Benutzer Teil der Quota der Mailbox die mit der
     *         Methode quota(String mailbox) übergeben wurde.
     * @throws QuotaNotInitialized - TODO doku
     */
    public final BigDecimal getUsed() throws QuotaNotInitialized {
        if (isNull(used)) {
            throw new QuotaNotInitialized();
        }
        return used;
    }// Ende getUsed()

    /**
     * Bevor die Methode getQuota() aufgerufen werden kann, muss die Methode
     * quota(String mailbox) aufgrufen werden. Die Methode getQuota() liefert
     * dann die aktuelle Quota der Mailbox.
     *
     * @return BigDecimal - Quota der Mailbox die mit der Methode quota(String
     *         mailbox) übergeben wurde.
     * @throws QuotaNotInitialized - TODO doku
     */
    public final BigDecimal getQuota() throws QuotaNotInitialized {
        if (isNull(used)) {
            throw new QuotaNotInitialized();
        }
        return quota;
    }// Ende getQuota()

    /**
     * Bevor die Methode getLoad() aufgerufen werden kann, muss die Methode
     * quota(String mailbox) aufgrufen werden. Die Methode getLoad() liefert
     * dann die aktuelle Load der Mailbox.
     *
     * @return BigDecimal - Load der Mailbox die mit der Methode quota(String
     *         mailbox) übergeben wurde.
     * @throws QuotaNotInitialized - TODO doku
     */
    public final BigDecimal getLoad() throws QuotaNotInitialized {
        if (isNull(used)) {
            throw new QuotaNotInitialized();
        }
        return load;
    }// Ende getLoad()

    /**
	 * Hilfs-Methode um ein Kommando an den Server zu senden.
	 * 
	 * @param command - Kommando das an den Server gesendet werden soll.
	 */
    private void sendCommand(final String command) {
        out.println(command);
        out.flush();
        LOGGER.debug("Client >| " + command);
    }// Ende sendCommand()
    
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
     * Hilfs-Methode die testet ob ein String ein gültiger String im Sinne
     * einer Cyrus Mailbox ist.
     *
     * @param mbString - String der als Mailbox übergeben wurde.
     * @return Boolean - Wenn gültig dann True.
     */
    private Boolean isValid(final String mbString) {
        return Pattern.matches("[a-zA-Z_]*", mbString)? true : false;
    }

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
