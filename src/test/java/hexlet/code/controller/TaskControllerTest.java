package hexlet.code.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.dto.TaskDTO;
import hexlet.code.exception.NoSuchResourceException;
import hexlet.code.mapper.TaskMapper;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskRepository;
import hexlet.code.util.Content;
import hexlet.code.util.GenerateModels;
import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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
    @Autowired
    private Content content;

    private String tokenAdmin;
    private String tokenUser;

    @BeforeEach
    public void gettingAdminToken() throws Exception {
        var password = faker.internet().domainWord();
        var testUser = generate.generateUser(password);
        tokenAdmin = generate.generateAdminToken(mockMvc);
        tokenUser = generate.generateUserToken(mockMvc, testUser.getEmail(), password);
    }

    @Test
    @Transactional
    public void testShow() throws Exception {
        long expectedTaskId = 2;
        var expectedTask = taskRepository.findById(expectedTaskId)
                .orElseThrow(() -> new NoSuchResourceException(format("(TstShow)No task with id %o", expectedTaskId)));
        var body = mockMvc.perform(get("/api/tasks/" + expectedTaskId)
                .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        var actualTaskDTO = objectMapper.readValue(body, new TypeReference<TaskDTO>() {
        });
        assertThat(actualTaskDTO.getTitle()).isEqualTo(expectedTask.getName());
        assertThat(actualTaskDTO.getTaskLabelIds().size()).isEqualTo(expectedTask.getLabels().size());
        var expectedTaskLabelsIds = expectedTask.getLabels().stream().map(l -> Math.toIntExact(l.getId())).toList();
        assertThat(actualTaskDTO.getTaskLabelIds()).containsExactlyInAnyOrderElementsOf(expectedTaskLabelsIds);

        mockMvc.perform(get("/api/tasks/" + expectedTaskId))
                .andExpect(status().is(401));
    }

    @Test
    @Transactional
    public void testShowAll() throws Exception {
        var expectedTasks = taskRepository.findAll();
        var expectedTasksDTO = expectedTasks.stream().map(tsk -> taskMapper.map(tsk)).toList();
        var body = mockMvc.perform(get("/api/tasks")
                .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        var actualTasksDTO = objectMapper.readValue(body, new TypeReference<List<TaskDTO>>() {
            });
        assertFalse(actualTasksDTO.isEmpty());
        assertThat(actualTasksDTO.size()).isEqualTo(expectedTasksDTO.size());
        assertThat(actualTasksDTO).containsExactlyInAnyOrderElementsOf(expectedTasksDTO);

        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().is(401));
    }

    @Test
    @Transactional
    public void testCreate() throws Exception {
        var testLabel1 = generate.generateLabel("confirmed");
        var testLabel2 = generate.generateLabel("suspended");
        var requestContent = content
                .add("index", 123)
                .add("assignee_id", 3)
                .add("title", "Cleanness")
                .add("content", "Dust the room.")
                .add("taskLabelIds", List.of(testLabel1.getId(), testLabel2.getId()))
                .add("status", "to_review")
                .build();
        var body = mockMvc.perform(post("/api/tasks")
                .header("Authorization", "Bearer " + tokenAdmin)
                .header("content-type", "application/json")
                .content(requestContent))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        var actualTaskDTO = objectMapper.readValue(body, new TypeReference<TaskDTO>() {
            });
        //check returned value
        assertThat(actualTaskDTO.getTitle()).isEqualTo("Cleanness");
        assertThat(actualTaskDTO.getContent()).isEqualTo("Dust the room.");
        //check data from db
        long actualTaskID = actualTaskDTO.getId();
        var actualTask = taskRepository.findById(actualTaskID).orElse(null);
        assertThat(actualTask).isNotEqualTo(null);
        assertThat(actualTask.getName()).isEqualTo("Cleanness");
        assertThat(actualTask.getTaskStatus().getSlug()).isEqualTo("to_review");
        assertThat(actualTask.getLabels()).containsExactlyInAnyOrderElementsOf(List.of(testLabel1, testLabel2));

        requestContent = content
                .add("index", 123)
                .add("status", "to_review")
                .build();
        mockMvc.perform(post("/api/tasks")
                .header("content-type", "application/json")
                .content(requestContent))
                .andExpect(status().is(401));
    }

    @Test
    @Transactional
    public void testUpdate() throws Exception {
        long testTaskId = 3;
        var testTask = taskRepository.findById(testTaskId).get();
        var testLabel1 = labelRepository.findByName("bug").get();
        var testLabel2 = labelRepository.findByName("feature").get();
        assertThat(testTask.getLabels().size()).isEqualTo(2);
        assertThat(testTask.getLabels()).containsExactlyInAnyOrderElementsOf(List.of(testLabel1, testLabel2));

        var testLabel3 = generate.generateLabel("suspend");
        var testLabel4 = generate.generateLabel("reviewed");
        assertThat(testLabel3.getTasks().size()).isEqualTo(0);
        assertThat(testLabel4.getTasks().size()).isEqualTo(0);

        var requestContent = content
                .add("assignee_id", 2)
                .add("content", "Dust bathroom.")
                .add("taskLabelIds", List.of(testLabel3.getId(), testLabel4.getId()))
                .add("title", "Do flat")
                .add("status", "to_review")
                .build();
        var body = mockMvc.perform(put("/api/tasks/" + testTaskId)
                .header("Authorization", "Bearer " + tokenAdmin)
                .header("content-type", "application/json")
                .content(requestContent))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        var actualTaskDTO = objectMapper.readValue(body, new TypeReference<TaskDTO>() {
            });
        //check returned value
        assertThat(actualTaskDTO.getContent()).isEqualTo("Dust bathroom.");
        assertThat(actualTaskDTO.getAssigneeId()).isEqualTo(2);
        //check data from db
        var actualTask = taskRepository.findById(testTaskId).orElse(null);
        assertThat(actualTask).isNotEqualTo(null);
        assertThat(actualTask.getDescription()).isEqualTo("Dust bathroom.");
        assertThat(actualTask.getAssignee().getId()).isEqualTo(2);
        assertThat(actualTask.getTaskStatus().getSlug()).isEqualTo("to_review");
        assertThat(actualTask.getName()).isEqualTo("Do flat");
        assertThat(actualTask.getLabels().size()).isEqualTo(2);
        assertThat(actualTask.getLabels()).containsExactlyInAnyOrderElementsOf(List.of(testLabel3, testLabel4));
        assertThat(testLabel3.getTasks()).containsExactlyInAnyOrderElementsOf(List.of(actualTask));
        assertThat(testLabel4.getTasks()).containsExactlyInAnyOrderElementsOf(List.of(actualTask));

        requestContent = content
                .add("assignee_id", 2)
                .add("status", "to_review")
                .build();
        mockMvc.perform(put("/api/tasks/" + testTaskId)
                .header("content-type", "application/json")
                .content(requestContent))
                .andExpect(status().is(401));
    }

    @Test
    public void testDelete() throws Exception {
        var testTask1 = generate.generateTask();
        long testTaskId = testTask1.getId();

        mockMvc.perform(delete("/api/tasks/" + testTaskId))
                .andExpect(status().is(401));

        mockMvc.perform(delete("/api/tasks/" + testTaskId)
                .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isNoContent());
        var actual1 = taskRepository.findById(testTaskId).orElse(null);
        assertThat(actual1).isEqualTo(null);

        var testTask2 = generate.generateTask();
        long testTask2Id = testTask2.getId();
        mockMvc.perform(delete("/api/tasks/" + testTask2Id)
                .header("Authorization", "Bearer " + tokenUser))
                .andExpect(status().isNoContent());
        var actual2 = taskRepository.findById(testTask2Id).orElse(null);
        assertThat(actual2).isEqualTo(null);
    }

    @Test
    @Transactional
    public void testFiltering() throws Exception {

        var paramTitle = "titleCont=ea";
        var paramAssignee = "assigneeId=2";
        var paramStatus = "status=to_be_fixed";
        var paramLabel = "labelId=1";

        var bodyWithTitleParam = mockMvc.perform(get("/api/tasks?" + paramTitle)
                .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().is(200)).andReturn().getResponse().getContentAsString();
        var actualTasksWithTitleParam = objectMapper.readValue(bodyWithTitleParam, new TypeReference<List<TaskDTO>>() {
        });
        var expectedTasksWithTitleParam = taskRepository.findAll().stream()
                        .filter(t -> t.getName().contains("ea"))
                        .map(model -> taskMapper.map(model))
                        .toList();
        assertThat(actualTasksWithTitleParam).containsExactlyInAnyOrderElementsOf(expectedTasksWithTitleParam);

        var bodyWithAssigneeParam = mockMvc.perform(get("/api/tasks?" + paramAssignee)
                .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().is(200)).andReturn().getResponse().getContentAsString();
        var actualTasksWithAssigneeParam = objectMapper.readValue(bodyWithAssigneeParam,
                                                                new TypeReference<List<TaskDTO>>() {
            });
        var expectedTasksWithAssigneeParam = taskRepository.findAll().stream()
                        .filter(t -> t.getAssignee().getId() == 2L)
                        .map(model -> taskMapper.map(model))
                        .toList();
        assertThat(actualTasksWithAssigneeParam).containsExactlyInAnyOrderElementsOf(expectedTasksWithAssigneeParam);

        long testLabelId = 1;
        var testLabel = labelRepository.findById(testLabelId)
                .orElseThrow(() -> new NoSuchResourceException("(TstTskFltr)No label with id 1"));
        var bodyWithStatusAndLabelParams = mockMvc.perform(get("/api/tasks?" + paramStatus + "&" + paramLabel)
                .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().is(200)).andReturn().getResponse().getContentAsString();
        var actualTasksWithStatusAndLabelParams = objectMapper.readValue(bodyWithStatusAndLabelParams,
                                                                new TypeReference<List<TaskDTO>>() {
            });
        var expectedTasksWithStatusAndLabelParams = taskRepository.findAll().stream()
                        .filter(t -> t.getTaskStatus().getSlug().equals("to_be_fixed"))
                        .filter(t -> t.getLabels().contains(testLabel))
                        .map(model -> taskMapper.map(model))
                        .toList();
        assertThat(actualTasksWithStatusAndLabelParams)
                .containsExactlyInAnyOrderElementsOf(expectedTasksWithStatusAndLabelParams);
    }
}
