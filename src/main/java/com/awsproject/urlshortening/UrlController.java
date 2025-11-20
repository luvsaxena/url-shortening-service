package com.awsproject.urlshortening;

import com.awsproject.urlshortening.service.UrlService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/url")
public class UrlController {

    private final UrlService service;

    public UrlController(UrlService service) {
        this.service = service;
    }

    @PostMapping("/shorten")
    public String shorten(@RequestBody String longUrl) {
        return service.shorten(longUrl);
    }

    @GetMapping("/{code}")
    public ResponseEntity<String> resolve(@PathVariable String code) {
        String longUrl = service.resolve(code);
        if (longUrl == null) return ResponseEntity.notFound().build();

        return ResponseEntity.ok(longUrl);
    }
}