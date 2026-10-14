package com.jerzymaj.major.integration_tests;

import com.jerzymaj.major.configuration.BaseIntegrationTest;
import com.jerzymaj.major.configuration.WithMockCustomUser;
import com.jerzymaj.major.services.GptService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ActivityLogControllerIntegration extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockCustomUser
    public void retrieveAllActivityLogs() throws Exception {

        mockMvc.perform(get("/major/api/activity-logs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)))
                .andExpect(jsonPath("$").isArray());
    }
}
