package no.ntnu.idatt2106.nettdetektivene.security;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

/**
 * Servlet filter that extracts a Bearer JWT from the {@code Authorization} header and populates the Spring Security context on each request.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsServiceImpl userDetailsService;

    /**
     * Validates the JWT from the Authorization header and sets the authentication in the security context if valid.
     * Requests without a Bearer token are passed through unauthenticated.
     *
     * @param request  the incoming HTTP request
     * @param response the HTTP response
     * @param chain    the filter chain
     * @throws ServletException if an underlying servlet error occurs
     * @throws IOException      if an I/O error occurs during filtering
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        try {
            if (jwtTokenProvider.isTokenValid(token)) {
                Long userId = jwtTokenProvider.extractUserId(token);
                UserDetails userDetails = userDetailsService.loadUserById(userId);
                UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);
                log.debug("Authenticated userId={} for {} {}", userId, request.getMethod(), request.getRequestURI());
            }
        } catch (Exception e) {
            log.warn("JWT authentication failed for {} {}: {}", request.getMethod(), request.getRequestURI(), e.getMessage());
            // Let request proceed unauthenticated — Spring Security handles the 401
        }
        chain.doFilter(request, response);
    }
}
