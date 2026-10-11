package com.example.api.configuration;

import com.example.api.service.JwtService;
import com.example.api.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final UserService userService;
    private final JwtService jwtService;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        final String tokenExtractHeader = request.getHeader("Authorization");
        final String tokenLimpio;
        final String userEmail;

        if(tokenExtractHeader == null || !tokenExtractHeader.startsWith("Bearer ")){
            filterChain.doFilter(request,response);
            return;
        }

        tokenLimpio = tokenExtractHeader.substring(7);
        userEmail = jwtService.extractUsername(tokenLimpio);

        if(userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null){
            SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
            UserDetails userDetails = this.userService.userDetailsService().loadUserByUsername(userEmail);
            if(jwtService.validateToken(tokenLimpio, userDetails) && !jwtService.isRefreshToken(tokenLimpio)){
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails,null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                securityContext.setAuthentication(authToken);
                SecurityContextHolder.setContext(securityContext);
            }
        }
        filterChain.doFilter(request, response);
    }
}
