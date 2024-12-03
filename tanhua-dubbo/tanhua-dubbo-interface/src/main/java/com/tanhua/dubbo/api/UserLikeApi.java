package com.tanhua.dubbo.api;

public interface UserLikeApi {
    Boolean saveOrUpdate(Long userId, long likeUserId, boolean likeOrNot);
}
