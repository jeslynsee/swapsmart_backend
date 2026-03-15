package com.group.SwapSmart.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.KeyFactory;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECPoint;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.EllipticCurve;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class JWTAuthFilter extends OncePerRequestFilter {

    @Value("${supabase.url}")
    private String supabaseUrl;

    private ECPublicKey cachedPublicKey = null;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {
            // Fetch public key from Supabase JWKS endpoint if not cached
            if (cachedPublicKey == null) {
                cachedPublicKey = fetchSupabasePublicKey();
            }

            // Verify JWT using EC public key
            Claims claims = Jwts.parser()
                    .verifyWith(cachedPublicKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String userId = claims.getSubject();
            // System.out.println("Authenticated user ID: " + userId); // debug print statement

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userId, null, List.of());
            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (Exception e) {
            System.out.println("JWT validation failed: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        filterChain.doFilter(request, response);
    }

    // Fetches Supabase's public key from their JWKS endpoint
    private ECPublicKey fetchSupabasePublicKey() throws Exception {
        String jwksUrl = supabaseUrl + "/auth/v1/.well-known/jwks.json";

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(jwksUrl))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> jwks = mapper.readValue(response.body(), Map.class);
        List<Map<String, Object>> keys = (List<Map<String, Object>>) jwks.get("keys");

        // Grab first key
        Map<String, Object> key = keys.get(0);
        String x = (String) key.get("x");
        String y = (String) key.get("y");

        // Decode x and y coordinates
        byte[] xBytes = Base64.getUrlDecoder().decode(x);
        byte[] yBytes = Base64.getUrlDecoder().decode(y);

        ECPoint point = new ECPoint(new BigInteger(1, xBytes), new BigInteger(1, yBytes));

        // Use P-256 curve (ES256)
        KeyFactory keyFactory = KeyFactory.getInstance("EC");
        java.security.spec.ECParameterSpec ecSpec =
                ((ECPublicKey) keyFactory.generatePublic(
                        new ECPublicKeySpec(point,
                                ((java.security.interfaces.ECKey)
                                        java.security.KeyPairGenerator.getInstance("EC")
                                                .generateKeyPair().getPublic()).getParams())))
                        .getParams();

        ECPublicKeySpec pubKeySpec = new ECPublicKeySpec(point, ecSpec);
        return (ECPublicKey) keyFactory.generatePublic(pubKeySpec);
    }
}