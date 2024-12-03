package com.tanhua.dubbo.api;

import java.util.List;

public interface UserLocationApi {
    Boolean uploadLocation(Long userId, Double latitude, Double longitude, String addrStr);

    List<Long> getNearUsers(Long userId, Double distance);
}
