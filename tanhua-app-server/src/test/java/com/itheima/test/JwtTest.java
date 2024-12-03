package com.itheima.test;

import io.jsonwebtoken.*;
import org.junit.Test;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JwtTest {

    /**
     * 生成token测试
     */
    @Test
    public void testCreatToken() {
        //1.准备数据
        Map map = new HashMap();
        map.put("id", "1");
        map.put("email", "482734085@qq.com");
        long now = System.currentTimeMillis();
        //2.使用jwt的工具类生成token
        String token = Jwts.builder()
                .signWith(SignatureAlgorithm.HS512, "itcast")
                .setClaims(map)
                .setExpiration(new Date(now + 50000))
                .compact();
        System.out.println(token);
    }

    @Test
    public void testParseToken() {
        String token = "eyJhbGciOiJIUzUxMiJ9.eyJpZCI6IjEiLCJleHAiOjE3MzEzMTcyODQsImVtYWlsIjoiNDgyNzM0MDg1QHFxLmNvbSJ9.9MyThSISygV3LNtO9sP3hgUQ6N5Q7RvVlzFpnepzd39sL_4C1vZ4eK7npyXjM8dyPUQ7UPtZE2YSpgpjH71sQg";
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey("itcast")
                    .parseClaimsJws(token)
                    .getBody();
            System.out.println(claims);
        }catch (ExpiredJwtException e) {
            System.out.println("token已过期");
        }catch (SignatureException e) {
            System.out.println("token不合法");
        }

    }

}
