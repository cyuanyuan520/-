package com.tanhua.server.controller;

import com.tanhua.server.service.BaiduService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/baidu")
public class BaiduController {

    @Autowired
    private BaiduService baiduService;

    /**
     * 上报地理信息
     */
    @PostMapping("/location")
    public ResponseEntity location(@RequestBody Map param) {
        //解析数据
        Double latitude = Double.valueOf(param.get("latitude").toString());
        Double longitude = Double.valueOf(param.get("longitude").toString());
        String addrStr = param.get("addrStr").toString();
        //调用service代码
        baiduService.updateLocation(latitude, longitude, addrStr);
        return ResponseEntity.ok(null);
    }
}
