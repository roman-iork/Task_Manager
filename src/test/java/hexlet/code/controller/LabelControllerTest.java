package hexlet.code.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.dto.LabelDTO;
import hexlet.code.exception.NoSuchResourceException;
import hexlet.code.mapper.LabelMapper;
import hexlet.code.repository.LabelRepository;
import hexlet.code.util.Content;
import hexlet.code.util.GenerateModels;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class LabelControllerTest {
    @Autowired
    private GenerateModels generate;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private LabelRepository labelRepository;
    @Autowired
    private LabelMapper labelMapper;
    @Autowired
    private Content content;

    private String tokenAdmin;

    @BeforeEach
    public void gettingAdminToken() throws Exception {
        tokenAdmin = generate.generateAdminToken(mockMvc);
    }

    @Test
    public void testShow() throws Exception {
        long labelId = 1;
        var expectedLabel = labelRepository.findById(labelId)
                .orElseThrow(() -> new NoSuchResourceException(format("(TstShw)No label with id %o", labelId)));
        var body = mockMvc.perform(get("/api/labels/" + labelId)
                .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        var actualLabelDTO = objectMapper.readValue(body, new TypeReference<LabelDTO>() {
        });
        assertThat(actualLabelDTO.getName()).isEqualTo(expectedLabel.getName());

        mockMvc.perform(get("/api/labels/" + labelId))
                .andExpect(status().is(401));
    }

    @Test
    public void testShowAll() throws Exception {
        var expectedLabels = labelRepository.findAll();
        var body = mockMvc.perform(get("/api/labels")
                .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        var actualLabelsDTO = objectMapper.readValue(body, new TypeReference<List<LabelDTO>>() {
        });
        var actualLabels = actualLabelsDTO.stream().map(l -> labelMapper.mapFromLabelView(l)).toList();
        assertThat(actualLabels.size()).isEqualTo(expectedLabels.size());
        assertThat(actualLabels).containsExactlyInAnyOrderElementsOf(expectedLabels);

        mockMvc.perform(get("/api/labels"))
                .andExpect(status().is(401));
    }

    @Test
    @Transactional
    public void testCreate() throws Exception {
        var requestContent = content.add("name", "to_remove").build();
        var body = mockMvc.perform(post("/api/labels")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestContent)
                .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        var actualLabelDTO = objectMapper.readValue(body, new TypeReference<LabelDTO>() {
            });
        //check returned value
        assertThat(actualLabelDTO.getName()).isEqualTo("to_remove");
        //check data from db
        var actualLabel = labelRepository.findByName("to_remove").orElse(null);
        assertThat(actualLabel.getName()).isEqualTo("to_remove");
        assertThat(actualLabel.getTasks().size()).isEqualTo(0);

        mockMvc.perform(post("/api/labels")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"to_improve\"}"))
                .andExpect(status().is(401));
    }

    @Test
    @Transactional
    public void testUpdate() throws Exception {
        var testLabel = generate.generateLabel("to_cancel");
        var testLabelId = testLabel.getId();
        var requestContent = content.add("name", "to_review").build();
        var body = mockMvc.perform(put("/api/labels/" + testLabelId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestContent)
                .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        var actualLabelabelDTO = objectMapper.readValue(body, new TypeReference<LabelDTO>() {
            });
        //check returned value
        assertThat(actualLabelabelDTO.getName()).isEqualTo("to_review");
        //check data from db
        var actualLabel = labelRepository.findByName("to_review").orElse(null);
        assertThat(actualLabel).isNotEqualTo(null);
        assertThat(actualLabel.getId()).isEqualTo(testLabelId);
        assertThat(actualLabel.getTasks().size()).isEqualTo(0);

        requestContent = content.add("name", "to_discuss").build();
        mockMvc.perform(put("/api/labels/" + testLabelId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestContent))
                .andExpect(status().is(401));
    }

    @Test
    public void testDelete() throws Exception {
        var testLabel = generate.generateLabel("to_cancel");
        var testLabelId = testLabel.getId();

        mockMvc.perform(delete("/api/labels/" + testLabelId))
                .andExpect(status().is(401));

        mockMvc.perform(delete("/api/labels/" + testLabelId)
                .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isNoContent());

        var actual = labelRepository.findById(testLabelId).orElse(null);
        assertThat(actual).isEqualTo(null);
    }
}
