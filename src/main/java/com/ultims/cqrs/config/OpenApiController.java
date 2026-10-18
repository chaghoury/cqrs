package com.ultims.cqrs.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin
public class OpenApiController {

    @Value("classpath:static/openapi.yaml")
    private Resource openApiYaml;

    @GetMapping(
        value = "/openapi.yaml",
        produces = {
            "application/yaml", "text/yaml", MediaType.TEXT_PLAIN_VALUE,
        }
    )
    public ResponseEntity<Resource> getOpenApiYaml() {
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType("application/yaml"))
            .body(openApiYaml);
    }
}
