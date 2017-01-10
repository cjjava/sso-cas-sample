package com.cjoop.client.d.controller;

import java.net.URL;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;

import org.jasig.cas.client.ssl.HttpsURLConnectionFactory;
import org.jasig.cas.client.util.CommonUtils;
import org.springframework.security.cas.authentication.CasAuthenticationToken;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ClientCProxyController {
	String targetUrl = "https://c.milkoutlet.com:9443/test.html";
	
	@RequestMapping(value="/pt/c/test",produces="text/html;charset=UTF-8")
	public String d_test(HttpServletRequest request) throws Exception {
		// NOTE: The CasAuthenticationToken can also be obtained using
		// SecurityContextHolder.getContext().getAuthentication()
		final CasAuthenticationToken token = (CasAuthenticationToken) request.getUserPrincipal();
		// proxyTicket could be reused to make calls to the CAS service even if
		// the
		// target url differs
		final String proxyTicket = token.getAssertion().getPrincipal().getProxyTicketFor(targetUrl);
		// Make a remote call to ourself. This is a bit silly, but it works well
		// to
		// demonstrate how to use proxy tickets.
		final String serviceUrl = targetUrl + "?ticket=" + URLEncoder.encode(proxyTicket, "UTF-8");
		String proxyResponse = CommonUtils.getResponseFromServer(new URL(serviceUrl),new HttpsURLConnectionFactory(),"UTF-8");
		// modify the response and write it out to inform the user that it was
		// obtained
		// using a proxy ticket.
		proxyResponse = proxyResponse.replaceFirst("Secure Page", "Secure Page using a Proxy Ticket");
		proxyResponse = proxyResponse + 
						"<p>This page is rendered by " + getClass().getSimpleName()
						+ " by making a remote call to the Secure Page using a proxy ticket (" + proxyTicket
						+ ") and inserts this message. ";
		return proxyResponse;
	}

}
