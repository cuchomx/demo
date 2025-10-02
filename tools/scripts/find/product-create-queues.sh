#!/bin/zsh

echo "Create Queue Web"
aws --endpoint-url=http://localhost:9324 sqs create-queue --queue-name product-find-web

echo "Create Queue Service"
aws --endpoint-url=http://localhost:9324 sqs create-queue --queue-name product-find-service

echo "Test Create Queue"
aws --endpoint-url=http://localhost:9324 sqs get-queue-url --queue-name product-find-web
aws --endpoint-url=http://localhost:9324 sqs get-queue-url --queue-name product-find-service

echo "List Existing Queues:"
aws --endpoint-url=http://localhost:9324 sqs list-queues
