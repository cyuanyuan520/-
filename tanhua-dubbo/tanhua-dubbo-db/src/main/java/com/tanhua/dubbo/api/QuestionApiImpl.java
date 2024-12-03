package com.tanhua.dubbo.api;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tanhua.dubbo.mappers.QuestionMapper;
import com.tanhua.model.domain.Question;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

@DubboService
public class QuestionApiImpl implements QuestionApi{

    @Autowired
    private QuestionMapper questionMapper;

    /**
     * 查询陌生人问题
     * @param userId
     * @return
     */
    @Override
    public Question findByUserId(Long userId) {
        QueryWrapper<Question> queryWrapper = new QueryWrapper<Question>();
        queryWrapper.eq("user_id", userId);
        Question question = questionMapper.selectOne(queryWrapper);
        return question;
    }

    /**
     * 保存(新建/插入)陌生人问题
     * @param res
     */
    @Override
    public void save(Question res) {
        questionMapper.insert(res);
    }

    /**
     * 更新陌生人问题
     * @param res
     */
    @Override
    public void update(Question res) {
        questionMapper.updateById(res);
    }
}
