package com.awsproject.urlshortening.repo;

import com.awsproject.urlshortening.model.UrlMapping;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

@Repository
public class UrlMappingRepo {

    private final DynamoDbEnhancedClient enhancedClient;
    private final DynamoDbTable<UrlMapping> table;

    public UrlMappingRepo(DynamoDbEnhancedClient enhancedClient) {
        this.enhancedClient = enhancedClient;
        this.table = enhancedClient.table("UrlMapping", TableSchema.fromBean(UrlMapping.class));
    }

    public void save(UrlMapping mapping) {
        table.putItem(mapping);
    }

    public UrlMapping getByShortCode(String code) {
        return table.getItem(Key.builder().partitionValue(code).build());
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