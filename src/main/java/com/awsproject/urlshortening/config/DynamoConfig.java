package com.awsproject.urlshortening.config;

import com.awsproject.urlshortening.model.UrlMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.net.URI;

@Configuration
public class DynamoConfig {

    private static final Logger log = LoggerFactory.getLogger(DynamoConfig.class);
    private static final String TABLE_NAME = "UrlMapping";

    @Bean
    public DynamoDbClient dynamoDbClient() {
        DynamoDbClient client =  DynamoDbClient.builder()
//                .endpointOverride(URI.create("http://localhost:8000"))
                .region(Region.US_EAST_1)
//                .credentialsProvider(
//                        StaticCredentialsProvider.create(
//                                AwsBasicCredentials.create("dummy", "dummy")
//                        )
//                )
                .build();

        URI endpoint = DynamoDbClient.serviceMetadata().endpointFor(client.serviceClientConfiguration().region());
        log.info("DynamoDbClient initialized. Targeting URI: {}", endpoint);

        return client;
    }

    @Bean
    public DynamoDbEnhancedClient enhancedClient(DynamoDbClient dynamoDbClient) {
        return DynamoDbEnhancedClient.builder()
                .dynamoDbClient(dynamoDbClient)
                .build();
    }

    @Bean
    public DynamoDbTable<UrlMapping> urlMappingTable(DynamoDbEnhancedClient enhancedClient) {
        log.info("Initializing DynamoDbTable bean for table: {}", TABLE_NAME);
        return enhancedClient.table(TABLE_NAME, TableSchema.fromBean(UrlMapping.class));
    }
}