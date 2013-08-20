package de.tivsource.lib.jcyradm.test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import junit.framework.TestCase;
import de.tivsource.lib.jcyradm.JCyrAdm;
import de.tivsource.lib.jcyradm.exception.AuthenticationFailure;
import de.tivsource.lib.jcyradm.exception.NoLogMessagesFile;
import de.tivsource.lib.jcyradm.exception.NoPropertiesFile;
import de.tivsource.lib.jcyradm.exception.NoServerAnswerFile;
import de.tivsource.lib.jcyradm.exception.NoServerResponse;
import de.tivsource.lib.jcyradm.exception.NoServerStream;
import de.tivsource.lib.jcyradm.exception.UnexpectedServerAnswer;

/**
 * In diesem Test werden die Methoden connect() und disconnect() der Klasse
 * JCyrAdm getestet.
 *
 * @author Marc Michele
 *
 */
public class JCyrAdmConnectTest extends TestCase {

    private JCyrAdm jcyradm;
    private Properties props;

    /**
     * Create the test case
     * 
     * @param testName
     *            name of the test case
     */
    public JCyrAdmConnectTest(String testName) {
        super(testName);
    }

    protected void setUp() throws NoPropertiesFile, NoServerAnswerFile, NoLogMessagesFile {
        
        
        props = new Properties();
        InputStream inputStream = getClass().getClassLoader()
                .getResourceAsStream("JCyrAdmTest.properties");
        try {
            props.load(inputStream);
            inputStream.close();
        } catch( Exception ex ) {
          fail("Fehler: Properties-Datei fehlt.");
        }

        System.setProperty("javax.net.ssl.trustStore", props.getProperty("trustStore"));
        System.setProperty("javax.net.ssl.trustStorePassword", props.getProperty("trustStorePassword"));
        
        jcyradm = new JCyrAdm();
        jcyradm.setAdministrator(props.getProperty("administrator"));
        jcyradm.setPassword(props.getProperty("password"));
        jcyradm.setHost(props.getProperty("testServer"));
    }

    protected void tearDown() {
    }

    /**
     * Test ob die im setUp() gesetzten Werte für die Verbindung (Host/Port)
     * funktionieren dabei wird eine SSL-Verbindung getestet.
     * TODO: Doku überarbeiten.
     * @throws IOException
     */
    public void testConnect() {
        try {
            jcyradm.connect(false);
        } catch (IOException e) {
            fail( "Fehler: die Verbindung konnte nicht hergestellt werden." );
        }
        try {
            jcyradm.disconnect();
        } catch (IOException e) {
            fail( "Fehler: Es besteht keine Verbindung oder die Verbindung konnte nicht bendet werden." );
        }
    }

    /**
     * Test ob bei einem nicht bekannten Hostnamen eine Exception geworfen wird.
     * TODO: Doku überarbeiten.
     */
    public void testConnectUnknownHost() {
        jcyradm.setHost("LiLaLauenBär");
        try {
            jcyradm.connect(false);
            fail("Eine Verbindung herzustellen hätte nicht gelingen sollen.");
        } catch (IOException e) {
            assertTrue("Die verbindung zu LiLaLauenBär war wie erwartet nicht erfolgreich", true);
        }
    }
    
    /**
     * Test ob bei einem nicht bekannten Hostnamen eine Exception geworfen wird.
     * TODO: Doku überarbeiten.
     * @throws IOException 
     */
    public void testConnectPort() {
        jcyradm.setPort(Integer.parseInt(props.getProperty("testPort")));
        try {
            jcyradm.connect(false);
        } catch (IOException e) {
            fail( "Fehler: die Verbindung konnte nicht hergestellt werden. Es wurde der Port: 143 verwendet" );
        }
        try {
            jcyradm.disconnect();
        } catch (IOException e) {
            fail( "Fehler: Es besteht keine Verbindung oder die Verbindung konnte nicht bendet werden." );
        }
    }
    
    /**
     * Test ob bei einem nicht bekannten Hostnamen eine Exception geworfen wird.
     * TODO: Doku überarbeiten.
     * @throws IOException 
     */
    public void testConnectWelcome() throws IOException {
        jcyradm.connect(false);
        assertEquals(props.getProperty("welcome"), jcyradm.getWelcomeMsg());
        jcyradm.disconnect();
    }

    
    public void testDisconnect() throws IOException {
        jcyradm.connect(false);
        jcyradm.disconnect();
    }

    public void testDisconnectException() throws IOException {
        jcyradm.connect(false);
        jcyradm.disconnect();
        try {
            jcyradm.disconnect();
        } catch (IOException e) {
            assertTrue(true);
        }
    }

    
    /**
     * Test ob die im setUp() gesetzten Werte für die Verbindung (Host/Port)
     * funktionieren dabei wird eine SSL-Verbindung getestet.
     * 
     * @throws IOException
     */
    public void testConnectSSL() throws IOException {
        jcyradm.connect(true);
        jcyradm.disconnect();
    }

    
    /**
     * Test ob bei einem nicht bekannten Hostnamen eine Exception geworfen wird.
     */
    public void testConnectSSLUnknownHost() {
        jcyradm.setHost("LiLaLauenBär");
        try {
            jcyradm.connect(true);
        } catch (IOException e) {
            assertTrue(true);
        }
    }

    /**
     * Test ob bei einem nicht bekannten Hostnamen eine Exception geworfen wird.
     * TODO: Doku überarbeiten.
     * @throws IOException 
     */
    public void testConnectSSLPort() throws IOException {
        jcyradm.setPort(993);
        jcyradm.connect(true);
        jcyradm.disconnect();
    }
    
    /**
     * Test ob bei einem nicht bekannten Hostnamen eine Exception geworfen wird.
     * TODO: Doku überarbeiten.
     * @throws IOException 
     */
    public void testConnectSSLWelcome() throws IOException {
        jcyradm.connect(true);
        assertEquals(props.getProperty("welcomeSSL"), jcyradm.getWelcomeMsg());
        jcyradm.disconnect();
    }

    public void testDisconnectSSL() throws IOException {
        jcyradm.connect(true);
        jcyradm.disconnect();
    }

    public void testDisconnectSSLException() throws IOException {
        jcyradm.connect(true);
        jcyradm.disconnect();
        try {
            jcyradm.disconnect();
        } catch (IOException e) {
            assertTrue(true);
        }
    }

    public void testVersion() throws IOException {
        jcyradm.connect(true);
        jcyradm.capability();
        try {
            jcyradm.login();
        } catch (NoServerResponse e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (UnexpectedServerAnswer e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (AuthenticationFailure e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        assertEquals(props.getProperty("testVersion"), jcyradm.version());
        try {
            jcyradm.logout();
        } catch (NoServerResponse e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoServerStream e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (UnexpectedServerAnswer e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        jcyradm.disconnect();
    }
} // Ende class
