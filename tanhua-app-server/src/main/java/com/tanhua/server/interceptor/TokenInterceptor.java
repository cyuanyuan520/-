package com.tanhua.server.interceptor;

import com.tanhua.commons.utils.JwtUtils;
import com.tanhua.model.domain.User;
import com.tanhua.model.vo.ErrorResult;
import com.tanhua.server.exception.BusinessException;
import io.jsonwebtoken.Claims;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class TokenInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //从请求头获取token
        String token = request.getHeader("Authorization");
        //验证token是否有效
        boolean checked = JwtUtils.verifyToken(token);
        //响应数据
        if (!checked) {
            throw new BusinessException(ErrorResult.tokenError()); //token不合法:使用401状态码拦截
        } else {
            //token正常可用则放行
                //解析token并将User对象存入threadLocal
            Claims claims = JwtUtils.getClaims(token);
            String mobile = (String) claims.get("mobile");//获取qq号码
            Integer id = (Integer) claims.get("id");//获取用户id

            User user = new User();
            user.setMobile(mobile);
            user.setId(Long.valueOf(id));
            UserHolder.set(user);//在拦截器中把合法token中包含的用户信息设置到threadLocal中
            return true;
        }
    }

    /**
     * 当前线程结束后 删除线程中存储的User对象 防止内存溢出
     * @param request
     * @param response
     * @param handler
     * @param ex
     * @throws Exception
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserHolder.remove();
    }
}
