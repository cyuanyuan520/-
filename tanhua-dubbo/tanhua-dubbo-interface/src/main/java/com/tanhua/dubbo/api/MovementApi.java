package com.tanhua.dubbo.api;

import com.tanhua.model.mongo.Movement;
import com.tanhua.model.vo.PageResult;

import java.util.List;

public interface MovementApi {
    void publish(Movement movement);

    PageResult getMovementById(Long userId, int page, int pagesize);

    List<Movement> getFriendMovement(Long userId, int page, int pagesize);

    List<Movement> getRandomMovements(int pagesize);

    List<Movement> getRecommandMovement(List<Long> pids);

    Movement getMovementByMId(String id);
}
