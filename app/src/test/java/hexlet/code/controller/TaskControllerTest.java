package hexlet.code.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.dto.TaskDTO;
import hexlet.code.exception.NoSuchResourceException;
import hexlet.code.mapper.TaskMapper;
import hexlet.code.model.Task;
import hexlet.code.model.User;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
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

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TaskControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private TaskStatusRepository statusRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TaskMapper taskMapper;
    @Autowired
    private Faker faker;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private String tokenAdmin;
    private String tokenUser;

    @BeforeEach
    public void gettingAdminToken() throws Exception {
        tokenAdmin = this.mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\": \"hexlet@example.com\","
                                + "\"password\": \"qwerty\"}"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        var testUser = new User();
        var password = faker.internet().domainWord();
        testUser = new User();
        testUser.setFirstName(faker.name().firstName());
        testUser.setLastName(faker.name().lastName());
        testUser.setEmail(faker.internet().emailAddress());
        testUser.setPasswordHashed(passwordEncoder.encode(password));
        testUser.setRole("ROLE_USER");
        userRepository.save(testUser);

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
    public void testShow() throws Exception {
        long taskId = 2;
        var testTask = taskRepository.findById(taskId)
                .orElseThrow(() -> new NoSuchResourceException(format("(TstShow)No task with id %o", taskId)));
        var body = mockMvc.perform(get("/api/tasks/" + taskId)
                .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        var taskDTO = objectMapper.readValue(body, new TypeReference<TaskDTO>() {
        });
        assertThat(taskDTO.getTitle()).isEqualTo(testTask.getName());

        mockMvc.perform(get("/api/tasks/" + taskId))
                .andExpect(status().is(401));
    }

    @Test
    public void testShowAll() throws Exception {
        var testTasks = taskRepository.findAll();
        var testTasksDTO = testTasks.stream().map(tsk -> taskMapper.map(tsk)).toList();
        var body = mockMvc.perform(get("/api/tasks")
                .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        var tasksDTO = objectMapper.readValue(body, new TypeReference<List<TaskDTO>>() {
        });
        assertThat(tasksDTO.size()).isEqualTo(testTasksDTO.size());
        assertThat(tasksDTO).contains(testTasksDTO.getFirst());

        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().is(401));
    }

    @Test
    public void testCreate() throws Exception {
        var body = mockMvc.perform(post("/api/tasks")
                .header("Authorization", "Bearer " + tokenAdmin)
                .header("content-type", "application/json")
                .content("{\"index\": 123,"
                        + "\"assigneeId\": 3,"
                        + "\"title\": \"Cleanness\","
                        + "\"content\": \"Dust the room.\","
                        + "\"status\": \"to_review\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        var taskDTO = objectMapper.readValue(body, new TypeReference<TaskDTO>() {
        });

        long createdTaskId = taskDTO.getId();
        var taskOpt = taskRepository.findById(createdTaskId);
        assertThat(taskOpt).isNotEqualTo(null);
        assertThat(taskOpt.get().getName()).isEqualTo("Cleanness");
        assertThat(taskOpt.get().getTaskStatus().getSlug()).isEqualTo("to_review");

        mockMvc.perform(post("/api/tasks")
                        .header("content-type", "application/json")
                        .content("{\"index\": 123,"
                                + "\"assigneeId\": 3,"
                                + "\"title\": \"Cleanness\","
                                + "\"content\": \"Dust the room.\","
                                + "\"status\": \"to_review\"}"))
                .andExpect(status().is(401));
    }

    @Test
    public void testUpdate() throws Exception {
        long taskId = 3;
        mockMvc.perform(put("/api/tasks/" + taskId)
                .header("Authorization", "Bearer " + tokenAdmin)
                .header("content-type", "application/json")
                .content("{\"assignee\": 2,"
                        + "\"content\": \"Dust bathroom\","
                        + "\"title\": \"Do flat\","
                        + "\"status\": \"to_review\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        var taskOpt = taskRepository.findById(taskId);
        assertThat(taskOpt).isNotEqualTo(null);
        assertThat(taskOpt.get().getDescription()).isEqualTo("Dust bathroom");
        assertThat(taskOpt.get().getAssignee().getId()).isEqualTo(2);
        assertThat(taskOpt.get().getTaskStatus().getSlug()).isEqualTo("to_review");
        assertThat(taskOpt.get().getName()).isEqualTo("Do flat");

        mockMvc.perform(put("/api/tasks/" + taskId)
                        .header("content-type", "application/json")
                        .content("{\"assignee\": 2,"
                                + "\"content\": \"Dust bathroom\","
                                + "\"title\": \"Do flat\","
                                + "\"status\": \"to_review\"}"))
                .andExpect(status().is(401));
    }

    @Test
    public void testDelete() throws Exception {
        var task = generateTask();
        long taskId = task.getId();

        mockMvc.perform(delete("/api/tasks/" + taskId))
                .andExpect(status().is(401));

        mockMvc.perform(delete("/api/tasks/" + taskId)
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isNoContent());

        var nullTask = taskRepository.findById(taskId).orElse(null);
        assertThat(nullTask).isEqualTo(null);

        var task1 = generateTask();
        long task1Id = task.getId();
        mockMvc.perform(delete("/api/tasks/" + task1Id)
                        .header("Authorization", "Bearer " + tokenUser))
                .andExpect(status().isNoContent());
    }

    private Task generateTask() {
        long statusId = 2;
        long userId = 3;
        var task = new Task();
        task.setName("Walk");
        task.setIndex(135);
        task.setDescription("Make a stroll");
        task.setTaskStatus(statusRepository.findById(statusId)
                .orElseThrow(() -> new NoSuchResourceException("(TestTask)No such status")));
        task.setAssignee(userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchResourceException("(TestTask)No such user")));
        taskRepository.save(task);
        return task;
    }
}
