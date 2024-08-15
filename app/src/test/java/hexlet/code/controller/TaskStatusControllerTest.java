package hexlet.code.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.model.TaskStatus;
import hexlet.code.repository.TaskStatusRepository;
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
class TaskStatusControllerTest {
    @Autowired
    private TaskStatusRepository taskStatusRepository;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    private String tokenAdmin;

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
    }

    @Test
    public void testShowStatus() throws Exception {
        long testId = 2;
        var testStatus = taskStatusRepository.findById(testId).get();

        var body = mockMvc.perform(get("/api/task_statuses/" + testId)
                        .header("Authorization", "Bearer " + tokenAdmin))
                        .andExpect(status().isOk())
                                .andReturn().getResponse().getContentAsString();
        assertThat(body).contains(testStatus.getSlug());
    }

    @Test
    public void testShowAllStatuses() throws Exception {
        var testStatuses = taskStatusRepository.findAll();

        var body = mockMvc.perform(get("/api/task_statuses")
                        .header("Authorization", "Bearer " + tokenAdmin))
                        .andExpect(status().isOk())
                                .andReturn().getResponse().getContentAsString();
        var statuses = objectMapper.readValue(body, new TypeReference<List<TaskStatus>>() {
        });
        assertThat(statuses.size()).isEqualTo(testStatuses.size());
        assertThat(statuses).contains(testStatuses.get(1));
    }

    @Test
    public void testCreate() throws Exception {
        var body = mockMvc.perform(post("/api/task_statuses")
                                    .header("Authorization", "Bearer " + tokenAdmin)
                                    .header("content-type", "application/json")
                                    .content("{\"name\": \"New\","
                                        + "\"slug\": \"new\"}"))
                                .andExpect(status().isCreated())
                                .andReturn().getResponse().getContentAsString();
        var status = objectMapper.readValue(body, new TypeReference<TaskStatus>() {
        });
        var statusId = status.getId();

        assertThat(taskStatusRepository.findById(statusId).get().getName()).isEqualTo("New");
    }

    @Test
    public void testUpdate() throws Exception {
        long testId = 2;
        var testStatus = taskStatusRepository.findById(testId).get();

        var body = mockMvc.perform(put("/api/task_statuses/" + testId)
                                    .header("Authorization", "Bearer " + tokenAdmin)
                                    .header("content-type", "application/json")
                                    .content("{\"name\": \"Updated\"}"))
                                .andExpect(status().isOk())
                                .andReturn().getResponse().getContentAsString();

        var updatedStatus = objectMapper.readValue(body, new TypeReference<TaskStatus>() {
        });

        assertThat(updatedStatus.getName()).isEqualTo("Updated");
        assertThat(updatedStatus.getSlug()).isEqualTo(testStatus.getSlug());
    }

    @Test
    public void testDelete() throws Exception {
        var status = generateStatus();
        long testStatusId = status.getId();
        mockMvc.perform(delete("/api/task_statuses/" + testStatusId)
                                    .header("Authorization", "Bearer " + tokenAdmin))
                                .andExpect(status().is(204));
        assertThat(taskStatusRepository.findById(testStatusId).orElse(null)).isEqualTo(null);
    }

    private TaskStatus generateStatus() {
        var status = new TaskStatus();
        status.setName("Archived");
        status.setSlug("archived");
        taskStatusRepository.save(status);
        return status;
    }
}
