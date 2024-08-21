package hexlet.code.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.dto.UserDTO;
import hexlet.code.mapper.UserMapper;
import hexlet.code.model.User;
import hexlet.code.repository.UserRepository;
import hexlet.code.util.Content;
import hexlet.code.util.GenerateModels;
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
    @Autowired
    private GenerateModels generate;
    @Autowired
    private Content content;

    private User testUser;
    private String tokenAdmin;
    private String testUserToken;

    @BeforeEach
    public void setUp() throws Exception {
        var password = faker.internet().domainWord();
        testUser = generate.generateUser(password);
        tokenAdmin = generate.generateAdminToken(mockMvc);
        testUserToken = generate.generateUserToken(mockMvc, testUser.getEmail(), password);
    }

    @Test
    public void testShowUser() throws Exception {
        var expectedUserId = testUser.getId();
        var body = mockMvc.perform(get("/api/users/" + expectedUserId)
                .header("Authorization", "Bearer " + testUserToken))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        var actualUserDTO = objectMapper.readValue(body, new TypeReference<UserDTO>() {
            });
        var expectedUserDTO = userMapper.map(testUser);
        assertThat(actualUserDTO).isEqualTo(expectedUserDTO);

        mockMvc.perform(get("/api/users/" + expectedUserId))
                .andExpect(status().is(401));
    }

    @Test
    public void testShowAllUsers() throws Exception {
        var expectedUserDTOs = userRepository.findAll().stream().map(u -> userMapper.map(u)).toList();
        var body = mockMvc.perform(get("/api/users")
                .header("Authorization", "Bearer " + testUserToken))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        var actualUserDTOs = objectMapper.readValue(body, new TypeReference<List<UserDTO>>() {
            });
        assertThat(actualUserDTOs.size()).isEqualTo(expectedUserDTOs.size());
        assertThat(actualUserDTOs).containsExactlyInAnyOrderElementsOf(expectedUserDTOs);

        mockMvc.perform(get("/api/users"))
                .andExpect(status().is(401));
    }

    @Test
    public void testCreateUser() throws Exception {
        var requestContent = content
                .add("email", "jack@google.com")
                .add("firstName", "Jack")
                .add("lastName", "Jons")
                .add("password", "some-password")
                .build();
        var request = post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestContent)
                .header("Authorization", "Bearer " + testUserToken);
        var body = mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        var actualUserDTO = objectMapper.readValue(body, new TypeReference<UserDTO>() {
            });
        //check returned value
        assertThat(actualUserDTO.getFirstName()).isEqualTo("Jack");
        assertThat(actualUserDTO.getEmail()).isEqualTo("jack@google.com");
        //check data from db
        var actualUser = userRepository.findByEmail("jack@google.com").orElse(null);
        assertThat(actualUser).isNotEqualTo(null);
        assertThat(actualUser.getFirstName()).isEqualTo("Jack");
        assertThat(actualUser.getEmail()).isEqualTo("jack@google.com");

        requestContent = content
                .add("email", "jack@google.com")
                .add("password", "some-password")
                .build();
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestContent))
                .andExpect(status().is(401));
    }

    @Test
    public void testUpdateUserAsAdmin() throws Exception {
        var testUserId = testUser.getId();
        var requestContent = content
                .add("email", "new@new.com")
                .add("password", "new-password")
                .build();
        var request = put("/api/users/" + testUserId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestContent)
                .header("Authorization", "Bearer " + tokenAdmin);
        var body = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        var actualUserDTO = objectMapper.readValue(body, new TypeReference<UserDTO>() {
            });
        //check returned value
        assertThat(actualUserDTO.getEmail()).isEqualTo("new@new.com");
        //check data from db
        var actualUser = userRepository.findByEmail("new@new.com").orElse(null);
        assertThat(actualUser).isNotEqualTo(null);
        assertThat(actualUser.getEmail()).isEqualTo("new@new.com");

        requestContent = content.add("email", "no_way@new.com").build();
        mockMvc.perform(put("/api/users/" + testUserId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestContent))
                .andExpect(status().is(401));
    }

    @Test
    public void testUpdateUserAsAuthorizedUser() throws Exception {
        var testUserId = testUser.getId();
        var requestContent = content
                .add("email", "abc@new.com")
                .add("password", "new-password")
                .build();
        var request = put("/api/users/" + testUserId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestContent)
                .header("Authorization", "Bearer " + testUserToken);
        mockMvc.perform(request)
                .andExpect(status().isOk());
    }

    @Test
    public void testUpdateUserAsUnauthorizedUser() throws Exception {
        var newUser = generate.generateUser();
        var requestContent = content
                .add("email", "efg@new.com")
                .add("password", "new-password")
                .build();
        var request = put("/api/users/" + newUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestContent)
                .header("Authorization", "Bearer " + testUserToken);
        mockMvc.perform(request)
                .andExpect(status().is(403));
    }

    @Test
    public void testDeleteUserByAdmin() throws Exception {
        var testUserId = testUser.getId();
        mockMvc.perform(delete("/api/users/" + testUserId))
                .andExpect(status().is(401));

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
                .header("Authorization", "Bearer " + testUserToken);
        mockMvc.perform(request)
                .andExpect(status().isNoContent());
        var user = userRepository.findById(testUserId).orElse(null);
        assertThat(user).isEqualTo(null);
    }

    @Test
    public void testDeleteUserByUnauthorizedUser() throws Exception {
        var newUser = generate.generateUser();
        var request = delete("/api/users/" + newUser.getId())
                .header("Authorization", "Bearer " + testUserToken);
        mockMvc.perform(request)
                .andExpect(status().is(403));
    }

    @Test
    public void testUnauthorized() throws Exception {
        mockMvc.perform(get("/api/login")
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().is(200));

        mockMvc.perform(get("/api/login")
                        .header("Authorization", "Bearer " + testUserToken))
                .andExpect(status().is(403));
    }
}
