#!/bin/sh

#aws dynamodb delete-table --table-name Users --endpoint-url http://dynamodb-local:8000

echo "Creating Users table for twitter application"

aws dynamodb create-table \
    --table-name Users \
    --attribute-definitions \
        AttributeName=nickname,AttributeType=S \
        AttributeName=category,AttributeType=S \
    --key-schema \
        AttributeName=nickname,KeyType=HASH \
        AttributeName=category,KeyType=RANGE \
    --provisioned-throughput \
        ReadCapacityUnits=5,WriteCapacityUnits=5 \
    --table-class STANDARD \
    --endpoint-url http://dynamodb-local:8000

