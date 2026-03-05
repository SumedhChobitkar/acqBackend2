package com.aquaindica.util;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;


    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        try {

            if (authHeader != null && authHeader.startsWith("Bearer ")) {

                String token = authHeader.substring(7);

                String username = jwtUtil.extractUsername(token);
                String role = jwtUtil.extractRole(token);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                username,
                                null,
                                List.of(new SimpleGrantedAuthority("ROLE_" + role))
                        );

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Token expired");
            return;
        }

//        if (authHeader != null && authHeader.startsWith("Bearer ")) {
//
//            String token = authHeader.substring(7);
//
//            String username = jwtUtil.extractUsername(token);
//            String role = jwtUtil.extractRole(token);  // 👈 NEW
//
//            UsernamePasswordAuthenticationToken authentication =
//                    new UsernamePasswordAuthenticationToken(
//                            username,
//                            null,
//                            List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + role))
//                    );
//
//            SecurityContextHolder.getContext().setAuthentication(authentication);
//        }

        filterChain.doFilter(request, response);
    }
//    @Override
//    protected void doFilterInternal(HttpServletRequest request,
//                                    HttpServletResponse response,
//                                    FilterChain filterChain)
//            throws ServletException, IOException {
//        String auth = request.getHeader("Authorization");
//        if (auth != null && auth.startsWith("Bearer ")) {
//            String token = auth.substring(7);
//            String username = jwtUtil.extractUsername(token); // ✅ safe
//        }

//        String auth = request.getHeader("Authorization");
//
//        if (auth != null && auth.startsWith("Bearer ")) {
//            String token = auth.substring(7);
//            String username = jwtUtil.extractUsername(token);
//
//            UsernamePasswordAuthenticationToken authentication =
//                    new UsernamePasswordAuthenticationToken(username, null, List.of());
//
//            SecurityContextHolder.getContext().setAuthentication(authentication);
//        }

       // filterChain.doFilter(request, response);
    }
//}
