package ltd.cylleneworks.sandbox.springboot.hbase;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @RequestMapping("/user/{username}")
    public User user(@PathVariable("username") String username) {
        return userRepository.find(username);
    }
}
