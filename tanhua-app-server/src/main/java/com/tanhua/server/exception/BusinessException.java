package com.tanhua.server.exception;

import com.easemob.im.server.api.block.group.msg.BlockUserSendMsgToGroup;
import com.tanhua.model.vo.ErrorResult;
import lombok.Data;

/**
 * 自定义异常
 */
@Data
public class BusinessException extends RuntimeException{

    private ErrorResult errorResult;

    public BusinessException(ErrorResult errorResult) {
        super(errorResult.getErrMessage());
        this.errorResult = errorResult;
    }
}
