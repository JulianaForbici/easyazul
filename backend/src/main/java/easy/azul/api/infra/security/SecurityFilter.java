package easy.azul.api.infra.security;

import easy.azul.api.repository.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class SecurityFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(SecurityFilter.class);

    @Autowired
    private TokenService tokenService;

    @Autowired
    private UsuarioRepository repository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        var tokenJWT = recuperarToken(request);
        if (tokenJWT == null) {
            filterChain.doFilter(request, response);
            return;
        }
        try {
            String email = tokenService.getSubject(tokenJWT);
            String role = tokenService.getRole(tokenJWT);

            var userOpt = repository.findByEmail(email);
            if (userOpt.isEmpty()) {
                SecurityErrorWriter.write(request, response, HttpStatus.UNAUTHORIZED, "Falha na autenticação");
                return;
            }

            var user = userOpt.get();
            var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));

            var authentication = new UsernamePasswordAuthenticationToken(
                    user,
                    null,
                    authorities);

            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (Exception e) {
            logger.warn("Erro no filtro JWT: {}", e.getMessage());
            SecurityErrorWriter.write(request, response, HttpStatus.UNAUTHORIZED, "Token inválido ou expirado");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String recuperarToken(HttpServletRequest request) {
        var authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7).trim();
        }
        return null;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return request.getMethod().equalsIgnoreCase("OPTIONS")
                || path.equals("/login")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/swagger-ui");
    }
}