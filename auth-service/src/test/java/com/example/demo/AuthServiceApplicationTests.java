package com.example.demo;

import com.example.demo.service.outbox.OutboxEventPublisher;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class AuthServiceApplicationTests {

	@MockitoBean
	private OutboxEventPublisher outboxEventPublisher;

	@Test
	void contextLoads() {
	}

}
