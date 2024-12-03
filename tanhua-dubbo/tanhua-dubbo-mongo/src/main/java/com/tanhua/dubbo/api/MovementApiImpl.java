package com.tanhua.dubbo.api;

import cn.hutool.core.collection.CollUtil;
import com.tanhua.dubbo.utils.IdWorker;
import com.tanhua.dubbo.utils.TimelineService;
import com.tanhua.model.mongo.Friend;
import com.tanhua.model.mongo.Movement;
import com.tanhua.model.mongo.MovementTimeLine;
import com.tanhua.model.vo.PageResult;
import org.apache.dubbo.config.annotation.DubboService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.TypedAggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@DubboService
public class MovementApiImpl implements MovementApi{
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private IdWorker idWorker;
    @Autowired
    private TimelineService timelineService;

    @Override
    public void publish(Movement movement) {
        try {
            //对movement设置各项所需数据
            //pid
            movement.setPid(idWorker.getNextId("movement"));
            //time
            movement.setCreated(System.currentTimeMillis());
            //save
            mongoTemplate.save(movement);
            //保存timelineService
            timelineService.saveTimeline(movement);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据id返回所有动态
     * 封装成PageResult
     * @param userId
     * @param page
     * @param pagesize
     * @return
     */
    @Override
    public PageResult getMovementById(Long userId, int page, int pagesize) {
        //构造查询条件
        Query query = new Query(Criteria.where("userId").is(userId))
                .skip((page - 1) * pagesize)
                .limit(pagesize)
                .with(Sort.by(Sort.Order.desc("created")));
        List<Movement> movements = mongoTemplate.find(query, Movement.class);
        long count = mongoTemplate.count(query, Movement.class);//总数
        //构造最终返回对象
        PageResult pageResult = new PageResult(page, pagesize, count, movements);
        return pageResult;
    }

    /**
     * 查询好友动态
     * @param userId
     * @param page
     * @param pagesize
     * @return
     */
    @Override
    public List<Movement> getFriendMovement(Long userId, int page, int pagesize) {
        //先获取movement-timeLine数据
        Query query = new Query(Criteria.where("friendId").is(userId))
                .skip((page - 1) * pagesize)
                .limit(pagesize)
                .with(Sort.by(Sort.Order.desc("created")));
        List<MovementTimeLine> movementTimeLines = mongoTemplate.find(query, MovementTimeLine.class);
        //提炼出movementId数据
        List<ObjectId> movementIds = CollUtil.getFieldValues(movementTimeLines, "movementId", ObjectId.class);
        //一次性查询所有movement的详细数据(节省资源)
        Query movementQuery = new Query(Criteria.where("_id").in(movementIds)).with(Sort.by(Sort.Order.desc("created")));
        List<Movement> movements = mongoTemplate.find(movementQuery, Movement.class);
        return movements;
    }

    /**
     * 随机获取pagesize条movement
     * @param pagesize
     * @return
     */
    @Override
    public List<Movement> getRandomMovements(int pagesize) {
        //构造统计参数 采样数量
        TypedAggregation<Movement> aggregation = Aggregation.newAggregation(Movement.class, Aggregation.sample(pagesize));
        //查询
        AggregationResults<Movement> aggregate = mongoTemplate.aggregate(aggregation, Movement.class);
        //提取最终结果
        return aggregate.getMappedResults();
    }

    /**
     * 根据pid集合 返回movement集合
     * @param pids
     * @return
     */
    @Override
    public List<Movement> getRecommandMovement(List<Long> pids) {
        //构造查询条件
        Query query = new Query(Criteria.where("pid").in(pids));
        List<Movement> movements = mongoTemplate.find(query, Movement.class);
        return movements;
    }

    @Override
    public Movement getMovementByMId(String id) {
        Movement byId = mongoTemplate.findById(id, Movement.class);
        return byId;
    }

}
