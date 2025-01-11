package com.gsc.gsc.user.security.util;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtUtil {


    @Value("${jwt.secret}")
    private String SECRET_KEY;


    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }


    public AuthenticatedUser extractAuthenticatedUser(String token, Integer userId) {
        try {


            Integer id  =  Integer.parseInt(extractClaim(token, Claims::getId));


            AuthenticatedUser authenticatedUser = new AuthenticatedUser(userId,null);

            if (id.equals(userId))
                return  authenticatedUser; //Integer.parseInt(extractClaim(token, Claims::getId));
            else
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);


        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
    }


    public int extractUserId(String token) {

        Integer id  =  Integer.parseInt(extractClaim(token, Claims::getId));
        return id;
    }

    public int extractUserId(String token, Integer userId) {
        try {

            Integer id  =  Integer.parseInt(extractClaim(token, Claims::getId));

            if (id.equals(userId))
                return Integer.parseInt(extractClaim(token, Claims::getId));
            else
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);


        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
    }

    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token).getBody();
    }

    public Object getOneClaim(String token, String ClaimName) {
        return getAllClaimsFromToken(token).get(ClaimName);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public String extractName(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token).getBody();
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    public String generateToken( Integer userId,int customerType, long tokenExpiryTime) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("customerType", customerType);
        claims.put("userId", userId);
        String id = String.valueOf(userId);
        return createToken(claims, id, tokenExpiryTime);
    }

    private String createToken(Map<String, Object> claims, String userId, long tokenExpiryTime) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userId)  // Set the subject (userID)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + tokenExpiryTime))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

    public String generateVerificationToken(Integer userId) {

        return Jwts.builder().setId(userId.toString())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY).compact();

    }

    public String extractSubject(String token) {

        try {
            String subject  =  extractClaim(token, Claims::getSubject);
            return subject;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
    }

}