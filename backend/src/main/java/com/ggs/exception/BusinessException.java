package com.ggs.exception;

/**
 * 业务异常：message 为可直接展示给用户的信息，由全局异常处理器统一透出。
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}