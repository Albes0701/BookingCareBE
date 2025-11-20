package com.bookingcare.account.service;

import com.bookingcare.account.entity.Accounts;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String SECRET_KEY;

    @Value("${jwt.expiration}")
    private long ACCESS_TOKEN_EXPIRATION;

    @Value("${jwt.refresh-token.expiration}")
    private long REFRESH_TOKEN_EXPIRATION;

    // Phương thức tạo Access Token (thường có thời gian hết hạn ngắn hơn)
    public String generateAccessToken(Accounts accounts) {
        String authorities = "ROLE_" + accounts.getRoles().getName();

        return Jwts.builder()
                .setSubject(accounts.getUsername())
                .claim("authorities", authorities)
                .claim("userId", accounts.getUser().getId())             // thêm claim userId
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // Phương thức tạo Refresh Token (thường có thời gian hết hạn dài hơn)
    public String generateRefreshToken(Accounts account) {
        return Jwts.builder()
                .setSubject(account.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRATION))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // Lấy Key dùng để ký token
    private Key getSigningKey() {
        // Lấy Key dùng để ký token từ chuỗi secret
        byte[] keyBytes = this.SECRET_KEY.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // === Các phương thức dùng để trích xuất thông tin từ Token (được sử dụng trong JWTTokenValidatorFilter) ===

    // Trích xuất tất cả các thông tin (claims) từ token
    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // Trích xuất username từ token
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    // Kiểm tra xem token đã hết hạn chưa
    public boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }
}
