package cn.itcast.core.service;

import cn.itcast.core.pojo.user.User;

public interface UserService {
    void sendCode(String phone);

    void add(String smscode, User user);
}
