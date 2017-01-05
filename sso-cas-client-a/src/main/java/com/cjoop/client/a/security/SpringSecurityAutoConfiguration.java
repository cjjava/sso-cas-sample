package com.cjoop.client.a.security;

import java.util.HashSet;
import java.util.Set;

import org.jasig.cas.client.session.SingleSignOutFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.cas.authentication.CasAssertionAuthenticationToken;
import org.springframework.security.cas.authentication.CasAuthenticationProvider;
import org.springframework.security.cas.web.CasAuthenticationEntryPoint;
import org.springframework.security.cas.web.CasAuthenticationFilter;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionFixationProtectionStrategy;

import com.cjoop.client.a.service.AuthoritiesConstants;
import com.cjoop.client.a.service.CustomUserDetailsService;

/**
 * 安全框架配置信息
 * @author 陈均
 *
 */
@Configuration
@AutoConfigureAfter(SpringSecurityCasAutoConfiguration.class)
public class SpringSecurityAutoConfiguration extends WebSecurityConfigurerAdapter{
	@Autowired
	@Qualifier("casAuthenticationEntryPoint")
	CasAuthenticationEntryPoint casAuthenticationEntryPoint;
	@Autowired
	@Qualifier("singleSignOutFilter")
	SingleSignOutFilter singleSignOutFilter;
	@Autowired
	@Qualifier("requestSingleLogoutFilter")
	LogoutFilter requestSingleLogoutFilter;
	@Autowired
	AuthenticationManager authenticationManager;
	SessionAuthenticationStrategy sessionStrategy = new SessionFixationProtectionStrategy();
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		
		http.exceptionHandling().authenticationEntryPoint(casAuthenticationEntryPoint);
		http.addFilter(casAuthenticationFilter());
		http.addFilterBefore(requestSingleLogoutFilter, LogoutFilter.class);
		http.addFilterBefore(singleSignOutFilter, CasAuthenticationFilter.class);
		
		http.authorizeRequests()
		.antMatchers("/","/index.html").permitAll()
		.antMatchers("/**").authenticated().antMatchers("/admin.html")
		.hasAuthority(AuthoritiesConstants.ADMIN).anyRequest().authenticated();
		
	}
	
	/**
	 * 配置认证过滤器
	 * @return CasAuthenticationFilter
	 * @throws Exception
	 */
	public CasAuthenticationFilter casAuthenticationFilter() throws Exception {
		CasAuthenticationFilter casAuthenticationFilter = new CasAuthenticationFilter();
		casAuthenticationFilter.setAuthenticationManager(authenticationManager);
		casAuthenticationFilter.setSessionAuthenticationStrategy(sessionStrategy);
		return casAuthenticationFilter;
	}
	
	
	/**
	 * 配置认证提供商
	 * @param authenticationManagerBuilder
	 * @param casAuthenticationProvider
	 */
	@Autowired
	public void configureGlobal(
			AuthenticationManagerBuilder authenticationManagerBuilder,CasAuthenticationProvider casAuthenticationProvider) {
		authenticationManagerBuilder.authenticationProvider(casAuthenticationProvider);
	}
	
	
	/**
	 * 配置用户详情服务，这个根据实际情况编写
	 * @return AuthenticationUserDetailsService
	 */
	@Bean
	public AuthenticationUserDetailsService<CasAssertionAuthenticationToken> customUserDetailsService() {
		return new CustomUserDetailsService(adminList());
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
	 * 配置会话策略,根据实际情况调整
	 * @return SessionAuthenticationStrategy
	 */
	@Bean
	public SessionAuthenticationStrategy sessionStrategy() {
		return sessionStrategy;
	}
	
}
