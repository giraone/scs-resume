package com.giraone.scs.resume.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.adapter.ForwardedHeaderTransformer;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@Configuration
// Take care with @EnableWebFlux - see https://github.com/spring-projects/spring-boot/issues/13277
@EnableWebFlux
@Slf4j
public class WebConfiguration implements WebFluxConfigurer {

    // Needs Spring Boot 2.1.* or newer - this enables X-Forwarded header support
    @Bean
    public ForwardedHeaderTransformer forwardedHeaderTransformer() {
        log.info("ForwardedHeaderTransformer initialized");
        return new ForwardedHeaderTransformer();
    }

    // Map / to /index.html
    // See https://stackoverflow.com/a/50324512
    @Bean
    public RouterFunction<ServerResponse> indexHtmlRouter(@Value("classpath:/static/index.html") final Resource indexHtml) {
        return route(GET("/"), request -> ok().contentType(MediaType.TEXT_HTML).bodyValue(indexHtml));
    }

    @Bean
    public RouterFunction<ServerResponse> staticFilesResourceRouter() {
        return RouterFunctions.resources("/**", new ClassPathResource("static/"));
    }
}
