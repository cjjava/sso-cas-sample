package com.cjoop.client.b.security;

import java.util.HashSet;
import java.util.Set;

import org.jasig.cas.client.session.SingleSignOutFilter;
import org.jasig.cas.client.validation.Cas20ServiceTicketValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.cas.ServiceProperties;
import org.springframework.security.cas.authentication.CasAssertionAuthenticationToken;
import org.springframework.security.cas.authentication.CasAuthenticationProvider;
import org.springframework.security.cas.web.CasAuthenticationEntryPoint;
import org.springframework.security.cas.web.CasAuthenticationFilter;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionFixationProtectionStrategy;

/**
 * 安全框架单点登录配置信息
 * @author 陈均
 *
 */
@EnableConfigurationProperties(CasProperties.class)
@Configuration
public class SpringSecurityCasAutoConfiguration{
	@Autowired
	private CasProperties casProperties;
	@Autowired
	AuthenticationManager authenticationManager;
	
	/**
	 * 配置认证过滤器
	 * @return CasAuthenticationFilter
	 * @throws Exception
	 */
	@Bean
	public CasAuthenticationFilter casAuthenticationFilter(SessionAuthenticationStrategy sessionStrategy) throws Exception {
		CasAuthenticationFilter casAuthenticationFilter = new CasAuthenticationFilter();
		casAuthenticationFilter.setAuthenticationManager(authenticationManager);
		casAuthenticationFilter.setSessionAuthenticationStrategy(sessionStrategy);
		return casAuthenticationFilter;
	}
	
	@Bean
	public LogoutFilter requestSingleLogoutFilter() {
		String logoutSuccessUrl = casProperties.getServerLogoutUrl() + "?service=" + casProperties.getLogoutSuccessUrl();
		LogoutFilter logoutFilter = new LogoutFilter(logoutSuccessUrl, new SecurityContextLogoutHandler());
		logoutFilter.setFilterProcessesUrl("/logout/cas");
		return logoutFilter;
	}
	
	@Bean
	public SingleSignOutFilter singleSignOutFilter() {
		SingleSignOutFilter singleSignOutFilter = new SingleSignOutFilter();
		singleSignOutFilter.setCasServerUrlPrefix(casProperties.getServerUrl());
		return singleSignOutFilter;
	}
	
	/**
	 * 配置认证提供商cas
	 * @return CasAuthenticationProvider
	 */
	@Bean
	public CasAuthenticationProvider casAuthenticationProvider(Cas20ServiceTicketValidator cas20ServiceTicketValidator,ServiceProperties serviceProperties,AuthenticationUserDetailsService<CasAssertionAuthenticationToken> customUserDetailsService) {
		CasAuthenticationProvider casAuthenticationProvider = new CasAuthenticationProvider();
		casAuthenticationProvider.setAuthenticationUserDetailsService(customUserDetailsService);
		casAuthenticationProvider.setServiceProperties(serviceProperties);
		casAuthenticationProvider.setTicketValidator(cas20ServiceTicketValidator);
		casAuthenticationProvider.setKey("an_id_for_this_auth_provider_only");
		return casAuthenticationProvider;
	}
	
	/**
	 * 配置服务票据的验证方式
	 * @return Cas20ServiceTicketValidator
	 */
	@Bean
	public Cas20ServiceTicketValidator cas20ServiceTicketValidator() {
		return new Cas20ServiceTicketValidator(casProperties.getServerUrl());
	}
	
	/**
	 * 配置管理员账号信息,测试使用
	 * @return admins
	 */
	public Set<String> adminList() {
		Set<String> admins = new HashSet<String>();
		admins.add("admin");
		return admins;
	}
	
	
	/**
	 * 配置认证切入点
	 * @return CasAuthenticationEntryPoint
	 */
	@Bean
	public CasAuthenticationEntryPoint casAuthenticationEntryPoint(ServiceProperties serviceProperties) {
		CasAuthenticationEntryPoint casAuthenticationEntryPoint = new CasAuthenticationEntryPoint();
		casAuthenticationEntryPoint.setLoginUrl(casProperties.getServerLoginUrl());
		casAuthenticationEntryPoint.setServiceProperties(serviceProperties);
		return casAuthenticationEntryPoint;
	}
	
	/**
	 * 配置当前服务的属性
	 * @return ServiceProperties
	 */
	@Bean
	public ServiceProperties serviceProperties() {
		ServiceProperties sp = new ServiceProperties();
		sp.setService(casProperties.getService());
		sp.setSendRenew(casProperties.isSendRenew());
		return sp;
	}
	
	/**
	 * 配置会话策略,根据实际情况调整
	 * @return SessionAuthenticationStrategy
	 */
	@Bean
	public SessionAuthenticationStrategy sessionStrategy() {
		return new SessionFixationProtectionStrategy();
	}
}
