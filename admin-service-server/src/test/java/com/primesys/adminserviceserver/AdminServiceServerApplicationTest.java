package com.primesys.adminserviceserver;

import com.primesys.adminserviceserver.job.WorkflowInitializer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class AdminServiceServerApplicationTest {

    @MockBean
    WorkflowInitializer workflowInitializer;

    @Test
    void contextLoads() {
    }
}
