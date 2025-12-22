#!/bin/bash

# Create UrlMapping table in local DynamoDB

aws dynamodb create-table \
--table-name UrlMapping \
--attribute-definitions \
AttributeName=shortCode,AttributeType=S \
AttributeName=longUrl,AttributeType=S \
--key-schema \
AttributeName=shortCode,KeyType=HASH \
--global-secondary-indexes "[
{
\"IndexName\": \"LongUrlIndex\", \
\"KeySchema\": [ \
{\"AttributeName\": \"longUrl\", \"KeyType\": \"HASH\"} \
], \
\"Projection\": {\"ProjectionType\": \"ALL\"}, \
\"ProvisionedThroughput\": {\"ReadCapacityUnits\": 5, \"WriteCapacityUnits\": 5}
}
]" \
--provisioned-throughput ReadCapacityUnits=5,WriteCapacityUnits=5 \
--endpoint-url http://localhost:8000
