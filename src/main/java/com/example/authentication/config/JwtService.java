package com.example.authentication.config;

import java.security.Key;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    private final static String SECRET_KEY = "6FC8F2CC4A7D9FD3FFE5F64AA14AE";

    public String extractUserId(String token) {
        return extractClaims(token, Claims::getSubject);
    }

    public String generateToken(Map<String, Object> extraClaim, UserDetails userDetails){
        return Jwts
            .builder()
            .setClaims(extraClaim)
            .setSubject(userDetails.getUsername())
            .setIssuedAt(new Date(System.currentTimeMillis()))
            .setExpiration(new Date(System.currentTimeMillis()+1000*60*24*30))
            .signWith(getSigningKey(),SignatureAlgorithm.HS256)
            .compact();
    }

    public boolean isTokenValid(String token, UserDetails userDetails){
        String username = extractUserId(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpirationDate(token).before(new Date());
    }

   private Date extractExpirationDate(String token) {
        return extractClaims(token, Claims::getExpiration);
    }

 public <T> T extractClaims(String token, Function<Claims, T> claimsResolver){
        final Claims claims = extractAlClaims(token);
        return claimsResolver.apply(claims);
    }
    private Claims extractAlClaims(String token){
        return Jwts
            .parserBuilder()
            .setSigningKey(getSigningKey())
            .build()
            .parseClaimsJws(token)
            .getBody();
    }

    private Key getSigningKey() {
        byte[] keyByte = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyByte);
    }

}