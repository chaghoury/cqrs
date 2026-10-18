package com.ultims.cqrs.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(OpenApiController.class)
class OpenApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldServeOpenApiYaml() throws Exception {
        mockMvc
            .perform(get("/openapi.yaml"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith("application/yaml"));
    }
}
