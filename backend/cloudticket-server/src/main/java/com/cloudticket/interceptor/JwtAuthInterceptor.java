package com.cloudticket.interceptor;

import cn.hutool.json.JSONUtil;
import com.cloudticket.common.util.JwtUtil;
import com.cloudticket.common.context.BaseContext;
import com.cloudticket.common.result.Result;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

public class JwtAuthInterceptor implements HandlerInterceptor {

    private final String secret;      // 本区密钥
    private final String claimKey;    // 身份claim名（userId / merchantId / adminId）

    //构造器构造,注入字段
    public JwtAuthInterceptor(String secret, String claimKey) {
        this.secret = secret;
        this.claimKey = claimKey;
    }

    //核心,拦截器通过bool值判断token是否可用
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {


        // 1. 只拦 Controller 方法：拦截器会扫到静态资源等非 Controller 处理器，放行
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        //执行token校验
        String token=request.getHeader("Authorization");
        if(token==null||token.isBlank()){
            write401(response,"请先登录");
            return false;
        }
        Claims claims;
        try {
            claims = JwtUtil.parse(token, secret);
        } catch (ExpiredJwtException e) {//令牌过期（exp 已过）
            write401(response, "登录已过期，请重新登录");
            return false;
        } catch (JwtException e) {//信息错误
            write401(response, "登录信息错误,请重新登录");
            return false;
        }
        // 4. 身份写入 ThreadLocal
        //校验token中是否存在userid字段,没有则错误
        Object object=claims.get(claimKey);
        if(!(object instanceof Number number)){
            write401(response,"请先登录");
            return false;
        }
        BaseContext.setCurrentId(number.longValue());
        // 5. 放行
        return true;
    }

    //结束清理线程池threadLocal的存储信息,防止串用
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        BaseContext.removeCurrentId();
    }

    //token错误给response添加状态码和错误提示信息(result形式)
    private void write401(HttpServletResponse response, String msg) throws IOException {
        response.setStatus(401);
        response.setContentType("application/json;charset=UTF-8");   // 不设 UTF-8，中文提示乱码
        response.getWriter().write(JSONUtil.toJsonStr(Result.error(msg)));   // Hutool 的 JSONUtil
    }
}
