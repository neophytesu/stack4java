package spring.service;

import mvc.dto.User;
import spring.di.annotation.Autowired;
import spring.service.annotations.Service;
import spring.service.annotations.Transactional;

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

    @Transactional
    public User updateUser(String idStr, User request) {
        int id = parseId(idStr);
        userRepository.findById(id).orElseThrow(() -> new RuntimeException("用户不存在：" + id));
        userRepository.update(id, request.name(), request.age());
        return userRepository.findById(id).orElseThrow();
    }

    @Transactional
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

    @Transactional
    public boolean transferAge(int fromId, int toID, int amount, boolean failAfterDebit) {
        userRepository.findById(fromId).orElseThrow();
        userRepository.findById(toID).orElseThrow();
        if (amount <= 0) {
            throw new IllegalStateException("传递数不能小于0");
        }
        boolean ok = userRepository.adjustAge(fromId, -amount);
        if (!ok) {
            throw new RuntimeException("传递失败");
        }
        if (failAfterDebit) {
            throw new RuntimeException("模拟中途失败");
        }
        ok = userRepository.adjustAge(toID, amount);
        if (!ok) {
            throw new RuntimeException("传递失败");
        }
        return true;
    }
}
