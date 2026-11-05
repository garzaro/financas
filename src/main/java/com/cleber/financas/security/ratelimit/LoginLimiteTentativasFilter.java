package com.cleber.financas.security.ratelimit;

import com.cleber.financas.exception.TooManyRequestsException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.time.Duration;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class LoginLimiteTentativasFilter extends OncePerRequestFilter {

    private final LimiteTentativasService limiteTentativasService;
    private final HandlerExceptionResolver resolver;
    private final long ipMax;
    private final long ipWindowSec;
    private final long ipBlockSec;

    public LoginLimiteTentativasFilter(
            @Lazy LimiteTentativasService limiteTentativasService,
            @Lazy @Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver,
            @Value("${app.ratelimit.ip.max:60}") long ipMax,
            @Value("${app.ratelimit.ip.window.sec:60}") long ipWindowSec,
            @Value("${app.ratelimit.ip.block.sec:300}") long ipBlockSec
    ) {
        this.limiteTentativasService = limiteTentativasService;
        this.resolver = resolver;
        this.ipMax = ipMax;
        this.ipWindowSec = ipWindowSec;
        this.ipBlockSec = ipBlockSec;
    }


    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String path = request.getServletPath();
        return !("/api/auth/sign-in".equals(path) || "/api/auth/refresh".equals(path));
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String ip = request.getRemoteAddr();
        String chave = "rl:ip:" + ip;

        if (limiteTentativasService.isBloqueado(chave)) {
            long retryAfter = limiteTentativasService.obterRetryAfter(chave);
            resolver.resolveException(request, response, null,
                    new TooManyRequestsException("Muitas tentativas", retryAfter));
            return;
        }

        limiteTentativasService.registrar(chave, ipMax,
                Duration.ofSeconds(ipWindowSec),
                Duration.ofSeconds(ipBlockSec));

        filterChain.doFilter(request, response);
    }
}
