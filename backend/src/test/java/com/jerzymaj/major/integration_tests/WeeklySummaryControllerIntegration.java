package com.jerzymaj.major.integration_tests;

import com.jerzymaj.major.configuration.WithMockCustomUser;
import com.jerzymaj.major.models.WeeklySummary;
import com.jerzymaj.major.repos.WeeklySummaryRepository;
import com.jerzymaj.major.services.GptService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class WeeklySummaryControllerIntegration {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WeeklySummaryRepository weeklySummaryRepository;

    @MockitoBean
    private GptService gptService;

    @BeforeEach
    public void setup() {
        WeeklySummary weeklySummary1 = WeeklySummary.builder()
                .content("Completed 3 tasks, created 5 new tasks this week.")
                .periodStart(LocalDate.now().minusWeeks(3))
                .periodEnd(LocalDate.now().minusWeeks(2))
                .tasksCreated(5L)
                .tasksCompleted(3L)
                .build();

        WeeklySummary weeklySummary2 = WeeklySummary.builder()
                .content("Completed 7 tasks, created 2 new tasks this week.")
                .periodStart(LocalDate.now().minusDays(5))
                .periodEnd(LocalDate.now())
                .tasksCreated(2L)
                .tasksCompleted(7L)
                .build();

        weeklySummaryRepository.save(weeklySummary1);
        weeklySummaryRepository.save(weeklySummary2);
    }

    @Test
    @WithMockCustomUser
    public void retrieveWeeklySummary() throws Exception {

        mockMvc.perform(get("/major/api/weekly-summary/last-week"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Completed 7 tasks, created 2 new tasks this week."));
    }

    @Test
    @WithMockCustomUser
    public void retrieveAllWeeklySummary() throws Exception {

        mockMvc.perform(get("/major/api/weekly-summary/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].content").value("Completed 3 tasks, created 5 new tasks this week."))
                .andExpect(jsonPath("$[1].content").value("Completed 7 tasks, created 2 new tasks this week."));
    }
}
