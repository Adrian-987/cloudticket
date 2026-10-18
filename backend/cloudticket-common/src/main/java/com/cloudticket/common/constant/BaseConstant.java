package com.cloudticket.common.context;

/**
 * 请求上下文：拦截器解析完登录身份后写入当前用户 id，
 * 同一请求线程内任意位置可读取。
 * 必须在拦截器 afterCompletion 中调用 remove()，
 * 否则 Tomcat 线程复用会导致用户身份串号。
 */
public class BaseContext {

    private static final ThreadLocal<Long> THREAD_LOCAL = new ThreadLocal<>();

    public static void setCurrentId(Long id) {
        THREAD_LOCAL.set(id);
    }

    public static Long getCurrentId() {
        return THREAD_LOCAL.get();
    }

    public static void removeCurrentId() {
        THREAD_LOCAL.remove();
    }
}
