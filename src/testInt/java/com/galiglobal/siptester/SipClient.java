package com.galiglobal.siptester;

import org.cafesip.sipunit.SipPhone;
import org.cafesip.sipunit.SipStack;
import javax.sip.InvalidArgumentException;
import javax.sip.address.SipURI;
import java.net.Socket;
import java.text.ParseException;
import java.util.Properties;

/**
 * It simulates a SIP Client for integrated testing.
 *
 * @author Jonathan Rodriguez
 * @author Antón R. Yuste
 *
 */

public class SipClient {

    /**
     * Port where the SIP Client is listening for SIP Messages.
     */
    private Integer localPort;

    /**
     * Port where the SIP Server being tested is listening for SIP Messages.
     */
    private Integer serverPort;

    /**
     * Public IP where the SIP Server being tested is listening for SIP Messages.
     */
    private String serverHost;

    /**
     * IP where the SIP Client is listening for SIP Messages.
     * If not defined, the local IPs will be used for bind address.
     */
    private String localHost; //This is the local IP. Is used for bind address.

    /**
     * The SIP URI used to identify the SIP Client in the server.
     */
    private String localSipURI;

    /**
     * Protocol used to bind. It can be used TCP or UDP.
     */
    private static String sipProtocol = SipStack.PROTOCOL_UDP;

    /**
     * This class is the starting point for a SipUnit test.
     * Before establishing any SIP sessions, the test program must instantiate this class.
     */
    private SipStack sipUnitStack;

    /**
     * This class provides a test program with User Agent (UA) access to the SIP protocol in the form of a SIP phone.
     */
    private SipPhone sipUnitPhone;

    /**
     * Constructor with all the variables except the protocol.
     *
     * @param serverTestedHost @see serverHost
     * @param serverTestedPort @see serverPort
     * @param localTestingHost @see localHost
     * @param localTestingPort @see localPort
     * @throws Exception Something went wrong creating the SIP Stack.
     */
    public SipClient(
            final String serverTestedHost,
            final Integer serverTestedPort,
            final String localTestingHost,
            final Integer localTestingPort)
            throws Exception {
        this.localHost = localTestingHost;
        this.localPort = localTestingPort;
        this.serverPort = serverTestedPort;
        this.serverHost = serverTestedHost;

        this.sipUnitStack = new SipStack(
                sipProtocol,
                this.localPort,
                makeProperties(sipProtocol,
                        this.localHost,
                        this.localPort.toString(),
                        this.getServerURL(),
                        true,
                        null,
                        null)
        );
    }

    /**
     * Constructor with all the variables except the protocol and local IP.
     *
     * @param serverTestedHost @see serverHost
     * @param serverTestedPort @see serverPort
     * @param localTestingPort @see localPort
     * @throws Exception Something went wrong creating the SIP Stack.
     */
    public SipClient(
            final String serverTestedHost,
            final Integer serverTestedPort,
            final Integer localTestingPort)
            throws Exception {
        this.localPort = localTestingPort;
        this.serverPort = serverTestedPort;
        this.serverHost = serverTestedHost;

        Socket s = new Socket(this.serverHost, this.serverPort);
        this.localHost = s.getLocalAddress().getHostAddress();

        this.sipUnitStack = new SipStack(
                sipProtocol,
                this.localPort,
                makeProperties(sipProtocol,
                        this.localHost,
                        this.localPort.toString(),
                        this.getServerURL(),
                        true,
                        null,
                        null)
        );
    }

    /**
     * It formats the Server URL.
     *
     * @return The Server URL in format IP:port
     */
    protected final String getServerURL() {
        return this.serverHost.concat(":").concat(this.serverPort.toString());
    }

    /**
     * The method tries to do a registration against the SIP Server.
     *
     * @param mySipURI The URI of the SIP Server being tested.
     * @param contact The id of the user being registered.
     * @param expires The time before the registration expires.
     * @return True if it was able to register against the server.
     * @throws ParseException Problem parsing the SIP Messages.
     * @throws InvalidArgumentException Trying to register with invalid arguments.
     */
    public final boolean register(final String mySipURI, final String contact, final Integer expires)
            throws ParseException, InvalidArgumentException {

        this.localSipURI = mySipURI;
        this.sipUnitPhone = sipUnitStack.createSipPhone(
                this.serverHost,
                sipProtocol,
                this.serverPort,
                this.localSipURI);

        String serverHostName = this.localSipURI.substring(this.localSipURI.lastIndexOf("@") + 1);

        SipURI requestUri = sipUnitStack.getAddressFactory().createSipURI((String) null, serverHostName);
        requestUri.setPort(this.serverPort);
        requestUri.setTransportParam(sipProtocol);

        return sipUnitPhone.register(requestUri, null, null, contact, expires, 0L);
    }

