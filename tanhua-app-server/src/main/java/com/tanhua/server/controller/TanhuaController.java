package com.tanhua.server.controller;


import com.tanhua.model.dto.RecommendUserDto;
import com.tanhua.model.vo.NearUserVo;
import com.tanhua.model.vo.PageResult;
import com.tanhua.model.vo.TodayBest;
import com.tanhua.server.service.TanhuaService;
import com.tanhua.server.service.TodayBestService;
import lombok.experimental.PackagePrivate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/tanhua")
public class TanhuaController {

    @Autowired
    private TodayBestService todayBestService;
    @Autowired
    private TanhuaService tanhuaService;

    /**
     * 今日佳人
     * @return
     */
    @GetMapping("/todayBest")
    public ResponseEntity getTodayBest() {
        TodayBest todayBest = todayBestService.getTodayBest();
        return ResponseEntity.ok(todayBest);
    }

    /**
     * 推荐朋友
     * @param recommendUserDto
     * @return
     */
    @GetMapping("/recommendation")
    public ResponseEntity recommendation(RecommendUserDto recommendUserDto) {
        PageResult pr = tanhuaService.getRecommendList(recommendUserDto);
        return ResponseEntity.ok(pr);
    }

    /**
     * 查询佳人信息
     */
    @GetMapping("/{id}/personalInfo")
    public ResponseEntity personalInfo(@PathVariable long id) {
        TodayBest todayBest = tanhuaService.getInfoById(id);
        return ResponseEntity.ok(todayBest);
    }

    /**
     * 查询陌生人问题
     */
    @GetMapping("/strangerQuestions")
    public ResponseEntity strangerQuestions(@RequestParam String userId) {
        String question = tanhuaService.getStrangerQuestions(userId);
        return ResponseEntity.ok(question);
    }

    /**
     * 回复陌生人问题
     */
    @PostMapping("/strangerQuestions")
    public ResponseEntity strangerQuestions (@RequestBody Map map) {
        String obj = map.get("userId").toString();
        Long userId = Long.valueOf(obj);//我想加的人的id 我自己的id待会从threadLocal取出来
        String reply = map.get("reply").toString();
        tanhuaService.replyQuestions(userId, reply);
        return ResponseEntity.ok(null);
    }

    /**
     * 探花: 左滑右滑卡片
     */
    @GetMapping("/cards")
    public ResponseEntity cards() {
        List<TodayBest> list = tanhuaService.getCards();
        return ResponseEntity.ok(list);
    }

    /**
     * 探花: 右滑喜欢
     */
    @GetMapping("/{id}/love")
    public ResponseEntity love(@PathVariable long id) {
        tanhuaService.likeUser(id);
        return ResponseEntity.ok(null);
    }

    /**
     * 探花: 左滑不喜欢
     */
    @GetMapping("/{id}/unlove")
    public ResponseEntity unLove(@PathVariable long id) {
        tanhuaService.unLikeUser(id);
        return ResponseEntity.ok(null);
    }

    /**
     * 搜附近的人
     */
    @GetMapping("/search")
    public ResponseEntity searchNear(@RequestParam(defaultValue = "woman") String gender, @RequestParam(defaultValue = "2000") String distance) {
        List<NearUserVo> vos = tanhuaService.searchNear(gender, Double.valueOf(distance));
        return ResponseEntity.ok(vos);
    }


}
