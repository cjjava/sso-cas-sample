package com.cjoop.client.a.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cas")
public class CasProperties {
	/**
	 * cas服务根地址
	 */
	private String serverUrl = "https://localhost:8443/cas";
	/**
	 * 本服务认证地址
	 */
	@Value("http://localhost:${server.port}/login/cas")
	private String service;
	
	private boolean sendRenew = false;
	/**
	 * cas服务登陆地址
	 */
	private String serverLoginUrl = serverUrl + "/login";
	/**
	 * cas服务登出地址
	 */
	private String serverLogoutUrl = serverUrl + "/logout";
	
	/**
	 * 登出成功后的跳转地址
	 */
	@Value("http://localhost:${server.port}/")
	private String logoutSuccessUrl;

	public String getServerUrl() {
		return serverUrl;
	}

	public void setServerUrl(String serverUrl) {
		this.serverUrl = serverUrl;
	}

	public String getService() {
		return service;
	}

	public void setService(String service) {
		this.service = service;
	}

	public String getServerLoginUrl() {
		return serverLoginUrl;
	}

	public void setServerLoginUrl(String serverLoginUrl) {
		this.serverLoginUrl = serverLoginUrl;
	}

	public boolean isSendRenew() {
		return sendRenew;
	}

	public void setSendRenew(boolean sendRenew) {
		this.sendRenew = sendRenew;
	}

	public String getServerLogoutUrl() {
		return serverLogoutUrl;
	}

	public void setServerLogoutUrl(String serverLogoutUrl) {
		this.serverLogoutUrl = serverLogoutUrl;
	}

	public String getLogoutSuccessUrl() {
		return logoutSuccessUrl;
	}

	public void setLogoutSuccessUrl(String logoutSuccessUrl) {
		this.logoutSuccessUrl = logoutSuccessUrl;
	}
}
