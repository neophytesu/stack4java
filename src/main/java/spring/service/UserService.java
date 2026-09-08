package spring.service;

import mvc.dto.User;
import spring.di.annotation.Autowired;
import spring.service.annotations.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public List<User> listUsers() {
        return userRepository.findAll();
    }

    public User getUser(String idStr) {
        int id = parseId(idStr);
        return userRepository.findById(id).orElseThrow(() -> new RuntimeException("用户不存在：" + id));
    }

    public User createUser(User request) {
        if (request.name() == null || request.name().isBlank()) {
            throw new RuntimeException("name 不能为空");
        }
        if (request.age() == null) {
            throw new RuntimeException("age不能为空");
        }
        return userRepository.insert(request.name(), request.age());
    }

    public User updateUser(String idStr, User request) {
        int id = parseId(idStr);
        userRepository.findById(id).orElseThrow(() -> new RuntimeException("用户不存在：" + id));
        userRepository.update(id, request.name(), request.age());
        return userRepository.findById(id).orElseThrow();
    }

    public boolean deleteUser(String idStr) {
        int id = parseId(idStr);
        userRepository.findById(id).orElseThrow(() -> new RuntimeException("用户不存在：" + id));
        return userRepository.deleteById(id);
    }

    private int parseId(String idStr) {
        try {
            return Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            throw new RuntimeException("非法 id:" + idStr);
        }
    }
}
