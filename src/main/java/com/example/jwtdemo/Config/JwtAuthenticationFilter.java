package com.example.jwtdemo.Config;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.jwtdemo.Model.User;
import com.example.jwtdemo.Repository.UserRepository;
import com.example.jwtdemo.Service.JwtUserDetailsService;
import com.example.jwtdemo.Service.JwtUtils;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

@Autowired private JwtUtils jwtUtils;
@Autowired private JwtUserDetailsService userDetailsService;
 @Autowired private UserRepository UserRepository; 
@Override
protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                FilterChain filterChain) throws ServletException, IOException {
    final String authHeader = request.getHeader("Authorization");

    String username = null;
    String jwt = null;

    if (authHeader != null && authHeader.startsWith("Bearer ")) {
        jwt = authHeader.substring(7);
        username = jwtUtils.extractUsername(jwt);
    }

   if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
    UserDetails userDetails = userDetailsService.loadUserByUsername(username); // email used here

    if (jwtUtils.validateToken(jwt, userDetails)) {
        // ✅ Get the full User object using email
        User fullUser = UserRepository.findByEmail(username)
                        .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // ✅ Create a new UserDetails with actual username
        UserDetails updatedUserDetails = new org.springframework.security.core.userdetails.User(
            fullUser.getUsername(), // use actual username here
            fullUser.getPassword(),
            List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        UsernamePasswordAuthenticationToken authToken =
            new UsernamePasswordAuthenticationToken(updatedUserDetails, null, updatedUserDetails.getAuthorities());

        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }
}

    filterChain.doFilter(request, response);
}
}
