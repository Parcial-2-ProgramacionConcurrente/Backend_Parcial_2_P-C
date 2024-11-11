package org.main_java.parcial_2_concurrente.config;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Locale;

@Component
public class WebUtils {

    public static final String MSG_SUCCESS = "MSG_SUCCESS";
    public static final String MSG_INFO = "MSG_INFO";
    public static final String MSG_ERROR = "MSG_ERROR";
    private static MessageSource messageSource;

    public WebUtils(final MessageSource messageSource) {
        WebUtils.messageSource = messageSource;
    }

    public static Mono<String> getMessage(final String code, final Object... args) {
        Locale locale = LocaleContextHolder.getLocale();
        return Mono.just(messageSource.getMessage(code, args, code, locale));
    }

    public static Mono<Locale> resolveLocale(ServerWebExchange exchange) {
        Locale locale = exchange.getLocaleContext().getLocale();
        return Mono.justOrEmpty(locale);
    }
}
