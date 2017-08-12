package ltd.cylleneworks.sandbox.springboot.hbase;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(String user) {
        super("Could not find user '%s'.".format(user));
    }
}