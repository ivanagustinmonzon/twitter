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

aws dynamodb put-item \
    --table-name User \
    --item '{"nickname": {"S": "USER#johnbauer1"}, "category": {"S": "USER#johnbauer1"}, "realname": {"S": "John Bauer" } }' \
    --endpoint-url http://0.0.0.0:8000

aws dynamodb put-item \
    --table-name User \
    --item '{"nickname": {"S": "USER#juliarobers2"}, "category": {"S": "USER#juliarobers2"}, "realname": {"S": "Julia Robers" } }' \
    --endpoint-url http://0.0.0.0:8000

echo "Added users registration"

aws dynamodb put-item \
    --table-name User \
    --item '{"nickname": {"S": "USER#juliarobers2"}, "category": {"S": "FOLLOWED#johnbauer1"}, "timestamp": {"S": "202204071222" } }' \
    --endpoint-url http://0.0.0.0:8000

echo "juliarobers2 is followed by johnbahuer1"

aws dynamodb put-item \
    --table-name User \
    --item '{"nickname": {"S": "USER#juliarobers2"}, "category": {"S": "TWEET#123abc"}, "message": {"S": "Hello twitter!" }, "timestamp": {"S": "202204071322" } }' \
    --endpoint-url http://0.0.0.0:8000

echo "juliarobers2 tweeted hello"





#echo "added data"

# aws dynamodb get-item --consistent-read \
#     --table-name User \
#     --key '{ "nickname": {"S": "USER#johnbauer1"}, "category": {"S": "USER#johnbauer1"}}' \
#     --endpoint-url http://0.0.0.0:8000
# 
