package spring.service;

import spring.di.annotation.Autowired;
import spring.service.annotations.Service;

@Service
public class BarService {
    @Autowired
    UserService userService;

    public boolean bounce(int fromId, int toId, int amount, boolean failAfterDebit) {
        return userService.transferAge(fromId, toId, amount, failAfterDebit);
    }
}
