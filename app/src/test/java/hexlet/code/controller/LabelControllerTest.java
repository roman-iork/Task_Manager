package hexlet.code.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.dto.LabelDTO;
import hexlet.code.exception.NoSuchResourceException;
import hexlet.code.mapper.LabelMapper;
import hexlet.code.repository.LabelRepository;
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
    public void testShow() throws Exception {
        long labelId = 1;
        var label = labelRepository.findById(labelId)
                .orElseThrow(() -> new NoSuchResourceException(format("(TstShw)No label with id %o", labelId)));
        var body = mockMvc.perform(get("/api/labels/" + labelId)
                .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        var labelDTO = objectMapper.readValue(body, new TypeReference<LabelDTO>() {
        });
        assertThat(labelDTO.getName()).isEqualTo(label.getName());

        mockMvc.perform(get("/api/labels/" + labelId))
                .andExpect(status().is(401));
    }

    @Test
    public void testShowAll() throws Exception {
        var labels = labelRepository.findAll();
        var body = mockMvc.perform(get("/api/labels")
                .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        var labelsDTO = objectMapper.readValue(body, new TypeReference<List<LabelDTO>>() {
        });
        var actualLabels = labelsDTO.stream().map(l -> labelMapper.mapFromLabelView(l)).toList();
        assertThat(actualLabels.size()).isEqualTo(labels.size());
        assertThat(actualLabels).containsExactlyInAnyOrderElementsOf(labels);

        mockMvc.perform(get("/api/labels"))
                .andExpect(status().is(401));
    }

    @Test
    @Transactional
    public void testCreate() throws Exception {
        var body = mockMvc.perform(post("/api/labels")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"name\": \"to_remove\"}")
                    .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        var labelDTO = objectMapper.readValue(body, new TypeReference<LabelDTO>() {
        });
        var labelId = (long) labelDTO.getId();

        var labelActual = labelRepository.findById(labelId)
                .orElseThrow(() -> new NoSuchResourceException(format("(TstCrt)No label with id %o", labelId)));
        assertThat(labelActual.getName()).isEqualTo("to_remove");
        assertThat(labelActual.getTasks().size()).isEqualTo(0);

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
        var body = mockMvc.perform(put("/api/labels/" + testLabelId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"to_review\"}")
                .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        var labelActualDTO = objectMapper.readValue(body, new TypeReference<LabelDTO>() {
        });
        assertThat(labelActualDTO.getName()).isEqualTo("to_review");

        var labelActual = labelMapper.mapFromLabelView(labelActualDTO);
        assertThat(labelActual.getTasks().size()).isEqualTo(0);

        mockMvc.perform(put("/api/labels/" + testLabelId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"to_discuss\"}"))
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

        var labelActual = labelRepository.findById(testLabelId).orElse(null);
        assertThat(labelActual).isEqualTo(null);
    }
}