    /**
     * It liberates all the SIP resources.
     */
    public final void dispose() {
        this.sipUnitPhone.dispose();
        this.sipUnitStack.dispose();

        this.sipUnitPhone = null;
        this.sipUnitStack = null;
    }

    /**
     * It liberates the resources.
     * @throws Throwable Problem liberating the resources.
     */
    @Override
    protected final void finalize() throws Throwable {
        this.dispose();
        super.finalize();
    }

    /**
     * Create the properties needed to create the SIP Stack.
     *
     * @param myTransport Protocol used @see sipProtocol.
     * @param myHost Local IP @see localHost.
     * @param myPort Local port @see localPort.
     * @param outboundProxy Proxy if used.
     * @param myAutoDialog Auto Dialog property.
     * @param threadPoolSize Size of thread pool.
     * @param reentrantListener Listener if used.
     * @return The properties needed to create the SIP Stack.
     * @throws Exception Problem creating the properties.
     */
    private static Properties makeProperties(
            final String myTransport,
            final String myHost,
            final String myPort,
            final String outboundProxy,
            final Boolean myAutoDialog,
            final String threadPoolSize,
            final String reentrantListener) throws Exception {

        Properties properties = new Properties();
        String mySelectedHost;
        String mySelectedTransport;

        if (myHost == null) {
            mySelectedHost = "127.0.0.1";
        } else {
           mySelectedHost = myHost;
        }

        if (myTransport == null) {
            mySelectedTransport = "udp";
        } else {
            mySelectedTransport = myTransport;
        }

        properties.setProperty("javax.sip.IP_ADDRESS", mySelectedHost);
        properties.setProperty("javax.sip.STACK_NAME", "UAC_" + mySelectedTransport + "_" + myPort);

        if (myAutoDialog) {
            properties.setProperty("javax.sip.AUTOMATIC_DIALOG_SUPPORT", "on");
        } else {
            properties.setProperty("javax.sip.AUTOMATIC_DIALOG_SUPPORT", "off");
        }

        properties.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "32");
        properties.setProperty("gov.nist.javax.sip.DEBUG_LOG", "logs/testAgent_debug_"
                + myPort + "_" + mySelectedTransport + ".txt");
        properties.setProperty("gov.nist.javax.sip.SERVER_LOG", "logs/testAgent_serverLog_"
                + myPort + "_" + mySelectedTransport + ".xml");
        properties.setProperty("gov.nist.javax.sip.READ_TIMEOUT", "1000");
        properties.setProperty("gov.nist.javax.sip.CACHE_SERVER_CONNECTIONS", "false");
        properties.setProperty("gov.nist.javax.sip.PASS_INVITE_NON_2XX_ACK_TO_LISTENER", "true");
        properties.setProperty("sipunit.trace", "true");
        properties.setProperty("sipunit.test.protocol", mySelectedTransport);
        properties.setProperty("sipunit.test.port", myPort);
        properties.setProperty("sipunit.BINDADDR", mySelectedHost);

        if (outboundProxy != null) {
            properties.setProperty("javax.sip.OUTBOUND_PROXY", outboundProxy + "/" + mySelectedTransport);
            String proxyHost = outboundProxy.split(":")[0];
            String proxyPort = outboundProxy.split(":")[1];
            properties.setProperty("sipunit.proxy.host", proxyHost);
            properties.setProperty("sipunit.proxy.port", proxyPort);
        }

        if (threadPoolSize == null) {
            properties.setProperty("gov.nist.javax.sip.THREAD_POOL_SIZE", "1");
        } else {
            properties.setProperty("gov.nist.javax.sip.THREAD_POOL_SIZE", threadPoolSize);
        }

        if (reentrantListener == null) {
            properties.setProperty("gov.nist.javax.sip.REENTRANT_LISTENER", "false");
        } else {
            properties.setProperty("gov.nist.javax.sip.REENTRANT_LISTENER", reentrantListener);
        }

        return properties;
    }
}
