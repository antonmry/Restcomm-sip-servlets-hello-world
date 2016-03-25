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
 * This example shows a typical UAS and reply 200 OK to any INVITE or BYE it receives
 *
 * @author Jean Deruelle
 * @author Antón R. Yuste
 *
 */


public class HelloSipWorld extends SipServlet {

	
    @Resource
    SipFactory sipFactory;

	private static Log logger = LogFactory.getLog(HelloSipWorld.class);
    HashMap<SipSession, SipSession> sessions= new HashMap<SipSession, SipSession>();
	  HashMap<String, Address> registeredUsersToIp = new HashMap<String, Address>();

	
	@Override
	public void init(ServletConfig servletConfig) throws ServletException {
		logger.info("the HelloSipWorld servlet has been started");
		super.init(servletConfig);
	}

	
	/**
	@Override
    protected void doInvite(SipServletRequest request) throws ServletException,
                    IOException {

            logger.info("Got request:${symbol_escape}n"
                            + request.toString());
            String fromUri = request.getFrom().getURI().toString();
            logger.info(fromUri);
           
            SipServletResponse sipServletResponse = request.createResponse(SipServletResponse.SC_OK);
            sipServletResponse.send();              
    }
*/
	
	
	@Override
	  protected void doInvite(SipServletRequest request) throws ServletException,
    IOException {
request.getSession().setAttribute("lastRequest", request);
if(logger.isInfoEnabled()) {
    logger.info("HelloSipWorld INVITE: Got request:\n"
                    + request.getMethod());
}

SipServletRequest outRequest = sipFactory.createRequest(request.getApplicationSession(),
            "INVITE", request.getFrom().getURI(), request.getTo().getURI());
String user = ((SipURI) request.getTo().getURI()).getUser();
Address calleeAddress = registeredUsersToIp.get(user);
if(calleeAddress == null) {
    request.createResponse(SipServletResponse.SC_NOT_FOUND).send();
    return;
}
outRequest.setRequestURI(calleeAddress.getURI());
if(request.getContent() != null) {
    outRequest.setContent(request.getContent(), request.getContentType());
}
outRequest.send();
sessions.put(request.getSession(), outRequest.getSession());
sessions.put(outRequest.getSession(), request.getSession());
}

	 
	
	 
	 protected void doResponse(SipServletResponse response)
             throws ServletException, IOException {
     if(logger.isInfoEnabled()) {
             logger.info("HelloSipWorld: Got response:\n" + response);
     }
     response.getSession().setAttribute("lastResponse", response);
     SipServletRequest request = (SipServletRequest) sessions.get(response.getSession()).getAttribute("lastRequest");
     SipServletResponse resp = request.createResponse(response.getStatus());
     if(response.getContent() != null) {
             resp.setContent(response.getContent(), response.getContentType());
     }
     resp.send();
}

	

      
      protected void doRegister(SipServletRequest request) throws ServletException,
      IOException {
  		logger.info("HelloSipWorld UA REGISTERING:\n"
				+ request.toString());
             
              Address addr = request.getAddressHeader("Contact");
              SipURI sipUri = (SipURI) addr.getURI();
              registeredUsersToIp.put(sipUri.getUser(), addr);
              if(logger.isInfoEnabled()) {
                      logger.info("Address registered " + addr);
              }
              SipServletResponse sipServletResponse = request.createResponse(SipServletResponse.SC_OK);
              sipServletResponse.send();
      }

	
	

	@Override
	protected void doBye(SipServletRequest request) throws ServletException,
			IOException {
		logger.info("the HelloSipWorld has received a BYE.....");
		SipServletResponse sipServletResponse = request.createResponse(SipServletResponse.SC_OK);
		sipServletResponse.send();	
	}
}
