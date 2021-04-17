package com.thoughtbend.enterprise.clientsvc.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

import com.thoughtbend.enterprise.clientsvc.security.AudienceValidator;

@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {
	
	@Value("${auth0.audience}")
	private String audience;
	
	@Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
	private String issuer;
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		
		http.cors();
		http.csrf().disable();
		
		//super.configure(http);
		http.authorizeRequests().regexMatchers("/v1/*")
			/*.anyRequest()*/.authenticated().and().oauth2ResourceServer()
			.jwt().jwtAuthenticationConverter(grantedAuthoritiesExtractor()).decoder(jwtDecoer());
	}
	
	Converter<Jwt, AbstractAuthenticationToken> grantedAuthoritiesExtractor() {
	    JwtAuthenticationConverter jwtAuthenticationConverter =
	            new JwtAuthenticationConverter();

	    jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter
	            (new GrantedAuthoritiesExtractor());

	    return jwtAuthenticationConverter;
	}
	
	JwtDecoder jwtDecoer() {
		
		OAuth2TokenValidator<Jwt> withAudience = new AudienceValidator(this.audience);
		OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(this.issuer);
		OAuth2TokenValidator<Jwt> validator = new DelegatingOAuth2TokenValidator<>(withAudience, withIssuer);
		
		NimbusJwtDecoder jwtDecoder = (NimbusJwtDecoder) JwtDecoders.fromOidcIssuerLocation(this.issuer);
		jwtDecoder.setJwtValidator(validator);
		
		return jwtDecoder;
	}
	
	static class GrantedAuthoritiesExtractor implements Converter<Jwt, Collection<GrantedAuthority>> {

		@Override
		public Collection<GrantedAuthority> convert(Jwt source) {
			
			String scopeValue = source.getClaimAsString("scope");
			
			Collection<String> scopes = (scopeValue != null) ? Arrays.asList(scopeValue.split(" ")) : new ArrayList<>();
			
			Collection<GrantedAuthority> scopeAuthorities = scopes.stream()
					.map(scopeString -> {
						return String.format("SCOPE_%1$s", scopeString);
					})
					.map(SimpleGrantedAuthority::new)
					.collect(Collectors.toList());
			
			Collection<String> permissions = source.getClaimAsStringList("permissions");
			
			if (permissions == null) {
				permissions = new ArrayList<>();
			}
			
			Collection<GrantedAuthority> permissionAuthorities = permissions.stream()
					.map(permissionString -> {
						return String.format("PERMISSION_%1$s", permissionString);
					})
					.map(SimpleGrantedAuthority::new)
					.collect(Collectors.toList());
			
			Collection<GrantedAuthority> mergedAuthorities = new ArrayList<>();
			mergedAuthorities.addAll(scopeAuthorities);
			mergedAuthorities.addAll(permissionAuthorities);
			
			return mergedAuthorities;
		}
		
	}

}
