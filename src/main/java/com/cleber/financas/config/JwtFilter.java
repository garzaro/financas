package com.cleber.financas.config;

import java.io.IOException;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.cleber.financas.service.JwtService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtFilter extends OncePerRequestFilter {

	private final JwtService jwtService;
	
	/**
	 * precisa injetar o servico de busca la no banco
	 * Esta é a identidade oficial que o Spring Security usa para carregar as informações do usuário
     * Esse cara vai la no banco caçar o usuario com o cracha do UserDetails
     * 
     * **/
    private final UserDetailsService userDetailsService;

    public JwtFilter(JwtService jwtService, UserDetailsService userDetailsService) {
        super();
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        /**Ignorar filtro se não houver Bearer token **/
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7);
        final String username;

        try {
            username = jwtService.getUserLogin(jwt);
        } catch (Exception e) {
            /**Token malformado ou expirado — permite que a configuração
             * de segurança o rejeite com um código 401.
             * **/
            filterChain.doFilter(request, response);
            return;
        }

        /**So autentica se ainda não estiver autenticado no contexto atual.**/
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            /**o cracha padronizado**/
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            if (jwtService.isTokenValido(jwt, userDetails)) {
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}
