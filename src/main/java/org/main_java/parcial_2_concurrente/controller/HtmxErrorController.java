package org.main_java.parcial_2_concurrente.controller;

import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Configuration
public class HtmxErrorController {

    private final ErrorAttributes errorAttributes;

    public HtmxErrorController(ErrorAttributes errorAttributes) {
        this.errorAttributes = errorAttributes;
    }

    @Bean
    public ErrorWebExceptionHandler errorWebExceptionHandler() {
        return (exchange, ex) -> {
            exchange.getResponse().setStatusCode(HttpStatus.BAD_REQUEST);
            return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory()
                    .wrap("Custom error message for htmx requests".getBytes())));
        };
    }
}
