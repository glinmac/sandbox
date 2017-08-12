package ltd.cylleneworks.sandbox.springboot.hbase;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

@RunWith(SpringRunner.class)
@WebMvcTest(UserController.class)
public class UserControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    @Test
    public void missingUserShouldReturn404() throws Exception {
        given(this.userRepository.find("jdoe"))
                .willReturn(Optional.empty());
        this.mockMvc.perform(get("/user/jdoe"))
                .andDo(print())
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void existingUserShouldSucceed() throws Exception {
        given(this.userRepository.find("jdoe"))
                .willReturn(Optional.of(new User("jdoe", "John Doe", "jdoe@localhost.com", "1970-01-01")));
        this.mockMvc.perform(get("/user/jdoe"))
                .andDo(print()).andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("jdoe"))
                .andExpect(jsonPath("$.fullname").value("John Doe"))
                .andExpect(jsonPath("$.email").value("jdoe@localhost.com"))
                .andExpect(jsonPath("$.dob").value("1970-01-01"));
    }

}
