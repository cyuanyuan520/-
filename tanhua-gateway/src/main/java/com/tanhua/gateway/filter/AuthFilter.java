package com.tanhua.gateway.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tanhua.commons.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AuthFilter implements GlobalFilter, Ordered {

    @Value("${gateway.excludeUrls}")
    private List<String> excludeUrls;//不需要校验的链接

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //1.排除不需要校验的请求
        String path = exchange.getRequest().getURI().getPath();
        if (excludeUrls.contains(path)) {
            return chain.filter(exchange);
        }
        //获取token并校验
        String token = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (!StringUtils.isEmpty(token)) {
            token = token.replaceAll("Bearer ", "");
        }
        boolean verified = JwtUtils.verifyToken(token);
        if (!verified) {
            Map<String, Object> responseMap = new HashMap<String, Object>();
            responseMap.put("errorCode", 401);
            responseMap.put("errMessage", "用户未登录");
            return responseError(exchange.getResponse(), responseMap);
        }
        return null;
    }

    @Override
    public int getOrder() {
        return 0;
    }

    //响应错误数据
    private Mono<Void> responseError(ServerHttpResponse response, Map<String, Object> responseData){
        // 将信息转换为 JSON
        ObjectMapper objectMapper = new ObjectMapper();
        byte[] data = new byte[0];
        try {
            data = objectMapper.writeValueAsBytes(responseData);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        // 输出错误信息到页面
        DataBuffer buffer = response.bufferFactory().wrap(data);
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");
        return response.writeWith(Mono.just(buffer));
    }
}
