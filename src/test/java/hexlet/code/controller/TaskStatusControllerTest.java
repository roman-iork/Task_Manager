package hexlet.code.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.dto.TaskStatusDTO;
import hexlet.code.model.TaskStatus;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.util.Content;
import hexlet.code.util.GenerateModels;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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
class TaskStatusControllerTest {
    @Autowired
    private TaskStatusRepository taskStatusRepository;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private GenerateModels generate;
    @Autowired
    private Content content;

    private String tokenAdmin;

    @BeforeEach
    public void gettingAdminToken() throws Exception {
        tokenAdmin = generate.generateAdminToken(mockMvc);
    }

    @Test
    public void testShowStatus() throws Exception {
        long expectedStatusId = 2;
        var expectedStatus = taskStatusRepository.findById(expectedStatusId).get();

        var body = mockMvc.perform(get("/api/task_statuses/" + expectedStatusId)
                .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        var actualStatus = objectMapper.readValue(body, new TypeReference<TaskStatus>() {
        });
        assertThat(actualStatus).isEqualTo(expectedStatus);

        mockMvc.perform(get("/api/task_statuses/" + expectedStatusId))
                .andExpect(status().is(401));
    }

    @Test
    public void testShowAllStatuses() throws Exception {
        var expectedStatuses = taskStatusRepository.findAll();

        var body = mockMvc.perform(get("/api/task_statuses")
                .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        var actualStatuses = objectMapper.readValue(body, new TypeReference<List<TaskStatus>>() {
        });
        assertThat(actualStatuses.size()).isEqualTo(expectedStatuses.size());
        assertThat(actualStatuses).containsExactlyInAnyOrderElementsOf(expectedStatuses);

        mockMvc.perform(get("/api/task_statuses"))
                .andExpect(status().is(401));
    }

    @Test
    public void testCreate() throws Exception {
        var requestContent = content
                .add("name", "New")
                .add("slug", "new")
                .build();
        var body = mockMvc.perform(post("/api/task_statuses")
                .header("Authorization", "Bearer " + tokenAdmin)
                .header("content-type", "application/json")
                .content(requestContent))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        var actualStatusDTO = objectMapper.readValue(body, new TypeReference<TaskStatusDTO>() {
            });
        //check returned value
        assertThat(actualStatusDTO.getName()).isEqualTo("New");
        //check data from db
        var actualStatus = taskStatusRepository.findBySlug("new").orElse(null);
        assertThat(actualStatus).isNotEqualTo(null);
        assertThat(actualStatus.getName()).isEqualTo("New");

        requestContent = content
                .add("name", "NoWay")
                .add("slug", "no_way")
                .build();
        mockMvc.perform(post("/api/task_statuses")
                .header("content-type", "application/json")
                .content(requestContent))
                .andExpect(status().is(401));
    }

    @Test
    public void testUpdate() throws Exception {
        long testStatusId = 2;
        var testStatus = taskStatusRepository.findById(testStatusId).get();

        var requestContent = content.add("name", "Updated").build();
        var body = mockMvc.perform(put("/api/task_statuses/" + testStatusId)
                .header("Authorization", "Bearer " + tokenAdmin)
                .header("content-type", "application/json")
                .content(requestContent))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        var actualStatusDTO = objectMapper.readValue(body, new TypeReference<TaskStatusDTO>() {
            });
        //check returned value
        assertThat(actualStatusDTO.getName()).isEqualTo("Updated");
        //check data from db
        var actualStatus = taskStatusRepository.findById(testStatusId).get();
        assertThat(actualStatus.getName()).isEqualTo("Updated");
        assertThat(actualStatus.getSlug()).isEqualTo(testStatus.getSlug());

        requestContent = content.add("name", "NotUpdated").build();
        mockMvc.perform(put("/api/task_statuses/" + testStatusId)
                .header("content-type", "application/json")
                .content(requestContent))
                .andExpect(status().is(401));
    }

    @Test
    public void testDelete() throws Exception {
        var testStatus = generate.generateStatus();
        long testStatusId = testStatus.getId();
        mockMvc.perform(delete("/api/task_statuses/" + testStatusId))
                .andExpect(status().is(401));

        mockMvc.perform(delete("/api/task_statuses/" + testStatusId)
                .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().is(204));
        assertThat(taskStatusRepository.findById(testStatusId).orElse(null)).isEqualTo(null);
    }
}
