package com.coderscampus.AssignmentSubmissionApp.filter;

import com.coderscampus.AssignmentSubmissionApp.repository.UserRepository;
import com.coderscampus.AssignmentSubmissionApp.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {

        final String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (!StringUtils.hasText(header) || (StringUtils.hasText(header) && !header.startsWith("Bearer "))) {
            chain.doFilter(request, response);
            return;
        }

        final String token = header.split(" ")[1].trim();

        UserDetails userDetails = userRepo
                .findUserByUsername(jwtUtil.getUsernameFromToken(token))
                .orElse(null);

        if (!jwtUtil.validateToken(token, userDetails)) {
            chain.doFilter(request, response);
            return;
        }

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null,
                userDetails == null ? List.of() : userDetails.getAuthorities()
        );

        authentication.setDetails(
                new WebAuthenticationDetailsSource().buildDetails(request)
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        chain.doFilter(request, response);
    }
}
