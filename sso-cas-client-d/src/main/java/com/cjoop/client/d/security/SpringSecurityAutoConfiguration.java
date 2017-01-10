package com.cjoop.client.d.security;

import java.util.HashSet;
import java.util.Set;

import org.jasig.cas.client.session.SingleSignOutFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.cas.authentication.CasAssertionAuthenticationToken;
import org.springframework.security.cas.authentication.CasAuthenticationProvider;
import org.springframework.security.cas.web.CasAuthenticationEntryPoint;
import org.springframework.security.cas.web.CasAuthenticationFilter;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.web.authentication.logout.LogoutFilter;

import com.cjoop.client.d.service.AuthoritiesConstants;
import com.cjoop.client.d.service.CustomUserDetailsService;

/**
 * 安全框架配置信息
 * @author 陈均
 *
 */
@Configuration
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
	CasAuthenticationFilter casAuthenticationFilter;
			
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.csrf().disable();
		http.exceptionHandling().authenticationEntryPoint(casAuthenticationEntryPoint);
		http.addFilter(casAuthenticationFilter);
		http.addFilterBefore(requestSingleLogoutFilter, LogoutFilter.class);
		http.addFilterBefore(singleSignOutFilter, CasAuthenticationFilter.class);
		http.authorizeRequests()
		.antMatchers("/","/index.html").permitAll()
		.antMatchers("/**").authenticated().antMatchers("/admin.html")
		.hasAuthority(AuthoritiesConstants.ADMIN).anyRequest().authenticated();
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
	
}
