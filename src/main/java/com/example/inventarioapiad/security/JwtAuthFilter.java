package com.example.inventarioapiad.security;

import com.example.inventarioapiad.repository.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

// Filtro que se ejecuta una vez por petición HTTP. Lee la cabecera
// "Authorization: Bearer <token>", valida el JWT y, si es correcto,
// marca al usuario como autenticado dentro del SecurityContext.
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        // Si no hay cabecera o no empieza por "Bearer ", seguimos la cadena.
        // Spring Security devolverá 401 más adelante si el endpoint estaba
        // protegido.
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Quitamos "Bearer " (7 caracteres) para quedarnos con el token
        String token = authHeader.substring(7);

        if (jwtService.esTokenValido(token)) {
            String username = jwtService.extraerUsername(token);

            // Comprobamos que el usuario sigue existiendo en la BD: si lo
            // han borrado, el token no sirve aunque la firma sea válida.
            usuarioRepository.findByUsername(username).ifPresent(usuario -> {
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(
                                usuario.getUsername(),
                                null,
                                Collections.emptyList());
                auth.setDetails(new WebAuthenticationDetailsSource()
                        .buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);
            });
        }

        filterChain.doFilter(request, response);
    }
}
