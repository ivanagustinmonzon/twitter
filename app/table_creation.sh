#!/bin/zsh

# aws dynamodb create-table \
#     --table-name Music \
#     --attribute-definitions \
#         AttributeName=Artist,AttributeType=S \
#         AttributeName=SongTitle,AttributeType=S \
#     --key-schema \
#         AttributeName=Artist,KeyType=HASH \
#         AttributeName=SongTitle,KeyType=RANGE \
#     --provisioned-throughput \
#         ReadCapacityUnits=5,WriteCapacityUnits=5 \
#     --table-class STANDARD

#aws dynamodb delete-table --table-name User --endpoint-url http://localhost:8000
#
aws dynamodb create-table \
    --table-name User \
    --attribute-definitions \
        AttributeName=nickname,AttributeType=S \
        AttributeName=category,AttributeType=S \
    --key-schema \
        AttributeName=nickname,KeyType=HASH \
        AttributeName=category,KeyType=RANGE \
    --provisioned-throughput \
        ReadCapacityUnits=5,WriteCapacityUnits=5 \
    --table-class STANDARD \
    --endpoint-url http://0.0.0.0:8000

echo "created table User"
