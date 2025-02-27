package com.tanhua.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResult {

    private String errCode;
    private String errMessage;

    public static ErrorResult error() {
        return ErrorResult.builder().errCode("999999").errMessage("系统异常稍后再试").build();
    }

    public static ErrorResult fail() {
        return ErrorResult.builder().errCode("000001").errMessage("发送验证码失败").build();
    }

    public static ErrorResult loginError() {
        return ErrorResult.builder().errCode("000002").errMessage("验证码失效").build();
    }

    public static ErrorResult faceError() {
        return ErrorResult.builder().errCode("000003").errMessage("图片非人像，请重新上传!").build();
    }

    public static ErrorResult mobileError() {
        return ErrorResult.builder().errCode("000004").errMessage("手机号已注册").build();
    }

    public static ErrorResult tokenError() {
        return ErrorResult.builder().errCode("000099").errMessage("token无效").build();
    }

    public static ErrorResult contentError() {
        return ErrorResult.builder().errCode("000098").errMessage("内容不能为空!").build();
    }

    public static ErrorResult likeError() {
        return ErrorResult.builder().errCode("000006").errMessage("用户已点赞").build();
    }

    public static ErrorResult disLikeError() {
        return ErrorResult.builder().errCode("000007").errMessage("用户未点赞").build();
    }

    public static ErrorResult loveError() {
        return ErrorResult.builder().errCode("000008").errMessage("用户已喜欢").build();
    }

    public static ErrorResult disloveError() {
        return ErrorResult.builder().errCode("000009").errMessage("用户未喜欢").build();
    }
    public static ErrorResult likeCommentError() {
        return ErrorResult.builder().errCode("000010").errMessage("不存在该评论").build();
    }

    public static ErrorResult followError() {
        return ErrorResult.builder().errCode("000011").errMessage("用户已关注").build();
    }

    public static ErrorResult unFollowError() {
        return ErrorResult.builder().errCode("000012").errMessage("用户未关注").build();
    }

}