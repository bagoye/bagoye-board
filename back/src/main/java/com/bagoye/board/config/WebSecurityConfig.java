package com.bagoye.board.config;

import com.bagoye.board.filter.JwtAuthenticationFilter;
import com.bagoye.board.common.ResponseCode;
import com.bagoye.board.common.ResponseMessage;
import com.bagoye.board.dto.response.ResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.annotation.web.configurers.HttpBasicConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.util.StringUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final NoPermissionAccessDeniedHandler noPermissionAccessDeniedHandler;
    private final ObjectMapper objectMapper;

    @Value("${cors.allowed-origins}")
    private String[] allowedOrigins;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .cors(cors -> cors
                        .configurationSource(corsConfigrationSource())
                )
                .csrf(CsrfConfigurer::disable)
                .httpBasic(HttpBasicConfigurer::disable)
                .sessionManagement(sessionManagement -> sessionManagement
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(request -> request
                        .requestMatchers("/", "/api/v1/auth/**", "/api/v1/search/**", "/file/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/board/**", "/api/v1/user/*").permitAll()
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint(new FailedAuthenticationEntryPoint(objectMapper))
                        .accessDeniedHandler(noPermissionAccessDeniedHandler)
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);


        return httpSecurity.build();
    }

    @Bean
    protected CorsConfigurationSource corsConfigrationSource() {
        List<String> origins = Arrays.stream(allowedOrigins)
                .map(String::trim)
                .filter(StringUtils::hasText)
                .toList();

        if (origins.isEmpty() || origins.contains("*")) {
            throw new IllegalStateException("cors.allowed-origins must list explicit origins (wildcard '*' is not allowed).");
        }

        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowedOrigins(origins);
        corsConfiguration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        corsConfiguration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        corsConfiguration.setAllowCredentials(false);
        corsConfiguration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);
        return source;
    }
}

@RequiredArgsConstructor
class FailedAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {

        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        objectMapper.writeValue(response.getOutputStream(),
                new ResponseDto(ResponseCode.AUTHORIZATION_FAIL, ResponseMessage.AUTHORIZATION_FAIL));
    }
}
