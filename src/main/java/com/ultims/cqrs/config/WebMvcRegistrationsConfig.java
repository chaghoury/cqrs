package com.ultims.cqrs.config;

import java.lang.reflect.Method;

import org.springframework.boot.webmvc.autoconfigure.WebMvcRegistrations;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * Ensures that when multiple controller beans implement the same OpenAPI
 * interface (such as AccountApi),
 * only the methods that are actually overridden on that concrete controller
 * bean are registered as endpoints.
 * Un-overridden default interface methods are ignored, eliminating ambiguous
 * mapping collisions.
 */
@Configuration
public class WebMvcRegistrationsConfig implements WebMvcRegistrations {

    @Override
    public RequestMappingHandlerMapping getRequestMappingHandlerMapping() {
        return new RequestMappingHandlerMapping() {
            @Override
            protected RequestMappingInfo getMappingForMethod(Method method, Class<?> handlerType) {
                if (method.getDeclaringClass().isInterface()) {
                    return null;
                }
                return super.getMappingForMethod(method, handlerType);
            }
        };
    }
}
