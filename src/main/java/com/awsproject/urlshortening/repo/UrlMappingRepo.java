package com.awsproject.urlshortening.repo;

import com.awsproject.urlshortening.model.UrlMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

@Repository
public class UrlMappingRepo {

//    private final DynamoDbEnhancedClient enhancedClient;
    private final DynamoDbTable<UrlMapping> table;
    private static final Logger log = LoggerFactory.getLogger(UrlMappingRepo.class);

    public UrlMappingRepo(DynamoDbTable<UrlMapping> table) {
//        this.enhancedClient = enhancedClient;
        this.table = table;
    }

    public void save(UrlMapping mapping) {
        try {
            table.putItem(mapping);
            log.info("Successfully saved item with shortCode: {}", mapping.getShortCode());
        }
        catch (Exception e) {
            log.error("DynamoDB save failed for shortCode: {}", mapping.getShortCode(), e);
        }
    }

    public UrlMapping getByShortCode(String code) {
        try {
            UrlMapping mapping = table.getItem(Key.builder().partitionValue(code).build());
            log.info("Successfully found item for shortCode : {}", mapping.getLongUrl());
            return mapping;
        }
        catch (Exception e) {
            log.error("DynamoDB get failed for shortCode: {}", code, e);
            throw new RuntimeException(e);
        }

    }

    public UrlMapping getByLongUrl(String longUrl) {
        DynamoDbIndex<UrlMapping> index = table.index("LongUrlIndex");

        SdkIterable<Page<UrlMapping>> results = index.query(r -> r.queryConditional(
                QueryConditional.keyEqualTo(Key.builder().partitionValue(longUrl).build())
        ));

        return results.stream()
                .flatMap(page -> page.items().stream())
                .findFirst()
                .orElse(null);
    }
}