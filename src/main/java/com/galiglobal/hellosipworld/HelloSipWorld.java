/*
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.galiglobal.hellosipworld;

import java.io.IOException;
import java.util.HashMap;

import javax.annotation.Resource;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.sip.Address;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipSession;
import javax.servlet.sip.SipURI;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This example shows a typical UAS and reply 200 OK to any INVITE or BYE it receives.
 *
 * @author Jean Deruelle
 * @author Antón R. Yuste
 *
 */


public class HelloSipWorld extends SipServlet {

    /**
     * Factory interface for a variety of SIP Servlet API abstractions.
     * SIP servlet containers are requried to make a SipFactory instance available to applications through
     * a ServletContext attribute with name javax.servlet.sip.SipFactory.
     */
    @Resource
    private SipFactory sipFactory;

    /**
     * Log class for logging.
     */
    private static Log logger = LogFactory.getLog(HelloSipWorld.class);

    /**
     *  HashMap to store the initiated sessions.
     */
    private HashMap<SipSession, SipSession> sessions = new HashMap<SipSession, SipSession>();

    /**
     * HashMap to map the Session id with the address.
     */
    private HashMap<String, Address> registeredUsersToIp = new HashMap<String, Address>();

    /**
     * Called by the servlet container to indicate when servlet is being placed into service.
     *
     * @param servletConfig A servlet configuration object used by a servlet container
     * to pass information to a servlet during initialization.
     * @throws ServletException Defines a general exception a servlet can throw when it encounters difficulty.
     * @throws IOException Signals that an I/O exception of some sort has occurred.
     */
    @Override
    public final void init(final ServletConfig servletConfig) throws ServletException {
        logger.info("The HelloSipWorld servlet has been started");
        super.init(servletConfig);
    }

    /**
     * Called by the servlet container when a SIP INVITE is received.
     *
     * @param request Represents SIP request messages.
     * @throws ServletException Defines a general exception a servlet can throw when it encounters difficulty.
     * @throws IOException Signals that an I/O exception of some sort has occurred.
     */
    @Override
    protected final void doInvite(final SipServletRequest request)
            throws ServletException, IOException {

        request.getSession().setAttribute("lastRequest", request);
        if (logger.isInfoEnabled()) {
            logger.info("HelloSipWorld INVITE: Got request:\n" + request.getMethod());
        }

        SipServletRequest outRequest = sipFactory.createRequest(
                request.getApplicationSession(),
                "INVITE",
                request.getFrom().getURI(),
                request.getTo().getURI()
        );
        String user = ((SipURI) request.getTo().getURI()).getUser();
        Address calleeAddress = registeredUsersToIp.get(user);

        if (calleeAddress == null) {
            request.createResponse(SipServletResponse.SC_NOT_FOUND).send();
            return;
        }

        outRequest.setRequestURI(calleeAddress.getURI());

        if (request.getContent() != null) {
            outRequest.setContent(request.getContent(), request.getContentType());
        }

        outRequest.send();
        sessions.put(request.getSession(), outRequest.getSession());
        sessions.put(outRequest.getSession(), request.getSession());
    }

    /**
     * Called by the servlet container when a response is received.
     *
     * @param response Represents SIP responses.
     * @throws ServletException Defines a general exception a servlet can throw when it encounters difficulty.
     * @throws IOException Signals that an I/O exception of some sort has occurred.
     */
    protected final void doResponse(final SipServletResponse response)
            throws ServletException, IOException {

        if (logger.isInfoEnabled()) {
            logger.info("HelloSipWorld: Got response:\n" + response);
        }

        response.getSession().setAttribute("lastResponse", response);
        SipServletRequest request = (SipServletRequest) sessions.get(response.getSession()).getAttribute("lastRequest");
        SipServletResponse resp = request.createResponse(response.getStatus());

        if (response.getContent() != null) {
            resp.setContent(response.getContent(), response.getContentType());
        }

        resp.send();
    }

    /**
     * Called by the servlet container when a SIP REGISTER is received.
     *
     * @param request Represent a SIP Register request
     * @throws ServletException Defines a general exception a servlet can throw when it encounters difficulty.
     * @throws IOException Signals that an I/O exception of some sort has occurred.
     */
    protected final void doRegister(final SipServletRequest request)
            throws ServletException, IOException {

        logger.info("HelloSipWorld UA REGISTERING:\n" + request.toString());

        Address addr = request.getAddressHeader("Contact");
        SipURI sipUri = (SipURI) addr.getURI();
        registeredUsersToIp.put(sipUri.getUser(), addr);

        if (logger.isInfoEnabled()) {
            logger.info("Address registered " + addr);
        }

        SipServletResponse sipServletResponse = request.createResponse(SipServletResponse.SC_OK);
        sipServletResponse.send();
    }

    /**
     * Called by the servlet container when a SIP BYE is received.
     *
     * @param request Represent a SIP BYE request
     * @throws ServletException Defines a general exception a servlet can throw when it encounters difficulty.
     * @throws IOException Signals that an I/O exception of some sort has occurred.
     */
    @Override
    protected final void doBye(final SipServletRequest request)
            throws ServletException, IOException {

        logger.info("HelloSipWorld: received a BYE.....");

        SipServletResponse sipServletResponse = request.createResponse(SipServletResponse.SC_OK);
        sipServletResponse.send();
    }
}
