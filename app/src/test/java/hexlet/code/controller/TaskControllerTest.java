package hexlet.code.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.dto.TaskDTO;
import hexlet.code.exception.NoSuchResourceException;
import hexlet.code.mapper.TaskMapper;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskRepository;
import hexlet.code.util.GenerateModels;
import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
    private LabelRepository labelRepository;
    @Autowired
    private TaskMapper taskMapper;
    @Autowired
    private Faker faker;
    @Autowired
    private GenerateModels generate;

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

        var password = faker.internet().domainWord();
        var testUser = generate.generateUser(password);

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
    @Transactional
    public void testShow() throws Exception {
        long taskId = 2;
        var expectedTask = taskRepository.findById(taskId)
                .orElseThrow(() -> new NoSuchResourceException(format("(TstShow)No task with id %o", taskId)));
        var body = mockMvc.perform(get("/api/tasks/" + taskId)
                .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        var taskDTO = objectMapper.readValue(body, new TypeReference<TaskDTO>() {
        });
        assertThat(taskDTO.getTitle()).isEqualTo(expectedTask.getName());
        assertThat(taskDTO.getTaskLabelIds().size()).isEqualTo(expectedTask.getLabels().size());
        assertThat(taskDTO.getTaskLabelIds()).contains(Math.toIntExact(expectedTask.getLabels().getFirst().getId()));

        mockMvc.perform(get("/api/tasks/" + taskId))
                .andExpect(status().is(401));
    }

    @Test
    @Transactional
    public void testShowAll() throws Exception {
        var expectedTasks = taskRepository.findAll();
        var testTasksDTO = expectedTasks.stream().map(tsk -> taskMapper.map(tsk)).toList();
        var body = mockMvc.perform(get("/api/tasks")
                .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        var tasksDTO = objectMapper.readValue(body, new TypeReference<List<TaskDTO>>() {
        });
        assertFalse(tasksDTO.isEmpty());
        assertThat(tasksDTO.size()).isEqualTo(testTasksDTO.size());
        assertThat(tasksDTO).contains(testTasksDTO.getFirst());

        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().is(401));
    }

    @Test
    @Transactional
    public void testCreate() throws Exception {
        var label = generate.generateLabel("confirmed");
        var label1 = generate.generateLabel("suspended");
        var body = mockMvc.perform(post("/api/tasks")
                .header("Authorization", "Bearer " + tokenAdmin)
                .header("content-type", "application/json")
                .content("{\"index\": 123,"
                        + "\"assignee_id\": 3,"
                        + "\"title\": \"Cleanness\","
                        + "\"content\": \"Dust the room.\","
                        + "\"taskLabelIds\": [" + label.getId() + ", " + label1.getId() + "],"
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

        var actualTask = taskOpt.get();
        assertThat(actualTask.getLabels()).containsExactlyInAnyOrderElementsOf(List.of(label, label1));

        mockMvc.perform(post("/api/tasks")
                        .header("content-type", "application/json")
                        .content("{\"index\": 123,"
                                + "\"status\": \"to_review\"}"))
                .andExpect(status().is(401));
    }

    @Test
    @Transactional
    public void testUpdate() throws Exception {
        long taskId = 3;
        var taskBeforeUpdate = taskRepository.findById(taskId).get();
        var label = labelRepository.findByName("bug").get();
        var label1 = labelRepository.findByName("feature").get();
        assertThat(taskBeforeUpdate.getLabels().size()).isEqualTo(2);
        assertThat(taskBeforeUpdate.getLabels()).containsExactlyInAnyOrderElementsOf(List.of(label, label1));

        var label2 = generate.generateLabel("suspend");
        var label3 = generate.generateLabel("reviewed");
        assertThat(label2.getTasks().size()).isEqualTo(0);
        assertThat(label3.getTasks().size()).isEqualTo(0);

        mockMvc.perform(put("/api/tasks/" + taskId)
                    .header("Authorization", "Bearer " + tokenAdmin)
                    .header("content-type", "application/json")
                    .content("{\"assignee_id\": 2,"
                            + "\"content\": \"Dust bathroom\","
                            + "\"taskLabelIds\": [" + label2.getId() + ", " + label3.getId() + "],"
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

        assertThat(taskOpt.get().getLabels().size()).isEqualTo(2);
        assertThat(taskOpt.get().getLabels()).containsExactlyInAnyOrderElementsOf(List.of(label2, label3));
        assertThat(label2.getTasks()).containsExactlyInAnyOrderElementsOf(List.of(taskOpt.get()));
        assertThat(label3.getTasks()).containsExactlyInAnyOrderElementsOf(List.of(taskOpt.get()));

        mockMvc.perform(put("/api/tasks/" + taskId)
                    .header("content-type", "application/json")
                    .content("{\"assignee\": 2,"
                            + "\"status\": \"to_review\"}"))
                .andExpect(status().is(401));
    }

    @Test
    public void testDelete() throws Exception {
        var task = generate.generateTask();
        long taskId = task.getId();

        mockMvc.perform(delete("/api/tasks/" + taskId))
                .andExpect(status().is(401));

        mockMvc.perform(delete("/api/tasks/" + taskId)
                    .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isNoContent());

        var nullTask = taskRepository.findById(taskId).orElse(null);
        assertThat(nullTask).isEqualTo(null);

        var task1 = generate.generateTask();
        long task1Id = task1.getId();
        mockMvc.perform(delete("/api/tasks/" + task1Id)
                    .header("Authorization", "Bearer " + tokenUser))
                .andExpect(status().isNoContent());
    }

    @Test
    @Transactional
    public void testFiltering() throws Exception {

        var paramTitle = "titleCont=ea";
        var paramAssignee = "assigneeId=2";
        var paramStatus = "status=to_be_fixed";
        var paramLabel = "labelId=1";

        var bodyTitle = mockMvc.perform(get("/api/tasks?" + paramTitle)
                    .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().is(200)).andReturn().getResponse().getContentAsString();
        var actualTasksTitle = objectMapper.readValue(bodyTitle, new TypeReference<List<TaskDTO>>() {
        });
        var tasksExpectedTitle = taskRepository.findAll().stream()
                        .filter(t -> t.getName().contains("ea"))
                        .map(model -> taskMapper.map(model))
                        .toList();
        assertThat(actualTasksTitle).containsExactlyInAnyOrderElementsOf(tasksExpectedTitle);

        var bodyAssignee = mockMvc.perform(get("/api/tasks?" + paramAssignee)
                    .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().is(200)).andReturn().getResponse().getContentAsString();
        var actualTasksAssignee = objectMapper.readValue(bodyAssignee, new TypeReference<List<TaskDTO>>() {
        });
        var tasksExpectedAssignee = taskRepository.findAll().stream()
                        .filter(t -> t.getAssignee().getId() == 2L)
                        .map(model -> taskMapper.map(model))
                        .toList();
        assertThat(actualTasksAssignee).containsExactlyInAnyOrderElementsOf(tasksExpectedAssignee);

        var label = labelRepository.findById(1L)
                .orElseThrow(() -> new NoSuchResourceException("(TstTskFltr)No label with id 1"));
        var bodyStatusAndLabel = mockMvc.perform(get("/api/tasks?" + paramStatus + "&" + paramLabel)
                    .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().is(200)).andReturn().getResponse().getContentAsString();
        var actualTasksStatusAndLabel = objectMapper.readValue(bodyStatusAndLabel, new TypeReference<List<TaskDTO>>() {
        });
        var tasksExpectedStatusAndLabel = taskRepository.findAll().stream()
                        .filter(t -> t.getTaskStatus().getSlug().equals("to_be_fixed"))
                        .filter(t -> t.getLabels().contains(label))
                        .map(model -> taskMapper.map(model))
                        .toList();
        assertThat(actualTasksStatusAndLabel).containsExactlyInAnyOrderElementsOf(tasksExpectedStatusAndLabel);
    }
}
