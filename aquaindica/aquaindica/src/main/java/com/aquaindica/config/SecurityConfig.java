package com.aquaindica.config;

import com.aquaindica.util.JwtFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/admin/login",
                        "/api/admin/verify-otp",
                        "/api/contact",
                        "/api/admin/blogs/getBlogs",
                        "/api/gallery",
                        "/gallery/{id}").permitAll()

                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        //contact
                        .requestMatchers("/api/contact/all").hasRole("ADMIN")
                        .requestMatchers("/api/contact/archive/{id}").hasRole("ADMIN")
                        .requestMatchers("/api/contact/archived").hasRole("ADMIN")
                        .requestMatchers("/api/contact/unarchive/{id}").hasRole("ADMIN")
                        //Blogs
                        .requestMatchers("/api/admin/blogs").hasRole("ADMIN")
                        .requestMatchers("/api/admin/blogs/{id}").hasRole("ADMIN")
                        //gallary
                        .requestMatchers("/api/admin/gallery").hasRole("ADMIN")
                        .requestMatchers("/api/admin/gallery/{id}").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOrigins(Arrays.asList("http://localhost:4200"));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(Arrays.asList("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
