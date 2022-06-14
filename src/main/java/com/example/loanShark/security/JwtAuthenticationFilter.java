package com.example.loanShark.security;

import com.example.loanShark.security.models.UserDetailsImpl;
import com.example.loanShark.security.services.UserDetailsServiceImpl;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RequiredArgsConstructor
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private static final String INVALID_TOKEN = "Invalid JWT Token!";
    private static final String AUTHENTICATED_USER_MESSAGE = "Authenticated user with email %s, setting security context! ";

    private static final String TOKEN_PREFIX = "Bearer ";
    private static final String HEADER_STRING = "Authorization";
    private static final String DELIMITER = "";

    private static final String HEADER_AUTHORIZATION = "Header Authorization ";
    private static final String EXTRACTING_TOKEN_FROM_HEADER = "Extracting token from header ";
    private static final String EXTRACTING_EMAIL_FROM_TOKEN = "Extracting email from token ";

    private final JwtTokenUtil jwtTokenUtil;
    private final UserDetailsServiceImpl userService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader(HEADER_STRING);
        String email = null;
        String authToken = null;

        if (header != null && header.startsWith(TOKEN_PREFIX)) {
            LOGGER.info(HEADER_AUTHORIZATION + header);
            authToken = header.replace(TOKEN_PREFIX, DELIMITER);
            LOGGER.info(EXTRACTING_TOKEN_FROM_HEADER + authToken);
            try {
                email = jwtTokenUtil.getEmailFromToken(authToken);
                LOGGER.info(EXTRACTING_EMAIL_FROM_TOKEN + email);
            } catch (ExpiredJwtException | MalformedJwtException | UnsupportedJwtException | SignatureException ex) {
                jwtExceptionHandler(ex, response);
                return;
            }
        }

        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userService.loadUserByUsername(email);
            if (jwtTokenUtil.validateToken(authToken, ((UserDetailsImpl) userDetails).getEmail())) {
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                LOGGER.info(String.format(AUTHENTICATED_USER_MESSAGE, email));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        filterChain.doFilter(request, response);
    }

    private void jwtExceptionHandler(Exception ex, HttpServletResponse response) throws IOException {
        LOGGER.error(ex.getMessage());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write(INVALID_TOKEN);
    }
}