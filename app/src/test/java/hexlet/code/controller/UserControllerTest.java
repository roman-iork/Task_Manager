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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static hexlet.code.model.Role.ROLE_USER;
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
    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;
    private String tokenAdmin;
    private String tokenUser;

    @BeforeEach
    public void setUp() throws Exception {

        var password = faker.internet().domainWord();
        testUser = new User();
        testUser.setFirstName(faker.name().firstName());
        testUser.setLastName(faker.name().lastName());
        testUser.setEmail(faker.internet().emailAddress());
        testUser.setPasswordHashed(passwordEncoder.encode(password));
        testUser.setRole(ROLE_USER);
        userRepository.save(testUser);

        tokenAdmin = this.mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\": \"hexlet@example.com\","
                                + "\"password\": \"qwerty\"}"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        tokenUser = this.mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\": \"" + testUser.getEmail()
                                + "\", \"password\": \"" + password + "\"}"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
    }

    @Test
    public void testShowUser() throws Exception {
        var testUserId = testUser.getId();
        var body = mockMvc.perform(get("/api/users/" + testUserId)
                        .header("Authorization", "Bearer " + tokenUser))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        var actualUserDTO = objectMapper.readValue(body, new TypeReference<UserDTO>() {
        });
        assertThat(actualUserDTO.getFirstName()).isEqualTo(testUser.getFirstName());
    }

    @Test
    public void testShowAllUsers() throws Exception {
        var body = mockMvc.perform(get("/api/users")
                .header("Authorization", "Bearer " + tokenUser))
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
                        + "\"password\": \"some-password\"}")
                .header("Authorization", "Bearer " + tokenUser);
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
    public void testUpdateUserAsAdmin() throws Exception {
        var testUserId = testUser.getId();
        var request = put("/api/users/" + testUserId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"new@new.com\", \"password\": \"new-password\"}")
                .header("Authorization", "Bearer " + tokenAdmin);
        var body = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        var actualUserDTO = objectMapper.readValue(body, new TypeReference<UserDTO>() {
            });
        var user = userRepository.findById(testUserId)
                .orElseThrow(() -> new NoSuchResourceException("No user with such id!"));
        var expectedUserDTO = userMapper.map(user);
        assertThat(actualUserDTO.getEmail()).isEqualTo("new@new.com");
        assertThat(expectedUserDTO.getEmail()).isEqualTo("new@new.com");
    }

    @Test
    public void testUpdateUserAsAuthorizedUser() throws Exception {
        var testUserId = testUser.getId();
        var request = put("/api/users/" + testUserId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"abc@new.com\", \"password\": \"new-password\"}")
                .header("Authorization", "Bearer " + tokenUser);
        mockMvc.perform(request)
                .andExpect(status().isOk());
    }

    @Test
    public void testUpdateUserAsUnauthorizedUser() throws Exception {
        var user = generateUser();
        var request = put("/api/users/" + user.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"efg@new.com\", \"password\": \"new-password\"}")
                .header("Authorization", "Bearer " + tokenUser);
        mockMvc.perform(request)
                .andExpect(status().is(403));
    }

    @Test
    public void testDeleteUserByAdmin() throws Exception {
        var testUserId = testUser.getId();
        var request = delete("/api/users/" + testUserId)
                .header("Authorization", "Bearer " + tokenAdmin);
        mockMvc.perform(request)
                .andExpect(status().isNoContent());
        var user = userRepository.findById(testUserId).orElse(null);
        assertThat(user).isEqualTo(null);
    }

    @Test
    public void testDeleteUserByAuthorizedUser() throws Exception {
        var testUserId = testUser.getId();
        var request = delete("/api/users/" + testUserId)
                .header("Authorization", "Bearer " + tokenUser);
        mockMvc.perform(request)
                .andExpect(status().isNoContent());
        var user = userRepository.findById(testUserId).orElse(null);
        assertThat(user).isEqualTo(null);
    }

    @Test
    public void testDeleteUserByUnauthorizedUser() throws Exception {
        var user = generateUser();
        var request = delete("/api/users/" + user.getId())
                .header("Authorization", "Bearer " + tokenUser);
        mockMvc.perform(request)
                .andExpect(status().is(403));
    }

    @Test
    public void testNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + tokenUser))
                .andExpect(status().is(200));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().is(401));
    }

    @Test
    public void testUnauthorized() throws Exception {
        mockMvc.perform(get("/api/admin")
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().is(200));

        mockMvc.perform(get("/api/admin")
                        .header("Authorization", "Bearer " + tokenUser))
                .andExpect(status().is(403));
    }

    private User generateUser() {
        var user = new User();
        var password = faker.internet().domainWord();
        user.setFirstName(faker.name().firstName());
        user.setLastName(faker.name().lastName());
        user.setEmail(faker.internet().emailAddress());
        user.setPasswordHashed(passwordEncoder.encode(password));
        user.setRole(ROLE_USER);
        userRepository.save(user);
        return user;
    }
}
