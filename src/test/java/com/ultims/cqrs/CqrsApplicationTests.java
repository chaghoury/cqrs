package com.ultims.cqrs;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.modulith.core.ApplicationModules;

@SpringBootTest
class CqrsApplicationTests {

    @Test
    void contextLoads() {}

    @Test
    void verifiesModularStructure() {
        ApplicationModules.of(CqrsApplication.class).verify();
    }
}
