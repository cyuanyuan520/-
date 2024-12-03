package com.tanhua.dubbo.api;

import com.tanhua.model.mongo.Friend;

import java.util.List;

public interface FriendApi {
    void addContact(Long userId, Long friendId);

    List<Friend> getFriendsById(Integer page, Integer pagesize, Long userId);
}
