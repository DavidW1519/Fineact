/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.fineract.infrastructure.core.config;

import static org.apache.fineract.infrastructure.security.vote.SelfServiceUserAuthorizationManager.selfServiceUserAuthManager;
import static org.springframework.security.authorization.AuthenticatedAuthorizationManager.fullyAuthenticated;
import static org.springframework.security.authorization.AuthorityAuthorizationManager.hasAuthority;
import static org.springframework.security.authorization.AuthorizationManagers.allOf;

import java.util.Collection;
import org.apache.fineract.infrastructure.core.exceptionmapper.OAuth2ExceptionEntryPoint;
import org.apache.fineract.infrastructure.security.data.FineractJwtAuthenticationToken;
import org.apache.fineract.infrastructure.security.filter.TenantAwareTenantIdentifierFilter;
import org.apache.fineract.infrastructure.security.filter.TwoFactorAuthenticationFilter;
import org.apache.fineract.infrastructure.security.service.TenantAwareJpaPlatformUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.context.SecurityContextHolderFilter;

@Configuration
@ConditionalOnProperty("fineract.security.oauth.enabled")
@EnableMethodSecurity
public class OAuth2SecurityConfig {

    @Autowired
    private TwoFactorAuthenticationFilter twoFactorAuthenticationFilter;

    @Autowired
    private TenantAwareTenantIdentifierFilter tenantAwareTenantIdentifierFilter;

    @Autowired
    private TenantAwareJpaPlatformUserDetailsService userDetailsService;

    @Autowired
    private ServerProperties serverProperties;

    private static final JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();


    @Bean
    public SecurityFilterChain authorizationFilterChain(HttpSecurity http) throws Exception {
        http //
                .securityMatcher("/api/**").authorizeHttpRequests((auth) -> {
                    auth
                            .requestMatchers(HttpMethod.OPTIONS, "/api/**").permitAll() //
                            .requestMatchers(HttpMethod.POST, "/api/*/echo").permitAll() //
                            .requestMatchers(HttpMethod.POST, "/api/*/authentication").permitAll() //
                            .requestMatchers(HttpMethod.POST, "/api/*/self/authentication").permitAll() //
                            .requestMatchers(HttpMethod.POST, "/api/*/self/registration").permitAll() //
                            .requestMatchers(HttpMethod.POST, "/api/*/self/registration/user").permitAll() //
                            .requestMatchers(HttpMethod.POST, "/api/*/twofactor/validate").fullyAuthenticated() //
                            .requestMatchers("/api/*/twofactor").fullyAuthenticated() //
                            .requestMatchers("/api/**").access(allOf(fullyAuthenticated(), hasAuthority("TWOFACTOR_AUTHENTICATED"), selfServiceUserAuthManager())); //
                });
        return http.build();
    }

    @Bean
    public SecurityFilterChain FilterChain(HttpSecurity http) throws Exception {
        http //
                .csrf((csrf) -> csrf.disable()) // NOSONAR only creating a service that is used by non-browser clients
                .exceptionHandling((ehc) -> ehc.authenticationEntryPoint(new OAuth2ExceptionEntryPoint()))
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(authenticationConverter()))
                        .authenticationEntryPoint(new OAuth2ExceptionEntryPoint())) //
                .sessionManagement((smc) -> smc.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) //
                .addFilterAfter(tenantAwareTenantIdentifierFilter, SecurityContextHolderFilter.class) //
                .addFilterAfter(twoFactorAuthenticationFilter, BasicAuthenticationFilter.class); //

        if (serverProperties.getSsl().isEnabled()) {
            http.requiresChannel(channel -> channel.requestMatchers("/api/**").requiresSecure());
        }
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    private Converter<Jwt, FineractJwtAuthenticationToken> authenticationConverter() {
        return jwt -> {
            try {
                UserDetails user = userDetailsService.loadUserByUsername(jwt.getSubject());
                jwtGrantedAuthoritiesConverter.setAuthorityPrefix("");
                Collection<GrantedAuthority> authorities = jwtGrantedAuthoritiesConverter.convert(jwt);
                return new FineractJwtAuthenticationToken(jwt, authorities, user);
            } catch (UsernameNotFoundException ex) {
                throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.INVALID_TOKEN), ex);
            }
        };
    }

    @Bean
    public FilterRegistrationBean<TenantAwareTenantIdentifierFilter> tenantAwareTenantIdentifierFilterRegistration() throws Exception {
        FilterRegistrationBean<TenantAwareTenantIdentifierFilter> registration = new FilterRegistrationBean<TenantAwareTenantIdentifierFilter>(
                tenantAwareTenantIdentifierFilter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    public FilterRegistrationBean<TwoFactorAuthenticationFilter> twoFactorAuthenticationFilterRegistration() {
        FilterRegistrationBean<TwoFactorAuthenticationFilter> registration = new FilterRegistrationBean<TwoFactorAuthenticationFilter>(
                twoFactorAuthenticationFilter);
        registration.setEnabled(false);
        return registration;
    }
}
