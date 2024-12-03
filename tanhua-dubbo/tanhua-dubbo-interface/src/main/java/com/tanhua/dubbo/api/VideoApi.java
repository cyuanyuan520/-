package com.tanhua.dubbo.api;

import com.tanhua.model.mongo.FocusUser;
import com.tanhua.model.mongo.Video;

import java.util.ArrayList;
import java.util.List;

public interface VideoApi {
    String save(Video video);

    List<Long> randomVid(List<Long> vidList, int needSize);

    ArrayList<Video> findVideosByVid(List<Long> vidList);

    void follow(FocusUser focusUser);

    void unFollow(FocusUser focusUser);
}
