package com.cloudticket.common.exception;

/**
 * 业务异常基类：可预期的业务错误（如"库存不足"）抛出该类或其子类，
 * 由全局异常处理器统一翻译为 Result 返回。
 * 继承 RuntimeException 以便被声明式事务默认回滚。
 */
public class BaseException extends RuntimeException {

    public BaseException() {
    }

    public BaseException(String message) {
        super(message);
    }
}
