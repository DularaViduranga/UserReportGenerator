package com.userreport.UserReportBackend.services;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.util.Map;
import io.jsonwebtoken.Claims;
import java.util.Date;


@Service
public class JWTservice {
    private final SecretKey secretKey;

    public JWTservice() {
        try{
            SecretKey key = KeyGenerator.getInstance("HmacSHA256")
                    .generateKey();
            secretKey = Keys.hmacShaKeyFor(key.getEncoded());
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    public String getJwtToken(String username , Map<String,Object> claims){
        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis()+1000*60*15))
                .signWith(secretKey)
                .compact();
    }

    public String getUserName(String jwtToken){
        Claims data = getTokenData(jwtToken);
        if(data == null){
            return null;
        }
        return data.getSubject();
    }

    public Object getFieldFromToken(String jwtToken,String key){
        Claims data = getTokenData(jwtToken);
        if(data == null){
            return null;
        }
        return data.get(key);
    }

    public Claims getTokenData(String token){
        try{
            return Jwts
                    .parser()
                    .verifyWith(secretKey).build()
                    .parseSignedClaims(token)
                    .getPayload();
        }catch(Exception e){
            return null;
        }
    }
}
