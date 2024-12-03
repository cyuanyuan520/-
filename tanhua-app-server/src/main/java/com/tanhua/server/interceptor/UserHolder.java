package com.tanhua.server.interceptor;

import com.tanhua.model.domain.User;

/**
 * 向threadLocal存储数据的工具类
 */
public class UserHolder {

    private static ThreadLocal<User> tl = new ThreadLocal<>();

    /**
     * 将用户对象存入threadLocal
     */
    public static void set(User user) {
        tl.set(user);
    }

    /**
     * 从当前线程获取到user
     */
    public static User get() {
        return tl.get();
    }

    /**
     * 获取当前线程的User对象中存储的id
     */
    public static Long getUserId(){
        return tl.get().getId();
    }

    /**
     * 从当前线程中获取User的联系方式(由于token中的qq已被处理过 所以返回的是真实qq 如482734085)
     */
    public static String getMobile() {
        return tl.get().getMobile();
    }

    /**
     * 请求完毕后删除当前线程的数据
     * 防止内存溢出
     */
    public static void remove() {
        tl.remove();
    }

}
