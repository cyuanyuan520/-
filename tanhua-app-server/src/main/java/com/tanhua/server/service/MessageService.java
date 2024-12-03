package com.tanhua.server.service;

import cn.hutool.core.collection.CollUtil;
import com.tanhua.autoconfig.template.HuanXinTemplate;
import com.tanhua.commons.utils.Constants;
import com.tanhua.dubbo.api.*;
import com.tanhua.model.domain.User;
import com.tanhua.model.domain.UserInfo;
import com.tanhua.model.enums.CommentType;
import com.tanhua.model.mongo.Comment;
import com.tanhua.model.mongo.Friend;
import com.tanhua.model.vo.*;
import com.tanhua.server.exception.BusinessException;
import com.tanhua.server.interceptor.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class MessageService {

    @DubboReference
    private UserApi userApi;
    @DubboReference
    private UserInfoApi userInfoApi;
    @DubboReference
    private FriendApi friendApi;
    @DubboReference
    private CommentApi commentApi;
    @DubboReference
    private AnnouncementApi announcementApi;
    @Autowired
    private HuanXinTemplate huanXinTemplate;
    /**
     * 根据环信id寻找对应的UserInfoVo
     */
    public UserInfoVo findUserinfoByHxId(String huanxinId) throws InvocationTargetException, IllegalAccessException {
        //根据环信id找到对应的user
        User user = userApi.findUserByHxId(huanxinId);
        Long userId = user.getId();//得到用户id
        //调用userInfoApi:找详细信息
        UserInfo userInfo = userInfoApi.findById(userId);
        log.error("service层: dubbo接收到数据{}", userInfo);
        UserInfoVo vo = new UserInfoVo();
        BeanUtils.copyProperties(userInfo, vo);
        if (userInfo.getAge() != null) {
            //userInfo中年龄不为空
            vo.setAge(String.valueOf(userInfo.getAge()));
        }
        log.error("service最后返回的数据{}", vo);
        return vo;
    }

    /**
     * 添加好友
     */
    public void addContact(Long friendId) {
        //获取当前用户id
        Long userId = UserHolder.getUserId();
        //在环信添加好友
        Boolean check = huanXinTemplate.addContact(Constants.HX_USER_PREFIX + userId,
                Constants.HX_USER_PREFIX + friendId);
        if (!check) {
            throw new BusinessException(ErrorResult.error());
        }
        //环信好友注册成功: 则在mongodb中添加好友
        try {
            friendApi.addContact(userId, friendId);
        } catch (Exception e) {
           e.printStackTrace();
        }
    }

    /**
     * 查询好友列表
     */
    public PageResult getContacts(Integer page, Integer pagesize, String keyword) {
        //根据自己id查询friend列表
        Long userId = UserHolder.getUserId();
        List<Friend> friends = friendApi.getFriendsById(page, pagesize, userId);
        //根据id 查询出UserInfo
        List<Long> friendsId = CollUtil.getFieldValues(friends, "friendId", Long.class);
        UserInfo userInfo = new UserInfo();
        userInfo.setNickname(keyword);//后续模糊查询要用
        Map<Long, UserInfo> infoByIds = userInfoApi.getInfoByIds(friendsId, userInfo);
        //构造返回值
        ArrayList<ContactVo> list = new ArrayList<>();
            //循环friend列表
        for (Friend friend : friends) {
            UserInfo info = infoByIds.get(friend.getFriendId());
            if (info != null) {
                ContactVo vo = ContactVo.init(info);
                list.add(vo);
            }
        }
        PageResult pr = new PageResult(page ,pagesize, (long) friends.size(), list);
        return pr;
    }


    /**
     * 查询自己点赞数据列表
     */
    public PageResult getLikesRecords(Integer page, Integer pagesize) {
        //获取自己id
        Long userId = UserHolder.getUserId();
        //dubbo: 调用commentApi查询点赞记录
        List<Comment> comments = commentApi.getMyCommemts(userId, CommentType.LIKE, page, pagesize);
        ArrayList<CommentVo> list = getCommentVos(comments);
        PageResult pr = new PageResult(page ,pagesize, (long) comments.size(), list);
        return pr;
    }


    /**
     * 查看自己的评论列表
     */
    public PageResult getCommentsRecords(Integer page, Integer pagesize) {
        //获取自己id
        Long userId = UserHolder.getUserId();
        //dubbo: 调用commentApi查询评论记录
        List<Comment> comments = commentApi.getMyCommemts(userId, CommentType.COMMENT, page, pagesize);
        //构造返回pageResult
        ArrayList<CommentVo> list = getCommentVos(comments);
        PageResult pr = new PageResult(page ,pagesize, (long) comments.size(), list);
        return pr;
    }

    /**
     * 查看自己的被收藏列表
     */
    public PageResult getLovesRecords(Integer page, Integer pagesize) {
        //获取自己id
        Long userId = UserHolder.getUserId();
        //dubbo: 调用commentApi查询评论记录
        List<Comment> comments = commentApi.getMyCommemts(userId, CommentType.LOVE, page, pagesize);
        //构造返回pageResult
        ArrayList<CommentVo> list = getCommentVos(comments);
        PageResult pr = new PageResult(page ,pagesize, (long) comments.size(), list);
        return pr;
    }

    /**
     * 传入List<Comment> 返回List<CommentVo>
     */
    private ArrayList<CommentVo> getCommentVos(List<Comment> comments) {
        //构造返回pageResult
        List<Long> ids = CollUtil.getFieldValues(comments, "userId", Long.class);//ids是给动态点赞的人的id
        Map<Long, UserInfo> infoByIds = userInfoApi.getInfoByIds(ids, null);
        if (CollUtil.isEmpty(infoByIds)) {
            //没查询出来数据直接返回空集合
            return new ArrayList<>();
        }
        ArrayList<CommentVo> list = new ArrayList<>();
        for (Comment comment : comments) {
            Long id = comment.getUserId();//点赞者的id
            UserInfo userInfo = infoByIds.get(id);
            CommentVo vo = CommentVo.init(userInfo, comment);
            list.add(vo);
        }
        return list;
    }

    /**
     * 查询公告
     */
    public PageResult getAnnouncements(Integer page, Integer pagesize) {
        PageResult pr = announcementApi.getAnnouncements(page, pagesize);
        return pr;
    }
}
