package com.school_management_webapi.security;

import java.io.IOException;
import java.util.UUID;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.school_management_webapi.entity.User;
import com.school_management_webapi.repository.UserRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private static final String BEARER_PREFIX = "Bearer ";

	private final JwtService jwtService;
	private final UserRepository userRepository;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
			FilterChain filterChain) throws ServletException, IOException {
		String header = request.getHeader(HttpHeaders.AUTHORIZATION);

		if (header != null && header.startsWith(BEARER_PREFIX)
				&& SecurityContextHolder.getContext().getAuthentication() == null) {
			String token = header.substring(BEARER_PREFIX.length());

			if (jwtService.isValid(token) && jwtService.isAccessToken(token)) {
				UUID userId = jwtService.extractUserId(token);
				userRepository.findById(userId).ifPresent(this::authenticate);
			}
		}

		filterChain.doFilter(request, response);
	}

	private void authenticate(User user) {
		UserPrincipal principal = new UserPrincipal(user);
		UsernamePasswordAuthenticationToken authentication = UsernamePasswordAuthenticationToken.authenticated(
				principal, null, principal.getAuthorities());
		SecurityContextHolder.getContext().setAuthentication(authentication);
	}
}
