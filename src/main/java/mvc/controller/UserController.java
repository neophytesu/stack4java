package mvc.controller;

import mvc.annotation.Controller;
import mvc.annotation.param.PathVariable;
import mvc.annotation.param.RequestBody;
import mvc.annotation.param.ResponseBody;
import mvc.annotation.request.DeleteMapping;
import mvc.annotation.request.GetMapping;
import mvc.annotation.request.PostMapping;
import mvc.annotation.request.PutMapping;
import mvc.dto.User;
import spring.di.annotation.Autowired;
import spring.service.UserService;

import java.util.List;
import java.util.Map;

@Controller("/api/users")
@ResponseBody
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping()
    public List<User> list() {
        return userService.listUsers();
    }

    @GetMapping("/{id}")
    public User get(@PathVariable("id") String id) {
        return userService.getUser(id);
    }

    @PostMapping()
    public User create(@RequestBody User request) {
        return userService.createUser(request);
    }

    @PutMapping("/{id}")
    public User update(@PathVariable("id") String id, @RequestBody User request) {
        return userService.updateUser(id, request);
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable("id") String id) {
        boolean success = userService.deleteUser(id);
        return Map.of("success", success);
    }
}
