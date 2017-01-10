package com.cjoop.client.c.ssl;

import java.io.Serializable;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

public class AnyHostnameVerifier implements HostnameVerifier, Serializable {

	private static final long serialVersionUID = -6616628987882266637L;

	@Override
	public boolean verify(String hostname, SSLSession session) {
		return true;
	}

}
