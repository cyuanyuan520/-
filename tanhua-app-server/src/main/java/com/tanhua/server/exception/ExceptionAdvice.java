package com.tanhua.server.exception;

import com.tanhua.model.vo.ErrorResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.View;

@ControllerAdvice
public class ExceptionAdvice {

    private final View error;

    public ExceptionAdvice(View error) {
        this.error = error;
    }

    /**
     * 全局处理BusinessException.class
     * 捕获到异常就直接返回响应
     * @param businessException
     * @return
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity handlerException(BusinessException businessException) {
        businessException.printStackTrace();//打印堆栈信息
        ErrorResult errorResult = businessException.getErrorResult();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResult);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity handlerOther(Exception exception) {
        exception.printStackTrace();//打印堆栈信息
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ErrorResult.error());
    }


}
