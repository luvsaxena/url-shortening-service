package com.awsproject.urlshortening.service;

import com.awsproject.urlshortening.model.UrlMapping;
import com.awsproject.urlshortening.repo.UrlMappingRepo;
import org.springframework.stereotype.Service;

@Service
public class UrlService {

    private static final int MAX_RETRIES = 5;
    private final UrlMappingRepo repository;

    public UrlService(UrlMappingRepo repository) {
        this.repository = repository;
    }

    public String shorten(String longUrl) {
        // Prevent duplicate
        UrlMapping existing = repository.getByLongUrl(longUrl);
        if (existing != null) {
            return existing.getShortCode();
        }

//        // Generate new
//        String code = Base62Generator.generate(6);

        for (int attempt = 0; attempt < MAX_RETRIES; attempt++) {
            String code = Base62Generator.generate(6);

            // Step 3: Check collision
            if (repository.getByShortCode(code) == null) {
                // No collision, save and return
                UrlMapping m = new UrlMapping();
                m.setShortCode(code);
                m.setLongUrl(longUrl);
                m.setCreatedAt(System.currentTimeMillis());
                repository.save(m);
                return code;
            }

            // Collision happened, retry
        }

        throw new RuntimeException("Unable to generate unique short code after " + MAX_RETRIES + " attempts");
    }

    public String resolve(String code) {
        UrlMapping m = repository.getByShortCode(code);
        return m != null ? m.getLongUrl() : null;
    }
}