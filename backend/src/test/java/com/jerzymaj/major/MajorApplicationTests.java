package com.jerzymaj.major;

import com.jerzymaj.major.services.GptService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
class MajorApplicationTests {

	@MockitoBean
	private GptService gptService;

	@Test
	void contextLoads() {
	}

}
