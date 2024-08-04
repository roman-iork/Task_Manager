package hexlet.code.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.dto.UserDTO;
import hexlet.code.exception.NoSuchResourceException;
import hexlet.code.mapper.UserMapper;
import hexlet.code.model.User;
import hexlet.code.repository.UserRepository;
import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private Faker faker;

    private User testUser;


    @BeforeEach
    public void setUp() {
        testUser = new User();
        testUser.setFirstName(faker.name().firstName());
        testUser.setLastName("Mallow");
        testUser.setEmail(faker.internet().emailAddress());
        testUser.setPassword(faker.internet().password());
        userRepository.save(testUser);
    }

    @Test
    public void testShowUser() throws Exception {
        var testUserId = testUser.getId();
        var body = mockMvc.perform(get("/api/users/" + testUserId))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        var actualUserDTO = objectMapper.readValue(body, new TypeReference<UserDTO>() {
        });
        assertThat(actualUserDTO.getFirstName()).isEqualTo(testUser.getFirstName());
    }

    @Test
    public void testShowAllUsers() throws Exception {
        var body = mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        var actualUsersDTO = objectMapper.readValue(body, new TypeReference<List<UserDTO>>() {
            });
        var expectedUsersDTO = userRepository.findAll().stream().map(u -> userMapper.map(u)).toList();
        assertThat(actualUsersDTO.size()).isEqualTo(expectedUsersDTO.size());
        assertThat(actualUsersDTO.getFirst().getFirstName()).isEqualTo(expectedUsersDTO.getFirst().getFirstName());
    }

    @Test
    public void testCreateUser() throws Exception {
        var request = post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"jack@google.com\","
                        + "\"firstName\": \"Jack\","
                        + "\"lastName\": \"Jons\","
                        + "\"password\": \"some-password\"}");
        var body = mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        var actualUserDTO = objectMapper.readValue(body, new TypeReference<UserDTO>() {
            });
        var user = userRepository.findByEmail("jack@google.com")
                .orElseThrow(() -> new NoSuchResourceException("No user with such email!"));
        var expectedUserDTO = userMapper.map(user);
        assertThat(actualUserDTO.getFirstName()).isEqualTo(expectedUserDTO.getFirstName());
        assertThat(actualUserDTO.getId()).isEqualTo(expectedUserDTO.getId());
    }

    @Test
    public void testUpdateUser() throws Exception {
        var request = put("/api/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"new@new.com\", \"password\": \"new-password\"}");
        var body = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        var actualUserDTO = objectMapper.readValue(body, new TypeReference<UserDTO>() {
            });
        var user = userRepository.findById(1L)
                .orElseThrow(() -> new NoSuchResourceException("No user with such id!"));
        var expectedUserDTO = userMapper.map(user);
        assertThat(actualUserDTO.getEmail()).isEqualTo("new@new.com");
        assertThat(expectedUserDTO.getEmail()).isEqualTo("new@new.com");
    }

    @Test
    public void testDeleteUser() throws Exception {
        var request = delete("/api/users/1");
        mockMvc.perform(request)
                .andExpect(status().isNoContent());
        var user = userRepository.findById(1L).orElse(null);
        assertThat(user).isEqualTo(null);
    }
}
