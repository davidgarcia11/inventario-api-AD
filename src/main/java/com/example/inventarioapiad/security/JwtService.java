package com.example.inventarioapiad.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

// Clase encargada de generar y validar los tokens JWT.
// La clave secreta y el tiempo de expiración salen del application.properties
// (en producción se inyectan por variable de entorno).
@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    // Duración del token en milisegundos
    @Value("${jwt.expiration}")
    private long expirationMs;

    // Construye la clave HMAC a partir del secreto Base64 del properties.
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // Genera un token nuevo con el username dentro del campo "subject".
    public String generarToken(String username) {
        Date ahora = new Date();
        Date expira = new Date(ahora.getTime() + expirationMs);

        return Jwts.builder()
                .subject(username)
                .issuedAt(ahora)
                .expiration(expira)
                .signWith(getSigningKey())
                .compact();
    }

    // Saca el username (subject) de un token. Lanza excepción si está mal.
    public String extraerUsername(String token) {
        return parsearClaims(token).getSubject();
    }

    // Comprueba que el token sea válido y no haya expirado.
    public boolean esTokenValido(String token) {
        try {
            Claims claims = parsearClaims(token);
            return claims.getExpiration().after(new Date());
        } catch (Exception e) {
            // Token inválido, expirado, firma mala, etc.
            return false;
        }
    }

    // Devuelve la duración del token en segundos (la usa AuthResponse).
    public long getExpiracionEnSegundos() {
        return expirationMs / 1000;
    }

    private Claims parsearClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
