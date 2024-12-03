package com.tanhua.server.service;

import com.tanhua.autoconfig.template.AipFaceTemplate;
import com.tanhua.autoconfig.template.OssTemplate;
import com.tanhua.dubbo.api.UserInfoApi;
import com.tanhua.model.domain.UserInfo;
import com.tanhua.model.vo.ErrorResult;
import com.tanhua.model.vo.UserInfoVo;
import com.tanhua.server.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.IOException;

@Service
@Slf4j
public class UserInfoService {

    @DubboReference
    private UserInfoApi userInfoApi;
    @Autowired
    private OssTemplate ossTemplate;
    @Autowired
    private AipFaceTemplate aipFaceTemplate;

    /**
     * 保存用户基本信息
     * @param userInfo
     */
    public void save(UserInfo userInfo) {
        userInfoApi.save(userInfo);
    }


    /**
     * 更新头像
     * @param headPhoto
     * @param id
     * @throws IOException
     */
    public void updateHead(MultipartFile headPhoto, long id) throws IOException {
        //1.将用户头像上传到阿里云
        String url = ossTemplate.upload(headPhoto.getOriginalFilename(), new BufferedInputStream(headPhoto.getInputStream()));
        //2.将url给百度云识别人脸
        boolean faceCheck = aipFaceTemplate.detect(url);
        //3.不包含人脸则抛出异常
        if (!faceCheck) {
            throw new BusinessException(ErrorResult.faceError());//自定义异常 交给异常拦截处理
        }
        //4.包含人脸则调用userInfoApi更新数据库
        UserInfo userInfo = new UserInfo();
        userInfo.setId(id);
        userInfo.setAvatar(url);
        userInfoApi.update(userInfo);
    }

    /**
     * 根据id查询用户信息
     * 接收userapi的UserInfo  返回UserInfoVo
     * @param userID
     * @return
     */
    public UserInfoVo fingById(Long userID) {
        UserInfo userInfo = userInfoApi.findById(userID);
        UserInfoVo userInfoVo = new UserInfoVo();
        BeanUtils.copyProperties(userInfo, userInfoVo);
        userInfoVo.setAge(String.valueOf(userInfo.getAge()));
        return userInfoVo;
    }


    /**
     * 更新用户信息(不包含头像)
     * @param userInfo
     */
    public void update(UserInfo userInfo) {
        userInfoApi.update(userInfo);
    }
}
