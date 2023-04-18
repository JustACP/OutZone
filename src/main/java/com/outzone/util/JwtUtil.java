package com.outzone.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.sql.Date;
import java.util.Base64;
import java.util.UUID;

@Component
public class JwtUtil {
    public static final Long JWT_TTL = 60 * 60 * 1000L; //60 * 60 * 1000 一个小时
    public static final String JWT_KEY = "ttk213";// 密钥明文

    public static String getUUID(){
        String token = UUID.randomUUID().toString().replaceAll("-","");
        
        return token;
    }

    public static String createJWT(String subject){
        JwtBuilder builder = getJwtBuilder(subject, null,getUUID()); //设置过期时间
        
        return builder.compact();
    }
    

    public static void main(String[] args) throws Exception{
        // String s = createJWT("123456", JWT_KEY, JWT_TTL);
        // System.out.println(s);
        Claims claims =  praseJWT("eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiIxMjM0NTYiLCJzdWIiOiJ0dGsyMTMiLCJpc3MiOiJyZTFpZmUiLCJpYXQiOjE2NjY1MDc1ODgsImV4cCI6MTY2NjUxMTE4OH0.LRiM2Dcd8mGjHthsxUG4gAnzM301hlej6M1qzyE-CX4");
        System.out.println(claims.getSubject());
        
    }




    public static JwtBuilder getJwtBuilder(String subject, Long ttlMills,String uuid){
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
        SecretKey secretKey = generalKey();
        long nowMillis =  System.currentTimeMillis();
        Date now = new Date(nowMillis);
        if(ttlMills == null){
            ttlMills = JwtUtil.JWT_TTL;
        }
        long expMillis = nowMillis + ttlMills;
        Date expdDate = new Date(expMillis);
        return Jwts.builder()
                    .setId(uuid) //唯一ID
                    .setSubject(subject) //主题 可以为JSON
                    .setIssuer("re1ife") //签发者
                    .setIssuedAt(now) //签发时间
                    .signWith(signatureAlgorithm, secretKey) 
                    .setExpiration(expdDate); //期望时间


    }

    //创建token
    public static String createJWT(String id, String subject, Long ttlMills){
        JwtBuilder builder = getJwtBuilder(subject, ttlMills, id);
        return builder.compact();
    }
    public static String createJWT( String subject, Long ttlMills){
        JwtBuilder builder = getJwtBuilder(subject, ttlMills, getUUID());
        return builder.compact();
    }

    //生成加密后的密钥
    public static SecretKey generalKey(){
        byte[] encodedKey = Base64.getDecoder().decode(JwtUtil.JWT_KEY);
        SecretKey key = new SecretKeySpec(encodedKey,0, encodedKey.length,"AES");
        return key;
    }

    //解析Jwt
    public static Claims praseJWT(String jwt) throws Exception {
        SecretKey secretKey = generalKey();
        return  (Claims) Jwts.parser()
                    .setSigningKey(secretKey)
                    .parse(jwt)
                    .getBody();
    }

}

