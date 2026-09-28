package spring.service;

import mvc.dto.User;
import mybatis.mapper.UserMapper;
import spring.di.annotation.Autowired;
import spring.service.annotations.Service;
import spring.service.annotations.Transactional;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    public List<User> listUsers() {
        return userMapper.findAll();
    }

    public User getUser(String idStr) {
        int id = parseId(idStr);
        User user = userMapper.findById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在：" + id);
        }
        return user;
    }

    public User createUser(User request) {
        if (request.name() == null || request.name().isBlank()) {
            throw new RuntimeException("name 不能为空");
        }
        if (request.age() == null) {
            throw new RuntimeException("age不能为空");
        }
        userMapper.insert(request.name(), request.age());
        return userMapper.findLast();
    }

    @Transactional
    public User updateUser(String idStr, User request) {
        int id = parseId(idStr);
        User user = userMapper.findById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在：" + id);
        }
        userMapper.update(id, request.name(), request.age());
        return userMapper.findById(id);
    }

    @Transactional
    public boolean deleteUser(String idStr) {
        int id = parseId(idStr);
        User user = userMapper.findById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在：" + id);
        }
        return userMapper.deleteById(id) > 0;
    }

    private int parseId(String idStr) {
        try {
            return Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            throw new RuntimeException("非法 id:" + idStr);
        }
    }

    @Transactional
    public boolean transferAge(int fromId, int toID, int amount, boolean failAfterDebit) {
        User user = userMapper.findById(fromId);
        if (user == null) {
            throw new RuntimeException("用户不存在：" + fromId);
        }
        user = userMapper.findById(toID);
        if (user == null) {
            throw new RuntimeException("用户不存在：" + toID);
        }
        if (amount <= 0) {
            throw new IllegalStateException("传递数不能小于0");
        }
        boolean ok = userMapper.adjustAge(fromId, -amount) > 0;
        if (!ok) {
            throw new RuntimeException("传递失败");
        }
        if (failAfterDebit) {
            throw new RuntimeException("模拟中途失败");
        }
        ok = userMapper.adjustAge(toID, amount) > 0;
        if (!ok) {
            throw new RuntimeException("传递失败");
        }
        return true;
    }
}
