package com.cjoop.client.c.security;

import java.util.HashSet;
import java.util.Set;

import javax.net.ssl.HttpsURLConnection;

import org.jasig.cas.client.proxy.Cas20ProxyRetriever;
import org.jasig.cas.client.proxy.ProxyGrantingTicketStorage;
import org.jasig.cas.client.proxy.ProxyGrantingTicketStorageImpl;
import org.jasig.cas.client.session.SingleSignOutFilter;
import org.jasig.cas.client.validation.Cas20ProxyTicketValidator;
import org.jasig.cas.client.validation.TicketValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.ehcache.EhCacheManagerUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.cas.ServiceProperties;
import org.springframework.security.cas.authentication.CasAssertionAuthenticationToken;
import org.springframework.security.cas.authentication.CasAuthenticationProvider;
import org.springframework.security.cas.authentication.EhCacheBasedTicketCache;
import org.springframework.security.cas.authentication.StatelessTicketCache;
import org.springframework.security.cas.web.CasAuthenticationEntryPoint;
import org.springframework.security.cas.web.CasAuthenticationFilter;
import org.springframework.security.cas.web.authentication.ServiceAuthenticationDetailsSource;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionFixationProtectionStrategy;

import com.cjoop.client.c.ssl.AnyHostnameVerifier;
import com.cjoop.client.c.ssl.HttpsURLConnectionFactory;

import net.sf.ehcache.Cache;

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
	public CasAuthenticationFilter casAuthenticationFilter(SessionAuthenticationStrategy sessionStrategy,
			ServiceProperties serviceProperties, ProxyGrantingTicketStorage pgtStorage){
		CasAuthenticationFilter casAuthenticationFilter = new CasAuthenticationFilter();
		casAuthenticationFilter.setAuthenticationManager(authenticationManager);
		casAuthenticationFilter.setSessionAuthenticationStrategy(sessionStrategy);
		casAuthenticationFilter.setServiceProperties(serviceProperties);
		casAuthenticationFilter.setProxyGrantingTicketStorage(pgtStorage);
		casAuthenticationFilter.setProxyReceptorUrl(casProperties.getProxyReceptorUrl());
		casAuthenticationFilter
				.setAuthenticationDetailsSource(new ServiceAuthenticationDetailsSource(serviceProperties));
		return casAuthenticationFilter;
	}
	
	/**
	 * 配置会话策略,根据实际情况调整
	 * @return SessionAuthenticationStrategy
	 */
	@Bean
	public SessionAuthenticationStrategy sessionStrategy() {
		return new SessionFixationProtectionStrategy();
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
	public CasAuthenticationProvider casAuthenticationProvider(StatelessTicketCache statelessTicketCache,
			TicketValidator cas20ProxyTicketValidator, ServiceProperties serviceProperties,
			AuthenticationUserDetailsService<CasAssertionAuthenticationToken> customUserDetailsService) {
		
		CasAuthenticationProvider casAuthenticationProvider = new CasAuthenticationProvider();
		casAuthenticationProvider.setAuthenticationUserDetailsService(customUserDetailsService);
		casAuthenticationProvider.setServiceProperties(serviceProperties);
		casAuthenticationProvider.setTicketValidator(cas20ProxyTicketValidator);
		casAuthenticationProvider.setKey("casAuthProviderKey");
		casAuthenticationProvider.setStatelessTicketCache(statelessTicketCache);
		return casAuthenticationProvider;
	}
	
	/**
	 * 配置票据缓存方式ehCache
	 * @param casTicketsCache 缓存对象
	 * @return StatelessTicketCache
	 */
	@Bean
	public StatelessTicketCache statelessTicketCache(Cache casTicketsCache){
		EhCacheBasedTicketCache ehCacheBasedTicketCache = new EhCacheBasedTicketCache();
		ehCacheBasedTicketCache.setCache(casTicketsCache);
		return ehCacheBasedTicketCache;
	}
	
	/**
	 * 配置缓存对象
	 * @param ehCacheCacheManager 缓存管理器
	 * @return casTicketsCache
	 */
	@Bean(initMethod="initialise",destroyMethod="dispose")
	public Cache casTicketsCache(){
		Cache cache = new Cache("casTickets",50,true,false,3600,900);
		cache.setCacheManager(EhCacheManagerUtils.buildCacheManager());
		return cache;
	}
	
	/**
	 * 配置服务票据代理验证方式
	 * @return cas20ProxyTicketValidator
	 */
	@Bean
	public TicketValidator cas20ProxyTicketValidator(ProxyGrantingTicketStorage pgtStorage) {
		Cas20ProxyTicketValidator cas20ProxyTicketValidator = new Cas20ProxyTicketValidator(casProperties.getServerUrl());
		cas20ProxyTicketValidator.setProxyCallbackUrl(casProperties.getProxyCallbackUrl());
		cas20ProxyTicketValidator.setProxyGrantingTicketStorage(pgtStorage);
		cas20ProxyTicketValidator.setAcceptAnyProxy(true);
		//这里修改了https端口认证类，允许所有的主机通过，根据实际情况调整
		HttpsURLConnection.setDefaultHostnameVerifier(new AnyHostnameVerifier());
		//使用自定义的HttpsURLConnectionFactory,主要是实现了序列化接口
		Cas20ProxyRetriever cas20ProxyRetriever = new Cas20ProxyRetriever(casProperties.getServerUrl(), "UTF-8",new HttpsURLConnectionFactory());
		cas20ProxyTicketValidator.setProxyRetriever(cas20ProxyRetriever);
		return cas20ProxyTicketValidator;
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
		sp.setAuthenticateAllArtifacts(casProperties.isAuthenticateAllArtifacts());
		return sp;
	}
	
	/**
	 * 配置存储代理票据的对象，这里使用内存方式存储，实际情况不一样
	 * @return ProxyGrantingTicketStorage
	 */
	@Bean
	public ProxyGrantingTicketStorage pgtStorage(){
		ProxyGrantingTicketStorageImpl pgtStorage = new ProxyGrantingTicketStorageImpl();
		return pgtStorage;
	}
}
