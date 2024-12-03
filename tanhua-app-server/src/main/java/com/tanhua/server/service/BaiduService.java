package com.tanhua.server.service;

import com.tanhua.dubbo.api.UserLocationApi;
import com.tanhua.model.mongo.UserLocation;
import com.tanhua.model.vo.ErrorResult;
import com.tanhua.server.exception.BusinessException;
import com.tanhua.server.interceptor.UserHolder;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

@Service
public class BaiduService {

    @DubboReference
    private UserLocationApi userLocationApi;

    /**
     * 上报地理信息
     */
    public void updateLocation(Double latitude, Double longitude, String addrStr) {
        //dubbo远程调用api
        Boolean result = userLocationApi.uploadLocation(UserHolder.getUserId(), latitude, longitude, addrStr);
        //调用错误直接抛异常
        if (!result) {
            throw new BusinessException(ErrorResult.error());
        }
    }
}
