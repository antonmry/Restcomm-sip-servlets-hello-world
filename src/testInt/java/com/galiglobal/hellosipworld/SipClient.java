package com.galiglobal.hellosipworld;

import org.cafesip.sipunit.SipPhone;
import org.cafesip.sipunit.SipStack;

import javax.sip.InvalidArgumentException;
import javax.sip.address.SipURI;
import java.net.Socket;
import java.text.ParseException;
import java.util.Map;
import java.util.Properties;

public class SipClient {

    private Integer localPort;
    private Integer serverPort;
    private String serverHost;
    private String localHost; //This is the local IP. Is used for bind address.

    private String localSipURI;

    private static String sipProtocol = SipStack.PROTOCOL_UDP;

    private SipStack sipUnitStack;
    private SipPhone sipUnitPhone;

    public SipClient(String serverHost, Integer serverPort, String localHost, Integer localPort) throws Exception {
        this.localHost = localHost;
        this.localPort = localPort;
        this.serverPort = serverPort;
        this.serverHost = serverHost;

        this.sipUnitStack = new SipStack(sipProtocol,this.localPort,makeProperties(sipProtocol,this.localHost,this.localPort.toString(),this.getServerURL(),true,null,null,null));
    }

    public SipClient(String serverHost, Integer serverPort, Integer localPort) throws Exception {
        this.localPort = localPort;
        this.serverPort = serverPort;
        this.serverHost = serverHost;

        Socket s = new Socket(this.serverHost,this.serverPort);
        this.localHost = s.getLocalAddress().getHostAddress();

        this.sipUnitStack = new SipStack(sipProtocol,this.localPort,makeProperties(sipProtocol,this.localHost,this.localPort.toString(),this.getServerURL(),true,null,null,null));
    }

    protected String getServerURL() {
        return this.serverHost.concat(":").concat(this.serverPort.toString());
    }

    public boolean Register(String mySipURI, String contact, Integer expires) throws ParseException, InvalidArgumentException {
        this.localSipURI = mySipURI;
        this.sipUnitPhone = sipUnitStack.createSipPhone(this.serverHost,sipProtocol,this.serverPort, this.localSipURI);

        String serverHostName = this.localSipURI.substring(this.localSipURI.lastIndexOf("@")+1);

        SipURI requestUri = sipUnitStack.getAddressFactory().createSipURI((String)null, serverHostName);
        requestUri.setPort(this.serverPort);
        requestUri.setTransportParam(sipProtocol);

        return sipUnitPhone.register(requestUri,null,null,contact,expires,0L);
    }

    public void dispose() {
        this.sipUnitPhone.dispose();
        this.sipUnitStack.dispose();

        this.sipUnitPhone = null;
        this.sipUnitStack = null;
    }

    @Override
    protected void finalize() throws Throwable {
        this.dispose();
        super.finalize();
    }

    private static Properties makeProperties(String myTransport, String myHost, String myPort, String outboundProxy, Boolean myAutoDialog, String threadPoolSize, String reentrantListener, Map<String, String> additionalProperties) throws Exception {
        Properties properties = new Properties();
        if(myHost == null) {
            myHost = "127.0.0.1";
        }

        if(myTransport == null) {
            myTransport = "udp";
        }

        properties.setProperty("javax.sip.IP_ADDRESS", myHost);
        properties.setProperty("javax.sip.STACK_NAME", "UAC_" + myTransport + "_" + myPort);
        properties.setProperty("javax.sip.AUTOMATIC_DIALOG_SUPPORT", myAutoDialog ?"on":"off");
        properties.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "32");
        properties.setProperty("gov.nist.javax.sip.DEBUG_LOG", "logs/testAgent_debug_" + myPort + "_" + myTransport + ".txt");
        properties.setProperty("gov.nist.javax.sip.SERVER_LOG", "logs/testAgent_serverLog_" + myPort + "_" + myTransport + ".xml");
        properties.setProperty("gov.nist.javax.sip.READ_TIMEOUT", "1000");
        properties.setProperty("gov.nist.javax.sip.CACHE_SERVER_CONNECTIONS", "false");
        properties.setProperty("gov.nist.javax.sip.PASS_INVITE_NON_2XX_ACK_TO_LISTENER", "true");
        properties.setProperty("sipunit.trace", "true");
        properties.setProperty("sipunit.test.protocol", myTransport);
        properties.setProperty("sipunit.test.port", myPort);
        properties.setProperty("sipunit.BINDADDR", myHost);
        if(outboundProxy != null) {
            properties.setProperty("javax.sip.OUTBOUND_PROXY", outboundProxy + "/" + myTransport);
            String proxyHost = outboundProxy.split(":")[0];
            String proxyPort = outboundProxy.split(":")[1];
            properties.setProperty("sipunit.proxy.host", proxyHost);
            properties.setProperty("sipunit.proxy.port", proxyPort);
        }

        properties.setProperty("gov.nist.javax.sip.THREAD_POOL_SIZE", threadPoolSize == null?"1":threadPoolSize);
        properties.setProperty("gov.nist.javax.sip.REENTRANT_LISTENER", reentrantListener == null?"false":reentrantListener);
        if(additionalProperties != null) {
            properties.putAll(additionalProperties);
        }

        return properties;
    }
}
