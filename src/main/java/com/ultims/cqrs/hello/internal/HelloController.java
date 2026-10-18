package com.ultims.cqrs.hello.internal;

import com.ultims.cqrs.api.HelloApi;
import com.ultims.cqrs.dto.GreetingResponse;
import java.time.OffsetDateTime;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController implements HelloApi {

    @Override
    public ResponseEntity<GreetingResponse> sayHello(String name) {
        GreetingResponse response = new GreetingResponse();
        response.setMessage(
            "Hello, " + (name != null && !name.isBlank() ? name : "World") + "!"
        );
        response.setTimestamp(OffsetDateTime.now());
        return ResponseEntity.ok(response);
    }
}
