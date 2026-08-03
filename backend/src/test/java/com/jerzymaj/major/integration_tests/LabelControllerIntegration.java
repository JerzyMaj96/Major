package com.jerzymaj.major.integration_tests;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jerzymaj.major.Dtos.CreateLabelDto;
import com.jerzymaj.major.Dtos.UpdateLabelDto;
import com.jerzymaj.major.configuration.WithMockCustomUser;
import com.jerzymaj.major.models.Label;
import com.jerzymaj.major.repos.LabelRepository;
import com.jerzymaj.major.services.GptService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class LabelControllerIntegration {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LabelRepository labelRepository;

    @MockitoBean
    private GptService gptService;

    private ObjectMapper objectMapper = new ObjectMapper();

    private Label testLabel;

    @BeforeEach
    public void setup() {
        testLabel = Label.builder()
                .name("Test Label")
                .color("#FF5733")
                .build();

        labelRepository.save(testLabel);
    }

    @Test
    @WithMockCustomUser
    public void createLabel() throws Exception {
        CreateLabelDto createLabelDto = new CreateLabelDto("Backend", "Red");

        mockMvc.perform(post("/major/api/labels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createLabelDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(createLabelDto.name()))
                .andExpect(jsonPath("$.color").value(createLabelDto.color()));
    }

    @Test
    @WithMockCustomUser
    public void retrieveAllLabels() throws Exception {

        mockMvc.perform(get("/major/api/labels"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Test Label"));
    }

    @Test
    @WithMockCustomUser
    public void retrieveLabelById() throws Exception {

        mockMvc.perform(get("/major/api/labels/{id}", testLabel.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Label"));
    }

    @Test
    @WithMockCustomUser
    public void deleteLabelById() throws Exception {

        mockMvc.perform(delete("/major/api/labels/{id}", testLabel.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/major/api/labels/{id}", testLabel.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockCustomUser
    public void updateLabel() throws Exception {
        UpdateLabelDto updateLabelDto = new UpdateLabelDto("Backend", "Red");

        mockMvc.perform(patch("/major/api/labels/{id}", testLabel.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateLabelDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Backend"))
                .andExpect(jsonPath("$.color").value("Red"));
    }
}
