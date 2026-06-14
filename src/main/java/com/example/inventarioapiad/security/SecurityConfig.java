package com.example.inventarioapiad.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

// Configuración de Spring Security.
//
// IMPORTANTE: durante la defensa el JWT está DESACTIVADO. Todas las
// rutas son públicas. Esto permite a Postman llamar a la API sin
// necesidad de pasar token (y así no hay que configurar nada extra
// cuando se prueba contra AWS).
//
// Toda la infraestructura JWT (JwtAuthFilter, JwtService, AuthController,
// UsuarioService, etc.) sigue en el repo intacta: solo cambia esta clase
// para que la cadena de filtros permita todo. Reactivar el JWT es
// volver a la versión anterior de este fichero (un commit revert).
@Configuration
public class SecurityConfig {

    @Autowired
    private JwtAuthFilter jwtAuthFilter;

    // BCrypt para hashear las contraseñas. Lo sigue usando UsuarioService
    // por si volvemos a activar el JWT.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Es una API REST sin sesiones ni formularios web
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Mientras el JWT está desactivado, dejamos todo abierto.
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                // Mantenemos el filtro JWT en la cadena: si el cliente manda
                // token en la cabecera Authorization, lo procesa y rellena el
                // SecurityContext igual que antes. Si no lo manda, la petición
                // pasa sin más porque arriba hemos puesto permitAll.
                .addFilterBefore(jwtAuthFilter,
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
