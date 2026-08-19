package com.cleber.financas.security;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.cleber.financas.service.JwtService;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import com.cleber.financas.api.dto.ErroResposta;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import lombok.RequiredArgsConstructor;

/**
 * [] ignorar validacao de accesstoken em rotas publicas
 * []
 * **/

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final String BEARER_PREFIX = "Bearer ";

	private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    private final ObjectMapper objectMapper;

	@SuppressWarnings("null")
	@Override
	protected void doFilterInternal(
		HttpServletRequest request,
		HttpServletResponse response,
		FilterChain filterChain) throws ServletException, IOException {

		//Inicio do filtro
		final String authHeader = request.getHeader("Authorization");
		if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
			//Se o header não existir, ou não começar com "Bearer ", pula este filtro e vai para o próximo
			filterChain.doFilter(request, response);
			return;
		}

		// Verificar se o header existe e começa com "Bearer "
		String jwt = authHeader.substring(BEARER_PREFIX.length());

		String pegaEmailDoUsuario;
		try {
			/**Extrai o username/email embutido no accesstoken JWT -jwt**/
			pegaEmailDoUsuario = jwtService.extrairUsernameToken(jwt);

		} catch (ExpiredJwtException | MalformedJwtException ex) {
			/**Log crítico: accesstoken malformado/expirado é um evento de segurança relevante**/
			logger.error("Token expirado ou inválido: {}", ex.getMessage());
			handleAuthenticationError(response, "Token expirado ou inválido", HttpServletResponse.SC_UNAUTHORIZED);
			return;
		} catch (Exception ex) {
			logger.error("Erro ao extrair email do accesstoken: {}", ex.getMessage());
			handleAuthenticationError(response, "Erro de autenticação", HttpServletResponse.SC_UNAUTHORIZED);
			return;
		}

		/**Só autentica se houver email e o contexto ainda não tiver autenticação**/
        SecurityContext context = SecurityContextHolder.getContext();
        if(pegaEmailDoUsuario != null && context.getAuthentication() == null) {
        	/**Carrega os dados do usuário a partir da base (garante que ele existe e está ativo)**/
            try {
                UserDetails userDetails = userDetailsService.loadUserByUsername(pegaEmailDoUsuario);

                if (!userDetails.isEnabled()) {
                    handleAuthenticationError(response, "Usuário desativado", HttpServletResponse.SC_FORBIDDEN);
                    return;
                }

                /**o que ta na base e o extraido deve ser igual**/
                if (jwtService.isTokenValido(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null, // credentials nulas: já autenticado via accesstoken, não via senha
                                    userDetails.getAuthorities()
                            );

                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request));
                    /**Popula o contexto — daqui pra frente a requisição é tratada como autenticada**/
                    context.setAuthentication(authToken);
                }
            } catch (Exception ex) {
                logger.error("Erro ao carregar usuário: {}", ex.getMessage());
                handleAuthenticationError(response, "Usuário não encontrado ou inativo", HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    private void handleAuthenticationError(HttpServletResponse response, String message, int status) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        ErroResposta<Object> erro = new ErroResposta<>(message, status, java.util.List.of());

        PrintWriter writer = response.getWriter();
        writer.print(objectMapper.writeValueAsString(erro));
        writer.flush();
    }
}

