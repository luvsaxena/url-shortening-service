package com.awsproject.urlshortening;

import com.awsproject.urlshortening.service.UrlService;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.function.Function;

@Component
public class UrlShortenerFunction {

    private final UrlService urlService;

    public UrlShortenerFunction(UrlService urlService) {
        this.urlService = urlService;
    }

    @Bean
    public Function<String, String> shorten() {
        return longUrl -> urlService.shorten(longUrl);
    }

    @Bean
    public Function<Map<String, Object>, String> resolve() {
        return event ->
        {
            Map<String, String> pathParameters = (Map<String, String>) event.get("pathParameters");
            // The short code is stored under the key "shortId"
            String code = pathParameters.get("shortId");
            return urlService.resolve(code);
        };
    }
}